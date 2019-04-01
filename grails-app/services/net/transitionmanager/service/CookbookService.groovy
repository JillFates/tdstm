package net.transitionmanager.service

import com.tdsops.common.lang.CollectionUtils as CU
import com.tdsops.tm.domain.RecipeHelper
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.command.cookbook.ContextCommand
import net.transitionmanager.action.ApiAction
import net.transitionmanager.project.Project
import net.transitionmanager.task.Recipe
import net.transitionmanager.task.RecipeVersion
import net.transitionmanager.tag.Tag
import net.transitionmanager.imports.TaskBatch
import net.transitionmanager.security.Permission
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

import static com.tdsops.tm.enums.domain.ProjectStatus.ACTIVE
import static com.tdsops.tm.enums.domain.ProjectStatus.COMPLETED
import static net.transitionmanager.project.Project.DEFAULT_PROJECT_ID
/**
 * Handles the logic for creating recipes and running the cookbook
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Transactional
class CookbookService implements ServiceMethods {

	private static final List<String> allowedCatalogs = ['Event', 'Bundle', 'Application'].asImmutable()
	private static final List<String> searchAllowedCatalogs = ['All', 'Event', 'Bundle', 'Application'].asImmutable()

	NamedParameterJdbcTemplate 	namedParameterJdbcTemplate
	PartyRelationshipService 	partyRelationshipService
	JdbcTemplate 	jdbcTemplate
	PersonService 	personService
	ProjectService 	projectService
	TaskService		taskService

	/**
	 * Checks if current user can access project. If it can't then it throws an {@link UnauthorizedException}
	 * @param project the project
	 */
	private void checkAccess(Project project) {
		if (project == null || !securityService.loggedIn) {
			return
		}

		if (! personService.hasAccessToProject(project)) {
		// List<Long> peopleIdsInProject = partyRelationshipService.getAvailableProjectStaffPersons(project)*.id
		// if (project.id != DEFAULT_PROJECT_ID && !peopleIdsInProject.contains(securityService.currentPersonId)) {
			log.warn('CookbookService.checkAccess: Person does not have access to the project')
			throw new UnauthorizedException('The current user does not have access to the project')
		}
	}

	/**
	 * Creates a Recipe using the information passed
	 *
	 * @param name the name of the recipe
	 * @param description the description of the recipe
	 * @param context the context
	 * @param cloneFrom the id of the recipe to be cloned from
	 */
	RecipeVersion createRecipe(String recipeName, String description, cloneFrom) {
		securityService.requirePermission Permission.RecipeCreate

		Project project = securityService.getUserCurrentProjectOrException()

		//TODO check this checkAccess(project)

		Recipe clonedVersion
		String defaultSourceCode = ''
		String defaultChangelog = ''
		RecipeVersion clonedReleasedVersion
		String context = '{}'

		if (cloneFrom != null) {
			clonedVersion = Recipe.get(cloneFrom)
		} else {
			Recipe defaultRecipe = Recipe.findByNameAndProject('Default', Project.getDefaultProject())

			if (defaultRecipe?.releasedVersion != null) {
				defaultSourceCode = defaultRecipe.releasedVersion.sourceCode
				defaultChangelog = defaultRecipe.releasedVersion.changelog
			}
		}

		if (clonedVersion != null) {

			if(!clonedVersion.project.isDefaultProject()){
				context = clonedVersion.context
			}

			if (clonedVersion.releasedVersion != null) {
				defaultSourceCode = clonedVersion.releasedVersion.sourceCode
				clonedReleasedVersion = clonedVersion.releasedVersion
			} else {
				def recipeVersions = findRecipeVersionsWithSourceCode(cloneFrom)

				if (recipeVersions)  {
					def wip = recipeVersions[0]
					defaultSourceCode = wip.sourceCode
					clonedReleasedVersion = clonedVersion.releasedVersion
				}
			}
		}

		return createRecipeAndRecipeVersion(recipeName, description, context, project, defaultSourceCode, defaultChangelog, clonedReleasedVersion)
	}

	/**
	 * Clones a Recipe and RecipeVersion using the information passed
	 *
	 * @param recipeVersionId The id of the RecipeVersion record to clone
	 * @param name the name of the new recipe
	 * @param description the description of the new recipe
	 * @return the new RecipeVersion created with a new Recipe
	 */
	RecipeVersion cloneRecipe(recipeVersionId, String name, String description) {
		securityService.requirePermission Permission.RecipeEdit

		// TODO: 170822 oluna, have to lookup Projects that the user has access to.
		Project project = securityService.getUserCurrentProjectOrException()

		if (!name) {
			throw new EmptyResultException()
		}

		RecipeVersion recipeVersion = RecipeVersion.get(recipeVersionId)

		def recipe = recipeVersion.recipe
		String context = recipe.project.isDefaultProject() ? '{}' : recipe.context()
		boolean valid = recipe.projectId == DEFAULT_PROJECT_ID ||
		                recipe.project == project ||
		                projectService.getUserProjects().find() { recipe.projectId == it.id }
		if (!valid) {
			throw new EmptyResultException()
		}

		createRecipeAndRecipeVersion(name, description, context, project, recipeVersion.sourceCode, '', recipeVersion)
	}

	/**
	 * Unifies create recipe and reciveVersion
	 *
	 * @param name recipe name
	 * @param description recipe description
	 * @param context recipe context
	 * @param project recipe project
	 * @param sourceCode recipeVersion source code
	 * @param changelog recipeVersion changelog
	 * @param recipeVersion original recipeVersion
	 * @return the new RecipeVersion created with a new Recipe
	 */
	private RecipeVersion createRecipeAndRecipeVersion(
		String name,
		String description,
		String context,
		Project project,
		String sourceCode,
		String changelog,
		RecipeVersion recipeVersion) {

		def newRecipe = new Recipe(
			name: name,
			description: description,
			context: context,
			project: project,
			archived: false
		).save(flush:true)

		new RecipeVersion(sourceCode: sourceCode, changelog: changelog, clonedFrom: recipeVersion,
				recipe: newRecipe, versionNumber: 0, createdBy: securityService.loadCurrentPerson()).save()
	}

	/**
	 * Deletes a Recipe using the information passed
	 *
	 * @param recipeId the id of the recipe
	 */
	def deleteRecipe(recipeId) {
		securityService.requirePermission Permission.RecipeDelete

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		// Update all TaskBatch to null the reference to the recipe
		TaskBatch.executeUpdate('update TaskBatch set recipe=null where recipe=?', [recipe])

		def rvList = RecipeVersion.findAllByRecipe(recipe)
		log.debug 'Found {} recipe versions to be deleted', rvList.size()
		if (rvList) {
			// Update all TaskBatch to null the reference to the recipeVersions
			TaskBatch.executeUpdate('update TaskBatch set recipeVersionUsed=null where recipeVersionUsed in (:rvList)', [rvList:rvList])
			RecipeVersion.executeUpdate('update RecipeVersion rv set rv.clonedFrom=null where rv.clonedFrom in (:rvList)', [rvList:rvList])
		}

		recipe.releasedVersion=null
		recipe.save(flush:true)

		// Remove all versions of the recipes
		RecipeVersion.executeUpdate('delete RecipeVersion rv where rv.recipe=:recipe', [recipe:recipe])

		recipe.delete()

		return recipe
	}

	/**
	 * Deletes a Recipe version using the information passed
	 *
	 * @param recipeId the id of the recipe
	 * @param recipeVersion the version of the recipeVersion
	 */
	def deleteRecipeVersion(recipeId, recipeVersion) {
		securityService.requirePermission Permission.RecipeDelete

		if (recipeVersion == null || !recipeVersion.isNumber()) {
			throw new EmptyResultException()
		}

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		def rv = RecipeVersion.findByRecipeAndVersionNumber(recipe, recipeVersion)
		if (rv == null) {
			throw new EmptyResultException()
		}

		if (rv.recipe != recipe) {
			log.warn('Recipe and version does not have a common recipe')
			throw new InvalidParamException('Recipe and version does not have a common recipe')
		}

		if (recipe.releasedVersion == rv) {
			log.warn('Can not delete the currently published version')
			throw new InvalidParamException('Can not delete the currently published version')
		}

		jdbcTemplate.update('UPDATE recipe_version SET cloned_from_id=NULL WHERE cloned_from_id=?', rv.id)
		jdbcTemplate.update('UPDATE task_batch SET recipe_version_used_id=NULL WHERE recipe_version_used_id=?', rv.id)
		rv.delete()
		return rv
	}

	/**
	 * Update Existing Recipe data
	 * @param recipeId
	 * @param recipeName
	 * @param description
	 * @return return the edited Recipe object
	 */
	Recipe updateRecipe(long recipeId, recipeName, description) {
		//TODO check this checkAccess(project)
		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		recipe.name = recipeName
		recipe.description = description
		save recipe
		return recipe
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
	 */
	RecipeVersion saveOrUpdateWIPRecipe(long recipeId, Long recipeVersionId, String name, String description,
	                                    String sourceCode, String changelog) {
		securityService.requirePermission Permission.RecipeEdit

		Project project = securityService.userCurrentProject
		if (project == null) {
			log.warn 'SECURITY: User {} attempting to update a recipe without a valid project, recipe id: {}',
					securityService.currentUsername, recipeId
			throw new InvalidParamException('You must select a project before being able to edit recipes')
		}


		Recipe recipe = Recipe.get(recipeId)
		assertProject recipe, project

		// Validate that the syntax is correct before submitting
		// TODO - Add logic to validate syntax

		// TODO - why two lookups?
		def recipeVersion = RecipeVersion.get(recipeVersionId)

		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
		if (wip == null) {
			wip = new RecipeVersion()
			wip.versionNumber = 0
			wip.createdBy = securityService.loadCurrentPerson()
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

		wip.save()

		return wip
	}

	/**
	 * Releases a WIP recipe version using the recipeId
	 * @param recipeId the id
	 */
	void releaseRecipe(long recipeId) {
		// securityService.requirePermission Permission.RecipeRelease

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		//TODO check this checkAccess(project)
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)

		if (wip == null || wip.versionNumber != 0) {
			throw new IllegalArgumentException('Not a WIP')
		}

		int max = 0
		try {
			max = jdbcTemplate.queryForObject('SELECT MAX(version_number) FROM recipe_version WHERE recipe_id=?', Integer, wip.recipeId)
		} catch (IncorrectResultSizeDataAccessException e) {
			log.warn('No results when looking for a version number')
		}

		wip.versionNumber = max + 1
		wip.recipe.releasedVersion = wip

		wip.save()
	}

	/**
	 * Reverts the recipe to the recipeVersionId version
	 * @param recipeVersionId the id of the recipeVersion
	 * @return Recipe Version Applied
	 */
	Recipe revertRecipe(long recipeVersionId) {
		// securityService.requirePermission Permission.RecipeRevert ?

		Project project = securityService.getUserCurrentProjectOrException()

		RecipeVersion recipeVersion =  RecipeVersion.get(recipeVersionId)

		if (recipeVersion == null) {
			throw new IllegalArgumentException('Recipe Version Not found!')
		}

		if (recipeVersion.versionNumber == 0) {
			throw new IllegalArgumentException('Trying to revert a WIP')
		}

		Recipe recipe = recipeVersion.recipe
		assertProject(recipe, project)

		recipe.releasedVersion = recipeVersion

		if (! recipe.save(failOnError: false)) {
			throw new DomainUpdateException('Unable to save change', recipe)
		}

		return recipe
	}

	/**
	 * Validates the syntax for sourceCode for the current user and project
	 *
	 * @param sourceCode the source code to be validated
	 * @return the list of errors found or empty list if everything looks fine
	 */
	List<Map> validateSyntaxForUser(sourceCode) {
		securityService.requirePermission Permission.RecipeEdit
		securityService.getUserCurrentProjectOrException()

		validateSyntax(sourceCode)
	}

	/**
	 * Returns a list of groups based on the recipeVersionId and the context
	 *
	 * @param recipeVersionId the id of the RecipeVersion
	 * @param context the MoveEventId and or tags
	 * @return the list of groups
	 */
	List<Map> getGroups(Long recipeVersionId, ContextCommand context, String predSourceCode) {
		boolean validRecipeId = recipeVersionId != null

		if (!validRecipeId && predSourceCode == null) {
			throw new EmptyResultException('Invalid recipeVersionId')
		}

		RecipeVersion recipeVersion
		def sourceCode

		Project project = securityService.userCurrentProject
		if (validRecipeId) {
			recipeVersion = RecipeVersion.get(recipeVersionId)

			if (recipeVersion == null) {
				throw new EmptyResultException('Recipe version does not exists')
			}

			def recipe = recipeVersion.recipe
			assertProject recipe, project

			sourceCode = recipeVersion.sourceCode
		} else {
			sourceCode = predSourceCode ?: ''
		}

		checkAccess(project)

		def fetchedGroups = taskService.fetchGroups(parseRecipeSyntax(sourceCode), context, new StringBuilder(), project)
		return fetchedGroups.collect({ k, v ->
			def assets = v.collect { asset -> [id: asset.id, name: asset.assetName, assetType: asset.assetType] }
			[name: k, assets: assets]
		})
	}

	/**
	 * Returns the information about a specific version of the Recipe
	 *
	 * @param recipeId the id of the Recipe
	 * @param versionNumber the version of the Recipe
	 * @return a Map with information about the Recipe and the RecipeVersion
	 */
	Map getRecipe(long recipeId, Integer versionNumber) {
		Recipe recipe = Recipe.get(recipeId)
		if (versionNumber == null) {
			versionNumber = recipe.releasedVersion?.versionNumber ?: 0
		}

		def recipeVersion = RecipeVersion.findByRecipeAndVersionNumber(recipe, versionNumber)
		def wip = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)

		if (recipeVersion == null) {
			throw new EmptyResultException('Invalid recipe')
		}

		//checkAccess(`recipe.project`)

		Map context = recipe.context()

		context.tag.each{Map tag->
			boolean tagExists = Tag.where{id == tag.id}.count()
			tag.strike = !tagExists
		}

		[recipe: recipe, recipeVersion: recipeVersion, person: recipeVersion.createdBy, wip: wip, context: context]
	}

	/**
	 * Finds the recipes according to the information provided
	 *
	 * @param isArchived indicates if service should return active or archived recipes. Valid values (y|n).
	 * @param catalogContext used to filter context of recipes: All, Event, Bundle, Application
	 * @param searchText to search for text in name and description
	 * @param projectType project indicates which project to provide list of recipes for (master, active, complete or integer)
	 * @param sortField: the field to sort recipes by.
	 * @param sortOrder: sorting order (asc or desc)
	 * When set to master, it will list search the master/default project.
	 * When set to active, it will search all active projects the user has access to
	 * When set to completed, it will search all completed projects that the user has access to
	 * When set to a numeric value, it will search the specific project by id (as long as the user is associated to the project)
	 *
	 * @return a list of Maps with information about the recipes. See {@link RecipeMapper}
	 */
	List<Map> findRecipes(String isArchived, catalogContext, searchText, projectType, sortField="lastUpdated", sortOrder="desc") {
		def projectIds = []

		Project project = securityService.userCurrentProject

		if (projectType == null) {
			if (!project) {
				throw new EmptyResultException()
			}
			projectIds.add(project.id)
		} else {

			projectType = (projectType.class == String) ? projectType.toLowerCase() : projectType
			if (projectType.isNumber()) {
				projectType = projectType.toLong()
			}

			switch (projectType) {
				case 'master': projectIds.add(DEFAULT_PROJECT_ID); break
				case 'active': projectService.getUserProjects(true, ACTIVE).each { projectIds << it.id }; break
				case 'completed': projectService.getUserProjects(true, COMPLETED).each { projectIds << it.id }; break
				case Long:
					Project projectById = Project.get(projectType)
					if (!projectById) {
						throw new UnauthorizedException('The current user does not have access to the project')
					}
					def userProjectIds = projectService.getUserProjects(
							securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ANY)*.id

					if (!Project.isDefaultProject(projectById) && !userProjectIds.contains(projectById.id)) {
						throw new UnauthorizedException("The current user doesn't have access to the project")
					}

					projectIds.add(projectById.id)
					break
				default :
					log.info('Reach Default value. Rare case')
					return []
			}
		}

		def projectIdsAsString = projectIds.isEmpty() ? '-1' : GormUtil.asCommaDelimitedString(projectIds)
		isArchived = isArchived == 'y' ? '1' : 0
		catalogContext = searchAllowedCatalogs.contains(catalogContext) ? catalogContext : null

		def arguments = [isArchived: isArchived, projectIdsAsString: projectIdsAsString]

		String searchCondition = ''
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

		String catalogCondition = ''
		if (catalogContext != null && allowedCatalogs.contains(catalogContext)) {
			catalogCondition = 'AND recipe.context = :catalogContext'
			arguments.catalogContext = catalogContext
		}

		String sortBy = " ORDER BY $sortField $sortOrder "

		namedParameterJdbcTemplate.query('''
			SELECT DISTINCT recipe.recipe_id as recipeId,
				recipe.name,
				recipe.description,
				recipe.context,
				COALESCE(rv2.last_updated, recipe_version.last_updated) as lastUpdated,
			       IF(ISNULL(p2.first_name), CONCAT(p1.first_name, ' ', p1.last_name),
				CONCAT(p2.first_name, ' ', p2.last_name)) as createdBy,
				recipe_version.version_number as versionNumber,
				IF(ISNULL(rv2.version_number), false, true) as hasWIP
			FROM recipe
			LEFT OUTER JOIN recipe_version ON recipe.released_version_id = recipe_version.recipe_version_id
			LEFT OUTER JOIN recipe_version as rv2 ON recipe.recipe_id = rv2.recipe_id AND rv2.version_number = 0
			LEFT OUTER JOIN person as p1 ON p1.person_id = recipe_version.created_by_id
			LEFT OUTER JOIN person as p2 ON p2.person_id = rv2.created_by_id
			WHERE recipe.archived = :isArchived
			  AND recipe.project_id IN (:projectIdsAsString)
		''' + searchCondition + catalogCondition + sortBy, arguments, new RecipeMapper())
	}

	/**
	 * Finds the recipes versions for a given recipe id.
	 *
	 * @param recipeId recipe id to search
	 *
	 * @return a list of Maps with information about the recipes. See {@link RecipeMapper}
	 */
	List<Map> findRecipeVersions(recipeId) {
		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		def arguments = [
				"recipeId" : recipeId
		]

		return namedParameterJdbcTemplate.query("""
			SELECT DISTINCT recipe.recipe_id as recipeId, recipe_version.recipe_version_id as recipeVersionId,
							recipe_version.version_number as versionNumber, recipe_version.last_updated as lastUpdated,
							CONCAT(person.first_name, ' ', person.last_name) as createdBy, if ((recipe.released_version_id = recipe_version.recipe_version_id), true, false) as isCurrentVersion
			FROM recipe
			INNER JOIN recipe_version ON recipe.recipe_id = recipe_version.recipe_id
			INNER JOIN person ON person.person_id = recipe_version.created_by_id
			WHERE recipe.recipe_id = :recipeId
			ORDER BY version_number DESC
			""", arguments, new RecipeVersionMapper())
	}

	/**
	 * Finds the recipes versions for a given recipe id. Without checking project and including source code and changelog
	 *
	 * @param recipeId recipe id to search
	 *
	 * @return a list of Maps with information about the recipes. See {@link RecipeMapper}
	 */
	def findRecipeVersionsWithSourceCode(recipeId) {
		// arecordon: it seems that there are places where a string is passed, instead of a long.
		recipeId = NumberUtil.toLong(recipeId)

		if (recipeId == null) {
			throw new EmptyResultException()
		}

		 def arguments = [
		"recipeId" : recipeId
		]

		return namedParameterJdbcTemplate.query("""SELECT DISTINCT recipe.recipe_id as recipeId, recipe_version.recipe_version_id as recipeVersionId,
			        recipe_version.source_code as sourceCode, recipe_version.changelog as changelog,
			        recipe_version.version_number as versionNumber, recipe_version.last_updated as lastUpdated,
			        if ((recipe.released_version_id = recipe_version.recipe_version_id), true, false) as isCurrentVersion
	        FROM recipe
			 INNER JOIN recipe_version ON recipe.recipe_id = recipe_version.recipe_id
			 WHERE recipe.recipe_id=:recipeId
			 ORDER BY version_number ASC""", arguments, new RecipeVersionCompleteMapper())
	}

	/**
	 * Archives the recipe with recipeId depending on the archived parameter
	 *
	 * @param recipeId the id of the recipe
	 * @param archived true to archive, false to unarchived
	 */
	Recipe archivedUnarchived(recipeId, archived) {
		securityService.requirePermission Permission.RecipeEdit

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)

		recipe.archived = archived
		return save(recipe)
	}

	/**
	 * Used to convert the Recipe source code from syntax into a Map
	 * @param sourceCode the source code that represents the recipe (presently represents a Groovy Map)
	 * @return The recipe in a Groovy Map containing the various elements of the recipe
	 * @throws InvalidSyntaxException if the sourcecode is invalid
	 */
	Map parseRecipeSyntax(String  sourceCode) {
		if (!sourceCode) {
//			throw new InvalidSyntaxException('Recipe contains no source code')
			throw new RuntimeException('Recipe contains no source code')
		}

		//log.debug 'About to parse the recipe:\n{}', sourceCode
		try {
			return Eval.me("[$sourceCode]")
		} catch (e) {
			//throw new InvalidSyntaxException(e.message.replaceAll(/[\r]/, '<br/>'))
			throw new RuntimeException(e.message.replaceAll(/[\r]/, '<br/>'))
		}
	}

	List<Map> validateSyntax(String sourceCode, Project project = null) {
		try {
			basicValidateSyntax(sourceCode, project)
		} catch (e) {
			[[error: 1, reason: 'Invalid syntax', detail: e.message?.replaceAll(/[\r]/, '<br/>')]]
		}
	}

	def genericConditionallyMandatoryValidator(spec, options, attribute, value, i, errorList, errorLabel) {
		if (value in options) {
			if (!spec.containsKey(attribute)) {
				errorList << [error: 1, reason: 'Invalid Syntax',
				              detail: "$errorLabel in element $i. $value taskspec requires '$attribute' property."]
			}
			else {
				if (spec[attribute] instanceof String) {
					if (!spec[attribute].trim().size()) {
						errorList << [error: 1, reason: 'Invalid Syntax',
						              detail: "$errorLabel in element $i. Property '$attribute' for taskspec $value can't be empty."]
					}
				}
			}
		}
	}

	/**
	 * Validate the presence of the disposition attribute
	 * depending on the value given for 'action.' See TM-3244.
	 */
	def validateDisposition(spec, value, i, errorList, errorLabel) {
		genericConditionallyMandatoryValidator(spec, ['location', 'room', 'rack'],
		                                              'disposition', value, i, errorList, errorLabel)
	}

	def validateFilter(spec, value, i, errorList, errorLabel) {
		genericConditionallyMandatoryValidator(spec, ['set', 'truck', 'location', 'room', 'rack', 'cart'],
			'filter', value, i, errorList, errorLabel)
	}

	def validateSetOn(spec, value, i, errorList, errorLabel) {
		genericConditionallyMandatoryValidator(spec, ['set'], 'setOn', value, i, errorList, errorLabel)
	}

	def validate

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
	List<Map> basicValidateSyntax(sourceCode, Project currentProject = null) {

		def errorList = [] as HashSet
		def recipe
		def msg

		if ( !currentProject ) {
			// Reference to current project
			currentProject = securityService.getUserCurrentProjectOrException()
		}
		// Keeps an in-memory list of all the possible Api Actions to querying multiple times.
		List apiActions = ApiAction.findAllByProject(currentProject)

		// TODO: ValidateSyntax - Add a check to make sure that any filters that specify a group, that the group is defined

		// Helper closure that compares the properties of a spec to a defined map
		/**
		 * Used to validate a section of a recipe spec (e.g. group) against a map definition for the section. The closure will
		 * use recursion to validate maps within the maps.
		 * @param type - the type or section of the spec (e.g. task, group)
		 * @param spec - the map containing the values define in the actual recipe spec section
		 * @param map - the definition map for the section of the spec
		 * @param key - the primary reference code for the section as specified in the recipe (e.g task spec id or group name)
		 * @return List of error messages
		 */
		def validateAgainstMap
		validateAgainstMap = { String type, spec, map, String key ->
			int i = 0
			if (key == null) {
				key = type == 'task' ? "${spec.id ?: 'UNDEF'}" : "${spec.name ?: 'UNDEF'}"
			}
			def label = type == 'task' ? "Task id $key" : "Group $key"

			spec.each { n, v ->
				i++

				if (n == "action" && type == "task") {
					validateDisposition(spec, v, i, errorList, label)
					validateFilter(spec, v, i, errorList, label)
					validateSetOn(spec, v, i, errorList, label)
				}

				if (n == "sendNotification" && type == "task" && (v != true && v!= false)) {
					errorList << [error: 1, reason: 'Invalid Syntax',
						detail: "$label in element $i must be either true or false"]
				}

				// For docLink we support valid URLs and any #string referencing another string field in AssetEntity.
				if (n == "docLink") {
					String errorMsg = RecipeHelper.validateDocLinkSyntax(v)
					if (errorMsg) {
						errorList << [error: 1, reason: "Invalid Syntax",
							detail: "$label in element $i: $errorMsg"]
					}
				}

				if (n=="category" && !(v in AssetCommentCategory.list)) {
					errorList << [error: 1, reason: 'Invalid Category',
						detail: "$label in element $i contains unknown category '$v'"]
				}

				if ( n == "invoke" && type == "task") {
					String method = v["method"]
					if (method){
						ApiAction apiAction = null
						for (action in apiActions) {
							if (action.name == method ) {
								apiAction = action
								break
							}
						}
						if ( !apiAction) {
							errorList << [error: 4, reason: "Invalid API Action Reference",
							detail: "$label in element $i references an API action that doesn't exist."]
						}
					} else {
						errorList << [error: 1, reason: "Invalid Syntax",
							detail: "$label in element $i doesn't specify a method for '$n'"]
					}
					/*
						TODO (arecordon)
						As stated by jmartin in TM-5989. We aren't going to perform further
						analysis on this field.
						We need to break the execution to avoid a failure because of unexpected
						properties.
					*/
					return
				}

				if (map.containsKey(n)) {
					// can do more here to check the nested definitions later on.

					if ((map[n] instanceof Map)) {
						if (!(spec[n] instanceof Map)) {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$label in element $i property '$n' should be a map. Invalid value '$v' was given"]
						} else {
							// Check the sub section of the spec against a sub section of the map
							// TODO : JPM 6/2016 : TM-4989 the addAll should most likely be removed
							errorList.addAll(validateAgainstMap(type, spec[n], map[n], key))
						}

					} else if (map[n] instanceof List) {

						if (v in String) {
							// Check if the value of a property exists in the map defined list
							// Need to strip out any boolean expressions
							def cleanedExp = v.replaceAll(/[!=<>]/, '')
							if (!map[n].contains(cleanedExp)) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$label in element $i property '$n' contains invalid value '$v'"]
							}
						} else {
							errorList << [error: 1, reason: 'Invalid syntax',
									detail: "Simple value expected for property '$n' but '$v' was given."]
						}
					}
				}
				else {
					errorList << [error: 1, reason: 'Invalid syntax',
						detail: "$label in element $i contains unknown property '$n'"]
				}
			}
			return errorList as List
		}

		// Definition of the properties supported by group
		def groupProps = [
			name: '',
			description: '',
			filter: [
				group: 0,
				include: 0,
				exclude: 0,
				taskSpec: 0,
				class: ['device', 'database', 'application', 'storage'],
				asset: 0,
				dependency: [
					mode:  ['supports', 'requires'],
					class: ['device','database','application','storage'],
					asset: [physical: true, virtual: true],
					c1: '',
					c2: '',
					c3: '',
					c4: '',
					comment: '',
					dataFlowFreq: '',
					dataFlowDirection: '',
					type: 0,
					status: 0
				],
				tag: '',
				tagMatch: 'ANY'
			]
		]

		// Definition of the properties supported by task
		def taskProps = [
			id: 0,
			title:0,
			description:0,
			filter: groupProps.filter,
			type:['asset','action','milestone','gateway','general'],
			action: ['rollcall','location','room','rack','truck','set', 'cart'],
			disposition: ['source', 'target'],
			setOn:0,
			disabled:false,
			workflow:0,
			duration:'',
			team:0,
			category:0,
			estStart:0,
			estFinish:0,
			priority:0,
			effort:0,
			chain:0,
			sendNotification:0,
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
				required:true,
				typeSpec:0,
				taskSpec:0,
				inverse:true,
				classification: ['device','database','application','storage'],
				type:'',
				status:'',
				traverse:false
			],
			successor: [
				defer: '',
				gather: []
			],
			constraintTime:0,
			constraintType:0,
			class:['device','database','application','storage'],
			invoke:[:],
			docLink:0
		]

		def teamCodes = []
		if (partyRelationshipService) {
			teamCodes = partyRelationshipService.getStaffingRoles()*.id
			log.debug '**** teamCodes == {} ****', teamCodes
		}

		try {
			recipe = parseRecipeSyntax(sourceCode)
		} catch (e) {
			errorList << [error: 1, reason: 'Invalid syntax', detail: e.message.replaceAll(/[\r]/, '<br/>')]
		}

		if (!errorList) {
			// If the syntax compiled then we can start examining different sections of the recipe for common mistakes

			def index = 0

			// Note that Groups are optional
			def hasGroups = recipe.containsKey('groups')
			def groupKeys = [:]
			def classNames = ['application','device','database','storage']
			if (hasGroups) {
				// Check to see if groups has expected elements
				recipe.groups.each { group ->
					index++
					if (group.containsKey('name')) {
						// Check for spaces
						if (group.name.contains(' ')) {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "Group name '$group.name' in element $index contains unsupported space character(s)"]
						}
						if (group.name.trim() == '') {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "Group name '$group.name' in element $index is blank"]
						}
						if (groupKeys.containsKey(group.name)) {
							errorList << [error: 5, reason: 'Duplicate group',
								detail: "Group name '$group.name' duplicated in group ${groupKeys[group.name]} and $index"]
						}
						groupKeys[group.name] = index
					}
					if (group.containsKey('filter')) {
						if ((group.filter instanceof Map)) {
							if (group.filter.containsKey('taskSpec')) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "Group '$group.name' in element $index references a taskSpec which is not supported in groups"]
							}

							// Check for any unsupported properties (misspellings, etc)
							validateAgainstMap('group', group, groupProps, null)

							// Validate the filter.dependency map settings
							if (group.filter.containsKey('dependency')) {
								if ((group.filter.dependency instanceof Map)) {
									if (group.filter.dependency.containsKey('mode')) {
										// Now we need to find assets that are associated via the AssetDependency domain
										def depMode = group.filter.dependency.mode.toLowerCase()
										if (!depMode || !['s','r'].contains(depMode[0])) {
											errorList << [error: 1, reason: 'Invalid syntax',
												detail: "Group '$group.name' in element $index 'filter.dependency.mode' must be [supports|requires]"]
										}
										if (group.filter.dependency.containsKey('asset')) {
											if ((group.filter.dependency.asset instanceof Map)) {
												def suppAttribs = ['virtual','physical']
												group.filter.dependency.asset.each { n, v ->
													if (!suppAttribs.contains(n)) {
														errorList << [error : 1, reason: 'Invalid syntax',
														              detail: "Group '$group.name' in element $index 'filter.dependency.asset' contains unsupport property '$n'"]
													}
												}
												//
											} else {
												errorList << [error: 1, reason: 'Invalid syntax',
													detail: "Group '$group.name' in element $index 'filter.dependency.asset' element not properly defined as a map"]
											}
										}
									} else {
										errorList << [error: 3, reason: 'Missing property',
											detail: "Group '$group.name' in element $index is missing required 'filter.dependency.mode' property"]
									}
								} else {
									errorList << [error: 1, reason: 'Invalid syntax',
										detail: "Group '$group.name' in element $index 'filter.dependency' element not properly defined as a map"]
								}
							}
						} else {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "Group '$group.name' in element $index 'filter' element not properly defined as a map"]
						}
					} else {
						errorList << [error: 3, reason: 'Missing property',
							detail: "Group $group.name in element $index is missing require section 'filter'"]
					}
				}

				// Check to see that any include/exclude references match defined groups
				index = 0
				recipe.groups.each { group ->
					index++
					if (group.containsKey('filter') && (group.filter instanceof Map)) {
						['exclude','include'].each { ei ->
							if (group.filter.containsKey(ei)) {
								CU.asList(group.filter[ei]).each { gName ->
									if (!groupKeys.containsKey(gName)) {
										errorList << [error: 4, reason: 'Invalid group reference',
											detail: "Group $group.name in element $index 'filter.$ei' references undefined group $gName"]
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

			if (!recipe.containsKey('tasks')) {
				errorList << [error: 2, reason: 'Missing section', detail: '''Recipe is missing required 'tasks' section''']
			} else {

				def taskIds = []
				index=0
				def lastId=0
				def match

				recipe.tasks.each { task ->
					index++
					def taskId
					def taskRef = "Task in element $index"
					def type = task.type ?: ''

					if (!(task instanceof Map)) {
						errorList << [error: 1, reason: 'Invalid syntax',
							detail: "$taskRef is not a valid map definition"]
						return
					}

					// Test that the 'id' exists, that it isn't duplicated and that it is a positive whole number
					if (task.containsKey('id')) {
						if ((task.id instanceof Integer) && task.id > 0) {
							taskId = task.id
							taskRef = "Task id $taskId"
							if (taskIds.contains(task.id)) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef is a duplicate of an earlier task spec"]
							} else {
								taskIds << task.id
							}
							// Make sure that the id #s are assending
							if (lastId) {
								if (task.id < lastId) {
									errorList << [error: 1, reason: 'Invalid syntax',
										detail: "$taskRef task id is smaller than previous task spec. The ids must have ascending values."]
								}
							}
							lastId = task.id

						} else {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$taskRef 'id' must be a positive whole number > 0"]
						}
					} else {
						errorList << [error: 3, reason: 'Missing property',
							detail: "$taskRef is missing require property 'id' which must be a unique number"]
					}

					// Check for any unsupported properties (misspellings, etc)
					validateAgainstMap('task', task, taskProps, null)

					if (task.containsKey('filter') && (task['filter'] instanceof Map)) {
						def taskFilter = task.filter

						if (taskFilter.containsKey('group')) {
							validateGroupReferences(task, 'filter/group', taskFilter.group, groupKeys, errorList)
						}
						if (taskFilter.containsKey('include')) {
							validateGroupReferences(task, 'filter/include', taskFilter.include, groupKeys, errorList)
						}
						if (taskFilter.containsKey('exclude')) {
							validateGroupReferences(task, 'filter/exclude', taskFilter.exclude, groupKeys, errorList)
						}
					}

					// Validate the predecessor specifications
					if (task.containsKey('predecessor')) {
						def predecessor = task.predecessor

						if (!(predecessor instanceof Map)) {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$taskRef 'predecessor' attribute is not a valid map definition"]
						} else {

							// Validate if taskSpec was used, to make sure that it references a previously defined spec
							if (predecessor.containsKey('taskSpec')) {
								CU.asList(predecessor.taskSpec).each { tsid ->
									if (tsid instanceof Integer && tsid > 0) {
										if (taskIds.contains(tsid)) {
											if (taskId && tsid == taskId) {
												errorList << [error: 4, reason: 'Invalid Reference',
													detail: "$taskRef 'predecessor.taskSpec' contains reference ($tsid) to itself."]
											}
										} else {
											errorList << [error: 4, reason: 'Invalid Reference',
												detail: "$taskRef 'predecessor.taskSpec' contains invalid id reference ($tsid). TaskSpec ids must reference a previously defined TaskSpec."]
										}
									} else {
										errorList << [error: 1, reason: 'Invalid syntax',
											detail: "$taskRef 'predecessor.taskSpec' contains invalid id ($tsid). Ids must be a positive whole number > 0"]
									}
								}
							}

							// Validate the filter section
							if (predecessor.containsKey('filter')) {
								def filter = predecessor.filter
								if (!(filter instanceof Map)) {
									errorList << [error: 1, reason: 'Invalid syntax',
										detail: "$taskRef 'predecessor.filter' attribute is not a valid map definition"]
								}
							}

							// Check for Mode/TaskSpec/Group requirement
							boolean hasMode = predecessor.containsKey('mode')
							boolean hasTaskSpec = predecessor.containsKey('taskSpec')
							boolean hasGroup = predecessor.containsKey('group')
							boolean hasDefer = predecessor.containsKey('defer')
							boolean hasGather = predecessor.containsKey('gather')

							if (hasGroup) {
								validateGroupReferences(task, 'predecessor/group', predecessor.group, groupKeys, errorList)
							}
							if (!(hasMode || hasTaskSpec || hasGroup || hasGather)) {
								errorList << [error: 3, reason: 'Missing property',
									detail: "$taskRef 'predecessor' section requires [mode | taskSpec | group | gather] property"]
							} else if (hasMode && hasGroup) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef 'predecessor' section contains 'mode' and 'group' properties which are mutually exclusive"]
							} else if (hasTaskSpec && hasGroup) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef 'predecessor' section contains 'taskSpec' and 'group' properties which are mutually exclusive"]
							}

							if (hasMode && predecessor.mode == 'both' && hasDefer) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef 'predecessor' section contains 'defer' and mode with 'both' value which is not supported"]
							}
							if (hasTaskSpec && hasDefer) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef 'predecessor' section contains 'taskSpec' and 'defer' properties which are mutually exclusive"]
							}
							def modeValues = ['supports','requires', 'both']
							if (hasMode &&  !modeValues.contains(predecessor.mode)) {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef 'predecessor.mode' has invalid value '$predecessor.mode'. Value options are [supports | requires | both]"]
							}

							if (type == 'milestone') {
								errorList << [error: 1, reason: 'Invalid syntax',
									detail: "$taskRef of type 'milestone' does not support 'predecessor'"]
							}
						}
					} // predecessor validation

					if (task.containsKey('successor')) {
						def successor = task.successor
						if (!(successor instanceof Map)) {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$taskRef 'successor' element is not a valid map definition which requires at least one sub-element defined"]
						}
					}

					// Validate that the team supports a valid team or if using indirect ref with a default, that the default is valid
					if (task.containsKey('team')) {
						msg = ''
						def team = task.team
						if (team) {
							if (team[0]== '#') {
								if (team.contains(',')) {
									def split = team.split(',')*.trim()
									if (split.size() != 2) {
										msg = "Invalid syntax '$team' for 'team' attribute"
									} else {
										team = split[1]
									}
								} else {
									// It is strictly an indirect reference so don't error at all
									team = false
								}
							}
							if (!msg && team != false && teamCodes && !teamCodes.contains(team)) {
								log.debug 'validating team {}', team
								msg = "$taskRef 'team' element references an invalid team name $task.team ${teamCodes.size()}"
							}
						}
						if (msg) {
							errorList << [error: 1, reason: 'Invalid syntax', detail: msg]
						}
					}

					// Validate time constraints if specified and parse the definition so that the logic can use it
					if (task.containsKey('constraintType')) {
						if (!TimeConstraintType.asEnum(task.constraintType)) {
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$taskRef 'constraintType' attribute has invalid value ($task.constraintType). " +
									"Possible values include (${TimeConstraintType.getKeys().join(' | ')})"]
						}
					} else {
						// Set the default to MSO
						task.constraintType = TimeConstraintType.MSO.toString()
					}

					// Validate and parse the constraintTime property if defined
					def valid
					if (task.containsKey('constraintTime')) {
						// This can have:
						//   date (mm/dd/yyyy)
						//   datatime (mm/dd/yyyy hh:mmAP TZ)
						//   indirect (#FieldName - date or datetime)
						//   Event reference + date math (e.g. ES-5d or EC+4h)
						//   TaskSpec + date math (e.g. 2000-6d)
						def ct = task.constraintTime
						def t
						if (ct ==~ /\d{1,2}\/\d{1,2}\/\d{4}/) {
							t = TimeUtil.parseDate(TimeUtil.FORMAT_DATE, ct)
						} else if (ct ==~ /\d{1,2}\/\d{1,2}\/\d{4}.*/) {
							t = TimeUtil.parseDate(TimeUtil.FORMAT_DATE_TIME, ct, TimeUtil.FORMAT_DATE_TIME)
						} else if (ct ==~ /^#.*/) {
							t = ct
						} else if (ct ==~ /^(?i)(\d{1,}|es|ec).*/) {
							// ES or EC reference with optional date math
							if (ct ==~ /(?i)(\d{1,}|es|ec)/) {
								t = ct.toLowerCase()
							} else {
								match = (ct =~ /(?i)(\d{1,}|es|ec)(\+|-)(\d{1,})(?i)(m|h|d|w)/)
								if (match.matches()) {
									// validate if referencing a TaskSpec, that the id exists
									log.debug 'match[0][1] = {}', match[0][1]
									if (match[0][1].isInteger()) {
										if (!taskIds.contains(match[0][1].toInteger())) {
											errorList << [error: 1, reason: 'Invalid syntax',
												detail: "$taskRef 'constraintTime' ($task.constraintTime) references undefined task spec (${match[0][1]})"]
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
							errorList << [error: 1, reason: 'Invalid syntax',
								detail: "$taskRef 'constraintTime' has invalid value ($task.constraintTime)"]
						}

						log.debug '{} task.constraintTimeParsed={} ', taskRef, task.constraintTimeParsed
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
								if (match.matches()) {
									task.durationIndirect = match[0][1]
									task.durationValue = null
									task.durationUom = TimeScale.M
								} else {
									match = d =~ /#(\w{1,}),\s?(\d{1,})(?i)(m|h|d|w)?/
									if (match.matches()) {
										task.durationIndirect = match[0][1]
										task.durationValue = match[0][2]
										task.durationUom = match[0][3] != null ? TimeScale.asEnum(match[0][3].toUpperCase()) : TimeScale.M
									} else {
										errorList << [error: 1, reason: 'Invalid syntax',
											detail: "$taskRef 'duration' has invalid reference ($task.duration)"]
									}
								}

							} else {
								// Handle string time
								match = (d =~ /(\d{1,})(?i)(m|h|d|w)/)
								if (match.matches()) {
									task.durationValue = match[0][1]
									task.durationUom = TimeScale.asEnum(match[0][2].toUpperCase())

									if (task.durationUom == null) {
										errorList << [error: 1, reason: 'Invalid syntax',
											detail: "$taskRef 'duration' has an invalid timescale ${match[0][2].toUpperCase()}"]
									}
								} else {
									errorList << [error: 1, reason: 'Invalid syntax',
										detail: "$taskRef 'duration' has invalid value ($d)"]
								}
							}
						}
						log.debug '{} - duration={}, value={}, uom={}, indirect={}',
								taskRef, d, task.durationValue, task.durationUom, task.durationIndirect
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
						exceptions.append("Unable to parse title ($taskSpec.title) for taskSpec $taskSpec.id<br/>")
						task.comment = "** Error computing title **"
					}
				*/
			} // Tasks section Tests
		}

		return (errorList ? errorList as List : null)
	}

	void validateGroupReferences(taskRef, fieldName, field, existingGroups, errorList) {
		if (field) {
			def values = []

			if (field instanceof String) {
				values.add(field)
			} else if (field instanceof List) {
				values.addAll(field)
			}

			values.each { groupRef ->
				if (!existingGroups.containsKey(groupRef)) {
					errorList << [error: 1, reason: 'Invalid syntax',
						detail: "Task id $taskRef.id '$fieldName' references an invalid group $groupRef"]
				}
			}
		}
	}

	/**
	 * Saves the context to the recipe
	 *
	 * @param recipeId the recipe to add context(eventId/tags) to.
	 * @param contextCommand The command object that holds the recipe context.
	 */
	void saveRecipeContext(Long recipeId, ContextCommand contextCommand) {
		securityService.requirePermission Permission.RecipeEdit

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)

		Map context = [
			eventId : contextCommand.eventId,
			tagMatch: contextCommand.tagMatch,
			tag     : []
		]

		contextCommand.tag.each { Long tagId ->
			Tag tag = get(Tag, tagId, project)

			context.tag << [
				id    : tag.id,
				label : tag.name,
				strike: false,
				css   : tag.color.css
			]
		}

		recipe.context = JsonUtil.convertMapToJsonString(context)

		recipe.save(flush:true)
	}

	void deleteRecipeContext(Long recipeId) {
		securityService.requirePermission Permission.RecipeEdit

		Project project = securityService.getUserCurrentProjectOrException()
		Recipe recipe = Recipe.get(recipeId)
		assertProject(recipe, project)
		recipe.context = null

		recipe.save(flush:true)
	}

	private void assertProject(Recipe recipe, Project project) {
		if (recipe.project.id == project.id || recipe.project == project) {
			return
		}

		log.warn 'SECURITY: User {} illegally attempted to work with a recipe of different project, recipe id: {}, current project: {}',
				securityService.currentUsername, recipe.id, project

		throw new UnauthorizedException('User is trying to delete recipe whose project that is not the current ' +
				recipe.id + ' currentProject ' + project.id)
	}
}

@CompileStatic
class RecipeMapper implements RowMapper {
	def mapRow(ResultSet rs, int rowNum) throws SQLException {[
		recipeId:      rs.getInt('recipeId'),
		name:          rs.getString('name'),
		description:   rs.getString('description'),
		createdBy:     rs.getString('createdBy'),
		versionNumber: rs.getInt('versionNumber') ?: '',
		hasWIP:        rs.getString('hasWIP') == '1' ? 'yes' : '',
		context:       rs.getString('context'),
		lastUpdated:   rs.getTimestamp('lastUpdated')
	]}
}

@CompileStatic
class RecipeVersionMapper implements RowMapper {
	def mapRow(ResultSet rs, int rowNum) throws SQLException {[
		id:               rs.getInt('recipeVersionId'),
		versionNumber:    rs.getInt('versionNumber') ?: '',
		lastUpdated:      rs.getTimestamp('lastUpdated'),
		createdBy:        rs.getString('createdBy'),
		isCurrentVersion: rs.getInt('isCurrentVersion') == 1
	]}
}

@CompileStatic
class RecipeVersionCompleteMapper implements RowMapper {
	def mapRow(ResultSet rs, int rowNum) throws SQLException {[
		id:               rs.getInt('recipeVersionId'),
		versionNumber:    rs.getInt('versionNumber') ?: '',
		lastUpdated:      rs.getTimestamp('lastUpdated'),
		sourceCode:       rs.getString('sourceCode'),
		changelog:        rs.getString('changelog'),
		isCurrentVersion: rs.getInt('isCurrentVersion') == 1
	]}
}
