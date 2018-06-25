import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdssrc.grails.JsonUtil
import grails.validation.ValidationException
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil

import net.transitionmanager.agent.*
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.AwsService

import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import net.transitionmanager.service.ProviderService
import org.apache.commons.lang.RandomStringUtils as RSU
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Title
import test.helper.ProviderTestHelper

// TODO : SL - 06/14 : To be fixed with ticket TM-11121
@Title('Tests for the ApiActionService class')
class ApiActionServiceIntegrationTests extends Specification {

	ApiActionService apiActionService
	ApiActionTestHelper apiActionHelper = new ApiActionTestHelper()
	ProviderService providerService
	ProviderTestHelper providerHelper = new ProviderTestHelper()

	private ApiAction action
	private AssetComment task
	private AssetEntity asset
	private Project project
	ProjectTestHelper projectHelper = new ProjectTestHelper()

	private static final String paramsJson = """
		[ { "param": "taskId",
			"desc": "The id of the task",
			"context": "${ContextType.TASK.name()}",
			"property": "id"
		  },
		  {	"param": "serverRefId",
			"desc": "The unique id used to reference the server in the API",
			"context": "${ContextType.ASSET.name()}",
			"property": "assetName"
		  },
		  {	"param": "groupRefCode",
			"context": "${ContextType.USER_DEF.name()}",
			"value": "xk324-kj1i2-23ks-9sdl"
		  }
		]
	"""

	void setup() {
		project = projectHelper.createProject()
		project.save(flush:true)
		// println "projct ${project.hasErrors()}, ${project.id}"

		action = new ApiAction(
			name:'testAction',
			description: 'This is a bogus action for testing',
			agentClass: AgentClass.HTTP,
			agentMethod: 'sendSnsNotification',
			methodParams: paramsJson,
			asyncQueue: 'test_outbound_queue',
			callbackMethod: 'updateTaskState',
			callbackMode: CallbackMode.MESSAGE,
			httpMethod: ApiActionHttpMethod.GET,
			project: project
		)
		if (action.hasErrors()) println "action has errors: ${GormUtil.allErrorsString(action)}"
		action.save(flush:true)

		asset = new AssetEntity(
			assetName:'fubarsvr01',
			custom1: 'abc',
			project: project
		)

		task = new AssetComment(
			comment:'Test the crap out of this feature',
			commentType: AssetCommentType.TASK,
			assetEntity: asset,
			project: project,
			status: AssetCommentStatus.READY,
			apiAction: action
		)

	}

	def '1. Tests for the agentClassForAction method'() {
		setup: 'requires a Class variable so that'
			Class clazz

		when: 'calling agentClassForAction to get an implemented ApiAction'
			clazz = apiActionService.agentClassForAction(action)
		then: 'the specified class should be returned'
			clazz == net.transitionmanager.agent.HttpAgent

		when: 'the method is called referencing an unimplemented Agent'
			action.agentClass = AgentClass.RACIME
			clazz = apiActionService.agentClassForAction(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '2. Tests for the agentInstanceForAction method'() {
		setup: 'requires an Object to hold the Agent instance so that'
			Object aws

		when: 'calling the method to get an implemented Agent'
			aws = apiActionService.agentInstanceForAction(action)
		then: 'the requested Agent class instance should be returned'
			(aws instanceof AwsAgent)
		and: 'the AwsService was propery injected into the instance'
			aws.awsService != null

		when: 'the method is called requesting an unimplemented Agent'
			action.agentClass = AgentClass.RACIME
			clazz = apiActionService.agentInstanceForAction(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '3. Tests for methodDefinition'() {
		setup:
			def methodDef
		when: 'calling agentClassForAction for a good ApiAction'
			methodDef = apiActionService.methodDefinition(action)
		then: 'a valid DictionaryItem should be returned'
			methodDef
			(methodDef instanceof DictionaryItem)
		and: 'the method name is the one expected'
			methodDef.name == action.agentMethod

		when: 'the ApiAction has an unimplemented Agent'
			action.agentClass = AgentClass.RACIME
			clazz = apiActionService.methodDefinition(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '4. Tests for ApiAction.methodParamsList'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain
		setup:
			List paramsList

		when: 'accessing the methodParamsList property a.k.a. methodParamsList()'
			paramsList = action.methodParamsList
		then: 'a list containing a map representing each of the method params should be returned'
			paramsList
			paramsList.size() == 3
		and: 'the first map in the list should be the taskId as defined paramsJson above'
			paramsList[0].param == 'taskId'
			paramsList[0].context == 'TASK'
			paramsList[0].property == 'id'
			paramsList[0].desc
	}


	def '4.2 Tests for ApiAction.listMethodParams'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain
		setup:
			List paramsList

		when: 'accessing the methodParamsList property a.k.a. methodParamsList()'
			paramsList = action.listMethodParams
		then: 'a list containing a map representing each of the method params should be returned'
			paramsList
			paramsList.size() == 3
		and: 'the first map in the list should be the taskId as defined paramsJson above'
			paramsList[0].param == 'taskId'
			paramsList[0].context == 'TASK'
			paramsList[0].property == 'id'
			paramsList[0].desc
	}

	def '5. Test for AssetComment.actionInvocable'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain

		when: 'the apiActionInvokedAt property is set'
			task.status = AssetCommentStatus.READY
			task.apiActionInvokedAt = new Date()
		then: 'the task action should not be invocable because it was already invoked'
			! task.isActionInvocable()

		when: 'the apiActionCompletedAt property is set'
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = new Date()
		then: 'the task action should not be invocable because it was already invoked and completed'
			! task.isActionInvocable()

		when: 'the task status is not Ready or Started'
			task.status = AssetCommentStatus.PENDING
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = null
		then: 'the task action should not be invocable'
			! task.isActionInvocable()

		when: 'the task status is READY and has not been previously invoked'
			task.status = AssetCommentStatus.READY
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = null
		then: 'the task action should be invocable'
			task.isActionInvocable()

		when: 'the task status is set to STARTED'
			task.status = AssetCommentStatus.STARTED
		then: 'the task action should still be invocable'
			task.isActionInvocable()

		when: 'the task status is set to COMPLETED'
			task.status = AssetCommentStatus.COMPLETED
		then: 'the task action should not be invocable since people may jump directly to COMPLETED'
			! task.isActionInvocable()

		when: 'the task apiAction property is not set'
			task.apiAction = null
		then: 'the task action should not be invocable'
			! task.isActionInvocable()
	}

	def '6. Tests for buildMethodParamsWithContext'() {
		setup:
			Map methodParamsMap
		when: 'calling the method with the AwsAgent and the above mocked task'
			methodParamsMap = apiActionService.buildMethodParamsWithContext(action, task)
		then: 'a map with the method arguments should be returned'
			methodParamsMap
		and: 'the map should contain each of the parameter names as specified in paramsJson above'
			methodParamsMap.keySet().sort() == ['groupRefCode', 'serverRefId', 'taskId']
		and: 'the parameters should have the values based on the context and definition for USER_DEF params'
			methodParamsMap.taskId == null
			'xk324-kj1i2-23ks-9sdl' == methodParamsMap.groupRefCode
			asset.assetName == methodParamsMap.serverRefId
	}

	def '7. Test the invoke method'() {
		setup: 'will inject a mock awsService into the AwsAgent class to stub out calls to AWS so that'
			AwsService awsService = Mock()
			AwsAgent awsAgent = AwsAgent.instance
			awsAgent.awsService = awsService

		when: 'calling the invoke method on the service'
			apiActionService.invoke(action, task)
		then: 'the awsService.sendSnsNotification should be called with proper params'
			1 * awsService.sendSnsMessage(action.asyncQueue, { validateSendSnsMessageMap(it) } )
	}

	@Ignore
	def "8. tests not yet implemented"() {
		//		expect: 'when calling invoke without a valid context that an exception occurs'
		//			false
		//		expect: 'when calling invoke with a defined parameter that references an undefined property that an an exception occurs'
		//			false
		expect:
				true
	}

	def "9. tests deleting an ApiAction and other related methods"() {
		setup: "Create an API Action"
			ApiAction apiAction = apiActionHelper.createApiAction(project)
		when: "Looking up this API Action"
			ApiAction apiAction2 = apiActionService.find(apiAction.id, project)
		then: "It should have been found"
			apiAction2 != null
		when: "Deleting the API Action"
			apiActionService.delete(apiAction2.id, project, true)
		then: "This API Action should no longer exist"
			apiActionService.find(apiAction.id, project) == null
		and: "There's no ApiAction for this project"
			apiActionService.list(project).size() == 0
	}

	def "10. tests deleting an API Action for a different project"() {
		setup: "Create a API Action for different projects"
			Project project2 = projectHelper.createProject()
			ApiAction apiAction1 = apiActionHelper.createApiAction(project)
			ApiAction apiAction2 = apiActionHelper.createApiAction(project2)
		when: "Retrieving all the API Action for both projects"
			List<Map> actions1 = apiActionService.list(project)
			List<Map> actions2 = apiActionService.list(project2)
		then: "Both lists of actions have only one element"
			actions1.size() == 1
			actions2.size() == 1
		and: "Each list contains the expected element"
			actions1.get(0)["id"] == apiAction1.id
			actions2.get(0)["id"] == apiAction2.id

		when: "Executing a valid delete operation"
			apiActionService.delete(apiAction1.id, project, true)
			actions2 = apiActionService.list(project2)
		then: "The corresponding project doesn't have any API Action left"
			apiActionService.list(project).size() == 0
		and: "The other project still has its API Action"
			actions2.size() == 1
			actions2.get(0)["id"] == apiAction2.id

		when: "Trying to delete an API Action that belongs to some other project"
			apiActionService.delete(apiAction2.id, project)
		then: "A EmptyResultException is thrown"
			thrown EmptyResultException

		when: "trying to delete an API Action that doesn't exist"
			apiActionService.delete(0, project)
		then: "A EmptyResultException is thrown"
			thrown EmptyResultException

		when: "trying to delete an API Action without passing a project"
			apiActionService.delete(apiAction2.id, null)
		then: "An EmptyResultException is thrown"
			thrown EmptyResultException



	}

	def "11. Test validateApiActionName with different values"() {
		given: "Two projects with an API Action each."
			Project project1 = projectHelper.createProject()
			Project project2 = projectHelper.createProject()
			ApiAction apiAction1 = apiActionHelper.createApiAction(project1)
			ApiAction apiAction2 = apiActionHelper.createApiAction(project2)
		expect: "True when querying with no id and a valid name (a create operation.)"
			apiActionService.validateApiActionName(project1, apiAction2.name)
		and: "False when entering a duplicate name and no id (a create operation)."
			!apiActionService.validateApiActionName(project1, apiAction1.name)
		and: "True when the name and the id match (an update operation)"
			apiActionService.validateApiActionName(project1, apiAction1.name, apiAction1.id)
		and: "True when changing the name for an existing Action with a valid input (an update operation)"
			apiActionService.validateApiActionName(project1, apiAction2.name, apiAction1.id)
		and: "False when no project is given."
			!apiActionService.validateApiActionName(null, apiAction1.name)
		and: "False when no name is given."
			!apiActionService.validateApiActionName(project1, null)

	}

	def "12. Test Create ApiActions with valid and invalid data"() {
		setup: "some useful values"
			Provider provider1 = providerHelper.createProvider(project)
			String apiName = RSU.randomAlphabetic(10)
			ApiActionCommand cmd = new ApiActionCommand()
			cmd.with {
				name = apiName
				description = "some description"
				provider = provider1
				agentClass = "AWS"
				agentMethod = "X"
				callbackMode = "NA"
				callbackMethod = "Y"
				asyncQueue = "AQ"
				isPolling = 0
				producesData = 0
				pollingInterval = 0
				timeout = 0
				reactionScripts = "{\"SUCCESS\": \"success\",\"STATUS\": \"status\",\"ERROR\": \"error\"}"
				pollingLapsedAfter = 0
				pollingStalledAfter = 0
				useWithTask = 0
				useWithAsset = 0
				methodParams = """
						[ {
							"param":"assetId",
							"desc": "The unique id to reference the asset",
							"type":"string",
							"context": "${ContextType.ASSET.toString()}",
							"property": "id",
							"value": "user def value"
						}]
				"""
			}

		when: "Creating an API Action"
			ApiAction apiAction = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "The operation succeeded"
			apiAction != null
		when: "Trying to create a second API Action with the same name"
			ApiAction apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "A ValidationException is thrown"
			thrown ValidationException
		when: "Trying to create an ApiAction with missing params"
			cmd.agentMethod = null
			cmd.name = RSU.randomAlphabetic(10)
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "The missing field causes the validation step to fail with a ValidationException."
			thrown ValidationException
		when: "Trying to assign a provider of another project"
			Project project2 = projectHelper.createProject()
			Provider provider2 = providerHelper.createProvider(project2)
			cmd.provider = provider2
			cmd.agentClass = "AWS"
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "The invalid reference makes the validation fail with a ValidationException."
			thrown ValidationException

		when: "methodParams contains an invalid JSON "
			cmd.methodParams = """
				[ {
					'param':"assetId"
				} ]
			"""
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "The invalid JSON makes the validation fail with a ValidationException."
			thrown ValidationException
	}

	def "13. Test Create ApiActions with valid and invalid data"() {
		setup: "some useful values"
		Provider provider1 = providerHelper.createProvider(project)
		String apiName = RSU.randomAlphabetic(10)
		ApiActionCommand cmd = new ApiActionCommand()
		cmd.with {
			name = apiName
			description = "some description"
			provider = provider1
			agentClass = "AWS"
			agentMethod = "X"
			callbackMode = "NA"
			callbackMethod = "Y"
			asyncQueue = "AQ"
			producesData = 0
			pollingInterval = 0
			timeout = 0
			reactionScripts = "{\"SUCCESS\": \"success\",\"STATUS\": \"status\",\"ERROR\": \"error\"}"
			isPolling = 0
			pollingLapsedAfter = 0
			pollingStalledAfter = 0
			useWithTask = 0
			useWithAsset = 0
		}
		when: "Creating an API Action"
			ApiAction apiAction = apiActionService.saveOrUpdateApiAction(cmd, null, project)
		then: "The operation succeeded"
			apiAction != null
		when: "Trying to update the API Action with a different name"
			String newName =  RSU.randomAlphabetic(10)
			cmd.name = newName
			ApiAction apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, apiAction.id, project)
		then: "the name was updated"
			apiAction2.name == newName
		and: "the id didn't change (the action was updated)"
			apiAction.id == apiAction2.id
		when: "we try to update the name to that of an existing Action"
			cmd.name = RSU.randomAlphabetic(10)
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd,null, project)
			apiAction = apiActionService.saveOrUpdateApiAction(cmd, apiAction.id, project)
		then: "The name fails the validations throwing a ValidationException"
			thrown ValidationException
		when: "Trying to update with a provider of another project"
			Project project2 = projectHelper.createProject()
			Provider provider2 = providerHelper.createProvider(project2)
			cmd.name = RSU.randomAlphabetic(10)
			cmd.provider = provider2
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, apiAction2.id, project)
		then: "A ValidationException is thrown"
			thrown ValidationException
	}

	def "14. Test parseEnum with different valid and invalid inputs"(){
		when: "Trying to parse a valid value"
			AgentClass agentClass = apiActionService.parseEnum(AgentClass, "someField", "AWS")
		then: "The correct value was parsed"
			agentClass == AgentClass.AWS
		when: "Trying with an invalid value"
			agentClass = apiActionService.parseEnum(AgentClass, "someField", RSU.randomAlphabetic(10), true, true)
		then: "An InvalidParamException is thrown"
			thrown InvalidParamException
		when: "Trying with an invalid value"
			agentClass = apiActionService.parseEnum(AgentClass, "someField", null, true, true)
		then: "An InvalidParamException is thrown"
			thrown InvalidParamException
		when: "Trying with a class that is not an Enum"
			agentClass = apiActionService.parseEnum(Integer, "someField", "AWS", true, true)
		then: "An InvalidParamException is thrown"
			thrown InvalidParamException
		when: "Trying with a null class.	"
			agentClass = apiActionService.parseEnum(null, "someField", "AWS")
		then: "An NullPointerException is thrown"
			thrown NullPointerException
	}

	def "15. Test reactionScripts for The API Action in different scenarios" () {
		setup: "Create a valid ApiActionCommand"
			Provider provider1 = providerHelper.createProvider(project)
			ApiAction apiAction = new ApiAction(project: project)
			apiAction.with {
				name = RSU.randomAlphabetic(10)
				description = "some description"
				provider= provider1
				agentClass = "AWS"
				agentMethod = "X"
				callbackMode = "NA"
				callbackMethod = "Y"
				asyncQueue = "AQ"
				isPolling = 0
				producesData = 0
				pollingInterval = 0
				timeout = 0
				reactionScripts = "{\"SUCCESS\": \"success\",\"STATUS\": \"status\",\"ERROR\": \"error\"}"
				pollingLapsedAfter = 0
				pollingStalledAfter = 0
				useWithTask = 0
				useWithAsset = 0
			}
		when: "Validating the command object"
			apiAction.validate()
		then: "No errors detected"
			!apiAction.hasErrors()
		and: "The flag for the validity of the reactionScripts JSON is set"
			apiAction.reactionScriptsValid == 1
		when: "Using a JSON with no SUCCESS"
			apiAction.reactionScripts = "{\"STATUS\": \"status\",\"ERROR\": \"error\"}"
			apiAction.validate()
		then: "The validation fails"
			apiAction.hasErrors()
		and: "The cause of the failure is the reactionScripts field"
			apiAction.errors.hasFieldErrors("reactionScripts")
		and: "It's the only field failing"
			apiAction.errors.allErrors.size() == 1
		and: "The cause is the missing attribute in the JSON"
			apiAction.errors.allErrors[0].code == Message.ApiActionMissingStatusOrSuccessInReactionJson
		when: "Using a JSON with no DEFAULT and no ERROR"
			apiAction.reactionScripts = "{\"SUCCESS\": \"success\",\"STATUS\": \"status\"}"
			apiAction.validate()
		then: "The validation of the command object fails of the missing attributes."
			apiAction.errors.allErrors[0].code == Message.ApiActionMissingDefaultAndErrorInReactionJson
		when: "Using a JSON with no STATUS"
			apiAction.reactionScripts = "{\"SUCCESS\": \"success\" ,\"DEFAULT\": \"default\"}"
			apiAction.validate()
		then: "The validation fails because of the missing attributes."
			apiAction.errors.allErrors[0].code == Message.ApiActionMissingStatusOrSuccessInReactionJson
		when: "Using an invalid JSON"
			apiAction.reactionScripts = "BOGUS"
			apiAction.validate()
		then: "The validation of the command object fails of the missing attributes."
			apiAction.errors.allErrors[0].code == Message.InvalidFieldForDomain
		when: "Using a JSON with some bogus key"
			apiAction.reactionScripts = "{\"SUCCESS\": \"success\",\"STATUS\": \"status\",\"ERROR\": \"error\", \"BOGUS\": 0}"
			apiAction.validate()
		then: "The Action passes the validation"
			!apiAction.hasErrors()
		and: "The flag for the validity of the json is not set."
			apiAction.reactionScriptsValid == 0
		and: "The bogus entry was removed"
			JSONObject jsonObject = JsonUtil.parseJson(apiAction.reactionScripts)
			!jsonObject.containsKey("BOGUS")

	}
	/*
	 * This closure is used to validate the 'invoke' test case such that it validates that the argument
	 * sent to the awsService.sendSnsNotification method are properly formed
	 */
	private boolean validateSendSnsMessageMap(param) {
		// (param instancesof Map) &&
		param.serverRefId == asset.assetName &&
		param.callbackMethod == action.callbackMethod &&
		param.groupRefCode == 'xk324-kj1i2-23ks-9sdl'
	}
}
