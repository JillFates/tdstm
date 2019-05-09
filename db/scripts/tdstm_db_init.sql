--
-- This script should be run anytime that the database is dropped and reloaded so that the necessary 
-- stored procedures/functions are loaded.
--

DROP FUNCTION IF EXISTS `tdstm_sequencer`;
DELIMITER //
CREATE FUNCTION `tdstm_sequencer`(context_id BIGINT, name VARCHAR(16)) RETURNS bigint(20)
	BEGIN

	SET @prevs := NULL;

	INSERT INTO sequence_number(context_id, name, last) VALUES (context_id, name, 1)
	ON DUPLICATE KEY UPDATE last = IF((@prevs := last) <> NULL IS NULL, last + 1, NULL);

	return IF(ISNULL(@prevs), 1, @prevs + 1);
	END//
	
DELIMITER ;

SET @@group_concat_max_len = 100000;