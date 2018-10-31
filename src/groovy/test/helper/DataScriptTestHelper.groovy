package test.helper

import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang3.RandomStringUtils

/**
 * Use this class to create datasource domain objects in your tests
 */
class DataScriptTestHelper {

    /**
     * Create a default datasource domain object
     * @param project
     * @param provider
     * @param createdBy
     * @return
     */
    DataScript createDataScript(Project project, Provider provider, Person createdBy, String etlSourceCode = '', Map etlData = null) {
        DataScript existingDs
        // Because E2E project use to insert or update at database and BE integrations just rollback
        // first from E2E we need to check if it exists to avoid a broken integration test on creation
        if (etlData) {
            existingDs = DataScript.findWhere([name: etlData.name, project: project])
            if (existingDs) {
                existingDs.etlSourceCode = ''
                existingDs.dateCreated = new Date()
                existingDs.lastUpdated = existingDs.dateCreated
                existingDs.save(flush: true, failOnError: true)
                return existingDs
            }
        }

        DataScript dataScript = new DataScript(
                name: etlData.name ? etlData.name : 'Test DataScript-' + RandomStringUtils.randomAlphabetic(10),
                description: etlData.description ? etlData.description :'Test description',
                target: 'Test target', 
                etlSourceCode: etlSourceCode.trim(),
                project: project,
                provider: provider,
                mode: DataScriptMode.IMPORT,
                createdBy: createdBy
        ).save(flush: true, failOnError: true)
        return dataScript
    }
}
