import java.sql.ResultSet;
import java.sql.SQLException;

import com.tdssrc.grails.GormUtil;

import org.springframework.jdbc.core.RowMapper;
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
		
		if (recipeName == null || recipeContext == null || !allowedCatalogs.contains(recipeContext)) {
			throw new IllegalArgumentException('Please check allowed arguments')
		}
		
		def clonedVersion = null
		
		if (cloneFrom != null) {
			if (clonedFrom.isNumber()) {
				clonedVersion = RecipeVersion.get(clonedFrom)
				checkAccess(loginUser.person, clonedVersion.recipe.project)
			} else {
				log.info('Cloned from is not a number')
				throw new EmptyResultException()
			}
		}
		
		def recipe = new Recipe()
		def recipeVersion = new RecipeVersion()
		
		recipe.name = recipeName
		recipe.description = description
		recipe.context = recipeContext
		recipe.project = currentProject
		recipe.archived = false

		recipe.save(flush:true)
		
		recipeVersion.versionNumber = 0
		recipeVersion.createdBy = loginUser.person
		recipeVersion.recipe = recipe
		
		if (clonedVersion != null) {
			recipeVersion.cloneFrom = clonedVersion
			recipeVersion.sourceCode = clonedVersion.sourceCode
			recipeVersion.changelog = clonedVersion.changelog
		}
		
		recipeVersion.save()
	}

	/**
	 * Saves a specific recipe reference
	 * 
	 * @param recipeId
	 * @param recipeVersionId
	 * @param name
	 * @param description
	 * @param sourceCode
	 * @param changelog
	 * @return
	 */
	def save(recipeId, recipeVersionId, name, description, sourceCode, changelog) {
	}

	/**
	 * Returns the information about a specific version of the Recipe
	 * 
	 * @param recipeId the id of the Recipe
	 * @param versionNumber the version of the Recipe
	 * @return a Map with information about the Recipe and the RecipeVersion
	 */
	def getRecipe(recipeId, versionNumber, loginUser) {
		if (recipeId == null || !recipeId.isNumber()) {
			throw new EmptyResultException();
		}
		
		if (versionNumber == null) {
			def recipe = Recipe.get(recipeId)
			versionNumber = recipe.releasedVersion == null ? 0 : recipe.releasedVersion.versionNumber
		}

		def recipeVersion = RecipeVersion.findByIdAndVersionNumber(recipeId, versionNumber)
		def wip = RecipeVersion.findByIdAndVersionNumber(recipeId, 0)
		
		if (recipeVersion == null) {
			throw new EmptyResultException('Invalid recipe')
		}
		
		checkAccess(loginUser.person, recipeVersion.recipe.project)
		
		def recipeOfVersion = recipeVersion.recipe
		def person = recipeVersion.createdBy
		
		def result = [
			'recipe' : recipeOfVersion,
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
		if (catalogContext != null) {
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
