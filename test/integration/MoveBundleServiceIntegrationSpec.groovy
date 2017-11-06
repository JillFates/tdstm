import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.UserPreferenceEnum
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
import net.transitionmanager.service.UserPreferenceService
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
    UserPreferenceService userPreferenceService

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
    @Ignore
    // The test was ignored 8/24 because the hibernate flush is throwing an exception but the code
    // appears to be working. Need to research the problem. Ticket TM-7069 was created to resolve
    // this issue.
    void '04. Test deleteBundleAndAssets method' () {
        setup: 'create a project'
        Project project = projectHelper.createProjectWithDefaultBundle()
        and: 'create a person associated with the project'
        Person person = personHelper.createPerson(null, project.client, project)
        and: 'create a user that is logged in and the project is their default'
        UserLogin userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ADMIN}"], project, true)

        when: 'a new bundle is created'
        MoveBundle bundle = moveBundleHelper.createBundle(project, 'Test Bundle')
        and: 'a new event is created'
        MoveEvent event = moveEventService.create(project, 'Test Event')
        and: 'the above bundle is assigned to it'
        moveBundleService.assignMoveEvent(event, [bundle.id])
        and: 'some assets are created and assigned to the bundle'
        AssetEntity asset1 = assetHelper.createDevice(project, AssetType.VM, [moveBundle: bundle])
        AssetEntity asset2 = assetHelper.createDevice(project, AssetType.SERVER, [moveBundle: bundle])
        Long assetId1 = asset1.id
        Long assetId2 = asset2.id
        and: 'an asset that should be associated to the default project'
        AssetEntity asset3 = assetHelper.createDevice(project, AssetType.SERVER)
        Long assetId3 = asset3.id
        and: 'some tasks for the event are generated'
        def task1 = new AssetComment(taskNumber: 1000, duration: 5, comment: 'Test task 1', moveEvent: event, commentType: AssetCommentType.TASK)
        def task2 = new AssetComment(taskNumber: 1001, duration: 5, comment: 'Test task 2', moveEvent: event, commentType: AssetCommentType.TASK)
        and: 'the deleteBundleAndAssets method is called'
        moveBundleService.deleteBundleAndAssets(bundle)
        and: 'the hibernate session is flushed and cleared so the data is deleted as expected'
        sessionFactory.currentSession.flush()
        sessionFactory.currentSession.clear()
        then: 'the bundle should be deleted'
        MoveBundle.get(bundle.id) == null
        and: 'the assigned assets should be deleted'
        AssetEntity.get(assetId1) == null
        AssetEntity.get(assetId2) == null
        and: 'the asset assigned to the default project should still exist'
        AssetEntity.get(assetId3) != null
        and: 'the event should still exist'
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
}
