import com.tdsops.tm.enums.domain.Color
import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Transactional
import grails.test.mixin.integration.Integration
import grails.web.servlet.mvc.GrailsHttpSession
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.person.Person
import net.transitionmanager.project.DependencyConsoleService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Transactional
class DependencyConsoleServiceIntegrationSpec extends Specification {

	DependencyConsoleService dependencyConsoleService

	@Shared
	ProjectTestHelper projectHelper

	@Shared
	PersonTestHelper personHelper

	@Shared
	MoveBundleTestHelper moveBundleHelper

	@Shared
	AssetTestHelper assetHelper

	@Shared
	SettingServiceTests settingServiceTests


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
		if (!initialized) {
			session = Mock()
			dependencyConsoleService.metaClass.getSession = { session }

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


	void '01. Test Dependency Console Map'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, null, null, null, null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Remnants', 'Grouped', 1, 2, 3],
				application    : [0, 0, 0, 0, 0, 0],
				serversPhysical: [10, 2, 8, 4, 2, 2],
				serversVirtual : [0, 0, 0, 0, 0, 0],
				databases      : [0, 0, 0, 0, 0, 0],
				storage        : [0, 0, 0, 0, 0, 0],
				statusClass    : [null, null, null, 'depGroupConflict', 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
	}

	void '02. Test Dependency Console Map'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, null, null, "1", null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Grouped', 2, 3],
				application    : [0, 0, 0, 0],
				serversPhysical: [8, 8, 2, 2],
				serversVirtual : [0, 0, 0, 0],
				databases      : [0, 0, 0, 0],
				storage        : [0, 0, 0, 0],
				statusClass    : [null, null, 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]

	}


	void '03. Test Dependency Console Map Filtered by tags ANY tag1'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, [tag1.id], 'ANY', null, null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Remnants', 'Grouped', 1, 2, 3],
				application    : [0, 0, 0, 0, 0, 0],
				serversPhysical: [10, 2, 8, 4, 2, 2],
				serversVirtual : [0, 0, 0, 0, 0, 0],
				databases      : [0, 0, 0, 0, 0, 0],
				storage        : [0, 0, 0, 0, 0, 0],
				statusClass    : [null, null, null, 'depGroupConflict', 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
	}

	void '04. Test Dependency Console Map Filtered by tags ALL tag1'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, [tag1.id], 'ALL', null, null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Remnants', 'Grouped', 1, 2, 3],
				application    : [0, 0, 0, 0, 0, 0],
				serversPhysical: [10, 2, 8, 4, 2, 2],
				serversVirtual : [0, 0, 0, 0, 0, 0],
				databases      : [0, 0, 0, 0, 0, 0],
				storage        : [0, 0, 0, 0, 0, 0],
				statusClass    : [null, null, null, 'depGroupConflict', 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
	}

	void '05. Test Dependency Console Map Filtered by tags ANY tag1 + tag2'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, [tag1.id, tag2.id], 'ANY', null, null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Remnants', 'Grouped', 1, 2, 3],
				application    : [0, 0, 0, 0, 0, 0],
				serversPhysical: [10, 2, 8, 4, 2, 2],
				serversVirtual : [0, 0, 0, 0, 0, 0],
				databases      : [0, 0, 0, 0, 0, 0],
				storage        : [0, 0, 0, 0, 0, 0],
				statusClass    : [null, null, null, 'depGroupConflict', 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
	}

	void '06. Test Dependency Console Map Filtered by tags ALL tag1 + tag2'() {
		when: ''
			dependencyConsoleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
			Map dependencyConsole = dependencyConsoleService.dependencyConsoleMap(project, pBundle.id, [tag1.id, tag2.id], 'ALL', null, null)
		then: ''
			dependencyConsole.dependencyConsole == [
				group          : ['All', 'Remnants', 'Grouped', 1, 2, 3],
				application    : [0, 0, 0, 0, 0, 0],
				serversPhysical: [10, 2, 8, 4, 2, 2],
				serversVirtual : [0, 0, 0, 0, 0, 0],
				databases      : [0, 0, 0, 0, 0, 0],
				storage        : [0, 0, 0, 0, 0, 0],
				statusClass    : [null, null, null, 'depGroupConflict', 'depGroupConflict', 'depGroupConflict']
			]

			dependencyConsole.allMoveBundles == [[name: 'Non-planning Bundle', id: npBundle.id, useForPlanning: false], [name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
			dependencyConsole.planningBundles == [[name: 'Planning Bundle', id: pBundle.id, useForPlanning: true]]
	}
}
