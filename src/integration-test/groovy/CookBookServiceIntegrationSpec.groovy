import com.tdsops.tm.enums.domain.Color
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.cookbook.ContextCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.RecipeVersion
import net.transitionmanager.domain.Tag
import net.transitionmanager.service.CookbookService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class CookBookServiceIntegrationSpec extends Specification{
	@Shared
	CookbookService cookbookService

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3


	void setup() {
		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: project).save(flush: true, failOnError: true)


		cookbookService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() },
			requirePermission      : { String permission, boolean report = false, String violationMessage = null -> true },
			isLoggedIn             : { -> true },
			isSystemUser           : { -> true },
			getCurrentUsername     : { -> 'username' }
		] as SecurityService

		cookbookService.personService = [
			hasAccessToProject: { Project project -> true },
		] as PersonService
	}

	void 'Test recipe full context'() {
		given:
			Recipe existingRecipe = new Recipe(name: 'test', context: '{}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			ContextCommand contextCommand = new ContextCommand(eventId: 1, tag: [tag1.id, tag2.id])
			cookbookService.saveRecipeContext(existingRecipe.id, contextCommand)
			Recipe recipe = Recipe.get(existingRecipe.id)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == """{"eventId":1,"tagMatch":"ANY","tag":[{"id":${
				tag1.id
			},"label":"grouping assets","strike":false,"css":"tag-green"},{"id":${
				tag2.id
			},"label":"some assets","strike":false,"css":"tag-blue"}]}"""
	}

	void 'Test recipe ANY context'() {
		given:
			Recipe existingRecipe = new Recipe(name: 'test', context: '{}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			ContextCommand contextCommand = new ContextCommand(eventId: 1, tag: [tag1.id, tag2.id], tagMatch: "ANY")
			cookbookService.saveRecipeContext(existingRecipe.id, contextCommand)
			Recipe recipe = Recipe.get(existingRecipe.id)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == """{"eventId":1,"tagMatch":"ANY","tag":[{"id":${
				tag1.id
			},"label":"grouping assets","strike":false,"css":"tag-green"},{"id":${
				tag2.id
			},"label":"some assets","strike":false,"css":"tag-blue"}]}"""
	}

	void 'Test recipe event context'() {
		given:
			Recipe existingRecipe = new Recipe(name: 'test', context: '{}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			ContextCommand contextCommand = new ContextCommand(eventId: 1)
			cookbookService.saveRecipeContext(existingRecipe.id, contextCommand)
			Recipe recipe = Recipe.get(existingRecipe.id)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == '{"eventId":1,"tagMatch":"ANY","tag":[]}'
	}

	void 'Test recipe tag context'() {
		given:
			Recipe existingRecipe = new Recipe(name: 'test', context: '{}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			ContextCommand contextCommand = new ContextCommand(tag: [tag1.id, tag2.id])
			cookbookService.saveRecipeContext(existingRecipe.id, contextCommand)
			Recipe recipe = Recipe.get(existingRecipe.id)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == """{"eventId":null,"tagMatch":"ANY","tag":[{"id":${
				tag1.id
			},"label":"grouping assets","strike":false,"css":"tag-green"},{"id":${
				tag2.id
			},"label":"some assets","strike":false,"css":"tag-blue"}]}"""
	}


	void 'Test delete recipe context'() {
		given:
			Recipe existingRecipe = new Recipe(name: 'test', context: '{"eventId":null,"tagMatch":"ANY","tag":[]}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			cookbookService.deleteRecipeContext(existingRecipe.id)
			Recipe recipe = Recipe.get(existingRecipe.id)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == null
	}

	void 'Test get recipe context'() {
		given:
			PersonTestHelper personHelper = new PersonTestHelper()
			Person adminPerson = personHelper.createStaff(project.owner)
			//Person adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
			Recipe existingRecipe = new Recipe(name: 'test', context: '{"eventId":null,"tagMatch":"ANY","tag":[]}', project: project, projectId: project.id)
			existingRecipe.save(failOnError: true, flush: true)
			new RecipeVersion(recipe: existingRecipe, createdBy: adminPerson).save(failOnError: true, flush: true)
		when: 'Getting a list of tagAssets by asset'
			Map recipe = cookbookService.getRecipe(existingRecipe.id, 0)

		then: 'a list of tagAssets are returned for the asset'
			recipe.context == ["eventId":null,"tagMatch":"ANY","tag":[]]
	}


}