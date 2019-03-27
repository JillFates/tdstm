package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.json.JSONObject

/**
 * Helper class to create Dataview instances during test
 */
@Transactional
class DataviewTestHelper {

    Dataview createDataview(Project project) {
        Dataview dataview = new Dataview()
        dataview.project = project
        dataview.name = RandomStringUtils.randomAlphanumeric(10)
        dataview.isSystem = true
        dataview.isShared = true
        dataview.reportSchema = '{reportSchema}'
        dataview.save(flush: true)
        return dataview
    }

    /**
     * Create a custom View if not exists from given Map for E2EProjectSpec to persist at server DB
     * @param: [REQUIRED] assetData = [name: String, isShared: boolean, isSystem: boolean, schema: JSONObject]
     * @param: project
     * @param: person
     * @returm the view
     */
    Dataview createDataview(Project currentProject, Person currentPerson, JSONObject dataviewJson) {
        Dataview existingDataview = Dataview.findWhere([name: dataviewJson.name, project: currentProject])
        if (!existingDataview){
            Dataview dataview = new Dataview()
            dataview.with {
                project = currentProject
                person = currentPerson
                name = dataviewJson.name
                isShared = dataviewJson.isShared
                isSystem = dataviewJson.isSystem
                reportSchema = dataviewJson.schema
            }
            dataview.save(flush: true)
            return dataview
        } else {
            return existingDataview
        }
    }
}
