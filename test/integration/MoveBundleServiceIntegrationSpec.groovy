import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.MoveBundleService

import spock.lang.Specification

class MoveBundleServiceIntegrationSpec extends Specification {

    MoveBundleService moveBundleService

    private ProjectTestHelper projectHelper = new ProjectTestHelper()
    private MoveBundleTestHelper moveBundleHelper = new MoveBundleTestHelper()

    void 'Test lookupList with default parameters'() {

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

    void 'Test lookupList for specific fields and sorting criteria' () {
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

    void 'Test lookupList for a project with no bundles' () {
        given: 'A project with no bundles'
            Project project = projectHelper.createProject()

        when: 'calling lookupList for a project with no bundles'
            List result = moveBundleService.lookupList(project)
        then: 'the list should be empty'
            result.size() == 0
    }

}
