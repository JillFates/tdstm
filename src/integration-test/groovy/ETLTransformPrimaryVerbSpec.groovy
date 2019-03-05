import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorResult
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import grails.gorm.transactions.Rollback
import grails.testing.gorm.DataTest
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
import net.transitionmanager.service.FileSystemService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class ETLTransformPrimaryVerbSpec extends Specification implements DataTest, AutowiredTest {

	@Autowired
	FileSystemService fileSystemService

	void setup() {
		assert fileSystemService != null
	}

	void 'test can transform local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
			AssetDependencyId,AssetId,AssetName,AssetType,DependentId,DependentName,DependentType,Type
			1,151954,ACMEVMPROD01,VM,152402,VMWare Vcenter,Application,Hosts
		""")

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
			}

		cleanup:
			deleteTemporaryFile(fileName)
	}

	/**
	 *
	 * @param fileName
	 */
	void deleteTemporaryFile(String fileName) {
		if (fileName) {
			fileSystemService.deleteTemporaryFile(fileName)
		}
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	protected List buildCSVDataSet(String csvContent) {
		def (String fileName, OutputStream dataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		dataSetOS << csvContent
		dataSetOS.close()

		String fullName = fileSystemService.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}
}
