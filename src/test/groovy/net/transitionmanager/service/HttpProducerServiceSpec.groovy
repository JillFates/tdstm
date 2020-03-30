package net.transitionmanager.service

import com.stehno.ersatz.ErsatzServer
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.ThreadLocalUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.action.HttpProducerService
import net.transitionmanager.action.ReactionScriptInvocationParams
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.MessageSourceService
import net.transitionmanager.common.SettingService
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.connector.ContextType
import net.transitionmanager.action.ApiAction
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.action.Provider
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionRequestParameter
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskFacade
import net.transitionmanager.task.TaskService
import org.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import test.helper.ApiCatalogTestHelper
import test.helper.mock.ProjectMock

class HttpProducerServiceSpec extends Specification implements ServiceUnitTest<HttpProducerService>, DataTest, GrailsWebUnitTest {

	private Project project
	private Provider provider
	private ApiCatalog apiCatalog
	private ApiAction action
	@Shared
	private ErsatzServer ersatz

	static doWithConfig(c) {
		c.graph.tmpDir = '/tmp/'
	}

	private static final String paramsJson = """
		[
			{
				"param": "param1",
				"context": "${ContextType.USER_DEF.name()}",
				"value": "xk324-kj1i2-23ks-9sdl"
			}
		]
	"""

	def setupSpec() {
		mockDomains Project, Provider, ApiCatalog, ApiAction

		defineBeans {
			apiActionService(ApiActionService)
			messageSourceService(MessageSourceService) { bean ->
				messageSource = ref('messageSource')
			}
			coreService(CoreService) {
				grailsApplication = ref('grailsApplication')
			}
			fileSystemService(FileSystemService) {
				coreService = ref('coreService')
				transactionManager = ref('transactionManager')
			}
			settingService(SettingService)
		}
		service.apiActionService = applicationContext.getBean(ApiActionService)

		// http://stehno.com/ersatz/
		// http://guides.grails.org/grails-mock-http-server/guide/index.html
		ersatz = new ErsatzServer()
		ersatz.expectations {
			get('/test0') {
				responder {
					code(0)
				}
			}
			get('/test400') {
				responder {
					code(400)
				}
			}
			get('/test401') {
				responder {
					code(401)
				}
			}
			get('/test403') {
				responder {
					code(403)
				}
			}
			get('/test404') {
				responder {
					code(404)
				}
			}
			get('/test405') {
				responder {
					code(405)
				}
			}
			get('/test406-499') {
				responder {
					code(406)
				}
			}
			// get('/test500') {
			// 	responder {
			// 		code(500)
			// 	}
			// }
			// get('/test501') {
			// 	responder {
			// 		code(501)
			// 	}
			// }
			// get('/test502') {
			// 	responder {
			// 		code(502)
			// 	}
			// }
			// get('/test503') {
			// 	responder {
			// 		code(503)
			// 	}
			// }
			// get('/test504-599') {
			// 	responder {
			// 		code(504)
			// 	}
			// }
		}
	}

	def setup() {
		project = new ProjectMock().create()
		provider = new Provider(name: 'provider-name', project: project)
		apiCatalog = new ApiCatalog(name: 'api-cat', dictionary: ApiCatalogTestHelper.DICTIONARY, dictionaryTransformed: '{"key": "value"}', project: project, provider: provider)

		action = new ApiAction(
				name: 'testAction',
				description: 'This is an action for testing',
				apiCatalog: apiCatalog,
				connectorMethod: 'invokeHttpRequest',
				methodParams: paramsJson,
				producesData: 0,
				callbackMode: CallbackMode.DIRECT,
				httpMethod: ApiActionHttpMethod.GET,
				endpointUrl: 'http://zzz.about.yyy',
				reactionScripts: '{"STATUS": "// do nothing", "SUCCESS": "// do nothing", "DEFAULT": "// do nothing"}',
				isRemote: false,
				provider: provider,
				project: project
		)

		if (action.hasErrors()) {
			println "action has errors: ${GormUtil.allErrorsString(action)}"
		}

		project.save(failOnError: true, flush: true)
		action.save(failOnError: true, flush: true)
	}

	def cleanup() {
		action.delete()
		apiCatalog.delete()
		provider.delete()
		// project.delete()
	}

	def cleanupSpec() {
		ersatz.stop()
	}

	ActionRequest getActionRequest(ApiAction apiAction) {
		Map optionalRequestParams = [
				actionId: apiAction.id,
				producesData: apiAction.producesData,
				credentials: apiAction.credential?.toMap(),
				apiAction: GormUtil.domainObjectToMap(apiAction)
		]
		ActionRequest actionRequest = new ActionRequest()
		actionRequest.setOptions(new ActionRequestParameter(optionalRequestParams))
		ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.ACTION_REQUEST, actionRequest)
		return actionRequest
	}

	@See('TM-10046')
	void 'Test invalid action request throw exception'() {
		when:
			service.executeCall(null)
		then:
			thrown Exception
	}

	@See('TM-10046')
	void 'Test http service execute call return DNS name not found'() {
		when:
			ActionRequest actionRequest = getActionRequest(action)
			AssetComment task = new AssetComment()
			Person whom = new Person()
			TaskFacade taskFacade = new TaskFacade(task, whom)
			taskFacade.messageSourceService = applicationContext.getBean(MessageSourceService)
			TaskService taskService = Mock()
			taskFacade.taskService = taskService

			ApiActionResponse actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					actionRequest,
					taskFacade,
					new AssetFacade(null, [:], true)
				)
			)
		then:
			'Unable to resolve host name (zzz.about.yyy)' == actionResponse.error
	}

	@See('TM-10046')
	void 'Test http service execute call return failure to contact endpoint'() {
		when:
			action.endpointUrl = ersatz.httpUrl + '/test0'
			ActionRequest actionRequest = getActionRequest(action)
			AssetComment task = new AssetComment()
			Person whom = new Person()
			TaskFacade taskFacade = new TaskFacade(task, whom)
			taskFacade.messageSourceService = applicationContext.getBean(MessageSourceService)
			TaskService taskService = Mock()
			taskFacade.taskService = taskService

			ApiActionResponse actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					actionRequest,
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(0) == actionResponse.error
	}

	@See('TM-10046')
	void 'Test http service execute call return error'() {
		setup:
			AssetComment task = new AssetComment()
			Person whom = new Person()
			TaskFacade taskFacade = new TaskFacade(task, whom)
			taskFacade.messageSourceService = applicationContext.getBean(MessageSourceService)
			TaskService taskService = Mock()
			taskFacade.taskService = taskService

		when:
			action.endpointUrl = ersatz.httpUrl + '/test400'
			ApiActionResponse actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(400) == actionResponse.error

		when:
			action.endpointUrl = ersatz.httpUrl + '/test401'
			actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(401) == actionResponse.error
		when:
			action.endpointUrl = ersatz.httpUrl + '/test403'
			actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(403) == actionResponse.error
		when:
			action.endpointUrl = ersatz.httpUrl + '/test404'
			actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(404) == actionResponse.error
		when:
			action.endpointUrl = ersatz.httpUrl + '/test405'
			actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(405) == actionResponse.error
		when:
			action.endpointUrl = ersatz.httpUrl + '/test406-499'
			actionResponse = service.executeCall(new ReactionScriptInvocationParams(
					new JSONObject(),
					getActionRequest(action),
					taskFacade,
					new AssetFacade(null, [:], true)
			))
		then:
			service.getHttpResponseError(406) == actionResponse.error
		// when:
		// 	action.endpointUrl = ersatz.httpUrl + '/test500'
		// 	actionRequest = getActionRequest(action)
		// 	actionResponse = service.executeCall(actionRequest)
		// then:
		// 	service.getHttpResponseError(500) == actionResponse.error
		// when:
		// 	action.endpointUrl = ersatz.httpUrl + '/test501'
		// 	actionRequest = getActionRequest(action)
		// 	actionResponse = service.executeCall(actionRequest)
		// then:
		// 	service.getHttpResponseError(501) == actionResponse.error
		// when:
		// 	action.endpointUrl = ersatz.httpUrl + '/test502'
		// 	actionRequest = getActionRequest(action)
		// 	actionResponse = service.executeCall(actionRequest)
		// then:
		// 	service.getHttpResponseError(502) == actionResponse.error
		// when:
		// 	action.endpointUrl = ersatz.httpUrl + '/test503'
		// 	actionRequest = getActionRequest(action)
		// 	actionResponse = service.executeCall(actionRequest)
		// then:
		// 	service.getHttpResponseError(503) == actionResponse.error
		// when:
		// 	action.endpointUrl = ersatz.httpUrl + '/test504-599'
		// 	actionRequest = getActionRequest(action)
		// 	actionResponse = service.executeCall(actionRequest)
		// then:
		// 	service.getHttpResponseError(504) == actionResponse.error
	}
}
