import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil

import net.transitionmanager.agent.*
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.AwsService

import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetCommentStatus

import spock.lang.Specification
//import spock.lang.Stepwise
//import spock.util.mop.ConfineMetaClassChanges

class ApiActionServiceIntegrationTests extends Specification {

	// IOC
	ApiActionService apiActionService

	private ApiAction action
	private AssetComment task
	private AssetEntity asset
	private Project project

	private static final String paramsJson = '''
		[ { "param": "taskId",
			"desc": "The id of the task",
			"context": "TASK",
			"property": "id"
		  },
		  {	"param": "serverRefId",
			"desc": "The unique id used to reference the server in the API",
			"context": "SERVER",
			"property": "assetName"
		  },
		  {	"param": "groupRefCode",
			"context": "USER_DEF",
			"value": "xk324-kj1i2-23ks-9sdl"
		  }
		]
	'''

	void setup() {
		def projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()
		project.save(flush:true)
		// println "projct ${project.hasErrors()}, ${project.id}"

		action = new ApiAction(
			name:'testAction',
			description: 'This is a bogus action for testing',
			agentClass: AgentClass.AWS,
			agentMethod: 'sendSnsNotification',
			methodParams: paramsJson,
			asyncQueue: 'test_outbound_queue',
			callbackMethod: 'updateTaskState',
			callbackMode: CallbackMode.MESSAGE,
			project: project
		)
		if (action.hasErrors()) println "action has errors: ${GormUtil.allErrorsString(action)}"
		action.save(flush:true)

		asset = new AssetEntity(
			assetName:'foo',
			custom1: 'abc'
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

	def '1. Tests for agentClassForAction'() {
		if (project.hasErrors()) {
			println 'Project errors: ' + GormUtil.allErrorsString(project)
		}
		if (action.hasErrors()) {
			println 'action errors: ' + GormUtil.allErrorsString(action)
		}
		if (asset.hasErrors()) {
			println 'asset errors: ' + GormUtil.allErrorsString(asset)
		}
		if (task.hasErrors()) {
			println 'task errors: ' + GormUtil.allErrorsString(task)
		}

		setup:
			Class clazz
		when: 'calling agentClassForAction for a good ApiAction'
			clazz = apiActionService.agentClassForAction(action)
		then:
			clazz == net.transitionmanager.agent.AwsAgent

		when: 'the ApiAction has an unimplemented Agent'
			action.agentClass = AgentClass.RACIME
			clazz = apiActionService.agentClassForAction(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '2. Tests for agentInstanceForAction'() {
		setup:
			Object aws
		when: 'calling agentInstanceForAction for a good ApiAction'
			aws = apiActionService.agentInstanceForAction(action)
		then:
			(aws instanceof AwsAgent)
		and: 'the AwsService was propery injected into the instance'
			aws.awsService != null

		when: 'the ApiAction has an unimplemented Agent'
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
			methodDef.name == action.agentMethod
			// methodDef.keySet().size() > 0
			// methodDef.containsKey('sendSnsMessage')

		when: 'the ApiAction has an unimplemented Agent'
			action.agentClass = AgentClass.RACIME
			clazz = apiActionService.methodDefinition(action)
		then: 'an exception should be thrown'
			thrown InvalidRequestException
	}

	def '4. Tests for ApiAction.getMethodParamsMap'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain
		setup:
			List paramsList
		when: 'accessing the methodParamsMap property a.k.a. getMethodParamsMap()'
			paramsList = action.methodParamsList
		then: 'a map of method params should be returned'
			paramsList
			paramsList.size() == 3
		and: 'the first parameter should be the taskId'
			paramsList[0].param == 'taskId'
			paramsList[0].context == 'TASK'
			paramsList[0].property == 'id'
			paramsList[0].desc
	}

	def '5. Test for AssetComment.actionInvocable'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain

		when: 'apiActionInvokedAt is set'
			task.status = AssetCommentStatus.READY
			task.apiActionInvokedAt = new Date()
		then: 'the task should not be invocable'
			! task.isActionInvocable()

		when: 'apiActionCompletedAt is set'
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = new Date()
		then: 'the task should not be invocable'
			! task.isActionInvocable()

		when: 'status is not Ready or Started'
			task.status = AssetCommentStatus.PENDING
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = null
		then: 'the task should not be invocable'
			! task.isActionInvocable()

		when: 'the task is READY and has not been previously invoked'
			task.status = AssetCommentStatus.READY
			task.apiActionInvokedAt = null
			task.apiActionCompletedAt = null
		then: 'the task should be invocable'
			task.isActionInvocable()

		when: 'status is set to STARTED'
			task.status = AssetCommentStatus.STARTED
		then: 'the task should be invocable'
			task.isActionInvocable()

		when: 'apiAction is not set'
			task.apiAction = null
		then: 'the task should not be invocable'
			! task.isActionInvocable()

	}

	def '6. Tests for buildMethodParamsForTask'() {
		setup:
			Map methodArgsMap
		when: 'calling the buildMethodParamsForTask'
			methodArgsMap = apiActionService.buildMethodParamsForContext(action, task)
		then:
			methodArgsMap
			methodArgsMap.keySet().sort() == ['groupRefCode', 'serverRefId', 'taskId']
	}

	def '7. Test the invoke method'() {
		setup: 'inject a mock awsService into the AweAgent class'
			AwsService awsService = Mock()
			AwsAgent awsAgent = AwsAgent.instance
			awsAgent.awsService = awsService

		when: 'calling the invoke method on the service'
			apiActionService.invoke(action, task)
		then: 'the awsService.sendSnsNotification should be called with proper params'
			1 * awsService.sendSnsMessage(action.asyncQueue, { validateSendSnsMessageMap(it) } )

	}

	// A closure that is used to validate that the argument sent to the method
	// is correct.
	private boolean validateSendSnsMessageMap(param) {
		// (param instancesof Map) &&
		param.serverRefId == asset.assetName &&
		param.callbackMethod == action.callbackMethod &&
		param.groupRefCode == 'xk324-kj1i2-23ks-9sdl'
	}
}
