import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import com.tdsops.common.lang.CollectionUtils as CU
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil

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

	
	/**
	 * Deletes a Recipe using the information passed
	 *
	 * @param recipeId the id of the recipe
	 * @param loginUser the user that is creating this recipe
	 * @param currentProject the project owning this recipe
	 */
	def deleteRecipe(recipeId, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('DeleteRecipe')) {
			throw new UnauthorizedException('User doesn\'t have a DeleteRecipe permission')
		}
		
		if (recipeId == null || !recipeId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)
		
		if (recipe == null) {
			throw new EmptyResultException();
		}
		
		if (!recipe.project.equals(currentProject)) {
			throw new UnauthorizedException('User is trying to delete recipe whose project that is not the current ' + recipeId + ' currentProject ' + currentProject.id)
		}
		
		recipe.releasedVersion = null;
		recipe.save(flush:true, failOnError: true)
		
		namedParameterJdbcTemplate.update('UPDATE task_batch SET recipe_version_used_id = NULL WHERE recipe_version_used_id IN (SELECT recipe_version_id FROM recipe_version WHERE recipe_id = :recipeId)', ['recipeId' : recipeId])
		namedParameterJdbcTemplate.update('DELETE FROM recipe_version WHERE recipe_id = :recipeId', ['recipeId' : recipeId])
		
		recipe.delete(failOnError: true)
		
		return recipe
	}
	
	
	/**
	 * Deletes a Recipe version using the information passed
	 *
	 * @param recipeId the id of the recipe
	 * @param recipeVersion the version of the recipeVersion
	 * @param loginUser the user that is creating this recipe
	 * @param currentProject the project owning this recipe
	 */
	def deleteRecipeVersion(recipeId, recipeVersion, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('DeleteRecipe')) {
			throw new UnauthorizedException('User doesn\'t have a DeleteRecipe permission')
		}
		
		if (recipeId == null || !recipeId.isNumber() || currentProject == null || recipeVersion == null || !recipeVersion.isNumber()) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)
		if (recipe == null) {
			throw new EmptyResultException();
		}

		def rv = RecipeVersion.findByRecipeAndVersionNumber(recipe, recipeVersion)
		if (rv == null) {
			throw new EmptyResultException();
		}

		if (!recipe.project.equals(currentProject)) {
			log.warn('User is trying to delete recipe whose project that is not the current ' + recipeId + ' currentProject ' + currentProject.id)
			throw new UnauthorizedException('User is trying to delete recipe whose project that is not the current ' + recipeId + ' currentProject ' + currentProject.id)
		}
		
		if (!rv.recipe.equals(recipe)) {
			log.warn('Recipe and version does not have a common recipe')
			throw new UnauthorizedException('Recipe and version does not have a common recipe')
		}

		if (recipe.releasedVersion.equals(rv)) {
			log.warn('Can not delete the currently published version')
			throw new UnauthorizedException('Can not delete the currently published version')
		}

		namedParameterJdbcTemplate.update('UPDATE task_batch SET recipe_version_used_id = NULL WHERE recipe_version_used_id = :recipeVersionId', ['recipeVersionId' : rv.id])
		
		rv.delete(failOnError: true)
		
		return rv
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
		if (!RolePermissions.hasPermission('EditRecipe')) {
			log.warn "SECURITY: User $loginUser attempted to update a recipe without permission, recipe id: $recipeId"
			throw new UnauthorizedException('Sorry but you do not have the permission to update a recipe')
		}

		if ( currentProject == null) {
			log.warn "SECURITY: User $loginUser attempting to update a recipe without a valid project, recipe id: $recipeId"
			throw new InvalidParamException('You must select a project before being able to edit recipes');
		}

		if (recipeId == null || !recipeId.isNumber() || !recipeId.isInteger() ) {
			log.warn "SECURITY: User $loginUser attempted to update a recipe without invalid recipe id, recipe id: $recipeId"
			throw new InvalidParamException('Sorry but the recipe reference was invalid. Please contact support.');
		}
		
		def recipe = Recipe.get(recipeId)
		if (recipe == null) {
			log.warn "User attempted to update recipe but recipe was not found, user: $loginUser, project: $currentProject, recipe id: $recipeId"
			throw new EmptyResultException('Unable to find the recipe that you were attempting to update. Please contact support.');
		}
		
		if (!recipe.project.equals(currentProject)) {
			log.warn "SECURITY: User $loginUser illegally attempted to update a recipe of different project, recipe id: $recipeId, current project: $currentProject"
			throw new UnauthorizedException('Sorry but you can only update a recipe within the current project')
		}

		// Validate that the syntax is correct before submitting
		// TODO - Add logic to validate syntax

		// TODO - why two lookups? 		
		def recipeVersion = RecipeVersion.get(recipeVersionId)
		
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
	 * Releases a WIP recipe version using the recipeId
	 * @param recipeId the id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def releaseRecipe(recipeId, loginUser, currentProject) {
//		if (!RolePermissions.hasPermission('ReleaseRecipe')) {
//			throw new UnauthorizedException('User doesn\'t have a ReleaseRecipe permission')
//		}
		
		if (recipeId == null || !recipeId.isNumber() || currentProject == null) {
			throw new EmptyResultException(); 
		}
		
		def recipe = Recipe.get(recipeId)
		if (recipe == null) {
			throw new EmptyResultException();
		}
		
		//TODO check this checkAccess(loginUser.person, currentProject)
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		
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
		
		println(recipeId);
		println(loginUser);
		println(currentProject);

		if (recipeId == null) {
			println('001');
			throw new EmptyResultException('Recipe id is empty');
		}
		if (!recipeId.isNumber()) {
			println('002');
			throw new EmptyResultException('Recipe id is not a number');
		}
		if (currentProject == null) {
			println('003');
			throw new EmptyResultException('Project is empty');
		}
		//TODO check this checkAccess(loginUser.person, currentProject)
		def recipe = Recipe.get(recipeId)
		
		if (recipe.releasedVersion == null) {
			println('004');
			throw new EmptyResultException('Release version is empty');
		}
		
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		println(wip);

		if (wip != null) {
			wip.delete(failOnError: true)
		} else {
			throw new EmptyResultException('wip is null');
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
		
		//checkAccess(loginUser.person, recipe.project)
		
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

			projectType = (projectType.class == String) ? projectType.toLowerCase() : projectType
			
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
					
				case Long:
					def project = Project.get(projectType)
					if (project != null) {
//						def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(project)
//						if (peopleInProject.contains(loginUser.person)) {
							projectIds.add(projectType.toInteger())
//						} else {
//							throw new UnauthorizedException('The current user doesn\'t have access to the project')
//						}
					} else {
						throw new UnauthorizedException('The current user doesn\'t have access to the project')
					}
					break
				default :
					log.info('Reach Default value. Rare case')
					return []
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
				SELECT DISTINCT recipe.recipe_id as recipeId, recipe.name, recipe.description, recipe.context, IF(ISNULL(recipe_version.last_updated), rv2.last_updated, recipe_version.last_updated) as last_updated, CONCAT(person.first_name, ' ', person.last_name)  as createdBy, recipe.last_updated as lastUpdated, recipe_version.version_number as versionNumber, IF(ISNULL(rv2.version_number), false, true) as hasWIP 
				FROM recipe
				LEFT OUTER JOIN recipe_version ON recipe.released_version_id = recipe_version.recipe_version_id
				LEFT OUTER JOIN recipe_version as rv2 ON recipe.recipe_id = rv2.recipe_id AND rv2.version_number = 0
				LEFT OUTER JOIN person ON person.person_id = recipe_version.created_by_id
				WHERE recipe.archived = :isArchived
				AND recipe.project_id IN (:projectIdsAsString)
				${searchCondition}
				${catalogCondition}
			""", arguments, new RecipeMapper())
		
		return recipes
    }
	
	/**
	 * Archives the recipe with recipeId depending on the archived parameter
	 * 
	 * @param recipeId the id of the recipe
	 * @param archived true to archive, false to unarchived
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def archivedUnarchived(recipeId, archived, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('EditRecipe')) {
			throw new UnauthorizedException('User doesn\'t have a EditRecipe permission')
		}
		
		if (recipeId == null || !recipeId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def recipe = Recipe.get(recipeId)
		
		if (recipe == null) {
			throw new EmptyResultException();
		}
		
		if (!recipe.project.equals(currentProject)) {
			throw new UnauthorizedException('User is trying to archive/unarchived recipe whose project that is not the current ' + recipeId + ' currentProject ' + currentProject.id)
		}
		
		recipe.archived = archived
		recipe.save(flush:true, failOnError: true)
		
		return recipe
	}

    /**
     * Used to convert the Recipe source code from syntax into a Map
     * @param sourceCode the source code that represents the recipe (presently represents a Groovy Map)
     * @return The recipe in a Groovy Map containing the various elements of the recipe
     * @throws InvalidSyntaxException if the sourcecode is invalid
     */
    Map parseRecipeSyntax( sourceCode ) {
    	def recipe
		if (! sourceCode ) {
//			throw new InvalidSyntaxException('Recipe contains no source code')
			throw new RuntimeException('Recipe contains no source code')
		} else {
			//log.debug "About to parse the recipe:\n$sourceCode"
			try {
				recipe = Eval.me("[${sourceCode}]")
			} catch (e) {
				//throw new InvalidSyntaxException( e.getMessage().replaceAll(/[\r]/, '<br/>') )
				throw new RuntimeException( e.getMessage().replaceAll(/[\r]/, '<br/>') )
			}
		}
		return recipe
    }

	/**
	* Used to validate the syntax of a recipe and will return a list of syntax violations 
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
	* 4) Invalid reference
	* 5) Duplicate reference
	*/
	List<Map> validateSyntax( sourceCode ) {
		def errorList = []
		def recipe

		// Helper closure that compares the properties of a spec to a defined map
		def validateAgainstMap
		validateAgainstMap = { type, spec, map ->
			def i=0
			def label = ( type=='task' ? "Task id ${spec.id ?: 'UNDEF'}" : "Group ${spec.name ?: 'UNDEF'}" )
			spec.each { n, v -> 
				i++
				if (map.containsKey(n)) {
					// can do more here to check the nested definitions later on.

					if ( CU.isaMap(map[n])) {
						// Check the sub section of the spec against a sub section of the map
						// errorList.addAll( validateAgainstMap(type, spec[n], map[n]) )
					} else if ( CU.isaList(map[n]) ) {
						// Check if the value of a property exists in the map defined list
						if ( ! map[n].contains( v ) ) {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$label in element $i property '$n' contains invalid value '$v'" ]
						}
					}
				}
				else {
					errorList << [ error: 1, reason: 'Invalid syntax', 
						detail: "$label in element $i contains unknown property '$n'" ]
				}
			}
			return errorList
		}

		// Definition of the properties supported by group
		def groupProps = [
			name:'',
			description:'',
			filter: [
				group:0,
				include:0,
				exclude:0,
				taskSpec:0,
				class:['device','database','application','storage'],
				asset:0,
			]
		]

		// Definition of the properties supported by group
		def taskProps = [
			id:0,
			title:0,
			description:0,
			filter:0,
			type:['asset','action','milestone','gateway','general'],
			action: ['rollcall','location','room','rack','truck','set'],
			disposition:0,
			setOn:0,
			action:0,
			workflow:0,
			duration:0,
			team:0,
			category:0,
			estStart:0,
			estFinish:0,
			priority:0,
			effort:0,
			chain:0,
			terminal:0,
			whom:0,
			whomFixed:0,
			predecessor: [
				mode: ['supports','requires'],
				group:'',
				defer:'',
				gather:'',
				parent:0,
				ignore:true,
				require:true,
				typeSpec:0,
				inverse:true,
				classification: ['device','database','application','storage']
			],
			constraintTime:0,
			constraintType:0
		]

		try {
			recipe = parseRecipeSyntax(sourceCode)
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
						if ( CU.isaMap(group.filter)) {
							if ( group.filter.containsKey('class') ) {
								// Make sure that the filter has a class and proper value
								if (! classNames.contains( group.filter.class.toLowerCase() ) ) {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "Group '${group.name}' in element ${index} has invalid filter.class value. Allowed values [${classNames.join(',')}]" ]
								}
							} else {
								// We default class=device so no error if not found
								//errorList << [ error: 3, reason: 'Missing property', 
								//	detail: "Group '${group.name}' in element ${index} is missing required 'filter.class' property" ]
							}
							if ( group.filter.containsKey('taskSpec') ) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "Group '${group.name}' in element ${index} references a taskSpec which is not supported in groups" ]
							}

							// Check for any unsupported properties (misspellings, etc)
							validateAgainstMap( 'group', group, groupProps )

							// Validate the filter.dependency map settings
							if (group.filter.containsKey('dependency')) {
								if (CU.isaMap(group.filter.dependency) ) {
									if (group.filter.dependency.containsKey('mode')) {
										// Now we need to find assets that are associated via the AssetDependency domain
										def depMode = group.filter.dependency.mode.toLowerCase()
										if ( ! depMode || ! ['s','r'].contains(depMode[0]) ) {
											errorList << [ error: 1, reason: 'Invalid syntax', 
												detail: "Group '${group.name}' in element ${index} 'filter.dependency.mode' must be [supports|requires]" ]
										}
										if (group.filter.dependency.containsKey('asset')) {
											if (CU.isaMap(group.filter.dependency.asset)) {
												def suppAttribs = ['virtual','physical']
												group.filter.dependency.asset.each { n, v -> 
													if (! suppAttribs.contains(n)) 
														errorList << [ error: 1, reason: 'Invalid syntax', 
															detail: "Group '${group.name}' in element ${index} 'filter.dependency.asset' contains unsupport property '$n'" ]													
												}
												//
											} else {
												errorList << [ error: 1, reason: 'Invalid syntax', 
													detail: "Group '${group.name}' in element ${index} 'filter.dependency.asset' element not properly defined as a map" ]													
											}
										}
									} else {
										errorList << [ error: 3, reason: 'Missing property', 
											detail: "Group '${group.name}' in element ${index} is missing required 'filter.dependency.mode' property" ]
									}
								} else {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "Group '${group.name}' in element ${index} 'filter.dependency' element not properly defined as a map" ]	
								}

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
					if (group.containsKey('filter') && CU.isaMap(group.filter)) {
						['exclude','include'].each { ei -> 
							if (group.filter.containsKey(ei)) {
								def eiList = CU.asList(group.filter[ei])
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

			//
			// Validate the Tasks section
			//

			if ( ! recipe.containsKey('tasks')) {
				errorList << [ error: 2, reason: 'Missing section', detail: 'Recipe is missing required \'tasks\' section' ]
			} else {

				def taskIds = []
				index=0
				def lastId=0
				def match

				recipe.tasks.each { task -> 
					index++
					def taskId = null
					def taskRef = "Task in element $index"
					def hasFilter = false
					def type = task.type ?: ''

					if (! CU.isaMap(task) ) {
						errorList << [ error: 1, reason: 'Invalid syntax', 
							detail: "$taskRef is not a valid map definition" ]
						return	
					}

					// Test that the 'id' exists, that it isn't duplicated and that it is a positive whole number
					if (task.containsKey('id')) {
						if ( (task.id instanceof Integer) && task.id > 0) {
							taskId = task.id
							taskRef = "Task id $taskId"
							if (taskIds.contains(task.id)) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef is a duplicate of an earlier task spec" ]													
							} else {
								taskIds << task.id
							}
							// Make sure that the id #s are assending					
							if (lastId) {
								if (task.id < lastId) {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "$taskRef task id is smaller than previous task spec. The ids must have ascending values." ]													
								}
							}
							lastId = task.id

						} else {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$taskRef 'id' must be a positive whole number > 0" ]
						}
					} else {
						errorList << [ error: 3, reason: 'Missing property', 
							detail: "$taskRef is missing require property 'id' which must be a unique number" ]
					}

					// Check for any unsupported properties (misspellings, etc)
					validateAgainstMap( 'task', task, taskProps )

					// Validate the predecessor specifications
					if (task.containsKey('predecessor')) {
						def predecessor = task.predecessor

						if (! CU.isaMap(predecessor)) {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$taskRef 'predecessor' attribute is not a valid map definition" ]
						} else {

							// Validate if taskSpec was used, to make sure that it references a previously defined spec
							if (predecessor.containsKey('taskSpec')) {
								def ts = CU.asList(predecessor.taskSpec)
								ts.each { tsid ->
									if ( tsid instanceof Integer && tsid > 0 ) {
										if ( taskIds.contains(tsid) ) {
											if (taskId && tsid == taskId) {
												errorList << [ error: 4, reason: 'Invalid Reference', 
													detail: "$taskRef 'predecessor.taskSpec' contains reference ($tsid) to itself." ]
											}
										} else {
											errorList << [ error: 4, reason: 'Invalid Reference', 
												detail: "$taskRef 'predecessor.taskSpec' contains invalid id reference ($tsid). TaskSpec ids must reference a previously defined TaskSpec." ]
	 									}
									} else {
										errorList << [ error: 1, reason: 'Invalid syntax', 
											detail: "$taskRef 'predecessor.taskSpec' contains invalid id ($tsid). Ids must be a positive whole number > 0" ]
									}
									
								} 
							}

							// Validate the filter section
							if (predecessor.containsKey('filter')) {
								def filter = predecessor.filter
								if (CU.isaMap(filter)) {
									hasFilter=true
								} else {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "$taskRef 'predecessor.filter' attribute is not a valid map definition" ]
								}
							}

							// Check for Mode/TaskSpec/Group requirement
							def hasMode = predecessor.containsKey('mode')
							def hasTaskSpec = predecessor.containsKey('taskSpec')
							def hasGroup = predecessor.containsKey('group')
							def hasDefer = predecessor.containsKey('defer')
							def hasGather = predecessor.containsKey('gather')

							if ( ! (hasMode || hasTaskSpec || hasGroup || hasGather) ) {
								errorList << [ error: 3, reason: 'Missing property', 
									detail: "$taskRef 'predecessor' section requires [mode | taskSpec | group | gather] property" ]
							} else if ( hasMode && hasGroup ) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef 'predecessor' section contains 'mode' and 'group' properties which are mutually exclusive" ]
							} else if ( hasTaskSpec && hasGroup) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef 'predecessor' section contains 'taskSpec' and 'group' properties which are mutually exclusive" ]
							} 

							if (hasMode && predecessor.mode == 'both' && hasDefer) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef 'predecessor' section contains 'defer' and mode with 'both' value which is not supported" ]
							}
							if (hasTaskSpec && hasDefer) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef 'predecessor' section contains 'taskSpec' and 'defer' properties which are mutually exclusive" ]
							}
							def modeValues = ['supports','requires', 'both']
							if (hasMode &&  ! modeValues.contains(predecessor.mode)) {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef 'predecessor.mode' has invalid value '${predecessor.mode}'. Value options are [supports | requires | both]" ]
							}

							if (type == 'milestone') {
								errorList << [ error: 1, reason: 'Invalid syntax', 
									detail: "$taskRef of type 'milestone' does not support 'predecessor'" ]
							}
						}
					} // predecessor validation

					if (task.containsKey('successor')) {
						def successor = task.successor
						if (! isaMap(successor)) {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$taskRef 'successor' attribute is not a valid map definition" ]
						} 
					}

					// TODO - Add logic to validate that the Team / Role is valid

					// Validate time constraints if specified and parse the definition so that the logic can use it
					if ( task.containsKey('constraintType')) {
						if (! TimeConstraintType.asEnum(task.constraintType)) {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$taskRef 'constraintType' attribute has invalid value (${task.constraintType}). " +
									"Possible values include ( ${TimeConstraintType.getKeys().join(' | ')} )" ]
						}
					} else {
						// Set the default to MSO
						task.constraintType = TimeConstraintType.MSO.toString()
					}

					// Validate and parse the constraintTime property if defined
					def valid
					if ( task.containsKey('constraintTime')) {
						// This can have:
						//   date (mm/dd/yyyy)
						//   datatime (mm/dd/yyyy hh:mmAP TZ)
						//   indirect (#FieldName - date or datetime)
						//   Event reference + date math (e.g. ES-5d or EC+4h)
						//   TaskSpec + date math (e.g. 2000-6d)
						def ct = task.constraintTime
						def t
						if (ct ==~ /\d{1,2}\/\d{1,2}\/\d{4}/ ) {
							t = TimeUtil.parseDate(ct)
						} else if (ct ==~ /\d{1,2}\/\d{1,2}\/\d{4}.*/ ) {
							t = TimeUtil.parseDateTime(ct)
						} else if ( ct ==~ /^#.*/ ) {
							t = ct
						} else if ( ct ==~ /^(?i)(\d{1,}|es|ec).*/ ) {
							// ES or EC reference with optional date math
							if ( ct ==~ /(?i)(\d{1,}|es|ec)/ ) {
								t = ct.toLowerCase()
							} else {
								match = ( ct =~ /(?i)(\d{1,}|es|ec)(\+|-)(\d{1,})(?i)(m|h|d|w)/ )
								if (match.matches()) {
									// validate if referencing a TaskSpec, that the id exists
									log.debug "match[0][1] = ${match[0][1]}"
									if ( match[0][1].isInteger() ) {
										if (! taskIds.contains( match[0][1].toInteger() )) {
											errorList << [ error: 1, reason: 'Invalid syntax', 
												detail: "$taskRef 'constraintTime' (${task.constraintTime}) references undefined task spec (${match[0][1]})" ]
										}
									}
									t = match[0]
								}
							}
						}
						if (t) {
							// Stuff the parsed constraintTime into new property
							task.constraintTimeParsed=t
						} else {
							errorList << [ error: 1, reason: 'Invalid syntax', 
								detail: "$taskRef 'constraintTime' has invalid value (${task.constraintTime})" ]
						}

						log.debug "$taskRef task.constraintTimeParsed=${task.constraintTimeParsed} "
					} // ConstraintTime tests

					// Validate duration if specified, which supports:
					//    Integer - defaults to minutes (e.g. duration:15)
					//    String time - requires scale (e.g. duration:'60m')
					//    Indirect reference (e.g. duration:'#shutdownTime')
					//    Indirect reference with default value (e.g. duration:'#shutdownTime,15m')
					if (task.containsKey('duration')) {
						def d = task.duration
						task.durationIndirect = null
						if (d instanceof Integer) {
							task.durationValue = d
							task.durationUom = TimeScale.M
						} else {
							if (d ==~ /^#\b.*/) {
								// Handle indirect references
								match = d =~ /#(\w{1,})/
								if ( match.matches() ) {
									task.durationIndirect = match[0][1]
									task.durationValue = null
									task.durationUom = TimeScale.M
								} else {
									match = d =~ /#(\w{1,}),\s?(\d{1,})(?i)(m|h|d|w)/
									if ( match.matches() ) {
										task.durationIndirect = match[0][1]
										task.durationValue = match[0][2]
										task.durationUom = TimeScale.asEnum(match[0][3].toUpperCase())
									} else {
										errorList << [ error: 1, reason: 'Invalid syntax', 
											detail: "$taskRef 'duration' has invalid reference (${task.duration})" ]
									}
								}

							} else {
								// Handle string time
								match = ( d =~ /(\d{1,})(?i)(m|h|d|w)/ )
								if (match.matches()) {
									task.durationValue = match[0][1]
									task.durationUom = TimeScale.asEnum(match[0][2].toUpperCase())
								} else {
									errorList << [ error: 1, reason: 'Invalid syntax', 
										detail: "$taskRef 'duration' has invalid value (${d})" ]
								}
							}
						}
						log.debug "$taskRef - duration=${d}, value=${task.durationValue}, uom=${task.durationUom}, indirect=${task.durationIndirect}"
					} // Duration tests

				}

				// TODO - Add test to validate the title expression is valid 
				/*
					// Need to determine the asset type from the class property
					try {
						if (asset) {
							task.comment = new GStringEval().toString(taskSpec.title, asset)
						} else {
							task.comment = new GStringEval().toString(taskSpec.title, moveEvent)
						}
					} catch (Exception ex) {
						exceptions.append("Unable to parse title (${taskSpec.title}) for taskSpec ${taskSpec.id}<br/>")
						task.comment = "** Error computing title **"
					}
				*/
			} // Tasks section Tests
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
		rowMap.versionNumber = (rs.getInt('versionNumber').equals(0)) ? '' : rs.getInt('versionNumber')
		rowMap.hasWIP = (rs.getString('hasWIP').equals('1')) ? 'yes' : ''
		rowMap.context = rs.getString('context')
		rowMap.lastUpdated = rs.getTimestamp('last_updated')
		return rowMap
	}
}
