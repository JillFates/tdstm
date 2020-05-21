package net.transitionmanager.task.taskgraph

import net.transitionmanager.command.task.TaskSearchCommand
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import spock.lang.Shared
import spock.lang.Specification

class TaskSearchSpec extends Specification {

    @Shared
    MoveEvent event
    @Shared
    Project project
    @Shared
    TaskSearchCommand command

    void 'test can build a query for all fields null'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand()

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'
            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId 
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
            }
    }

    void 'test can build a query for assignedPersonId unassigned'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(assignedPersonId: 0l)

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId 
                AND TASK.assigned_to_id is null
            """

        and: 'results contains the correct amount of query params'
            with(results.params) {
                eventId == this.event.id
            }
    }

    void 'test can build a query for assignedPersonId assigned to a specific person'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(assignedPersonId: 123l)

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId 
                AND TASK.assigned_to_id = :assignedPersonId
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
                assignedPersonId == 123l
            }
    }

    void 'test can build a query for teamCode UNASSIGNED'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(teamCode: 'UNASSIGNED')

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId
                AND TASK.role is NULL
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
            }
    }

    void 'test can build a query for teamCode assigned to a specific team/role'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(teamCode: 'SYSTEM_ADMIN')

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId
                AND TASK.role = :teamCode
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
                teamCode == 'SYSTEM_ADMIN'
            }
    }

    void 'test can build a query for ownerSmeId assigned to a specific person'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(ownerSmeId: 45676l)

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                LEFT OUTER JOIN application APPLICATION on ASSET_ENTITY.asset_entity_id = APPLICATION.app_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId
                AND (ASSET_ENTITY.app_owner_id = :ownerSmeId OR APPLICATION.sme_id = :ownerSmeId OR APPLICATION.sme2_id = :ownerSmeId)
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
                ownerSmeId == 45676l
            }
    }

    void 'test can build a query for ownerSmeId assigned to a specific environment'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(environment: 'Development')

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId
                AND ASSET_ENTITY.environment = :environment
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
                environment == 'Development'
            }
    }

    void 'test can build a query for tagIds with tagMatch ALL'() {

        setup: 'an instance of TaskSearchCommand correctly configured'
            setupData()
                    .withProject()
                    .withEvent()
                    .withTaskSearchCommand(tagIds: [6, 7], tagMatch: 'ALL')

        when: 'TaskSearch builds SearchQuery'
            Map results = new TaskSearch(project, command).buildSearchQuery(event, false)

        then: 'results contains a correct query'

            that results.query shouldBe """
                SELECT TASK.asset_comment_id as taskId 
                FROM asset_comment TASK 
                LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id 
                LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id 
                INNER JOIN (SELECT TAG_ASSET.asset_id FROM tag_asset TAG_ASSET WHERE TAG_ASSET.tag_id IN (:tagList) GROUP BY TAG_ASSET.asset_id HAVING COUNT(*) = :tagListSize) TAG ON (TASK.asset_entity_id = TAG.asset_id)
                WHERE TASK.comment_type = 'issue' 
                AND TASK.move_event_id = :eventId 
            """

        and: 'results contains the correct amount of query params'
            verifyAll(results.params) {
                eventId == this.event.id
                tagList == [6, 7]
            }
    }

    ////////////////// DATA BUILDER METHODS
    /**
     * Defines a {@}* @return
     */
    TaskSearchSpec withProject() {
        this.project = Mock(Project) {
            getId() >> 2445
        }
        return this
    }

    TaskSearchSpec withEvent() {
        this.event = Mock(MoveEvent) {
            getId() >> 21345l
            getProject() >> this.project
        }
        return this
    }

    TaskSearchSpec withTaskSearchCommand(Map<String, ?> params = [:]) {
        this.command = new TaskSearchCommand(params)
        return this
    }

    TaskSearchSpec setupData() {
        this.event == null
        this.project == null
        this.command == null
        return this
    }

    /**
     * Creates an instance of a Custom Assert for equals query results.
     * It compares a String query with an expectedQuery in {@link AssertQueryMatcher#shouldBe(java.lang.String)}
     * <pre>
     *   that results.query shouldBe """ EXPECTED QUERY """
     * </pre>
     *
     * @param query
     * @return an instance of {@link AssertQueryMatcher}
     */
    static AssertQueryMatcher that(String query) {
        return new AssertQueryMatcher(query)
    }

    static class AssertQueryMatcher {
        String query

        AssertQueryMatcher(String query) {
            this.query = query
        }

        void shouldBe(final String expectedQuery) {
            List<String> queryLines = query.stripIndent().trim().readLines()*.trim()
            List<String> queryExpectedLines = expectedQuery.stripIndent().trim().readLines()*.trim()
            assert queryLines.size() == queryExpectedLines.size()
            for (int i = 0; i < queryLines.size(); i++) {
                assert queryLines[i] == queryExpectedLines[i]
            }
        }
    }
}
