import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.enums.domain.SecurityRole
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.ModelService
import net.transitionmanager.service.SecurityService

class ModelServiceTests extends IntegrationSpec {
	
	ModelService modelService
	private Project project
	private Person adminPerson
	UserLogin adminUser
	SecurityService securityService
	private PersonTestHelper personHelper = new PersonTestHelper()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()

	def setup() {
		projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()

		adminPerson = personHelper.createStaff(project.owner)
		assert adminPerson

		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])

		assert adminUser
		assert adminUser.username
		securityService.assumeUserIdentity(adminUser.username, false)
		println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
		assert securityService.isLoggedIn()
	}
	
	def '1. Test the isValidAlias method in many different situations' () {
		setup:
			def manRound = new Manufacturer(name: 'round').save()
			def manFood = new Manufacturer(name: 'food').save()
			
			def modRoundPizza = new Model(modelName:'pizza', manufacturer:manRound).save()
			def modRoundFrisbee = new Model(modelName:'frisbee', manufacturer:manRound).save()
			
			def modFoodPizza = new Model(modelName:'pizza', manufacturer:manFood).save()
			def modFoodCookie = new Model(modelName:'cookie', manufacturer:manFood).save()
			
			def a1ias1 = new ModelAlias(name:'pie', model:modRoundPizza, manufacturer:manRound).save()
			def a1ias2 = new ModelAlias(name:'cooldisk', model:modRoundFrisbee, manufacturer:manRound).save()
		
		
		// assertions without the local duplicate flag
		assert modelService.isValidAlias('pizzo', modRoundPizza) : 'VALID: model alias is completely unique'
		assert modelService.isValidAlias('cookie', modRoundPizza) : 'VALID: model alias matches other model\'s name from other manufacturer'
		assert modelService.isValidAlias('pizza_pie', modRoundPizza) : 'VALID: model alias matches other model\'s alias from other manufacturer'
		assert ! modelService.isValidAlias('frisbee', modRoundPizza) : 'INVALID: model alias matches other model\'s name from same manufacturer'
		assert ! modelService.isValidAlias('cooldisk', modRoundPizza) : 'INVALID: model alias matches other model\'s alias from same manufacturer'
		assert ! modelService.isValidAlias('frisbee', modRoundPizza) : 'INVALID: model alias matches the parent model\'s name'
		assert ! modelService.isValidAlias('pie', modRoundPizza) : 'INVALID: model alias matches the parent model\'s alias'
		
		// assertions with the local duplicate flag
		assert modelService.isValidAlias('pizzo', modRoundPizza, true) : 'VALID: model alias is completely unique (local duplicates allowed)'
		assert modelService.isValidAlias('cookie', modRoundPizza, true) : 'VALID: model alias matches other model\'s name from other manufacturer (local duplicates allowed)'
		assert modelService.isValidAlias('pizza_pie', modRoundPizza, true) : 'VALID: model alias matches other model\'s alias from other manufacturer (local duplicates allowed)'
		assert ! modelService.isValidAlias('frisbee', modRoundPizza, true) : 'INVALID: model alias matches other model\'s name from same manufacturer (local duplicates allowed)'
		assert ! modelService.isValidAlias('cooldisk', modRoundPizza, true) : 'INVALID: model alias matches other model\'s alias from same manufacturer (local duplicates allowed)'
		assert ! modelService.isValidAlias('frisbee', modRoundPizza, true) : 'INVALID: model alias matches the parent model\'s name (local duplicates allowed)'
		assert modelService.isValidAlias('pie', modRoundPizza, true) : 'VALID: model alias matches the parent model\'s alias (local duplicates allowed)'
		
		// test the manufacturer parameter
		assert modelService.isValidAlias('pizzo', modRoundPizza, false, manFood) : 'VALID: model alias is completely unique, using different manufacturer'
		assert modelService.isValidAlias('pie', modRoundPizza, false, manFood) : 'VALID: model alias matches own alias, but using different manufacturer'
		assert modelService.isValidAlias('pie', modRoundPizza, true, manFood) : 'VALID: same as above but with local duplicates allowed (should still be valid)'
		assert modelService.isValidAlias('frisbee', modRoundPizza, false, manFood) : 'VALID: model alias matches other model\'s name from same manufacturer, using different manufacturer'
		assert modelService.isValidAlias('pie', modRoundFrisbee, false, manFood) : 'VALID: model alias matches other model\'s alias from same manufacturer, using different manufacturer'
		assert ! modelService.isValidAlias('cookie', modRoundPizza, false, manFood) : 'INVALID: model alias matches other model\'s name from difffernt manufacturer, using that manufacturer'
		assert ! modelService.isValidAlias('cooldisk', modFoodCookie, false, manRound) : 'INVALID: model alias matches other model\'s alias from difffernt manufacturer, using that manufacturer'
		
		// test the model name parameter
		assert modelService.isValidAlias('frisbee', modRoundFrisbee, false, null, 'ultimate frisbee') : 'VALID: model alias matches own name, using different name'
	}

	def '2. Test validateModel() method'() {
		setup: 'create a Model and save'
			def testManufacturer = new Manufacturer(name: 'Test Manufacturer').save(flush: true)
			Model model = new Model(modelName: 'Test Model', manufacturer: testManufacturer)
			assert model.valid == false
			model.save(flush: true)
		when: 'the validate() method is called on the service'
			modelService.validateModel(model.id)
		then: 'the model is now validated'
			Model vModel = Model.get(model.id)
			vModel.valid
		and: 'it was validated by the logged person'
			securityService.loadCurrentPerson() == vModel.validatedBy
		when: 'the method is invoked with an invalid model id'
			modelService.validateModel(null)
		then: 'an exception is thrown'
			thrown ServiceException
	}
}
