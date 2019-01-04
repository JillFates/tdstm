import com.tdsops.tm.enums.domain.Color
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagEvent
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TagEventService
import net.transitionmanager.service.TagService
import spock.lang.Shared
import test.helper.MoveEventTestHelper

class TagEventServiceIntegrationSpec extends IntegrationSpec {
	TagService      tagService
	TagEventService tagEventService

	@Shared
	MoveEventTestHelper moveEventTestHelper = new MoveEventTestHelper()

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
	MoveEvent event

	/**
	 * a device in moveBundle(usedForPlanning = 1)
	 */
	@Shared
	MoveEvent event2

	/**
	 * a device in moveBundle2(usedForPlanning = 0)
	 */
	@Shared
	MoveEvent event3

	@Shared
	Map context

	@Shared
	Tag tag1

	@Shared
	Tag tag2

	@Shared
	Tag tag3

	@Shared
	TagEvent tagEvent1

	@Shared
	TagEvent tagEvent2

	@Shared
	TagEvent tagEvent3

	@Shared
	TagEvent tagEvent4

	@Shared
	Date now

	void setup() {
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		moveBundle2 = moveBundleTestHelper.createBundle(otherProject, null)

		event = moveEventTestHelper.createMoveEvent(project)
		event2 = moveEventTestHelper.createMoveEvent(project)
		event3 = moveEventTestHelper.createMoveEvent(otherProject)

		tag1 = new Tag(name: 'grouping events', description: 'This is a description', color: Color.Green, project: project).save(flush: true, failOnError: true)
		tag2 = new Tag(name: 'some events', description: 'Another description', color: Color.Blue, project: project).save(flush: true, failOnError: true)
		tag3 = new Tag(name: 'other', description: 'Yet another description', color: Color.Red, project: otherProject).save(flush: true, failOnError: true)

		tagEvent1 = new TagEvent(tag: tag1, event: event).save(flush: true, failOnError: true)
		tagEvent2 = new TagEvent(tag: tag1, event: event2).save(flush: true, failOnError: true)
		tagEvent3 = new TagEvent(tag: tag2, event: event2).save(flush: true, failOnError: true)
		tagEvent4 = new TagEvent(tag: tag3, event: event3).save(flush: true, failOnError: true)

		tagService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		tagEventService.securityService = [
			getUserCurrentProject  : { -> project },
			getUserCurrentProjectId: { -> "$project.id".toString() }
		] as SecurityService

		now = TimeUtil.nowGMT().clearTime()
	}

	void 'Test list'() {
		when: 'Getting a list of tagEvents by event'
			List<TagEvent> tagEvents = tagEventService.list(project, event2.id)

		then: 'a list of tagEvents are returned for the event'
			tagEvents.size() == 2
			tagEvents[0].tag == tag1
			tagEvents[1].tag == tag2
	}

	void 'Test list with event from another project'() {
		when: 'trying to get a list of tagEvents from an event, that belongs to another project'
			tagEventService.list(project, event3.id)

		then: 'An exception is thrown'
			thrown EmptyResultException
	}

	void 'Test getTags'() {
		when: 'getting tags based on an event'
			List<TagEvent> tagEvents = tagEventService.getTags(project, event2.id)

		then: 'a list of tags is returned'
			tagEvents.size() == 2
			tagEvents[0] == tag1
			tagEvents[1] == tag2
	}

	void 'Test getTags with event from another project'() {
		when: 'trying to get a list of tags, from an event, that belongs to another project'
			tagEventService.getTags(project, event3.id)

		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test add tags'() {
		when: 'adding a tag(s) to an event'
			List<TagEvent> tagEvents = tagEventService.applyTags(project, [tag2.id], event.id)

		then: 'a list of tagEvents is returned'
			tagEvents.size() == 1
			tagEvents[0].tag == tag2
	}

	void 'Test add tag from another project'() {
		when: 'trying to add tags from another project to an event'
			tagEventService.applyTags(project, [tag3.id], event.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test add tag to an event from another project'() {
		when: 'trying to add tags to an event from another project'
			tagEventService.applyTags(project, [tag1.id], event3.id)
		then: 'an exception is thrown'
			thrown EmptyResultException
	}

	void 'Test remove tags'() {
		when: 'removing a tagEvent, from an event, and checking the list of tagEvents'
			tagEventService.removeTags(project, [tagEvent3.id])
			List<TagEvent> tagEvents = tagEventService.list(project, event2.id)
		then: 'The list of tagEvents will not contain the delete tagEvent'
			tagEvents.size() == 1
			tagEvents[0].tag == tag1
	}

	void 'Test remove tags from another project'() {
		when: 'trying to remove a tagEvent from an event that belongs to another project'
			tagEventService.removeTags(project, [tagEvent4.id])
		then: 'an exception it thrown'
			thrown EmptyResultException
	}

	void 'Test event cascade delete'() {
		when: 'deleting an event'
			event.delete(flush: true)
			tagEventService.getTags(project, event.id)
		then: 'all related tagEvents are deleted'
			thrown EmptyResultException
	}

	void 'Test tag cascade delete'() {
		when: 'deleting a tag'
			tag1.delete(flush: true)
			List<TagEvent> tagEvents = tagEventService.list(project, event.id)
			List<TagEvent> tagEvents2 = tagEventService.list(project, event2.id)
		then: 'all related tagEvents are deleted'
			tagEvents.size() == 0
			tagEvents2.size() == 1
	}
}
