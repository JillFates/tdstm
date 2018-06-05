import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagLink
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TagService
import spock.lang.Shared
import test.helper.ApplicationTestHelper
import test.helper.AssetEntityTestHelper

class TagServiceIntegrationSpec extends IntegrationSpec {
	TagService tagService

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	ApplicationTestHelper applicationTestHelper = new ApplicationTestHelper()

	@Shared
	DataImportService dataImportService

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Project otherProject = projectTestHelper.createProject()

	/**
	 * A move bundle that is usedForPlanning = 1
	 */
	@Shared
	MoveBundle moveBundle

	/**
	 * A move bundle that is usedForPlanning = 0
	 */
	@Shared
	MoveBundle moveBundle2

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetEntity device2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetEntity device3

	/**
	 * an application in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	Application application1

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	Application application2

	@Shared
	AssetEntity otherProjectDevice

	@Shared
	Map context

	/**
	 * a dependency that has it's asset and dependent in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetDependency dependency1

	/**
	 * a dependency that has it's asset in moveBundle2(usedForPlanning = 0) and dependent in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	AssetDependency dependency2

	/**
	 * a dependency that has it's asset in moveBundle(usedForPlanning = 1) and dependent in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	AssetDependency dependency3

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagLink tagLink1

	@Shared
	TagLink tagLink2

	@Shared
	TagLink tagLink3


	@Shared
	TagLink tagLink4

	@Shared
	TagLink tagLink5

	@Shared
	Date now

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(project, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle2)

		application1 = applicationTestHelper.createApplication(AssetClass.APPLICATION, project, moveBundle)
		application2 = applicationTestHelper.createApplication(AssetClass.APPLICATION, project, moveBundle2)

		otherProjectDevice = assetEntityTestHelper.createAssetEntity(
			AssetClass.DEVICE,
			otherProject,
			moveBundleTestHelper.createBundle(otherProject, null)
		)

		context = dataImportService.initContextForProcessBatch(project, ETLDomain.Dependency)
		context.record = new ImportBatchRecord(sourceRowId: 1)

		device.assetType = 'Server'
		device.validation = 'BundleReady'
		device.save(flush: true, failOnError: true)

		device2.assetType = 'Server'
		device2.validation = 'Discovery'
		device2.save(flush: true, failOnError: true)

		moveBundle2.useForPlanning = 0
		moveBundle2.save(flush: true, failOnError: true)

		// Create a second project with a device with the same name and type as device above
		otherProjectDevice.assetName = device.assetName
		otherProjectDevice.assetType = device.assetType
		otherProjectDevice.validation = 'Discovery'
		otherProjectDevice.save(flush: true, failOnError: true)

		dependency1 = new AssetDependency(asset: application1, dependent: device, status: AssetDependencyStatus.VALIDATED).save(flush: true, failOnError: true)
		dependency2 = new AssetDependency(asset: application2, dependent: device2, status: AssetDependencyStatus.VALIDATED).save(flush: true, failOnError: true)
		dependency3 = new AssetDependency(asset: application1, dependent: device3, status: AssetDependencyStatus.VALIDATED).save(flush: true, failOnError: true)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Black, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagLink1 = new TagLink(tag: tag1, domainId: device.id, domain: ETLDomain.Device).save(flush: true, failOnError: true)
		tagLink2 = new TagLink(tag: tag2, domainId: device2.id, domain: ETLDomain.Device).save(flush: true, failOnError: true)
		tagLink3 = new TagLink(tag: tag3, domainId: device3.id, domain: ETLDomain.Device).save(flush: true, failOnError: true)
		tagLink4 = new TagLink(tag: tag1, domainId: dependency1.id, domain: ETLDomain.Dependency).save(flush: true, failOnError: true)
		tagLink5 = new TagLink(tag: tag3, domainId: application2.id, domain: ETLDomain.Application).save(flush: true, failOnError: true)

		tagService.securityService = [getUserCurrentProject: { -> project }] as SecurityService
		now = TimeUtil.nowGMT().clearTime()
	}

	void "test list with no parameters"() {
		when: 'Calling the list method with no parameters'
			List results = tagService.list()
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
			results[1].Dependencies == 0
			results[1].Tasks == 0
			results[1].DateCreated == now
			results[1].LastModified == now
	}

	void "test list with full name"() {
		when: 'Calling the list method with the full name of a tag'
			List results = tagService.list('grouping assets')
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with partial name"() {
		when: 'Calling the list method with a partial name of a tag'
			List results = tagService.list('group')
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with a name not in the db"() {
		when: 'Calling the list method with a tag name not in the db'
			List results = tagService.list('!@#@$!')
		then: 'We get an empty list'
			!results
	}


	void "test list with full description"() {
		when: 'Calling the list method with a full description parameter'
			List results = tagService.list('', 'Another description')
		then: 'We get a list of map results'
			results[0].id == tag2.id
			results[0].Color == Color.Blue
			results[0].css == Color.Blue.css
			results[0].Name == 'some assets'
			results[0].Description == 'Another description'
			results[0].Assets == 1
			results[0].Dependencies == 0
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with partial description"() {
		when: 'Calling the list method with a partial description parameter'
			List results = tagService.list('', 'Anot')
		then: 'We get a list of map results'
			results[0].id == tag2.id
			results[0].Color == Color.Blue
			results[0].css == Color.Blue.css
			results[0].Name == 'some assets'
			results[0].Description == 'Another description'
			results[0].Assets == 1
			results[0].Dependencies == 0
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with a description not in the db"() {
		when: 'Calling the list method with a description that does not match any in the db'
			List results = tagService.list('', 'This will not match.')
		then: 'We get an empty list'
			!results
	}


	void "test list with dateCreated"() {
		when: 'Calling the list method with a date created parameter'
			List results = tagService.list('', '', now)
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
			results[1].Dependencies == 0
			results[1].Tasks == 0
			results[1].DateCreated == now
			results[1].LastModified == now
	}

	void "test list with dateCreated tomorrow"() {
		when: 'Calling the list method with a date created parameter that does not match any tag'
			List results = tagService.list('', '', now + 1)
		then: 'We get an empty list'
			!results
	}

	void "test list with lastUpdated"() {
		when: '\'Calling the list method with a lastUpdated parameter'
			List results = tagService.list('', '', null, now)
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
			results[1].Dependencies == 0
			results[1].Tasks == 0
			results[1].DateCreated == now
			results[1].LastModified == now
	}

	void "test list with lastUpdated tomorrow"() {
		when: 'Calling the list method with a last updated parameter that does not match any tag '
			List results = tagService.list('', '', null, now + 1)
		then: 'We get an empty list'
			!results
	}


	void "test list with all parameters"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list('group', 'this is', now, now)
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 1
			results[0].Dependencies == 1
			results[0].Tasks == 0
			results[0].DateCreated == now
			results[0].LastModified == now
	}
}