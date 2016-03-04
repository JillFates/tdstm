--
-- The following SQL is used to create new user accounts that will have access to the system
--

SELECT @TDS_CO := party_group_id FROM party_group WHERE name='TDS' ORDER BY party_group_id LIMIT 1;

INSERT INTO party (date_created, party_type_id) values (now(), 'PERSON');
SELECT @DEVID := LAST_INSERT_ID();

INSERT INTO party (date_created, party_type_id) values (now(), 'PERSON');
SELECT @QAID := LAST_INSERT_ID();

INSERT INTO party (date_created, party_type_id) values (now(), 'PERSON');
SELECT @ROID := LAST_INSERT_ID();

INSERT INTO person (person_id, first_name, middle_name, last_name, active, email) VALUES
   (@DEVID, 'Dev', '', 'Engineer', 'Y', 'development@transitionaldata.com'),
   (@QAID, 'QA', '', 'Engineer', 'Y', 'development@transitionaldata.com'),
   (@ROID, 'ReadOnly', '', 'User', 'Y', 'development@transitionaldata.com');

INSERT INTO user_login (username, person_id, password, expiry_date, created_date, password_changed_date, last_modified, active) values
   ('dev', @DEVID, sha1('zelda'), date_add(now(),INTERVAL 1 YEAR), now(), now(), now(), 'Y'),
   ('qa', @QAID, sha1('zelda'), date_add(now(),INTERVAL 1 YEAR), now(), now(), now(), 'Y'),
   ('user', @QAID, sha1('zelda'), date_add(now(),INTERVAL 1 YEAR), now(), now(), now(), 'Y');

INSERT INTO party_role (party_id, role_type_id) VALUES
   (@DEVID, 'ADMIN'),
   (@QAID, 'ADMIN'),
   (@ROID, 'USER');

INSERT INTO user_preference (preference_code, value, user_login_id) VALUES
   ('CURR_TZ', 'America/New_York', = (select user_login_id from user_login where person_id=@DEVID)),
   ('CURR_TZ', 'America/New_York', = (select user_login_id from user_login where person_id=@QAID)),
   ('CURR_TZ', 'America/New_York', = (select user_login_id from user_login where person_id=@ROID)),
   ('CURR_DT_FORMAT', 'MM/DD/YYYY', = (select user_login_id from user_login where person_id=@DEVID)),
   ('CURR_DT_FORMAT', 'MM/DD/YYYY', = (select user_login_id from user_login where person_id=@QAID)),
   ('CURR_DT_FORMAT', 'MM/DD/YYYY', = (select user_login_id from user_login where person_id=@ROID));

-- Setup persons to be staff of TDS Company
INSERT INTO party_relationship 
      (party_relationship_type_id, party_id_from_id, role_type_code_from_id, role_type_code_to_id, party_id_to_id)
   VALUES
      ('STAFF', @TDS_CO, 'COMPANY', 'STAFF', @DEVID),
      ('STAFF', @TDS_CO, 'COMPANY', 'STAFF', @QAID),
      ('STAFF', @TDS_CO, 'COMPANY', 'STAFF', @ROID),
      ('STAFF', @TDS_CO, 'COMPANY', 'PROJ_MGR', @DEVID),
      ('STAFF', @TDS_CO, 'COMPANY', 'PROJ_MGR', @QAID),
      ('STAFF', @TDS_CO, 'COMPANY', 'CLEANER', @QAID),
      ('STAFF', @TDS_CO, 'COMPANY', 'ACCT_MGR', @ROID);
