import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
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

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagAsset tagAsset1

	@Shared
	TagAsset tagAsset2

	@Shared
	TagAsset tagAsset3

	@Shared
	TagAsset tagAsset4

	@Shared
	Date now

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(project, null)

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle2)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Black, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true, failOnError: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true, failOnError: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true, failOnError: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true, failOnError: true)

		tagService.securityService = [getUserCurrentProject: { -> project }, assertCurrentProject : { Project project -> }] as SecurityService
		now = TimeUtil.nowGMT().clearTime()
	}

	void "test list with no parameters"() {
		when: 'Calling the list method with no parameters'
			List results = tagService.list()
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
			results[1].DateCreated == now
			results[1].LastModified == now
	}

	void "test list with full name"() {
		when: 'Calling the list method with the full name of a tag'
			List results = tagService.list('grouping assets')
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with partial name"() {
		when: 'Calling the list method with a partial name of a tag'
			List results = tagService.list('group')
		then: 'We get a list of map results'
			results[0].id == tag1.id
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
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
			results[0].Color == Color.Blue.name()
			results[0].css == Color.Blue.css
			results[0].Name == 'some assets'
			results[0].Description == 'Another description'
			results[0].Assets == 1
			results[0].DateCreated == now
			results[0].LastModified == now
	}

	void "test list with partial description"() {
		when: 'Calling the list method with a partial description parameter'
			List results = tagService.list('', 'Anot')
		then: 'We get a list of map results'
			results[0].id == tag2.id
			results[0].Color == Color.Blue.name()
			results[0].css == Color.Blue.css
			results[0].Name == 'some assets'
			results[0].Description == 'Another description'
			results[0].Assets == 1
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
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
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
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
			results[0].DateCreated == now
			results[0].LastModified == now

			results[1].id == tag2.id
			results[1].Color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].Name == 'some assets'
			results[1].Description == 'Another description'
			results[1].Assets == 1
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
			results[0].Color == Color.Black.name()
			results[0].css == Color.Black.css
			results[0].Name == 'grouping assets'
			results[0].Description == 'This is a description'
			results[0].Assets == 2
			results[0].DateCreated == now
			results[0].LastModified == now
	}
}