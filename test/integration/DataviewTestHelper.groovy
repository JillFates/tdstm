import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

/**
 * Helper class to create Dataview instances during test
 */
class DataviewTestHelper {

    DataviewTestHelper() {
    }

    Dataview createDataview(Project project) {
        Dataview dataview = new Dataview()
        dataview.project = project
        dataview.name = RandomStringUtils.randomAlphanumeric(10)
        dataview.isSystem = true
        dataview.isShared = true
        dataview.reportSchema = '{reportSchema}'
        dataview.save(flush: true, failOnError: true)
        return dataview
    }
}
