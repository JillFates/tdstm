package test.helper

import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONObject

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