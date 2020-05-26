package net.transitionmanager.service

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.ThreadLocalUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import grails.util.Holders
import net.transitionmanager.action.ApiAction
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.action.HttpProducerService
import net.transitionmanager.action.Provider
import net.transitionmanager.action.TaskActionService
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.AssetOptionsService
import net.transitionmanager.asset.AssetService
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.SettingService
import net.transitionmanager.connector.AbstractConnector
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.connector.ContextType
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.SecurityService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskFacade
import net.transitionmanager.task.TaskService
import org.springframework.web.multipart.MultipartFile
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import test.helper.ApiCatalogTestHelper
import test.helper.mock.ProjectMock

import static net.transitionmanager.asset.AssetOptions.AssetOptionsType.TASK_CATEGORY

@ConfineMetaClassChanges([GormUtil])
class TaskActionServiceSpec extends Specification implements ServiceUnitTest<TaskActionService>, DataTest, GrailsWebUnitTest {

	@Shared
	List<String> notes = []

	@Shared
	Project      project

	@Shared
	Person       whom

	@Shared
	AssetComment assetComment

	@Shared
	Provider     provider

	@Shared
	ApiCatalog   apiCatalog

	@Shared
	ApiAction    action

	@Shared
	String reactionScripts = '{"STATUS": "// do nothing", "SUCCESS": "// do nothing", "DEFAULT": "// do nothing", "ERROR": "//do nothing"}'

	@Shared
	boolean initialized = false


	private static final String paramsJson = """
		[
			{
				"paramName": "param1",
				"context": "${ContextType.USER_DEF.name()}",
				"value": "xk324-kj1i2-23ks-9sdl"
			}
		]
	"""

	def setupSpec() {
		mockDomains Project, Provider, ApiCatalog, ApiAction, AssetComment, AssetOptions

		//Overriding this so that in the CustomValidators.inList the calls to getConstraintValue don't blow up.
		GormUtil.metaClass.static.getConstraintValue = {Class clazz, String propertyName, String constraintName -> [] as Object}

		service.taskService = [
					addNote: { AssetComment task, Person person, String note, int isAudit = 1 ->
						notes << note
						return true
					}
				] as TaskService
		TaskFacade taskFacade2 = new TaskFacade(new AssetComment(), new Person())
		taskFacade2.taskService = service.taskService
		Holders.grailsApplication.mainContext.beanFactory.registerSingleton("taskFacade", taskFacade2)

		defineBeans {
			coreService(CoreService) {
				grailsApplication = ref('grailsApplication')
			}
			fileSystemService(FileSystemService) {
				coreService = ref('coreService')
				transactionManager = ref('transactionManager')
			}
			settingService(SettingService)

			assetOptionsService(AssetOptionsService)

			httpProducerService(HttpProducerService)
			// A custom context holder to allow us to gain access to the application context and other components of the runtime environment
			applicationContextHolder(ApplicationContextHolder) { bean ->
				bean.factoryMethod = 'getInstance'
			}

			taskFacade(TaskFacade) { bean ->
				bean.scope = 'prototype'

				taskService = [
					addNote: { AssetComment task, Person person, String note, int isAudit = 1 ->
						notes << note
						return true
					},
					setTaskStatus: {AssetComment task, String status, Person whom, boolean isPM=false->
						notes << "Placed task on HOLD, previous state was 'Ready'"
						return null
					}
				] as TaskService
			}
		}
	}

	def setup() {
		notes = []
		new AssetOptions(type: TASK_CATEGORY, value: 'general').save(flush: true, failOnError: true)
		project = new ProjectMock().create()
		whom = new Person(firstName: 'Danger', lastName: 'Powers').save(flush: true)
		new Person(firstName: Person.SYSTEM_USER_AT.firstName,  lastName: Person.SYSTEM_USER_AT.lastName).save(flush: true)
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
			reactionScripts: reactionScripts,
			isRemote: true,
			provider: provider,
			project: project
		)

		if (action.hasErrors()) {
			println "action has errors: ${GormUtil.allErrorsString(action)}"
		}

		project.save(failOnError: true, flush: true)
		action.save(failOnError: true, flush: true)

		assetComment = new AssetComment(comment: 'Task', status: 'Ready', commentType: 'issue', apiAction: action, project: project, assetEntity: [] as AssetEntity)

		assetComment.save(failOnError: true, flush: true)

		service.taskService = [
			addNote: { AssetComment task, Person person, String note, int isAudit = 1 ->
				notes << note
				return true
			}
		] as TaskService

		service.apiActionService = [
			createActionRequest: {ApiAction action, Object object->
				ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS, JsonUtil.parseJson(reactionScripts))
				return null
			},
			invokeReactionScript: {
				ReactionScriptCode code,
				String script,
				ActionRequest request,
				ApiActionResponse response,
				TaskFacade task,
				AssetFacade asset,
				ApiActionJob job->
				notes << "Invoked $task.comment with status: ${code.name()}"
				return [:] as Map<String, ?>
			}
		] as ApiActionService

		service.fileSystemService = [
			writeFile: { MultipartFile file, String prefix = '' -> ['some file']}
		] as FileSystemService

		service.assetService = [
			getAssetFacade: {AssetEntity asset, boolean readonly-> null}
		] as AssetService

		service.securityService = [
			hasAccessToProject: { Project project -> true }

		] as SecurityService
	}

	void 'Test updateRemoteActionStatus started'() {
		setup:  'given a populated progress ActionCommand'
			ActionCommand action = new ActionCommand(
				message: 'some message',
				progress: 5,
				stdout: 'output message',
				stderr: 'error message',
				data: [someKey: 'some data'],
				datafile: null,
				project: project
			)
		when: 'updating the action status'
			service.actionStarted(action, assetComment.id, whom)
		then: 'task notes are added and the percentageComplete is updated'
			notes.size() ==2
			notes.contains('some message')
			notes[1].startsWith('testAction started at')
	}

	void 'Test updateRemoteActionStatus progress'() {
		setup: 'given a populated progress ActionCommand'
			ActionCommand action = new ActionCommand(
				message: 'some message',
				progress: 5,
				stdout: 'output message',
				stderr: 'error message',
				data: [someKey: 'some data'],
				datafile: null,
				project: project
			)
		when: 'updating the action status'
			service.actionProgress(action, assetComment.id, whom)
		then:
			notes.size() == 1
			notes.contains('some message')
			assetComment.percentageComplete == 5
	}

	void 'Test updateRemoteActionStatus error'() {
		setup: 'given a populated error ActionCommand'
			ActionCommand action = new ActionCommand(
				message: 'some message',
				progress: 5,
				stdout: 'output message',
				stderr: 'error message',
				data: [someKey: 'some data'],
				datafile: null,
				project: project
			)
		when: 'updating the action status'
			service.actionError(action, assetComment.id, whom)
		then: 'Task notes are added'
			notes.size() == 2
			notes.contains('some message')
			notes[1] == 'Invoked Task with status: ERROR'
	}

	void 'Test updateRemoteActionStatus success'() {
		setup: 'given a populated success ActionCommand'
			ActionCommand action = new ActionCommand(
				message: 'some message',
				progress: 5,
				stdout: 'output message',
				stderr: 'error message',
				data: [someKey: 'some data'],
				datafile: null,
				project: project
			)
		when: 'updating the action status'
			service.actionDone(action, assetComment.id, whom)
		then: 'task node are added, and the percentageComplete is set to 100'
			notes.size() == 2
			notes.contains('some message')
			assetComment.percentageComplete == 100
			notes[1] == 'Invoked Task with status: SUCCESS'
	}

	void 'Test updateRemoteActionStatus error with exception thrown in invokeReactionScript'() {
			setup: 'given and error Action Command, where the invokeReactionScript will throw an exception.'
				ActionCommand action = new ActionCommand(
					message: 'some message',
					progress: 5,
					stdout: 'output message',
					stderr: 'error message',
					data: [someKey: 'some data'],
					datafile: null,
					project: project
				)

				service.apiActionService = [
					createActionRequest: {ApiAction action2, Object object->
						ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS, JsonUtil.parseJson(reactionScripts))
						return null
					},
					invokeReactionScript: {
						ReactionScriptCode code,
						String script,
						ActionRequest request,
						ApiActionResponse response,
						TaskFacade task,
						AssetFacade asset,
						ApiActionJob job->
						throw new Exception('api invocation exception')
					}
				] as ApiActionService
			when: 'updating the action status'
				service.actionError(action, assetComment.id, whom)
			then: 'Task notes are added'
				notes.size() == 3
				notes.contains('some message')
				notes[1] == 'ERROR script failure: api invocation exception'
				notes[2] == "Placed task on HOLD, previous state was 'Ready'"
		}

	void 'Test updateRemoteActionStatus success with exception thrown in invokeReactionScript'() {
			setup: 'given and success Action Command, where the invokeReactionScript will throw an exception.'
				ActionCommand action = new ActionCommand(
					message: 'some message',
					progress: 5,
					stdout: 'output message',
					stderr: 'error message',
					data: [someKey: 'some data'],
					datafile: null,
					project: project
				)

				service.apiActionService = [
					createActionRequest: {ApiAction action2, Object object->
						ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS, JsonUtil.parseJson(reactionScripts))
						return null
					},
					invokeReactionScript: {
						ReactionScriptCode code,
						String script,
						ActionRequest request,
						ApiActionResponse response,
						TaskFacade task,
						AssetFacade asset,
						ApiActionJob job->
						throw new Exception('api invocation exception')
					}
				] as ApiActionService
			when: 'updating the action status'
				service.actionDone(action, assetComment.id, whom)
			then: 'Task notes are added'
				notes.size() == 3
				notes.contains('some message')
				notes[1] == 'SUCCESS script failure: api invocation exception'
				notes[2] == "Placed task on HOLD, previous state was 'Ready'"
		}

	void 'Test updateRemoteActionStatus success with exception thrown in invokeReactionScript for success but not error reaction scripts'() {
		setup: 'given and success Action Command, where the invokeReactionScript will throw an exception for success scripts only.'
			ActionCommand action = new ActionCommand(
				message: 'some message',
				progress: 5,
				stdout: 'output message',
				stderr: 'error message',
				data: [someKey: 'some data'],
				datafile: null,
				project: project
			)

			service.apiActionService = [
				createActionRequest: {ApiAction action2, Object object->
					ThreadLocalUtil.setThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS, JsonUtil.parseJson(reactionScripts))
					return null
				},
				invokeReactionScript: {
					ReactionScriptCode code,
					String script,
					ActionRequest request,
					ApiActionResponse response,
					TaskFacade task,
					AssetFacade asset,
					ApiActionJob job->
					if(code == ReactionScriptCode.SUCCESS){
						throw new Exception('api invocation exception')
					} else{
						notes << "Invoked $task.comment with status: ${code.name()}"
						 return [:] as Map<String, ?>
					}
				 }
			] as ApiActionService
		when: 'updating the action status'
			service.actionDone(action, assetComment.id, whom)
		then: 'Task notes are added'
			notes.size() == 2
			notes.contains('some message')
			notes[1] == 'Invoked Task with status: ERROR'
	}

	@ConfineMetaClassChanges([ApplicationContextHolder])
	void 'Test renderScript without username/password'() {
		when: 'rendering a script without username/password'
			String renderedScript = service.renderScript('echo {{param1}}',assetComment)
		then: 'The script is rendered to a string'
			renderedScript == 'echo xk324-kj1i2-23ks-9sdl'
	}

	@ConfineMetaClassChanges([ApplicationContextHolder])
	void 'Test renderScript with username/password'() {
		when: 'rendering a script with username/password'
			String renderedScript = service.renderScript('echo {{param1}} \necho {{username}} \necho {{password}}',assetComment)
		then: 'The script is rendered to a string'
			renderedScript == 'echo xk324-kj1i2-23ks-9sdl \necho {{username}} \necho {{password}}'
	}

	@ConfineMetaClassChanges([TaskActionService])
	void "Test ActionLookUp"(){
		setup: 'Given mocks for external services'
			service.apiActionService = [
				connectorInstanceForAction: { ApiAction action ->
					[
						buildMethodParamsWithContext: { ApiAction action2, AssetComment task ->
							[param1: 'test param']
						}
					] as AbstractConnector
				}
			] as ApiActionService

			service.metaClass.renderScript = {String script,  AssetComment task-> script}

			service.taskService  = [
				fillLabels: {Project project, List<Map> methodParams-> methodParams}
			] as TaskService
		when: 'calling actionLookUp'
			Map action = service.actionLookup(action.id, project)
		then: 'a map of API action details is returned'

			action == [
				name              : 'testAction',
				script            : null,
				isRemote          : true,
				type              : 'WebAPI',
				connector         : null,
				method            : 'invokeHttpRequest',
				description       : 'This is an action for testing',
				methodParams      : [
					[
						paramName: 'param1',
						context  : 'USER_DEF',
						value    : 'xk324-kj1i2-23ks-9sdl'
					]
				],
				methodParamsValues: [
					param1: 'test param'
				]
			]

	}
}
