import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
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
	test.helper.MoveEventTestHelper moveEventTestHelper = new test.helper.MoveEventTestHelper()

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()

	@Shared
	Project otherProject = projectTestHelper.createProject()

	@Shared
	MoveEvent moveEvent

	@Shared
	MoveEvent moveEvent2

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

		moveEvent = moveEventTestHelper.createMoveEvent(project)
		moveEvent2 = moveEventTestHelper.createMoveEvent(project)

		moveBundle.moveEvent = moveEvent
		moveBundle.save()

		moveBundle2.moveEvent = moveEvent2
		moveBundle2.save()

		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device3 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle2)

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true, failOnError: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true, failOnError: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true, failOnError: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true, failOnError: true)

		tagService.securityService = [
			getUserCurrentProject: { -> project },
			assertCurrentProject : { Project project -> }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void "test list with no parameters"() {
		when: 'Calling the list method with no parameters'
			List results = tagService.list(project)
		then: 'We get a list of map results'
			results.size() == 2
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2

			results[1].id == tag2.id
			results[1].color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].name == 'some assets'
			results[1].description == 'Another description'
			results[1].assets == 1
	}

	void "test list with full name"() {
		when: 'Calling the list method with the full name of a tag'
			List results = tagService.list(project, 'grouping assets')
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2
	}

	void "test list with partial name"() {
		when: 'Calling the list method with a partial name of a tag'
			List results = tagService.list(project, 'group')
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2
	}

	void "test list with a name not in the db"() {
		when: 'Calling the list method with a tag name not in the db'
			List results = tagService.list(project, '!@#@$!')
		then: 'We get an empty list'
			!results
	}


	void "test list with full description"() {
		when: 'Calling the list method with a full description parameter'
			List results = tagService.list(project, '', 'Another description')
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag2.id
			results[0].color == Color.Blue.name()
			results[0].css == Color.Blue.css
			results[0].name == 'some assets'
			results[0].description == 'Another description'
			results[0].assets == 1
	}

	void "test list with partial description"() {
		when: 'Calling the list method with a partial description parameter'
			List results = tagService.list(project, '', 'Anot')
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag2.id
			results[0].color == Color.Blue.name()
			results[0].css == Color.Blue.css
			results[0].name == 'some assets'
			results[0].description == 'Another description'
			results[0].assets == 1
	}

	void "test list with a description not in the db"() {
		when: 'Calling the list method with a description that does not match any in the db'
			List results = tagService.list(project, '', 'This will not match.')
		then: 'We get an empty list'
			!results
	}


	void "test list with dateCreated"() {
		when: 'Calling the list method with a date created parameter'
			List results = tagService.list(project, '', '', now)
		then: 'We get a list of map results'
			results.size() == 2
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2

			results[1].id == tag2.id
			results[1].color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].name == 'some assets'
			results[1].description == 'Another description'
			results[1].assets == 1
	}

	void "test list with dateCreated tomorrow"() {
		when: 'Calling the list method with a date created parameter that does not match any tag'
			List results = tagService.list(project, '', '', now + 1)
		then: 'We get an empty list'
			!results
	}

	void "test list with lastUpdated"() {
		when: '\'Calling the list method with a lastUpdated parameter'
			List results = tagService.list(project, '', '', null, now)
		then: 'We get a list of map results'
			results.size() == 2
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2

			results[1].id == tag2.id
			results[1].color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].name == 'some assets'
			results[1].description == 'Another description'
			results[1].assets == 1
	}

	void "test list with lastUpdated tomorrow"() {
		when: 'Calling the list method with a last updated parameter that does not match any tag '
			List results = tagService.list(project, '', '', null, now + 1)
		then: 'We get an empty list'
			!results
	}


	void "test list with all parameters"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now)
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2
	}

	void "test list with moveBundleId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, [moveBundle.id])
		then: 'We get a list of map results'
			results.size() == 2
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2

			results[1].id == tag2.id
			results[1].color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].name == 'some assets'
			results[1].description == 'Another description'
			results[1].assets == 1
	}

	void "test list with other moveBundleId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, [moveBundle2.id])
		then: 'We get a list of map results'
			!results.size()
	}

	void "test list with all parameters plus moveBundleId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, [moveBundle.id])
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2
	}

	void "test list with all parameters plus moveBundleId that will filter out all results"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, [moveBundle2.id])
		then: 'We get a list of map results'
			!results
	}

	void "test list with moveEventId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, null, moveEvent.id)
		then: 'We get a list of map results'
			results.size() == 2
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2

			results[1].id == tag2.id
			results[1].color == Color.Blue.name()
			results[1].css == Color.Blue.css
			results[1].name == 'some assets'
			results[1].description == 'Another description'
			results[1].assets == 1
	}

	void "test list with other moveEventId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, null, moveEvent2.id)
		then: 'We get a list of map results'
			!results
	}

	void "test list with all parameters plus moveEventId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, null, moveEvent.id)
		then: 'We get a list of map results'
			results.size() == 1
			results[0].id == tag1.id
			results[0].color == Color.Green.name()
			results[0].css == Color.Green.css
			results[0].name == 'grouping assets'
			results[0].description == 'This is a description'
			results[0].assets == 2
	}

	void "test list with all parameters plus moveEventId that will filter out all results"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, null, moveEvent2.id)
		then: 'We get a list of map results'
			!results
	}
}