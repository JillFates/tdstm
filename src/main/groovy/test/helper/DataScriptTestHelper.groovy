package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang3.RandomStringUtils

/**
 * Use this class to create datasource domain objects in your tests
 */
@Transactional
class DataScriptTestHelper {

    /**
     * Create a default datasource domain object
     * @param project
     * @param provider
     * @param createdBy
     * @return
     */
    DataScript createDataScript(Project project, Provider provider, Person createdBy, String etlSourceCode = '') {
        DataScript dataScript = new DataScript(
                name: 'Test DataScript-' + RandomStringUtils.randomAlphabetic(10),
                description: 'Test description',
                target: 'Test target', 
                etlSourceCode: etlSourceCode.trim(),
                project: project,
                provider: provider,
                mode: DataScriptMode.IMPORT,
                createdBy: createdBy
        ).save(flush: true)
        return dataScript
    }

    /**
     * Create a etl script if not exists from given Map for E2EProjectSpec to persist at server DB
     * @param: [REQUIRED] etlData = [name: String]
     * @param: project
     * @param: provider
     * @param: createdBy = Person
     * @returm the etl script
     */
    DataScript createDataScript(Project project, Provider provider, Person createdBy, Map etlData, String etlSourceCode = '') {
        DataScript existingDs = DataScript.findWhere([name: etlData.name, project: project])
        if (existingDs) {
            existingDs.etlSourceCode = ''
            existingDs.dateCreated = new Date()
            existingDs.lastUpdated = existingDs.dateCreated
            existingDs.save(flush: true)
            return existingDs
        } else {
            DataScript dataScript = new DataScript(
                    name: etlData.name,
                    description: etlData.description ? etlData.description :'Test description',
                    target: 'Test target',
                    etlSourceCode: etlSourceCode.trim(),
                    project: project,
                    provider: provider,
                    mode: DataScriptMode.IMPORT,
                    createdBy: createdBy
            ).save(flush: true)
            return dataScript
        }
    }
}
