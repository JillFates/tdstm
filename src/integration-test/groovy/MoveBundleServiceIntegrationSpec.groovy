import com.tdsops.tm.enums.domain.Color
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import grails.test.mixin.integration.Integration
import grails.validation.ValidationException
import grails.web.servlet.mvc.GrailsHttpSession
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.command.MoveBundleCommand
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.Task
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Transactional
class MoveBundleServiceIntegrationSpec extends Specification{

	MoveBundleService moveBundleService
	MoveEventService  moveEventService

	@Shared
	ProjectTestHelper    projectHelper

	@Shared
	PersonTestHelper     personHelper

	@Shared
	MoveBundleTestHelper moveBundleHelper

	@Shared
	AssetTestHelper      assetHelper

	@Shared
	SettingServiceTests  settingServiceTests


	@Shared
	Project project

	@Shared
	Person person

	@Shared
	UserLogin userLogin

	@Shared
	MoveBundle pBundle

	@Shared
	MoveBundle npBundle

	@Shared
	AssetEntity asset1

	@Shared
	AssetEntity asset2

	@Shared
	AssetEntity asset3

	@Shared
	AssetEntity asset4

	@Shared
	AssetEntity asset5

	@Shared
	AssetEntity asset6

	@Shared
	AssetEntity asset7

	@Shared
	AssetEntity asset8

	@Shared
	AssetEntity asset9

	@Shared
	AssetEntity asset10

	@Shared
	AssetEntity asset11

	@Shared
	AssetEntity asset12

	@Shared
	AssetEntity asset13

	@Shared
	AssetEntity asset14

	@Shared
	AssetEntity asset15

	@Shared
	AssetDependency dep1

	@Shared
	AssetDependency dep2

	@Shared
	AssetDependency dep3

	@Shared
	AssetDependency dep4

	@Shared
	AssetDependency dep5

	@Shared
	AssetDependency dep6

	@Shared
	AssetDependency dep7

	@Shared
	AssetDependency dep8

	@Shared
	AssetDependencyBundle adb1

	@Shared
	AssetDependencyBundle adb2

	@Shared
	AssetDependencyBundle adb3

	@Shared
	AssetDependencyBundle adb4

	@Shared
	AssetDependencyBundle adb5

	@Shared
	AssetDependencyBundle adb6

	@Shared
	AssetDependencyBundle adb7

	@Shared
	AssetDependencyBundle adb8

	@Shared
	AssetDependencyBundle adb9

	@Shared
	AssetDependencyBundle adb10

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagAsset tagAsset1

	@Shared
	TagAsset tagAsset2

	@Shared
	TagAsset tagAsset3

	@Shared
	TagAsset tagAsset4

	@Shared
	String connectionTypes = "'Unknown', 'Runs On', 'Hosts', 'DB', 'Web', 'Backup', 'File', 'FTP/SCP', 'Replacement', 'Network', 'Power', 'Virtual Desktop'"

	@Shared
	String statusTypes = "'Unknown', 'Validated', 'Questioned', 'Future'"

	@Shared
	GrailsHttpSession session

	@Shared
	boolean initialized = false

@Transactional
	void setup() {
		if(!initialized) {
			session = Mock()
			moveBundleService.metaClass.getSession = { session }

			projectHelper = new ProjectTestHelper()
			personHelper = new PersonTestHelper()
			moveBundleHelper = new MoveBundleTestHelper()
			assetHelper = new AssetTestHelper()
			settingServiceTests = new SettingServiceTests()

			project = projectHelper.createProject()
			// 'a person'
			person = personHelper.createPerson(null, project.client, project)

			// 'a user associated to the project'
			userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ROLE_ADMIN}"], project, true)

			// 'create two bundles associated to the project, one planning bundle and other non-planning bundle'
			pBundle = moveBundleHelper.createBundle(project, 'Planning Bundle', true)
			npBundle = moveBundleHelper.createBundle(project, 'Non-planning Bundle', false)

			// 'set the project default bundle to our planning bundle (so this are the only two bundles for the project)'
			project.defaultBundle = pBundle

			// 'some assets are created, and assigned to the planning bundle'
			asset1 = assetHelper.createDevice(project, AssetType.APPLICATION, [moveBundle: pBundle])
			asset2 = assetHelper.createDevice(project, AssetType.APPLICATION, [moveBundle: pBundle])
			asset3 = assetHelper.createDevice(project, AssetType.DATABASE, [moveBundle: pBundle])
			asset4 = assetHelper.createDevice(project, AssetType.FILES, [moveBundle: pBundle])
			asset5 = assetHelper.createDevice(project, AssetType.SERVER, [moveBundle: pBundle])
			asset6 = assetHelper.createDevice(project, AssetType.STORAGE, [moveBundle: pBundle])
			asset7 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			asset8 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			asset9 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			asset10 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])

			//'some more assets are created, and assigned to the non-planning bundle'
			asset11 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			asset12 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			asset13 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			asset14 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			asset15 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])

			//'some dependency relationships are established between this assets (see graph in TM-10261)'
			dep1 = new AssetDependency(asset: asset1, dependent: asset3).save(flush: true)
			dep2 = new AssetDependency(asset: asset2, dependent: asset3).save(flush: true)
			dep3 = new AssetDependency(asset: asset3, dependent: asset4).save(flush: true)
			dep4 = new AssetDependency(asset: asset5, dependent: asset6).save(flush: true)
			dep5 = new AssetDependency(asset: asset9, dependent: asset10).save(flush: true)
			dep6 = new AssetDependency(asset: asset9, dependent: asset11).save(flush: true)
			dep7 = new AssetDependency(asset: asset8, dependent: asset12).save(flush: true)
			dep8 = new AssetDependency(asset: asset14, dependent: asset15).save(flush: true)

			Integer dependencyBundle = 1
			adb1 = new AssetDependencyBundle(project: project, asset: asset1, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb2 = new AssetDependencyBundle(project: project, asset: asset2, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb3 = new AssetDependencyBundle(project: project, asset: asset3, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb4 = new AssetDependencyBundle(project: project, asset: asset4, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb5 = new AssetDependencyBundle(project: project, asset: asset5, dependencySource: 'the source', dependencyBundle: ++dependencyBundle).save(flush: true)
			adb6 = new AssetDependencyBundle(project: project, asset: asset6, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb7 = new AssetDependencyBundle(project: project, asset: asset7, dependencySource: 'the source', dependencyBundle: ++dependencyBundle).save(flush: true)
			adb8 = new AssetDependencyBundle(project: project, asset: asset8, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)
			adb9 = new AssetDependencyBundle(project: project, asset: asset9, dependencySource: 'the source', dependencyBundle: ++dependencyBundle).save(flush: true)
			adb10 = new AssetDependencyBundle(project: project, asset: asset10, dependencySource: 'the source', dependencyBundle: dependencyBundle).save(flush: true)

			tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true)
			tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true)
			tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: project).save(flush: true)

			tagAsset1 = new TagAsset(tag: tag1, asset: asset2).save(flush: true)
			tagAsset2 = new TagAsset(tag: tag1, asset: asset3).save(flush: true)
			tagAsset3 = new TagAsset(tag: tag2, asset: asset4).save(flush: true)
			tagAsset4 = new TagAsset(tag: tag3, asset: asset5).save(flush: true)

			initialized = true
		}
	}

	void '01. Test lookupList with default parameters'() {
		given: 'two projects with some bundles assigned to each'
			Project project1 = projectHelper.createProject()
			Project project2 = projectHelper.createProject()
			moveBundleHelper.createBundle(project1, 'B')
			moveBundleHelper.createBundle(project1, 'A')
			moveBundleHelper.createBundle(project1, 'C')
			moveBundleHelper.createBundle(project2, 'Proj2 Bundle 1')

		when: 'bundles for the first project are retrieved with default parameters'
			List result = moveBundleService.lookupList(project1)
		then: 'the list should have three results'
			result.size() == 3
		and: 'the elements returned should be id and name'
			result[0].containsKey('id')
			result[0].containsKey('name')
			result[0].keySet().size() == 2
		and: 'the bundles are correctly sorted'
			result[0].name == 'A'
			result[1].name == 'B'
			result[2].name == 'C'
	}

	void '02. Test lookupList for specific fields and sorting criteria'() {
		when: 'requesting the id and guid where the results are sorted by id'
			List result = moveBundleService.lookupList(project, ['id', 'description'], 'id')
		then: 'the results include fields id and guid'
			result[0].containsKey('id')
			result[0].containsKey('description')
			result[0].keySet().size() == 2
		and: 'the results are sorted correctly'
			result[0].id < result[1].id
	}

	void '03. Test lookupList for a project with no bundles'() {
		given: 'A project with no bundles'
			Project project = projectHelper.createProject()

		when: 'calling lookupList for a project with no bundles'
			List result = moveBundleService.lookupList(project)
		then: 'the list should be empty'
			result.size() == 0
	}

	@See('TM-6847')
	void '04. Test deleteBundleAndAssets method'() {
		when: 'a new Bundle is created'
			MoveBundle bundle = moveBundleHelper.createBundle(project, 'Test Bundle')
		and: 'a new Event is created'
			MoveEvent event = moveEventService.create(project, 'Test Event')
		and: 'the above Bundle is assigned to the Event'
			moveBundleService.assignMoveEvent(event, [bundle.id])
		and: 'some assets are created and assigned to the Bundle'
			AssetEntity asset1 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: bundle])
			AssetEntity asset2 = assetHelper.createDevice(project, AssetType.SERVER, [moveBundle: bundle])
		and: 'an asset not assigned to the Bundle (associated with the Default Bundle by default) is created'
			AssetEntity asset3 = assetHelper.createDevice(project, AssetType.SERVER)
		and: 'some tasks are generated for the Event'
			def task1 = new Task (taskNumber: 1000, duration: 5, comment: 'Test task 1', moveEvent: event, project: project).save(flush: true)
			def task2 = new Task(taskNumber: 1001, duration: 5, comment: 'Test task 2', moveEvent: event, project: project).save(flush: true)
		and: 'the deleteBundleAndAssets method is called'
			moveBundleService.deleteBundleAndAssets(bundle)
		then: 'the Bundle should be deleted'
			MoveBundle.get(bundle.id) == null
		and: 'the assigned assets should be deleted'
			AssetEntity.withNewSession {
				AssetEntity.get(asset1.id) == null
				AssetEntity.get(asset2.id) == null
			}
		and: 'the asset not assigned to the deleted Bundle should still exist'
			AssetEntity.get(asset3.id) != null
		and: 'the Event should still exist'
			MoveEvent.get(event.id) != null
		and: 'the tasks should still exist '
			def task1Test = AssetComment.get(task1.id)
			task1Test
			def task2Test = AssetComment.get(task2.id)
			task2Test
		and: 'the asset references on the tasks should be nulled out'
			task1Test.assetEntity == null
			task2Test.assetEntity == null
	}

	//@Ignore
	@See('TM-10261')
	void '05. Test Dependency Analyzer grouping'() {
		when: 'the dependency groups are generated'
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
		then: 'test that there is a group 1 composed of assets 1, 2, 3 and 4'
			List groupOne = AssetDependencyBundle.findAllByDependencyBundleAndProject(1, project)
			assert [asset1.id, asset2.id, asset3.id, asset4.id].sort() == groupOne.collect { it.asset.id }.sort()
		and: 'test that there is a group 2 composed of assets 5 and 6'
			List groupTwo = AssetDependencyBundle.findAllByDependencyBundleAndProject(2, project)
			assert [asset5.id, asset6.id].sort() == groupTwo.collect { it.asset.id }.sort()
		and: 'test that there is a group 3 composed of assets 7 and 8'
			List groupThree = AssetDependencyBundle.findAllByDependencyBundleAndProject(3, project)
			assert [asset7.id, asset8.id].sort() == groupThree.collect { it.asset.id }.sort()
		and: 'test that the Straggler group contains assets 9 and 10'
			List groupZero = AssetDependencyBundle.findAllByDependencyBundleAndProject(4, project)
			assert [asset9.id, asset10.id].sort() == groupZero.collect { it.asset.id }.sort()
	}


	void '06. Test Dependency Console Map'() {
		when: ''
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = moveBundleService.dependencyConsoleMap(project, pBundle.id, null, null, null, null)
		then: ''
			dependencyConsole.dependencyConsoleList == [
				[dependencyBundle: 0, appCount: 0, serverCount: 3, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict'],
				[dependencyBundle: 1, appCount: 0, serverCount: 4, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict'],
				[dependencyBundle: 2, appCount: 0, serverCount: 2, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict'],
				[dependencyBundle: 3, appCount: 0, serverCount: 2, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict']
			]

			dependencyConsole.gridStats == [
				app    : [0, 0],
				db     : [0, 0],
				server : [11, 8],
				vm     : [0, 0],
				storage: [0, 0]
			]

			dependencyConsole.dependencyBundleCount == 4

	}


	void '07. Test Dependency Console Map Filtered by tags ANY tag1'() {
		when: ''
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = moveBundleService.dependencyConsoleMap(project, pBundle.id, [tag1.id], 'ANY', null, null)
		then: ''
			dependencyConsole.dependencyConsoleList == [
					[dependencyBundle: 0, appCount: 0, serverCount: 0, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupDone'],
					[dependencyBundle: 1, appCount: 0, serverCount: 2, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict']
			]

			dependencyConsole.gridStats == [
				app    : [0, 0],
				db     : [0, 0],
				server : [2, 2],
				vm     : [0, 0],
				storage: [0, 0]
			]

			dependencyConsole.dependencyBundleCount == 4
	}

	void '08. Test Dependency Console Map Filtered by tags ALL tag1'() {
		when: ''
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = moveBundleService.dependencyConsoleMap(project, pBundle.id, [tag1.id], 'ALL', null, null)
		then: ''
			dependencyConsole.dependencyConsoleList == [
					[dependencyBundle: 0, appCount: 0, serverCount: 0, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupDone'],
					[dependencyBundle: 1, appCount: 0, serverCount: 2, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict']
			]

			dependencyConsole.gridStats == [
				app    : [0, 0],
				db     : [0, 0],
				server : [2, 2],
				vm     : [0, 0],
				storage: [0, 0]
			]

			dependencyConsole.dependencyBundleCount == 4
	}

	void '09. Test Dependency Console Map Filtered by tags ANY tag1 + tag2'() {
		when: ''
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = moveBundleService.dependencyConsoleMap(project, pBundle.id, [tag1.id, tag2.id], 'ANY', null, null)
		then: ''
			dependencyConsole.dependencyConsoleList == [
					[dependencyBundle: 0, appCount: 0, serverCount: 0, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupDone'],
					[dependencyBundle: 1, appCount: 0, serverCount: 3, vmCount: 0, dbCount: 0, storageCount: 0, statusClass: 'depGroupConflict']
			]

			dependencyConsole.gridStats == [
				app    : [0, 0],
				db     : [0, 0],
				server : [3, 3],
				vm     : [0, 0],
				storage: [0, 0]
			]

			dependencyConsole.dependencyBundleCount == 4
	}

	void '10. Test Dependency Console Map Filtered by tags ALL tag1 + tag2'() {
		when: ''
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = moveBundleService.dependencyConsoleMap(project, pBundle.id, [tag1.id, tag2.id], 'ALL', null, null)
		then: ''
			dependencyConsole.dependencyConsoleList == []

			dependencyConsole.gridStats == [
				app    : [0, 0],
				db     : [0, 0],
				server : [0, 0],
				vm     : [0, 0],
				storage: [0, 0]
			]

			dependencyConsole.dependencyBundleCount == 4
	}

	void '11. Create move bundle'() {
		when: 'creating a move bundle'
			MoveBundleCommand command = new MoveBundleCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10)
			)
			MoveBundle moveBundle = moveBundleService.saveOrUpdate(command, project)
		then: 'move bundle is saved to db'
			moveBundle
			moveBundle.id
		when: 'saving a second move bundle with same name and project'
			moveBundleService.saveOrUpdate(command, project)
		then: 'exception is thrown'
			thrown(ValidationException)

	}

	void '12. Update move bundle'() {
		given: 'a move bundle'
			MoveBundleCommand command = new MoveBundleCommand(
					name: RandomStringUtils.randomAscii(10),
					description: RandomStringUtils.randomAscii(10),
			)
			MoveBundle moveBundle = moveBundleService.saveOrUpdate(command, project)
		when: 'updating a move bundle'
			command.id = moveBundle.id
			command.startTime = TimeUtil.parseDateTime('2018-11-13')
			command.completionTime = TimeUtil.parseDateTime('2018-11-30')
			command.name = 'Test update MoveBundle'
			MoveBundle moveBundleUpdated = moveBundleService.saveOrUpdate(command, project, moveBundle.id)
		then: 'move bundle is updated in db'
			moveBundleUpdated
			moveBundleUpdated.id
			moveBundleUpdated.startTime == TimeUtil.parseDateTime('2018-11-13')
			moveBundleUpdated.name == 'Test update MoveBundle'
			moveBundleUpdated.completionTime == TimeUtil.parseDateTime('2018-11-30')
		when: 'updating a move bundle with errors'
			command.name = null
			moveBundleService.saveOrUpdate(command, project, moveBundle.id)
		then: 'exception is thrown'
			thrown(ValidationException)
	}
}
