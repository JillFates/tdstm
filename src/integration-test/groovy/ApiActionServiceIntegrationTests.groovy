import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.validation.ValidationException
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.connector.AwsConnector
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.connector.ContextType
import net.transitionmanager.connector.DictionaryItem
import net.transitionmanager.connector.GenericHttpConnector
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.AwsService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.ProviderService
import org.apache.commons.lang3.RandomStringUtils as RSU
import org.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import test.helper.ApiActionTestHelper
import test.helper.ApiCatalogTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
@Title('Tests for the ApiActionService class')
class ApiActionServiceIntegrationTests extends Specification{

	ApiActionService apiActionService

	@Shared
	ApiActionTestHelper apiActionHelper
	ProviderService providerService

	@Shared
	Provider provider

	@Shared
	ApiCatalog apiCatalog

	@Shared
	ProviderTestHelper providerHelper

	@Shared
	ApiCatalogTestHelper apiCatalogHelper


	@Shared
	ApiAction action
	@Shared
	AssetComment task
	@Shared
	AssetEntity asset
	@Shared
	Project project

	@Shared
	ProjectTestHelper projectHelper

	@Shared
	boolean initialized = false

	private static final String paramsJson = """
		[ { "param": "taskId",
			"desc": "The id of the task",
			"context": "${ContextType.TASK.name()}",
			"paramName": "taskId",
			"fieldName": "id"
		  },
		  {	"param": "serverRefId",
			"desc": "The unique id used to reference the server in the API",
			"context": "${ContextType.ASSET.name()}",
			"paramName": "serverRefId",
			"fieldName": "assetName"
		  },
		  {	"param": "groupRefCode",
			"context": "${ContextType.USER_DEF.name()}",
			"value": "xk324-kj1i2-23ks-9sdl",
			"paramName": "groupRefCode"
		  }
		]
	"""

	void setup() {
		if(!initialized) {
			apiActionHelper = new ApiActionTestHelper()
			apiCatalogHelper = new ApiCatalogTestHelper()
			providerHelper = new ProviderTestHelper()
			projectHelper = new ProjectTestHelper()
			project = projectHelper.createProject()
			provider = providerHelper.createProvider(project)
			apiCatalog = apiCatalogHelper.createApiCatalog(project, provider)
			apiCatalogHelper = new ApiCatalogTestHelper()
			projectHelper = new ProjectTestHelper()

			action = new ApiAction(
				name: 'testAction',
				description: 'This is a bogus action for testing',
				apiCatalog: apiCatalog,
				connectorMethod: 'callEndpoint',
				methodParams: paramsJson,
				asyncQueue: 'test_outbound_queue',
				callbackMethod: 'updateTaskState',
				callbackMode: CallbackMode.MESSAGE,
				httpMethod: ApiActionHttpMethod.GET,
				isRemote: false,
				reactionScripts: '{"STATUS": "// do nothing", "SUCCESS": "// do nothing", "DEFAULT": "// do nothing"}',
				project: project,
				provider: provider
			)
			if (action.hasErrors()) {
				println "action has errors: ${GormUtil.allErrorsString(action)}"
			}
			action.save(failsOnError: true)

			asset = new AssetEntity(
				assetName: 'fubarsvr01',
				custom1: 'abc',
				project: project
			)

			task = new AssetComment(
				comment: 'Test the crap out of this feature',
				commentType: AssetCommentType.TASK,
				assetEntity: asset,
				project: project,
				status: AssetCommentStatus.READY,
				apiAction: action
			)

			initialized = true
		}

	}

	def '1. Tests for the connectorClassForAction method'() {
		setup: 'requires a Class variable so that'
			Class clazz

		when: 'calling connectorClassForAction to get an implemented ApiAction'
			clazz = apiActionService.connectorInstanceForAction(action).class
		then: 'the specified class should be returned'
			clazz == GenericHttpConnector

		when: 'the method is called referencing an unimplemented Connector'
			// SL 07/2018 : Test kept just to preserve structure
			this.apiActionService = [connectorInstanceForAction: { throw new MissingPropertyException('Test') }] as ApiActionService

			clazz = apiActionService.connectorInstanceForAction(action).class
		then: 'an exception should be thrown'
			thrown MissingPropertyException
	}

	@Ignore
	// TODO : SL 6/2018 : Test being ignored until AWS Connector will be reestablished
	def '2. Tests for the connectorInstanceForAction method'() {
		setup: 'requires an Object to hold the Connector instance so that'
			Object aws

		when: 'calling the method to get an implemented Connector'
			aws = apiActionService.connectorInstanceForAction(action)
		then: 'the requested Connector class instance should be returned'
			(aws instanceof AwsConnector)
		and: 'the AwsService was propery injected into the instance'
			aws.awsService != null

		when: 'the method is called requesting an unimplemented Connector'
			// action.connectorClass = ConnectorClass.RACIME
			clazz = apiActionService.connectorInstanceForAction(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '3. Tests for methodDefinition'() {
		setup:
			def methodDef
		when: 'calling connectorClassForAction for a good ApiAction'
			methodDef = apiActionService.methodDefinition(action)
		then: 'a valid DictionaryItem should be returned'
			methodDef
			(methodDef instanceof DictionaryItem)
		and: 'the method name is the one expected'
			methodDef.apiMethod == action.connectorMethod

		when: 'the ApiAction has an unimplemented Connector'
			action.connectorMethod = 'DOES-NOT-EXISTS'
			clazz = apiActionService.methodDefinition(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '4. Tests for buildMethodParamsWithContext'() {
		setup:
			Map methodParamsMap
		when: 'calling the method with the AwsConnector and the above mocked task'
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

	@Ignore
	// TODO : SL 6/2018 : Test being ignored until AWS Connector will be reestablished
	def '5. Test the invoke method'() {
		setup: 'will inject a mock awsService into the AwsConnector class to stub out calls to AWS so that'
			AwsService awsService = Mock()
			AwsConnector awsConnector = AwsConnector.instance
			awsConnector.awsService = awsService

		when: 'calling the invoke method on the service'
			apiActionService.invoke(action, task)
		then: 'the awsService.sendSnsNotification should be called with proper params'
			1 * awsService.sendSnsMessage(action.asyncQueue, { validateSendSnsMessageMap(it) } )
	}

	def "6. tests deleting an ApiAction and other related methods"() {
		setup: "Create an API Action"
			ApiAction apiAction = apiActionHelper.createApiAction(project, provider, apiCatalog)
		when: "Looking up this API Action"
			ApiAction apiAction2 = apiActionService.find(apiAction.id, project)
		then: "It should have been found"
			apiAction2 != null
		when: "Deleting the API Action"
			apiActionService.delete(apiAction2.id, project, true)
		then: "This API Action should no longer exist"
			apiActionService.find(apiAction.id, project) == null
		and: "There's no ApiAction for this project"
			apiActionService.list(project).size() == 1
	}

	def "7. tests deleting an API Action for a different project"() {
		setup: "Create a API Action for different projects"
			Project project2 = projectHelper.createProject()
			ApiAction apiAction1 = apiActionHelper.createApiAction(project, provider, apiCatalog)
			ApiAction apiAction2 = apiActionHelper.createApiAction(project2, null, apiCatalog)
		when: "Retrieving all the API Action for both projects"
			List<Map> actions1 = apiActionService.list(project)
			List<Map> actions2 = apiActionService.list(project2)
		then: "Both lists of actions have only one element"
			actions1.size() == 2
			actions2.size() == 1
		and: "Each list contains the expected element"
			actions1.get(0)["id"] == apiAction1.id || actions1.get(1)["id"] == apiAction1.id || actions1.get(2)["id"] == apiAction1.id
			actions2.get(0)["id"] == apiAction2.id || actions2.get(1)["id"] == apiAction2.id || actions2.get(2)["id"] == apiAction2.id

		when: "Executing a valid delete operation"
			apiActionService.delete(apiAction1.id, project, true)
			actions2 = apiActionService.list(project2)
		then: "The corresponding project doesn't have any API Action left"
			apiActionService.list(project).size() == 1
		and: "The other project still has its API Action"
			actions2.size() == 1
			actions2.get(0)["id"] == apiAction2.id || actions2.get(1)["id"] == apiAction2.id || actions2.get(2)["id"] == apiAction2.id

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

	def "8. Test validateApiActionName with different values"() {
		given: "Two projects with an API Action each."
			Project project1 = projectHelper.createProject()
			Project project2 = projectHelper.createProject()
			ApiAction apiAction1 = apiActionHelper.createApiAction(project1, null, apiCatalog)
			ApiAction apiAction2 = apiActionHelper.createApiAction(project2, null, apiCatalog)
		expect: "True when querying with no id and a valid name (a create operation.)"
			apiActionService.validateApiActionName(project1, apiAction2.name)
		and: "False when entering a duplicate name and no id (a create operation)."
			!apiActionService.validateApiActionName(project1, apiAction1.name)
		and: "True when the name and the id match (an update operation)"
			apiActionService.validateApiActionName(project1, apiAction1.name, apiAction1.id)
		and: "True when changing the name for an existing Action with a valid input (an update operation)"
			apiActionService.validateApiActionName(project1, apiAction2.name, apiAction1.id)
		and: "User current project when no project is given."
			apiActionService.validateApiActionName(null, apiAction1.name)
		and: "False when no name is given."
			! apiActionService.validateApiActionName(project1, null)

	}

	def "9. Test Create ApiActions with valid and invalid data"() {
		setup: "some useful values"
			Provider provider1 = providerHelper.createProvider(project)
			ApiCatalog apiCatalog1 = apiCatalogHelper.createApiCatalog(project, provider1)
			String apiName = RSU.randomAlphabetic(10)
			ApiActionCommand cmd = new ApiActionCommand()
			cmd.with {
				name = apiName
				description = "some description"
				provider = provider1
				apiCatalog = apiCatalog1
				connectorMethod = "X"
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
				httpMethod = ApiActionHttpMethod.GET
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
			ApiAction apiAction = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "The operation succeeded"
			apiAction != null
		when: "Trying to create a second API Action with the same name"
			ApiAction apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "A ValidationException is thrown"
			thrown InvalidParamException
		when: "Trying to create an ApiAction with missing params"
			cmd.connectorMethod = null
			cmd.name = RSU.randomAlphabetic(10)
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "The missing field causes the validation step to fail with a ValidationException."
			thrown ValidationException
		when: "Trying to assign a provider of another project"
			Project project2 = projectHelper.createProject()
			Provider provider2 = providerHelper.createProvider(project2)
			cmd.provider = provider2
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "The invalid reference makes the validation fail with a ValidationException."
			thrown ValidationException

		when: "methodParams contains an invalid JSON "
			cmd.methodParams = """
				[ {
					'param':"assetId",
				} ]
			"""
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "The invalid JSON makes the validation fail with a ValidationException."
			thrown InvalidParamException
	}

	def "10. Test Create ApiActions with valid and invalid data"() {
		setup: "some useful values"
			Provider provider1 = providerHelper.createProvider(project)
			ApiCatalog apiCatalog1 = apiCatalogHelper.createApiCatalog(project, provider1)
			String apiName = RSU.randomAlphabetic(10)
			ApiActionCommand cmd = new ApiActionCommand()
			cmd.with {
				name = apiName
				description = "some description"
				provider = provider1
				apiCatalog = apiCatalog1
				connectorMethod = "X"
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
				httpMethod = ApiActionHttpMethod.GET
			}
		when: "Creating an API Action"
			ApiAction apiAction = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
		then: "The operation succeeded"
			apiAction != null
		when: "Trying to update the API Action with a different name"
			String newName =  RSU.randomAlphabetic(10)
			cmd.name = newName
			ApiAction apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, apiAction.id, 0, project)
		then: "the name was updated"
			apiAction2.name == newName
		and: "the id didn't change (the action was updated)"
			apiAction.id == apiAction2.id
		when: "we try to update the name to that of an existing Action"
			cmd.name = RSU.randomAlphabetic(10)
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, null, 0, project)
			apiAction = apiActionService.saveOrUpdateApiAction(cmd, apiAction.id, 0, project)
		then: "The name fails the validations throwing a ValidationException"
			thrown InvalidParamException
		when: "Trying to update with a provider of another project"
			Project project2 = projectHelper.createProject()
			Provider provider2 = providerHelper.createProvider(project2)
			cmd.name = RSU.randomAlphabetic(10)
			cmd.provider = provider2
			apiAction2 = apiActionService.saveOrUpdateApiAction(cmd, apiAction2.id, 0, project)
		then: "A ValidationException is thrown"
			thrown ValidationException
	}

	def "11. Test reactionScripts for The API Action in different scenarios" () {
		setup: "Create a valid ApiActionCommand"
			Provider provider1 = providerHelper.createProvider(project)
			ApiCatalog apiCatalog1 = apiCatalogHelper.createApiCatalog(project, provider1)
			ApiAction apiAction = new ApiAction(project: project)
			apiAction.with {
				name = RSU.randomAlphabetic(10)
				description = "some description"
				provider = provider1
				apiCatalog = apiCatalog1
				connectorMethod = "X"
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
