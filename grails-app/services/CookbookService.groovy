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

    static transactional = true
	static allowedCatalogs = ['All', 'Event', 'Bundle', 'Application']

	def namedParameterJdbcTemplate
	def projectService
	def partyRelationshipService
	def securityService
	
	/**
	 * Returns the information about a specific version of the Recipe
	 * 
	 * @param recipeId the id of the Recipe
	 * @param versionNumber the version of the Recipe
	 * @return a Map with information about the Recipe and the RecipeVersion
	 */
	def getRecipe(recipeId, versionNumber) {
		if (versionNumber == null) {
			def recipe = Recipe.get(recipeId)
			versionNumber = recipe.releasedVersion == null ? 0 : recipe.releasedVersion.versionNumber
		}

		def recipeVersion = RecipeVersion.findByIdAndVersionNumber(recipeId, versionNumber)
		def wip = RecipeVersion.findByIdAndVersionNumber(recipeId, 0)
		
		if (recipeVersion == null) {
			throw new EmptyResultException('Invalid recipe')
		}
		
		def loginUser = securityService.getUserLogin() 
		def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(recipeVersion.recipe.project)
		if (recipeVersion.recipe.project.id != Project.DEFAULT_PROJECT_ID
				&& !peopleInProject.contains(loginUser.person)) {
			throw new UnauthorizedException('The current user doesn\'t have access to the project')
		}
		
		def recipe = recipeVersion.recipe
		def person = recipeVersion.createdBy
		
		
		def result = [:]
		result.recipe = recipe
		result.recipeVersion = recipeVersion
		result.person = person
		result.wip = wip
		
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
	 * 
	 * @return a list of Maps with information about the recipes. See {@link RecipeMapper}
	 */
    def findRecipes(isArchived, catalogContext, searchText, projectType) {
		def projectIds = []
		
		def loginUser = securityService.getUserLogin() 
		if (projectType == null) {
			def currentProject = securityService.getUserCurrentProject()
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
		catalogContext = (allowedCatalogs.contains(catalogContext)) ? catalogContext : 'All'
		
		def arguments = [
			"isArchived" : isArchived,
			"catalogContext" : catalogContext,
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

		
		def recipes = namedParameterJdbcTemplate.query("""
				SELECT DISTINCT recipe.recipe_id as recipeId, recipe.name, recipe.description, CONCAT(person.first_name, ' ', person.last_name)  as createdBy, recipe.last_updated as lastUpdated, recipe_version.version_number as versionNumber, IF(ISNULL(rv2.version_number), false, true) as hasWIP 
				FROM recipe
				LEFT OUTER JOIN recipe_version ON recipe.released_version_id = recipe_version.recipe_version_id
				LEFT OUTER JOIN recipe_version as rv2 ON recipe.released_version_id = recipe_version.recipe_version_id AND recipe_version.version_number = 0
				INNER JOIN person ON person.person_id = recipe_version.created_by_id
				WHERE recipe.archived = :isArchived
				AND recipe.context = :catalogContext
				AND recipe.project_id IN (:projectIdsAsString)
				${searchCondition}
			""", arguments, new RecipeMapper())
		
		return recipes
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
		return rowMap
	}
}
