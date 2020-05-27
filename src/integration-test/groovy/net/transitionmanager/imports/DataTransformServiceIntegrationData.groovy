package net.transitionmanager.imports

import net.transitionmanager.action.Provider
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import test.helper.AssetEntityTestHelper
import test.helper.DataScriptTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

trait DataTransformServiceIntegrationData {

    FileSystemService fileSystemService
    ProjectTestHelper projectTestHelper
    PersonTestHelper personTestHelper
    ProviderTestHelper providerTestHelper
    DataScriptTestHelper dataScriptTestHelper
    AssetEntityTestHelper assetEntityTestHelper
    String datasetFilename
    Person whom
    Project project
    Provider provider
    DataScript dataScript

    DataTransformServiceIntegrationData setupData() {
        personTestHelper = new PersonTestHelper()
        projectTestHelper = new ProjectTestHelper()
        providerTestHelper = new ProviderTestHelper()
        assetEntityTestHelper = new AssetEntityTestHelper()
        dataScriptTestHelper = new DataScriptTestHelper()

        whom = personTestHelper.createPerson()
        project = projectTestHelper.createProject()
        provider = providerTestHelper.createProvider(project)

        this.datasetFilename = null
        return this
    }

    DataTransformServiceIntegrationData withDataset(String fileContent, String extension = 'csv') {

        def (fileUploadName, os) = fileSystemService.createTemporaryFile('intTest', extension)
        os << fileContent.stripIndent().trim()
        os.close()
        this.datasetFilename = fileUploadName
        return this
    }

    DataTransformServiceIntegrationData withDataScript(String etlSourceCode = '', Boolean isAutoProcess = false) {

        dataScript = dataScriptTestHelper.createDataScript(
                project,
                provider,
                whom,
                etlSourceCode.stripIndent().trim(),
                isAutoProcess
        )

        return this
    }

}