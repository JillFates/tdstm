--
-- This script is used to prune all of the data except for the lookup tables, the default project and TDS users
--

SET foreign_key_checks = 0;

-- Asset data
TRUNCATE TABLE app_move_event;
TRUNCATE TABLE application;
TRUNCATE TABLE data_base;
TRUNCATE TABLE files;
TRUNCATE TABLE application_asset_map;
TRUNCATE TABLE asset_cable_map;
TRUNCATE TABLE comment_note;
TRUNCATE TABLE task_dependency;
TRUNCATE TABLE asset_comment;
TRUNCATE TABLE asset_dependency;
TRUNCATE TABLE asset_dependency_bundle;

-- Move bundle and Event
TRUNCATE TABLE move_bundle_step;
TRUNCATE TABLE move_bundle;
TRUNCATE TABLE move_event_staff;
TRUNCATE TABLE move_event_snapshot;
TRUNCATE TABLE move_event_news;
TRUNCATE TABLE move_event;
TRUNCATE TABLE rack;
DELETE FROM room;
TRUNCATE TABLE step_snapshot;

TRUNCATE TABLE asset_transition;
TRUNCATE TABLE data_transfer_batch;
TRUNCATE TABLE data_transfer_value;

TRUNCATE TABLE project_asset_map;

TRUNCATE TABLE asset_entity;
TRUNCATE TABLE eav_entity;


TRUNCATE TABLE manufacturer_sync;
TRUNCATE TABLE model_connector_sync;
TRUNCATE TABLE model_sync;
TRUNCATE TABLE model_sync_batch;
TRUNCATE TABLE project_team;

--
-- Can't delete Default project and TDS employees
--

SET @DEF_PROJECT_ID = 2;
SET @TDS_CO_ID=18;
SET @COMPANY_IDS = '3,18';
SET @STAFF_IDS =
        ( SELECT GROUP_CONCAT(distinct party_id_to_id) from party_relationship
          WHERE party_relationship_type_id='STAFF' AND party_id_from_id=18 );
SET @USER_IDS =
        ( SELECT GROUP_CONCAT(distinct party_id_to_id) from party_relationship
          WHERE party_relationship_type_id='STAFF' AND party_id_from_id=18 );
SET @WF_ID=(SELECT workflow_id FROM workflow WHERE process = 'STD_PROCESS' );

-- Concat the various ids together
SET @GROUP_IDS=concat(@DEF_PROJECT_ID,',',@COMPANY_IDS);
SET @PARTY_IDS=concat(@DEF_PROJECT_ID,',',@COMPANY_IDS,',',@STAFF_IDS);

--
-- Start weeding out stuff
--
DELETE FROM field_importance WHERE project_id <> @DEF_PROJECT_ID;
DELETE FROM user_login WHERE NOT FIND_IN_SET(person_id, @STAFF_IDS);
DELETE FROM person WHERE NOT FIND_IN_SET(person_id, @STAFF_IDS);
DELETE FROM party_group WHERE NOT FIND_IN_SET(party_group_id, @GROUP_IDS);
DELETE FROM party_relationship WHERE NOT FIND_IN_SET(party_id_from_id, @COMPANY_IDS) AND party_relationship_type_id <> 'STAFF';
DELETE FROM party WHERE NOT FIND_IN_SET(party_id, @PARTY_IDS);
DELETE FROM project WHERE project_id <> @DEF_PROJECT_ID;
DELETE FROM project_logo WHERE project_id <> @PROJECT_ID;
DELETE FROM user_preference WHERE NOT FIND_IN_SET(user_login_id, @USER_IDS) OR preference_code = 'CURR_PROJ';

DELETE FROM workflow_transition WHERE NOT FIND_IN_SET(workflow_id, @WF_ID);
DELETE FROM workflow_transition_map WHERE NOT FIND_IN_SET(workflow_id, @WF_ID);
DELETE FROM workflow WHERE NOT FIND_IN_SET(workflow_id, @WF_ID);

-- Delete relationships that no longer exist
DELETE FROM party_relationship where party_id_from_id=@TDS_CO_ID and party_relationship_type_id='CLIENTS';
DELETE FROM party_relationship where party_id_from_id=@TDS_CO_ID and party_relationship_type_id='PARTNERS';

-- Additional cleanup
delete from party where party_id=1;
delete from person where person_id=1;
update party set party_type_id='PERSON' where party_type_id is null;
update party set party_type_id='COMPANY' where party_id=2;
delete from user_login where active='N' or last_login is null;
delete from user_preference;
insert into user_preference (select user_login_id, 'CURR_PROJ', 2 from user_login);
-- 
DELETE FROM key_value 
	WHERE key_value.project_id NOT IN 
		(SELECT project_id FROM project);

SET foreign_key_checks = 1;
