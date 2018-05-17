import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentType
import net.transitionmanager.domain.UserLogin
import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.SecurityService
import com.tdsops.tm.enums.domain.SecurityRole
import net.transitionmanager.service.SettingService
import org.hibernate.SessionFactory
import spock.lang.Ignore
import spock.lang.See


import spock.lang.Specification

class MoveBundleServiceIntegrationSpec extends Specification {

    MoveBundleService moveBundleService
    MoveEventService moveEventService
    SettingService settingService
    SecurityService securityService
    SessionFactory sessionFactory

    private ProjectTestHelper projectHelper = new ProjectTestHelper()
    private PersonTestHelper personHelper = new PersonTestHelper()
    private MoveBundleTestHelper moveBundleHelper = new MoveBundleTestHelper()
    private AssetTestHelper assetHelper = new AssetTestHelper()
    private SettingServiceTests settingServiceTests = new SettingServiceTests()

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

    void '02. Test lookupList for specific fields and sorting criteria' () {
        given: 'A project with a couple of bundles'
            Project project = projectHelper.createProject()
            moveBundleHelper.createBundle(project)
            moveBundleHelper.createBundle(project)

        when: 'requesting the id and workflowCode where the results are sorted by id'
            List result = moveBundleService.lookupList(project, ['id', 'workflowCode'], 'id')
        then: 'the results include fields id and workflowCode'
            result[0].containsKey('id')
            result[0].containsKey('workflowCode')
            result[0].keySet().size() == 2
        and: 'the results are sorted correctly'
            result[0].id < result[1].id
    }

    void '03. Test lookupList for a project with no bundles' () {
        given: 'A project with no bundles'
            Project project = projectHelper.createProject()

        when: 'calling lookupList for a project with no bundles'
            List result = moveBundleService.lookupList(project)
        then: 'the list should be empty'
            result.size() == 0
    }

    @See('TM-6847')
    void '04. Test deleteBundleAndAssets method' () {
        setup: 'create a project'
            Project project = projectHelper.createProjectWithDefaultBundle()
        and: 'create a person associated with the project'
            Person person = personHelper.createPerson(null, project.client, project)
        and: 'create a user that is logged in and the project is their default'
            UserLogin userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ADMIN}"], project, true)
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
            def task1 = new AssetComment(taskNumber: 1000, duration: 5, comment: 'Test task 1', moveEvent: event, commentType: AssetCommentType.TASK, project: project).save(flush: true)
            def task2 = new AssetComment(taskNumber: 1001, duration: 5, comment: 'Test task 2', moveEvent: event, commentType: AssetCommentType.TASK, project: project).save(flush: true)
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

	@Ignore
	@See('TM-10261')
	void '05. Test Dependency Analyzer grouping' () {
		setup: 'create a project'
			Project project = projectHelper.createProject()
		and: 'a person'
			Person person = personHelper.createPerson(null, project.client, project)
		and: 'a user associated to the project'
			UserLogin userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ADMIN}"], project, true)
		and: 'create two bundles associated to the project, one planning bundle and other non-planning bundle'
			MoveBundle pBundle = moveBundleHelper.createBundle(project, 'Planning Bundle', true)
			MoveBundle npBundle = moveBundleHelper.createBundle(project, 'Non-planning Bundle', false)
		and: 'set the project default bundle to our planning bundle (so this are the only two bundles for the project)'
			project.defaultBundle = pBundle
		and: 'some assets are created, and assigned to the planning bundle'
			AssetEntity asset1 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset2 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset3 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset4 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset5 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset6 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset7 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset8 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset9 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
			AssetEntity asset10 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: pBundle])
		and: 'some more assets are created, and assigned to the non-planning bundle'
			AssetEntity asset11 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			AssetEntity asset12 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			AssetEntity asset13 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			AssetEntity asset14 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
			AssetEntity asset15 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: npBundle])
		and: 'some dependency relationships are established between this assets (see graph in TM-10261)'
			AssetDependency dep1 = new AssetDependency(asset: asset1, dependent: asset3).save(failOnError:true)
			AssetDependency dep2 = new AssetDependency(asset: asset2, dependent: asset3).save(failOnError:true)
			AssetDependency dep3 = new AssetDependency(asset: asset3, dependent: asset4).save(failOnError:true)
			AssetDependency dep4 = new AssetDependency(asset: asset5, dependent: asset6).save(failOnError:true)
			AssetDependency dep5 = new AssetDependency(asset: asset9, dependent: asset10).save(failOnError:true)
			AssetDependency dep6 = new AssetDependency(asset: asset9, dependent: asset11).save(failOnError:true)
			AssetDependency dep7 = new AssetDependency(asset: asset8, dependent: asset12).save(failOnError:true)
			AssetDependency dep8 = new AssetDependency(asset: asset14, dependent: asset15).save(failOnError:true)
		when: 'the dependency groups are generated'
			String connectionTypes = "'Unknown', 'Runs On', 'Hosts', 'DB', 'Web', 'Backup', 'File', 'FTP/SCP', 'Replacement', 'Network', 'Power', 'Virtual Desktop'"
			String statusTypes = "'Unknown', 'Validated', 'Questioned', 'Future'"
			moveBundleService.generateDependencyGroups(project.id, connectionTypes, statusTypes, null, userLogin.getUsername(), null)
		then: 'test that there is a group 0 composed of assets 1, 2, 3 and 4'
			List groupZero = AssetDependencyBundle.findAllByDependencyBundle(0)
			assert [asset1.id, asset2.id, asset3.id, asset4.id] == groupZero.collect { it.asset.id }
		and: 'test that there is a group 1 composed of assets 5 and 6'
			List groupOne = AssetDependencyBundle.findAllByDependencyBundle(1)
			assert [asset5.id, asset6.id] == groupOne.collect { it.asset.id }
		and: 'test that there is a group 2 composed of assets 9 and 10'
			List groupTwo = AssetDependencyBundle.findAllByDependencyBundle(2)
			assert [asset9.id, asset10.id] == groupTwo.collect { it.asset.id }
		and: 'test that the Straggler group contains assets 7 and 8'
			List groupStraggler = AssetDependencyBundle.findAllByDependencySource('Straggler')
			assert [asset7.id, asset8.id] == groupStraggler.collect { it.asset.id }
	}
}
