--
-- Procedure GetApplicationDownUp 
--
-- This procedure is used to get a list of applications for a move event and return if and when the application was shutdown and restarted
--
-- Parameters:
-- 		projectId - the id of the project
-- 		eventName - the name of the event
--		downMatch - the string match used to find the app shutdown task by title using a SQL LIKE statement (e.g. App % is shutdown)
--		upMatch - the string match used to find the app started up task by title using a SQL LIKE statement (e.g. App % finished testing)
--      tzOffset - used to adjust the GMT times appropriate (e.g. EDT would be '-04:00')
--
-- Returns:
-- 		id INT - interal application id
--		ref_id VARCHAR(64) - the client's reference id of the application
-- 		name VARCHAR(255) - the name of the application
--		down BOOL - flag indicating if the application has been shutdown
--  	down_at DATETIME - the time that the app completed shutdown otherwise NULL
--		up BOOL - flag indicating if the application has been started up
-- 		up_at DATETIME - the time that the app finished the startup or testing indicating that the app is back online
--
-- Usage:
-- 		CALL GetApplicationDownUp(2468, 'MG01', 'Staff present and ready to start shutdown PROCESS of %', 'App % is tested', '-04:00');

DROP PROCEDURE IF EXISTS GetApplicationDownUp;
DELIMITER //
CREATE PROCEDURE GetApplicationDownUp(IN projectId INT, IN eventName VARCHAR(255), IN downMatch VARCHAR(255), IN upMatch VARCHAR(255), tzOffset VARCHAR(6) )
SQL SECURITY DEFINER
    BEGIN
		DECLARE GMT_TZ VARCHAR(6);
		DECLARE TIME_FORMAT VARCHAR(30);
		DECLARE eventId INT;
		DECLARE defaultDT DATETIME;

		SELECT move_event_id INTO eventId FROM move_event WHERE name = eventName; 
		SET GMT_TZ = '-00:00';
		SET TIME_FORMAT = '%a %b %D %H:%i%p';
		SET defaultDT = null;

		DROP TEMPORARY TABLE IF EXISTS apps;
		CREATE TEMPORARY TABLE apps AS (
			SELECT a. asset_entity_id AS id, 
				a.external_ref_id AS ref_id, 
				a.asset_name as name, 
				false as down, 
				defaultDT as down_at,
				false as up, 
				defaultDT as up_at
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

		-- Set the down_at time if the app is down
		UPDATE apps SET down_at = ( 
			COALESCE(
				( SELECT IF(status IN ('Started', 'Completed'), CONVERT_TZ(task.act_start, GMT_TZ, tzOffset), null)
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE downMatch
				), null
			)
		);

		-- Set the up column if the status is Completed
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

		-- Set the up_at column with the time resolved
		UPDATE apps SET up_at = ( 
			COALESCE(
				( SELECT IF(status='Completed', CONVERT_TZ(task.date_resolved, GMT_TZ, tzOffset), null) 
				FROM asset_comment task
				WHERE task.move_event_id=eventId AND 
					task.asset_entity_id=apps.id AND 
					task.comment LIKE upMatch
				), null
			)
		);

		SELECT id, ref_id, name, 
			down, date_format(down_at, TIME_FORMAT) AS down_at, 
			up, date_format(up_at, TIME_FORMAT) AS up_at
		FROM apps;
		DROP TEMPORARY TABLE apps;
    END //
DELIMITER ;