import net.transitionmanager.domain.Datasource
import net.transitionmanager.domain.DatasourceMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

/**
 * Use this class to create datasource domain objects in your tests
 */
class DatasourceTestHelper {

    /**
     * Create a default datasource domain object
     * @param project
     * @param provider
     * @param createdBy
     * @return
     */
    Datasource createDatasource(Project project, Provider provider, Person createdBy) {
        Datasource datasource = new Datasource(
                name: 'Test datasource', description: 'Test description',
                target: 'Test target', etlSourceCode: '{blah}',
                project: project,
                provider: provider,
                mode: DatasourceMode.EXPORT,
                createdBy: createdBy
        ).save(flush: true, failOnError: true)
        return datasource
    }
}
