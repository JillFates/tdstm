import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.service.DataImportService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.tag.TagService
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import test.helper.ApplicationTestHelper
import test.helper.AssetEntityTestHelper

@Integration
@Rollback
class TagServiceIntegrationSpec extends  Specification{
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
	Project project

	@Shared
	Project otherProject

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
		project = projectTestHelper.createProject()
		otherProject = projectTestHelper.createProject()

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

		tag1 = new Tag(name: 'grouping assets', description: 'This is a description', color: Color.Green, project: project).save(flush: true)
		tag2 = new Tag(name: 'some assets', description: 'Another description', color: Color.Blue, project: project).save(flush: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true)

		tagAsset1 = new TagAsset(tag: tag1, asset: device).save(flush: true)
		tagAsset2 = new TagAsset(tag: tag1, asset: device2).save(flush: true)
		tagAsset3 = new TagAsset(tag: tag2, asset: device2).save(flush: true)
		tagAsset4 = new TagAsset(tag: tag3, asset: device3).save(flush: true)

		tagService.securityService = [
			getUserCurrentProject: { -> project },
			assertCurrentProject : { Project project -> }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void "1. Test list with no parameters"() {
		when: 'Calling the list method with no parameters'
			List results = tagService.list(project)
		then: 'We get a list of map results'
			results.size() == 6 // 2 tags (tag1 and tag2) plus the 4 default tags that every project has
		and: 'the first four are the default tags associated to every new Project'
			results[0].color == Color.Yellow.name()
			results[0].css == Color.Yellow.css
			results[0].name == 'GDPR'
			results[0].description == 'General Data Protection Regulation Compliance'
			results[0].assets == 0

			results[1].color == Color.Yellow.name()
			results[1].css == Color.Yellow.css
			results[1].name == 'HIPPA'
			results[1].description == 'Health Insurance Portability and Accountability Act Compliance'
			results[1].assets == 0

			results[2].color == Color.Yellow.name()
			results[2].css == Color.Yellow.css
			results[2].name == 'PCI'
			results[2].description == 'Payment Card Industry Data Security Standard Compliance'
			results[2].assets == 0

			results[3].color == Color.Yellow.name()
			results[3].css == Color.Yellow.css
			results[3].name == 'SOX'
			results[3].description == 'Sarbanes–Oxley Act Compliance'
			results[3].assets == 0
		and: 'the last two are the tags associated to the Project'
			results[4].id == tag1.id
			results[4].color == Color.Green.name()
			results[4].css == Color.Green.css
			results[4].name == 'grouping assets'
			results[4].description == 'This is a description'
			results[4].assets == 2

			results[5].id == tag2.id
			results[5].color == Color.Blue.name()
			results[5].css == Color.Blue.css
			results[5].name == 'some assets'
			results[5].description == 'Another description'
			results[5].assets == 1
	}

	void "2. Test list with full name"() {
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

	void "3. Test list with partial name"() {
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

	void "4. Test list with a name not in the db"() {
		when: 'Calling the list method with a tag name not in the db'
			List results = tagService.list(project, '!@#@$!')
		then: 'We get an empty list'
			!results
	}


	void "5. Test list with full description"() {
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

	void "6. Test list with partial description"() {
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

	void "7. Test list with a description not in the db"() {
		when: 'Calling the list method with a description that does not match any in the db'
			List results = tagService.list(project, '', 'This will not match.')
		then: 'We get an empty list'
			!results
	}


	void "8. Test list with dateCreated"() {
		when: 'Calling the list method with a date created parameter'
			List results = tagService.list(project, '', '', now)
		then: 'We get a list of map results'
			results.size() == 6 // 2 tags (tag1 and tag2) plus the 4 default tags that every project has
		and: 'the first four are the default tags associated to every new Project'
			results[0].color == Color.Yellow.name()
			results[0].css == Color.Yellow.css
			results[0].name == 'GDPR'
			results[0].description == 'General Data Protection Regulation Compliance'
			results[0].assets == 0

			results[1].color == Color.Yellow.name()
			results[1].css == Color.Yellow.css
			results[1].name == 'HIPPA'
			results[1].description == 'Health Insurance Portability and Accountability Act Compliance'
			results[1].assets == 0

			results[2].color == Color.Yellow.name()
			results[2].css == Color.Yellow.css
			results[2].name == 'PCI'
			results[2].description == 'Payment Card Industry Data Security Standard Compliance'
			results[2].assets == 0

			results[3].color == Color.Yellow.name()
			results[3].css == Color.Yellow.css
			results[3].name == 'SOX'
			results[3].description == 'Sarbanes–Oxley Act Compliance'
			results[3].assets == 0
		and: 'the last two are the tags associated to the Project'
			results[4].id == tag1.id
			results[4].color == Color.Green.name()
			results[4].css == Color.Green.css
			results[4].name == 'grouping assets'
			results[4].description == 'This is a description'
			results[4].assets == 2

			results[5].id == tag2.id
			results[5].color == Color.Blue.name()
			results[5].css == Color.Blue.css
			results[5].name == 'some assets'
			results[5].description == 'Another description'
			results[5].assets == 1
	}

	void "9. Test list with dateCreated tomorrow"() {
		when: 'Calling the list method with a date created parameter that does not match any tag'
			List results = tagService.list(project, '', '', now + 1)
		then: 'We get an empty list'
			!results
	}

	void "10. Test list with lastUpdated"() {
		when: 'Calling the list method with a lastUpdated parameter'
			List results = tagService.list(project, '', '', null, now)
		then: 'We get a list of map results'
			results.size() == 6 // 2 tags (tag1 and tag2) plus the 4 default tags that every project has
		and: 'the first four are the default tags associated to every new Project'
			results[0].color == Color.Yellow.name()
			results[0].css == Color.Yellow.css
			results[0].name == 'GDPR'
			results[0].description == 'General Data Protection Regulation Compliance'
			results[0].assets == 0

			results[1].color == Color.Yellow.name()
			results[1].css == Color.Yellow.css
			results[1].name == 'HIPPA'
			results[1].description == 'Health Insurance Portability and Accountability Act Compliance'
			results[1].assets == 0

			results[2].color == Color.Yellow.name()
			results[2].css == Color.Yellow.css
			results[2].name == 'PCI'
			results[2].description == 'Payment Card Industry Data Security Standard Compliance'
			results[2].assets == 0

			results[3].color == Color.Yellow.name()
			results[3].css == Color.Yellow.css
			results[3].name == 'SOX'
			results[3].description == 'Sarbanes–Oxley Act Compliance'
			results[3].assets == 0
		and: 'the last two are the tags associated to the Project'
			results[4].id == tag1.id
			results[4].color == Color.Green.name()
			results[4].css == Color.Green.css
			results[4].name == 'grouping assets'
			results[4].description == 'This is a description'
			results[4].assets == 2

			results[5].id == tag2.id
			results[5].color == Color.Blue.name()
			results[5].css == Color.Blue.css
			results[5].name == 'some assets'
			results[5].description == 'Another description'
			results[5].assets == 1
	}

	void "11. Test list with lastUpdated tomorrow"() {
		when: 'Calling the list method with a last updated parameter that does not match any tag '
			List results = tagService.list(project, '', '', null, now + 1)
		then: 'We get an empty list'
			!results
	}


	void "12. Test list with all parameters"() {
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

	void "13. Test list with moveBundleId"() {
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

	void "14. Test list with other moveBundleId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, [moveBundle2.id])
		then: 'We get a list of map results'
			!results.size()
	}

	void "15. Test list with all parameters plus moveBundleId"() {
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

	void "16. Test list with all parameters plus moveBundleId that will filter out all results"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, [moveBundle2.id])
		then: 'We get a list of map results'
			!results
	}

	void "17. Test list with moveEventId"() {
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

	void "18. Test list with other moveEventId"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, null, null, null, null, null, moveEvent2.id)
		then: 'We get a list of map results'
			!results
	}

	void "19. Test list with all parameters plus moveEventId"() {
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

	void "20. Test list with all parameters plus moveEventId that will filter out all results"() {
		when: 'Calling the list method with all parameters set'
			List results = tagService.list(project, 'group', 'this is', now, now, null, moveEvent2.id)
		then: 'We get a list of map results'
			!results
	}

	@See('TM-10847')
	void "21. Test Include Tags from the Default Project when creating a New Project"() {
		when: 'A new Project is created'
			Project newProject = projectTestHelper.createProject()
		then: 'the new Project gets cloned Tag copies from the Default Project Tags'
			Project defaultProject = Project.get(Project.DEFAULT_PROJECT_ID)
			List newProjectTags = tagService.list(newProject)
			List defaultProjectTags = tagService.list(defaultProject)

			newProjectTags.size() == defaultProjectTags.size()
			newProjectTags*.name.sort{} == defaultProjectTags*.name.sort{}
	}
}