import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import org.apache.shiro.SecurityUtils

class CookbookService {

    static transactional = true
	static allowedCatalogs = ["All", "Event", "Bundle", "Application"]

	def jdbcTemplate
	def projectService
	def partyRelationshipService
	def securityService
	
	def getRecipe(recipeId, versionNumber) {
		if (versionNumber == null) {
			def recipe = Recipe.get(recipeId)
			versionNumber = recipe.releasedVersion == null ? 0 : recipe.releasedVersion.versionNumber
		}

		def recipeVersion = RecipeVersion.findByIdAndVersionNumber(recipeId, versionNumber)
		def wip = RecipeVersion.findByIdAndVersionNumber(recipeId, 0)
		
		if (recipeVersion == null) {
			throw new EmptyResultException("Invalid recipe")
		}
		
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(recipeVersion.recipe.project)
		if (recipeVersion.recipe.project.id != Project.DEFAULT_PROJECT_ID
				&& !peopleInProject.contains(loginUser.person)) {
			throw new UnauthorizedException("The current user doesn't have access to the project")
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
	
    def findRecipes(isArchived, catalogContext, searchText, projectType) {
		def projectIds = []
		
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		if (projectType == null) {
			def currentProject = securityService.getUserCurrentProject()
			if (currentProject == null) {
				throw new EmptyResultException()
			} else {
				projectIds.add(currentProject.id)
			}
		} else if (projectType.equals("master")) {
			projectIds.add(Project.DEFAULT_PROJECT_ID)
		} else if (projectType.equals("active")) {
		
			def projects = projectService.getActiveProject(new Date(), true, loginUser.person)
			projects.each { project ->
				projectIds.add(project.id)
			}
		} else if (projectType.equals("completed")) {
			def projects = projectService.getCompletedProject(new Date(), true);
			projects.each { project ->
				projectIds.add(project.id)
			}
		} else if (projectType?.isInteger()) {
			def project = Project.get(projectType.toInteger())
			if (project != null) {
				def peopleInProject = partyRelationshipService.getAvailableProjectStaffPersons(project)
				if (peopleInProject.contains(loginUser.person)) {
					projectIds.add(projectType.toInteger())
				} else {
					throw new UnauthorizedException("The current user doesn't have access to the project")
				}
			} else {
				throw new UnauthorizedException("The current user doesn't have access to the project")
			}
		}

		def projectIdsAsString = projectIds.isEmpty() ? "-1" : projectIds.join(',')
		isArchived = (isArchived.equals("y") ? "1" : 0)
		catalogContext = (allowedCatalogs.contains(catalogContext)) ? catalogContext : "All"
		searchText = searchText == null ? "" : searchText
		searchText = "%" + searchText + "%"

		def recipes = jdbcTemplate.query("""
				SELECT DISTINCT recipe.recipe_id as recipeId, recipe.name, recipe.description, CONCAT(person.first_name, ' ', person.last_name)  as createdBy, recipe.last_updated as lastUpdated, recipe_version.version_number as versionNumber, IF(ISNULL(rv2.version_number), false, true) as hasWIP 
				FROM recipe
				LEFT OUTER JOIN recipe_version ON recipe.released_version_id = recipe_version.recipe_version_id
				LEFT OUTER JOIN recipe_version as rv2 ON recipe.released_version_id = recipe_version.recipe_version_id AND recipe_version.version_number = 0
				INNER JOIN person ON person.person_id = recipe_version.created_by_id
				WHERE recipe.archived = ?
				AND recipe.context = ?
				AND (
					recipe.name like ?
					OR
					recipe.description like ?
				)
				AND
				recipe.project_id IN (?)
			""",
			[isArchived, catalogContext, searchText, searchText, projectIdsAsString].toArray(),
			new RecipeMapper())
		
		return recipes
    }
}

class RecipeMapper implements RowMapper {
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		def rowMap = [:]
		rowMap.recipeId = rs.getInt("recipeId")
		rowMap.name = rs.getString("name")
		rowMap.description = rs.getString("description")
		rowMap.createdBy = rs.getString("createdBy")
		rowMap.versionNumber = rs.getInt("versionNumber")
		rowMap.hasWIP = rs.getBoolean("hasWIP")
		return rowMap
	}
}
