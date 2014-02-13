import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import com.tdssrc.grails.GormUtil;

import java.sql.ResultSet
import java.sql.SQLException
import com.tdssrc.grails.GormUtil
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper

import org.apache.shiro.SecurityUtils

/**
 * The cookbook services handles the logic for creating recipes and running the cookbook
 * 
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class CookbookService {

    boolean transactional = true
	static allowedCatalogs = ['Event', 'Bundle', 'Application']
	static searchAllowedCatalogs = ['All', 'Event', 'Bundle', 'Application']

	def namedParameterJdbcTemplate
	def projectService
	def partyRelationshipService
	def securityService

	/**
	 * Checks if person can access project. If it can't then it throws an {@link UnauthorizedException}
	 * @param person the person interested in the project
	 * @param project the project
	 */
	private void checkAccess(person, project) {
		if (person == null || project == null) {
			return
		}
		
		def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(project)
		if (project.id != Project.DEFAULT_PROJECT_ID
				&& !peopleInProject.contains(person)) {
				log.warn('Person doesn\'t have access to the project')
			throw new UnauthorizedException('The current user doesn\'t have access to the project')
		}
	}
	
	/**
	 * Creates a Recipe using the information passed
	 * 
	 * @param name the name of the recipe
	 * @param description the description of the recipe
	 * @param context the context
	 * @param cloneFrom the id of the recipe to be cloned from
	 * @param loginUser the user that is creating this recipe
	 * @param currentProject the project owning this recipe
	 */
	def createRecipe(recipeName, description, recipeContext, cloneFrom, loginUser, currentProject) {
		//TODO check this checkAccess(loginUser.person, currentProject)
		
		if (!RolePermissions.hasPermission('CreateRecipe')) {
			throw new UnauthorizedException('User doesn\'t have a CreateRecipe permission')
		}
		
		if (currentProject == null) {
			log.info('Current project is null')
			throw new EmptyResultException()
		}
		
		def clonedVersion = null
		def defaultSourceCode = ''
		def defaultChangelog = ''
		
		if (cloneFrom != null) {
			if (clonedFrom.isNumber()) {
				clonedVersion = RecipeVersion.get(clonedFrom)
				if (clonedVersion == null) {
					log.warn('Empty cloned version')
					throw new EmptyResultException()
				}
				checkAccess(loginUser.person, clonedVersion.recipe.project)
			} else {
				log.info("Cloned from is not a number. Found: ${cloneFrom}")
				throw new EmptyResultException()
			}
		} else {
			def defaultRecipe = Recipe.findByNameAndProject('Default', Project.getDefaultProject())
			if (defaultRecipe != null && defaultRecipe.releasedVersion != null) {
				defaultSourceCode = defaultRecipe.releasedVersion.sourceCode
				defaultChangelog = defaultRecipe.releasedVersion.changelog
			}
		}
		
		def recipe = new Recipe()
		def recipeVersion = new RecipeVersion()
		
		recipe.name = recipeName
		recipe.description = description
		recipe.context = recipeContext
		recipe.project = currentProject
		recipe.archived = false

		recipe.save(flush:true, failOnError: true)
		
		recipeVersion.versionNumber = 0
		recipeVersion.createdBy = loginUser.person
		recipeVersion.recipe = recipe
		
		if (clonedVersion != null) {
			recipeVersion.cloneFrom = clonedVersion
			recipeVersion.sourceCode = clonedVersion.sourceCode
			recipeVersion.changelog = clonedVersion.changelog
		} else {
			recipeVersion.sourceCode = defaultSourceCode
			recipeVersion.changelog = defaultChangelog
		}
		
		recipeVersion.save(failOnError: true)
		
		return recipe
	}

	
	def updateRecipe(recipeId, recipeName, description, loginUser, currentProject) {
		//TODO check this checkAccess(loginUser.person, currentProject)
		
		if (recipeId == null || !recipeId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)
		
		if (recipe == null) {
			throw new EmptyResultException();
		}

		recipe.name = recipeName
		recipe.description = description

		recipe.save(flush:true, failOnError: true)
	}
	
	/**
	 * Saves a specific recipe reference
	 * 
	 * @param recipeId the recipe id
	 * @param recipeVersionId the id of the recipe version
	 * @param name the name of the recipe
	 * @param description the description of the recipe
	 * @param sourceCode the source code of the recipe
	 * @param changelog the change log of the recipe
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def saveOrUpdateWIPRecipe(recipeId, recipeVersionId, name, description, sourceCode, changelog, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('CatalogRecipeSave')) {
			throw new UnauthorizedException('User doesn\'t have a CatalogRecipeSave permission')
		}

		if (recipeId == null || !recipeId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)
		if (recipe == null) {
			throw new EmptyResultException();
		}
		
		def recipeVersion = RecipeVersion.get(recipeVersionId)
		
		if (!recipe.project.equals(currentProject)) {
			throw new UnauthorizedException('The current user can only edit recipes of the current project')
		}
		
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		if (wip == null) {
			wip = new RecipeVersion()
			wip.versionNumber = 0
			wip.createdBy = loginUser.person
			wip.recipe = recipe
			if (recipeVersion != null) {
				wip.clonedFrom = recipeVersion
			}
		}
		
		if (!name) {
			recipe.name = name
		}
		if (description != null) {
			recipe.description = description
		}
		
		wip.sourceCode = sourceCode
		wip.changelog = changelog
		
		wip.save(failOnError: true)
		
		return wip
	}

	
	/**
	 * Releases a WIP recipe version using the recipeVersionId
	 * @param recipeVersionId the version id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def releaseRecipe(recipeVersionId, loginUser, currentProject) {
//		if (!RolePermissions.hasPermission('ReleaseRecipe')) {
//			throw new UnauthorizedException('User doesn\'t have a ReleaseRecipe permission')
//		}
		
		if (recipeVersionId == null || !recipeVersionId.isNumber() || currentProject == null) {
			throw new EmptyResultException(); 
		}
		
		//TODO check this checkAccess(loginUser.person, currentProject)
		def wip = RecipeVersion.get(recipeVersionId)
		
		if (wip == null || wip.versionNumber != 0) {
			throw new IllegalArgumentException('Not a WIP')
		}
		
		def max = 0
		
		try {
			max = namedParameterJdbcTemplate.queryForInt('SELECT MAX(version_number) FROM recipe_version WHERE recipe_id = :recipeId', ['recipeId' : wip.recipe.id])
		} catch (IncorrectResultSizeDataAccessException e) {
			log.warn('No results when looking for a version number')
		}
		
		wip.versionNumber = max + 1
		wip.recipe.releasedVersion = wip
		
		wip.save(failOnError: true)
	}
	
	/**
	 * Reverts the recipe to the previous released version
	 * @param recipeId the id of the recipe
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def revertRecipe(recipeId, loginUser, currentProject) {
//		if (!RolePermissions.hasPermission('RevertRecipe')) {
//			throw new UnauthorizedException('User doesn\'t have a RevertRecipe permission')
//		}
		
		if (recipeId == null) {
			throw new EmptyResultException('Recipe id is empty');
		}
		if (!recipeId.isNumber()) {
			throw new EmptyResultException('Recipe id is not a number');
		}
		if (currentProject == null) {
			throw new EmptyResultException('Project is empty');
		}

		//TODO check this checkAccess(loginUser.person, currentProject)
		def recipe = Recipe.get(recipeId)
		
		if (recipe.releasedVersion == null) {
			throw new EmptyResultException('Release version is empty');
		}
		
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		
		if (wip != null) {
			wip.delete(failOnError: true)
		} else {
			throw new EmptyResultException();
		}
	}
	
	/**
	 * Validates the syntax for sourceCode for the current loginUser and currentProject
	 * 
	 * @param sourceCode the source code to be validated
	 * @param loginUser the current user
	 * @param currentProject the current project
	 * @return the list of errors found or empty list if everything looks fine
	 */
	def validateSyntaxForUser(sourceCode, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('EditRecipe')) {
			throw new UnauthorizedException('User doesn\'t have a EditRecipe permission')
		}
				
		if (currentProject == null) {
			throw new EmptyResultException();
		}
		
		def validationResult = this.validateSyntax(sourceCode);
		return validationResult
	}
	
	/**
	 * Returns the information about a specific version of the Recipe
	 * 
	 * @param recipeId the id of the Recipe
	 * @param versionNumber the version of the Recipe
	 * @param loginUser the current user
	 * @return a Map with information about the Recipe and the RecipeVersion
	 */
	def getRecipe(recipeId, versionNumber, loginUser) {
		if (recipeId == null || !recipeId.isNumber()) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)

		if (recipe == null) {
			throw new EmptyResultException('Recipe does not exists');
		}

		if (versionNumber == null) {
			versionNumber = recipe.releasedVersion == null ? 0 : recipe.releasedVersion.versionNumber
		}

		def recipeVersion = RecipeVersion.findByRecipeAndVersionNumber(recipe, versionNumber)
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		
		if (recipeVersion == null) {
			throw new EmptyResultException('Invalid recipe')
		}
		
		checkAccess(loginUser.person, recipe.project)
		
		def person = recipeVersion.createdBy
		
		def result = [
			'recipe' : recipe,
			'recipeVersion' : recipeVersion,
			'person' : person,
			'wip' : wip
		]
		
		return result
	}
	
	/**
	 * Finds the recipes according to the information provided
	 * 
	 * @param isArchived indicates if service should return active or archived recipes. Valid values (y|n).
	 * @param catalogContext used to filter context of recipes: All, Event, Bundle, Application 
	 * @param searchText to search for text in name and description
	 * @param projectType project indicates which project to provide list of recipes for (master, active, complete or integer)
	 * When set to master, it will list search the master/default project.
	 * When set to active, it will search all active projects the user has access to
	 * When set to completed, it will search all completed projects that the user has access to
	 * When set to a numeric value, it will search the specific project by id (as long as the user is associated to the project)
	 * @param loginUser the current user
	 * @param currentProject the current project
	 * 
	 * @return a list of Maps with information about the recipes. See {@link RecipeMapper}
	 */
    def findRecipes(isArchived, catalogContext, searchText, projectType, loginUser, currentProject) {
		def projectIds = []
		
		if (projectType == null) {
			if (currentProject == null) {
				throw new EmptyResultException()
			} else {
				projectIds.add(currentProject.id)
			}
		} else {

			projectType = projectType.toLowerCase()
			
			switch (projectType) {
				case 'master':
					projectIds.add(Project.DEFAULT_PROJECT_ID)
					break
					
				case 'active':
					def projects = projectService.getActiveProject(new Date(), true, loginUser.person)
					projects.each { project ->
						projectIds << project.id
					}
					break
					
				case 'completed':
					def projects = projectService.getCompletedProject(new Date(), true);
					projects.each { project ->
						projectIds << project.id
					}
					break
					
				case Integer:
					def project = Project.get(projectType.toInteger())
					if (project != null) {
						def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(project)
						if (peopleInProject.contains(loginUser.person)) {
							projectIds.add(projectType.toInteger())
						} else {
							throw new UnauthorizedException('The current user doesn\'t have access to the project')
						}
					} else {
						throw new UnauthorizedException('The current user doesn\'t have access to the project')
					}
					break
				default :
					throw new IllegalArgumentException('Invalid project type')
			}
		}

		def projectIdsAsString = projectIds.isEmpty() ? '-1' : GormUtil.asCommaDelimitedString(projectIds)
		isArchived = (isArchived.equals('y') ? '1' : 0)
		catalogContext = (searchAllowedCatalogs.contains(catalogContext)) ? catalogContext : null
		
		def arguments = [
			"isArchived" : isArchived,
			"projectIdsAsString" : projectIdsAsString
		]

		
		def searchCondition = ''
		if (searchText != null) {
			searchCondition = '''
				AND (
					recipe.name like :searchName
					OR
					recipe.description like :searchDescription
				)
			'''
			searchText = '%' + searchText + '%'
			arguments.searchName = searchText
			arguments.searchDescription = searchText
		}
		
		
		def catalogCondition = ''
		if (catalogContext != null && allowedCatalogs.contains(catalogContext)) {
			catalogCondition = 'AND recipe.context = :catalogContext'
			arguments.catalogContext = catalogContext
		}
		
		
		def recipes = namedParameterJdbcTemplate.query("""
				SELECT DISTINCT recipe.recipe_id as recipeId, recipe.name, recipe.description, recipe.context, recipe.last_updated, CONCAT(person.first_name, ' ', person.last_name)  as createdBy, recipe.last_updated as lastUpdated, recipe_version.version_number as versionNumber, IF(ISNULL(rv2.version_number), false, true) as hasWIP 
				FROM recipe
				LEFT OUTER JOIN recipe_version ON recipe.released_version_id = recipe_version.recipe_version_id
				LEFT OUTER JOIN recipe_version as rv2 ON recipe.released_version_id = recipe_version.recipe_version_id AND recipe_version.version_number = 0
				INNER JOIN person ON person.person_id = recipe_version.created_by_id
				WHERE recipe.archived = :isArchived
				AND recipe.project_id IN (:projectIdsAsString)
				${searchCondition}
				${catalogCondition}
			""", arguments, new RecipeMapper())
		
		return recipes
    }

	/**
	* Used to validate the syntax of a recipe 
	* 
	* @param sourceCode - the source code to validate
	* @return a list of errors if any otherwise null where the list map matches that used by web service errors
	* The map will consist of:
	*    error:Integer - 
	*    reason:String - General cause
	*    detail:String - The specific issue, typically what the user is most interested in
	* 1) Invalid syntax
	* 2) Missing section
	* 3) Missing property
	* 4) Invalid group reference
	* 5) Duplicate reference
	*/
	List<Map> validateSyntax( sourceCode ) {
		def errorList = []
		def recipe

		try {
			recipe = Eval.me("[${sourceCode}]")
		} catch (e) {
			errorList << [ error: 1, reason: 'Invalid syntax', detail: e.getMessage().replaceAll(/[\r]/, '<br/>') ]
		}

		if (! errorList) {
			// If the syntax compiled then we can start examining different sections of the recipe for common mistakes

			def index = 0

			// Note that Groups are optional
			def hasGroups = recipe.containsKey('groups')
			def groupKeys = [:]
			def classNames = ['application','device','database','storage']
			if ( hasGroups ) {
				// Check to see if groups has expected elements
				recipe.groups.each { group ->
					index++
					if (group.containsKey('name')) {
						// Check for spaces
						if (group.name.contains(' ')) {
							errorList << [ error: 1, reason: 'Invalid syntax',
								detail: "Group name '${group.name}' in element ${index} contains unsupported space character(s)" ]
						}
						if (group.name.trim() == '') {
							errorList << [ error: 1, reason: 'Invalid syntax',
								detail: "Group name '${group.name}' in element ${index} is blank" ]
						}
						if (groupKeys.containsKey(group.name)) {
							errorList << [ error: 5, reason: 'Duplicate group', 
								detail: "Group name '${group.name}' duplicated in group ${groupKeys[group.name]} and ${index}" ]
						}
						groupKeys.put(group.name, index)
					}
					if ( group.containsKey('filter') ) {
						if (group.filter instanceof Map) {
							if ( group.filter.containsKey('class') ) {
								// Make sure that the filter has a class and proper value
								if (! classNames.contains( group.filter.class.toLowerCase() ) ) {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "Group '${group.name}' in element ${index} has invalid filter.class value. Allowed values [${classNames.join(',')}]" ]
								}
							} else {
								errorList << [ error: 3, reason: 'Missing property', 
									detail: "Group '${group.name}' in element ${index} is missing required 'filter.class' property" ]
							}
							if ( group.filter.containsKey('taskSpec') ) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "Group '${group.name}' in element ${index} references a taskSpec which is not supported in groups" ]
							}
						} else {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "Group '${group.name}' in element ${index} 'filter' element not properly defined as a map" ]
						}
					} else {
						errorList << [ error: 3, reason: 'Missing property', 
							detail: "Group ${group.name} in element ${index} is missing require section 'filter'" ]
					}
				}

				// Check to see that any include/exclude references match defined groups
				index = 0
				recipe.groups.each { group -> 
					index++
					if (group.containsKey('filter') && group.filter instanceof Map ) {
						['exclude','include'].each { ei -> 
							if (group.filter.containsKey(ei)) {
								def eiList = group.filter[ei] instanceof java.util.ArrayList ? group.filter[ei] : [ group.filter[ei] ]
								eiList.each { gName -> 
									if (! groupKeys.containsKey(gName) ) {
										errorList << [ error: 4, reason: 'Invalid group reference', 
											detail: "Group ${group.name} in element ${index} 'filter.${ei}' references undefined group ${gName}" ]

									}
								}
							}
						}
					}
				}
			}

			if ( ! recipe.containsKey('tasks')) {
				errorList << [ error: 2, reason: 'Missing section', detail: 'Recipe is missing required \'tasks\' section' ]
			} else {
				// Check for Task Names
			}
		}

		return (errorList ?: null)
    }

}

class RecipeMapper implements RowMapper {
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		def rowMap = [:]
		rowMap.recipeId = rs.getInt('recipeId')
		rowMap.name = rs.getString('name')
		rowMap.description = rs.getString('description')
		rowMap.createdBy = rs.getString('createdBy')
		rowMap.versionNumber = rs.getInt('versionNumber')
		rowMap.hasWIP = rs.getBoolean('hasWIP')
		rowMap.context = rs.getString('context')
		rowMap.lastUpdated = rs.getTimestamp('last_updated')
		return rowMap
	}
}
