--
-- GetApplicationDownUp 
--
-- This procedure is used to get a list of applications for a move event and return if and when the application was shutdown and restarted
--
-- Parameters:
-- 		projectId - the id of the project
-- 		eventName - the name of the event
--		downMatch - the string match used to find the app shutdown task by title using a SQL LIKE statement (e.g. App % is shutdown)
--		upMatch - the string match used to find the app started up task by title using a SQL LIKE statement (e.g. App % finished testing)
--
-- Usage:
-- 		CALL GetApplicationDownUp(2468, 'MG01', 'Initiate shutdown of %', 'App % is tested');

DROP PROCEDURE IF EXISTS GetApplicationDownUp;
DELIMITER //
CREATE PROCEDURE GetApplicationDownUp(IN projectId INT, IN eventName VARCHAR(255), IN downMatch VARCHAR(255), IN upMatch VARCHAR(255) )
    BEGIN
		DECLARE eventId INT;
		SELECT move_event_id INTO eventId FROM move_event WHERE name = eventName; 

		DROP TEMPORARY TABLE IF EXISTS apps;

		CREATE TEMPORARY TABLE apps AS (
			SELECT a. asset_entity_id AS id, 
				a.external_ref_id AS ref_id, 
				a.asset_name as name, 
				false as down, time(null) as down_at,
				false as up, time(null) as up_at
			FROM asset_entity a
			JOIN move_bundle mb ON a.move_bundle_id = mb.move_bundle_id
			WHERE mb.move_event_id=eventId AND a.asset_class='Application'
		);
			
		UPDATE apps SET down = ( 
			COALESCE(
				( SELECT status IN ('Started', 'Completed') 
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE downMatch
				), false 
			)
		);

		UPDATE apps SET down_at = ( 
			COALESCE(
				( SELECT IF(status IN ('Started', 'Completed'), task.date_resolved, null)
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE downMatch
				), null
			)
		);

		UPDATE apps SET up = ( 
			COALESCE(
				( SELECT status='Completed' 
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE upMatch
				), false
			)
		);

		UPDATE apps SET up_at = ( 
			COALESCE(
				( SELECT IF(status='Completed', task.date_resolved, null) 
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE upMatch
				), null
			)
		);

		SELECT * from apps;
		DROP TEMPORARY TABLE apps;
    END //
DELIMITER ;