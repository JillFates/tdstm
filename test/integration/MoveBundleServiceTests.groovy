import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.MoveBundleService

import spock.lang.Specification

class MoveBundleServiceTests extends Specification {

    MoveBundleService moveBundleService

    private ProjectTestHelper projectHelper = new ProjectTestHelper()
    private MoveBundleTestHelper moveBundleHelper = new MoveBundleTestHelper()

    void "Test Move Bundle lookup with default configurations" () {

        given: "Two projects with some bundles assigned to each one"
            Project project1 = projectHelper.createProject()
            Project project2 = projectHelper.createProject()
            MoveBundle bundle1 = moveBundleHelper.createBundle(project1)
            MoveBundle bundle2 = moveBundleHelper.createBundle(project1)
            MoveBundle bundle3 = moveBundleHelper.createBundle(project2)
        when: "Bundles for the first project are retrieved with no projection fields specified"
            List bundlesForP1 = moveBundleService.lookupBundlesByProject(project1)
        then: "The list should have two results"
            bundlesForP1.size() == 2
        and: "The bundles are correctly sorted (considering that Java is case sensitive)"
            bundlesForP1[0].name.toUpperCase() < bundlesForP1[1].name.toUpperCase()
        and: "Only Bundle1 and Bundle2 were retrieved"
            bundlesForP1.each{
                it.name == bundle1.name || it.name == bundle2.name
            }
    }

    void "Test Move Bundle Lookup with specific projection fields and sorting criteria" () {
        given: "A project with a couple of bundles"
            Project project = projectHelper.createProject()
            moveBundleHelper.createBundle(project)
            moveBundleHelper.createBundle(project)
        when: "Requesting id and workflowCode and results sorted by id"
            List bundles = moveBundleService.lookupBundlesByProject(project, ["id", "workflowCode"], "id")
        then: "The results don't include the name of the record (a field not requested)"
            bundles.each{
                !it.name
            }
        and: "The results are sorted correctly"
            bundles[0].id < bundles[1].id

    }

    void "Test Move Bundle Lookup for a project with no bundles" () {
        given: "A project with no bundles"
            Project project = projectHelper.createProject()
        when: "Requesting bundles with default config."
            List bundles = moveBundleService.lookupBundlesByProject(project)
        then: "The size of the list of bundles should 0."
            bundles.size() == 0

    }

}