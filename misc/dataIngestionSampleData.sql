-- ===================================================================================
-- This script will delete all ApiAction records for the project specified and then
-- create a new set of actions to use
-- ===================================================================================

--
-- Set the project code here:
--
set @PROJECT_CODE='TM-Demo';

--
-- The rest should just work...
--
select @pid:=project_id from project where project_code=@PROJECT_CODE;
select @pid;

select @personId:=party_id_to_id from party_relationship where party_id_from_id=@pid and party_relationship_type_id='PROJ_STAFF' LIMIT 1;

update asset_comment set api_action_id = null where project_id = @pid;

delete from api_action where project_id = @pid;
delete from provider where project_id = @pid;
delete from data_script where project_id = @pid;

insert into data_transfer_set (data_transfer_id, set_code,title,transfer_mode,template_filename) values(3, 'ETL', 'TM Data Ingestion', 'B', '')
   ON DUPLICATE KEY UPDATE template_filename='';


insert into provider(provider_id, name, description, project_id, date_created, version)
values
   (1, 'TransitionManager', 'TransitionManager Application Web Services', @pid, now(), 0),
   (2, 'AWS', 'Amazon AWS Services', @pid, now(), 0),
   (3, 'REST', 'Standard REST Services', @pid, now(), 0),
   (4, 'ServiceNow', 'ServiceNow SaaS Web Services', @pid, now(), 0);


INSERT INTO data_script (
    data_script_id,
    name,
    project_id,
    created_by,
    provider_id,
    date_created,
    version,
    mode,
    etl_source_code
) VALUES
    (1, 'Applications to TM', @pid, @personId, 4, now(), 0, 'IMPORT', 'read labels
iterate {
   domain Application
   extract 0 load assetName
   extract 3 load externalRefId
   reference externalRefId with externalRefId
   reference assetName with assetName
}'),

    (2, 'Servers to TM', @pid, @personId, 4, now(), 0, 'IMPORT', 'read labels
iterate {
    domain Application
    extract 0 load assetName
}');



INSERT INTO api_action (
	name,
	description,
	project_id,
	provider_id,
    produces_data,
    default_data_script_id,
	agent_class,
    agent_method,
	async_queue,
    callback_method,
	callback_mode,
	method_params
	)
VALUES
	(
		'Application List',
		'Used to download assets of type application from ServiceNow',
		@pid,
		4, 1, 1,
		'SERVICE_NOW',
        'fetchAssets',
		'',
		'',
		'NA',
		JSON_ARRAY(
			JSON_OBJECT(
				"param", "url",
				"desc", "The ServiceNow end point URL",
				"context", "USER_DEF",
				"value", "https://dev23132.service-now.com"
			),
			JSON_OBJECT(
				"param", "path",
				"desc", "The ServiceNow end-point path used to fetch applications",
				"context", "USER_DEF",
				"value", "cmdb_ci_appl_list.do"
			),
			JSON_OBJECT(
				"param", "format",
				"desc", "Resultant file format",
				"context", "USER_DEF",
				"value", "CSV"
			),
			JSON_OBJECT(
				"param", "query",
				"desc", "The query definition to be executed",
				"context", "USER_DEF",
				"value", "sys_class_name=cmdb_ci_web_server^ORsys_class_name=cmdb_ci_app_server^ORsys_class_name=cmdb_ci_email_server^ORsys_class_name=cmdb_ci_appl"
			),
			JSON_OBJECT(
				"param", "fieldNames",
				"desc", "The comma separated list of field names to retrive",
				"context", "USER_DEF",
				"value", "name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn"
			)
		)
	),
	(
		'Database List',
		'Used to download assets of type database from ServiceNow',
		@pid,
		4, 1, 1,
		'SERVICE_NOW',
        'fetchAssets',
		'',
		'',
		'NA',
		JSON_ARRAY(
			JSON_OBJECT(
				"param", "url",
				"desc", "The ServiceNow end point URL",
				"context", "USER_DEF",
				"value", "https://dev23132.service-now.com"
			),
			JSON_OBJECT(
				"param", "path",
				"desc", "The ServiceNow end-point path used to fetch databases",
				"context", "USER_DEF",
				"value", "cmdb_ci_appl_list.do"
			),
			JSON_OBJECT(
				"param", "format",
				"desc", "Resultant file format",
				"context", "USER_DEF",
				"value", "CSV"
			),
			JSON_OBJECT(
				"param", "query",
				"desc", "The query definition to be executed",
				"context", "USER_DEF",
				"value", "sys_class_name=cmdb_ci_web_server^ORsys_class_name=cmdb_ci_app_server^ORsys_class_name=cmdb_ci_email_server^ORsys_class_name=cmdb_ci_appl"
			),
			JSON_OBJECT(
				"param", "fieldNames",
				"desc", "The comma separated list of field names to retrive",
				"context", "USER_DEF",
				"value", "name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn"
			)
		)
	),
	(
		'Storage List',
		'Used to download assets of type Storage from ServiceNow',
		@pid,
		4, 1, 1,
		'SERVICE_NOW',
        'fetchAssets',
		'',
		'',
		'NA',
		JSON_ARRAY(
			JSON_OBJECT(
				"param", "url",
				"desc", "The ServiceNow end point URL",
				"context", "USER_DEF",
				"value", "https://dev23132.service-now.com"
			),
			JSON_OBJECT(
				"param", "path",
				"desc", "The ServiceNow end-point path used to fetch storage",
				"context", "USER_DEF",
				"value", "cmdb_ci_appl_list.do"
			),
			JSON_OBJECT(
				"param", "format",
				"desc", "Resultant file format",
				"context", "USER_DEF",
				"value", "CSV"
			),
			JSON_OBJECT(
				"param", "query",
				"desc", "The query definition to be executed",
				"context", "USER_DEF",
				"value", "sys_class_name=cmdb_ci_web_server^ORsys_class_name=cmdb_ci_app_server^ORsys_class_name=cmdb_ci_email_server^ORsys_class_name=cmdb_ci_appl"
			),
			JSON_OBJECT(
				"param", "fieldNames",
				"desc", "The comma separated list of field names to retrive",
				"context", "USER_DEF",
				"value", "name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn"
			)
		)
	),
	(
		'Server List (Windows)',
		'Used to download assets of type servers (Windows) from ServiceNow',
		@pid,
		4, 1, 1,
		'SERVICE_NOW',
        'fetchAssets',
		'',
		'',
		'NA',
		JSON_ARRAY(
			JSON_OBJECT(
				"param", "url",
				"desc", "The ServiceNow end point URL",
				"context", "USER_DEF",
				"value", "https://dev23132.service-now.com"
			),
			JSON_OBJECT(
				"param", "path",
				"desc", "The ServiceNow end-point path used to fetch Windows servers",
				"context", "USER_DEF",
				"value", "cmdb_ci_appl_list.do"
			),
			JSON_OBJECT(
				"param", "format",
				"desc", "Resultant file format",
				"context", "USER_DEF",
				"value", "CSV"
			),
			JSON_OBJECT(
				"param", "query",
				"desc", "The query definition to be executed",
				"context", "USER_DEF",
				"value", "sys_class_name=cmdb_ci_web_server^ORsys_class_name=cmdb_ci_app_server^ORsys_class_name=cmdb_ci_email_server^ORsys_class_name=cmdb_ci_appl"
			),
			JSON_OBJECT(
				"param", "fieldNames",
				"desc", "The comma separated list of field names to retrive",
				"context", "USER_DEF",
				"value", "name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn"
			)
		)
	),
	(
		'Server List (*nix) List',
		'Used to download assets of type servers (Linux/Unix) from ServiceNow',
		@pid,
		4, 1, 1,
		'SERVICE_NOW',
        'fetchAssets',
		'',
		'',
		'NA',
		JSON_ARRAY(
			JSON_OBJECT(
				"param", "url",
				"desc", "The ServiceNow end point URL",
				"context", "USER_DEF",
				"value", "https://dev23132.service-now.com"
			),
			JSON_OBJECT(
				"param", "path",
				"desc", "The ServiceNow end-point path used to fetch servers",
				"context", "USER_DEF",
				"value", "cmdb_ci_appl_list.do"
			),
			JSON_OBJECT(
				"param", "format",
				"desc", "Resultant file format",
				"context", "USER_DEF",
				"value", "CSV"
			),
			JSON_OBJECT(
				"param", "query",
				"desc", "The query definition to be executed",
				"context", "USER_DEF",
				"value", "sys_class_name=cmdb_ci_web_server^ORsys_class_name=cmdb_ci_app_server^ORsys_class_name=cmdb_ci_email_server^ORsys_class_name=cmdb_ci_appl"
			),
			JSON_OBJECT(
				"param", "fieldNames",
				"desc", "The comma separated list of field names to retrive",
				"context", "USER_DEF",
				"value", "name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn"
			)
		)
	)

	;