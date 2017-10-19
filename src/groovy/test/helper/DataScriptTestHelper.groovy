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
    DataScript createDataScript(Project project, Provider provider, Person createdBy) {
        DataScript datascript = new DataScript(
                name: 'Test DataScript-' + RandomStringUtils.randomAlphabetic(10), 
                description: 'Test description',
                target: 'Test target', 
                etlSourceCode: '{blah}',
                project: project,
                provider: provider,
                mode: DataScriptMode.IMPORT,
                createdBy: createdBy
        ).save(flush: true, failOnError: true)
        return datascript
    }
}
