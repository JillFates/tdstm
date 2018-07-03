package net.transitionmanager.domain

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.GormUtil
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.agent.ContextType
import spock.lang.Specification
import spock.lang.Title

@Title('Tests for the ApiAction domain class')
class ApiActionSpec extends Specification {

	private Project project
	private ApiAction action
	private AssetComment task
	private AssetEntity asset

	private static final String paramsJson = """
		[ { "param": "taskId",
			"desc": "The id of the task",
			"context": "${ContextType.TASK.name()}",
			"paramName": "id"
		  },
		  {	"param": "serverRefId",
			"desc": "The unique id used to reference the server in the API",
			"context": "${ContextType.ASSET.name()}",
			"paramName": "assetName"
		  },
		  {	"param": "groupRefCode",
			"context": "${ContextType.USER_DEF.name()}",
			"value": "xk324-kj1i2-23ks-9sdl"
		  }
		]
	"""

	void setup() {
		project = new Project()

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
		if (action.hasErrors()) {
			println "action has errors: ${GormUtil.allErrorsString(action)}"
		}
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

	def '1. Tests for ApiAction.methodParamsList'() {
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
			paramsList[0].paramName == 'id'
			paramsList[0].desc
	}

	def '2 Tests for ApiAction.listMethodParams'() {
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
			paramsList[0].paramName == 'id'
			paramsList[0].desc
	}

	def '3. Test for AssetComment.actionInvocable'() {
		// TODO : JPM 2/2017 : This test should be refactored into a spec for ApiAction domain

		when: 'the apiActionInvokedAt property is set'
			task.status = AssetCommentStatus.READY
			task.apiActionInvokedAt = new Date()
		then: 'the task action should not be invocable because it was already invoked'
			! task.isActionInvocable()

		when: 'the apiActionCompletedAt property is set'
			task.apiActionInvokedAt = new Date()
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
}
