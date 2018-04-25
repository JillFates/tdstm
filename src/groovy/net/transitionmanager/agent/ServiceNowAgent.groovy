package net.transitionmanager.agent

import net.transitionmanager.integration.ActionRequest
import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.service.ServiceNowService

import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic

/**
 * Methods to interact with ServiceNow fetch/download assets lists
 */
@Slf4j()
@Singleton(strict=false)
@CompileStatic
class ServiceNowAgent extends AbstractAgent {
	private static final String DOCUMENTATION_URL = 'https://developer.servicenow.com/app.do#!/rest_api_doc?v=jakarta&id=r_TableAPI-GET'
	public ServiceNowService serviceNowService

	private static final LinkedHashMap HOSTNAME_PARAM = [
			paramName: 'HOSTNAME',
			desc: 'The ServiceNow Hostname of the instance to interact with',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'Enter your FQDN to ServiceNow',
			required: 1,
			readonly: 0,
			encoded: 0
	]

	private static final LinkedHashMap TABLE_PARAM = [
			paramName: 'TABLE',
			desc: 'The table name from the Now Table API',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: '$fieldName',
			value: 'Enter table name',
			required: 1,
			readonly: '$readOnly',
			encoded: 1
	]

	private static final List<LinkedHashMap> COMMON_PARAMS = [
		[
			paramName: 'sysparm_display_value',
			desc: 'Set to true will return all fields, false returns only the fields in sysparm_fields (option true|false, default false)',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'false',
			required: 1,
			readonly: 0,
			encoded: 1
		],
		[
			paramName: 'sysparm_limit',
			desc: 'Limit to be applied on pagination. The default is 10000',
			type: 'Integer',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 10000,
			required: 1,
			readonly: 0,
			encoded: 1
		],
		[
			paramName: 'sysparm_offset',
			desc: 'Use to obtain more records than specified in sysparm_limit',
			type: 'Integer',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 0,
			required: 1,
			readonly: 0,
			encoded: 1
		],
		[
			paramName: 'sysparm_exclude_reference_link',
			desc: 'Used to suppress link to resource provided for reference fields such as the URI',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'true',
			required: 1,
			readonly: 1,
			encoded: 1
		],
		[
			paramName: 'sysparm_suppress_pagination_header',
			desc: 'Set this value to true to remove the Link header from the response (option true|false)',
			type: 'String',
			context: ContextType.USER_DEF,
			required: 1,
			readonly: 1,
			encoded: 1,
			fieldName: null,
			value: 'true'
		],
		[
			paramName: 'CSV',
			desc: 'Indicate the list format as CSV',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'true',
			required: 1,
			readonly: 1,
			encoded: 1
		]
	]

	/*
	 * Constructor
	 */
	ServiceNowAgent() {
		setInfo(AgentClass.SERVICE_NOW, 'ServiceNow API')

		Map dictionary = [
			ApplicationList:
				new DictionaryItem( [
					agentMethod: 'ApplicationList',
					name: 'Application List (cmdb_ci_appl)',
					description: 'List of all Applications',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'operational_status,sys_created_on,used_for,owned_by,install_status,supported_by,name,short_description,manufacturer,vendor,department,comments,location,sys_updated_on,version',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('cmdb_ci_appl', 1)
					] + COMMON_PARAMS
				]),
			ServerList:
				new DictionaryItem(	[
					agentMethod: 'ServerList',
					name: 'Server List (cmdb_ci_server)',
					description: 'List of all server types',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,name,asset_tag,manufacturer,model_id,location,category,device_type,fqdn,ip_address,operational_status,dns_domain,short_description,supported_by,owned_by,sys_class_name,ram,disk_space,cpu_count,os_version,form_factor,os,sys_updated_on,sys_created_on',
							required: 1,
							readonly: 0,
							encoded:  0
						],
						tableParam('cmdb_ci_server', 1)
					] + COMMON_PARAMS
				] ),
			NetworkGearList:
				new DictionaryItem( [
					agentMethod: 'NetworkGearList',
					name: 'Network Gear List (cmdb_ci_netgear)',
					description: 'List of network gear',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,name,asset_tag,location,manufacturer,model_id,category,device_type,fqdn,ip_address,operational_status,supported_by,owned_by,dns_domain,short_description,sys_class_name,sys_updated_on,sys_created_on',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('cmdb_ci_netgear', 1)
					] + COMMON_PARAMS
				] ),
			DatabaseList:
				new DictionaryItem( [
					agentMethod: 'DatabaseList',
					name: 'Database List (cmdb_ci_database)',
					description: 'List of databases',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,name,category,operational_status,short_description,raid_type,dr_backup,supported_by,type,owned_by,supported_by,db_server',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('cmdb_ci_server', 1)
					] + COMMON_PARAMS
				] ),
			MSDList:
				new DictionaryItem( [
					agentMethod: 'MSDList',
					name: 'Mass Storage Device List (cmdb_ci_msd)',
					description: 'List of mass storage devices',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,name,asset_tag,manufacturer,model_id,category,device_type,ip_address,operational_status,short_description,sys_class_name,raid_type,dr_backup,owned_by,supported_by,capacity,location,sys_updated_on,sys_created_on',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('cmdb_ci_msd', 1)
					] + COMMON_PARAMS
				] ),
			RelationshipsList:
				new DictionaryItem( [
					agentMethod: 'RelationshipsList',
					name: 'CI Relationships List (cmdb_rel_ci)',
					description: 'List of relationships (aka dependencies)',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,parent,child,type,sys_updated_on,sys_created_on,connection_strength,port',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('cmdb_rel_ci', 1)
					] + COMMON_PARAMS
				] ),
			CustomTableList:
				new DictionaryItem( [
					agentMethod: 'CustomTableList',
					name: 'Custom Table List (User defined)',
					description: 'List of entities for user defined table',
					endpointUrl: 'https://{HOSTNAME}.service-now.com/{TABLE}.do',
					docUrl: DOCUMENTATION_URL,
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						HOSTNAME_PARAM,
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'operational_status,sys_created_on,name,short_description,department,comments,location,sys_updated_on',
							required: 1,
							readonly: 0,
							encoded: 0
						],
						tableParam('', 0)
					] + COMMON_PARAMS
				] )
		]

		setDictionary( dictionary )

		serviceNowService = (ServiceNowService) ApplicationContextHolder.getBean('serviceNowService')

		/*

			Business Services:

				List of Business Services
				https://dev23132.service-now.com/api/now/table/cmdb_ci_service?sysparm_limit=10

				Dependencies of a Service
				https://dev23132.service-now.com/ngRelationsProcessor.do?cacheKill=1521454695849&cmd=get&id=27d3f35cc0a8000b001df42d019a418f&level=3&sysClassName=cmdb_ci_service&type=flat

				Dependency Types:
				https://dev23132.service-now.com/ngRelationsProcessor.do?cacheKill=1521454695847&cmd=loadTypes

			Databases:
				Postgres Instances
				https://dev23132.service-now.com/api/now/table/cmdb_ci_db_postgresql_instance?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10

				MS SQL Instances

				MySQL Instances

				Oracle Instances

			Storage:
				Mass Storage Devices
				https://dev23132.service-now.com/api/now/table/cmdb_ci_msd?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10

				NAS Storage Devices
				https://dev23132.service-now.com/api/now/table/cmdb_ci_nas_storage?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10

				NAS File Systems
				https://dev23132.service-now.com/api/now/table/cmdb_ci_nas_file_system?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10

				NFS File Systems
				https://dev23132.service-now.com/api/now/table/cmdb_ci_file_system_nfs?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10

				SAN Endpoint
				https://dev23132.service-now.com/api/now/table/cmdb_ci_san_endpoint?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cos_version%2Cserial_number%2Cshort_description%2Cowned_by%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address&sysparm_limit=10
		*/

	}

	/**
	 * Complete the table param with passed variables
	 * @param fieldName - the field name variable
	 * @param readOnly - the read only flag
	 * @return a LinkedHashMap
	 */
	private LinkedHashMap tableParam(String fieldName, int readOnly) {
		LinkedHashMap<String, ?> table = new LinkedHashMap<>(TABLE_PARAM)
		table.put('fieldName', fieldName)
		table.put('value', fieldName)
		table.put('readOnly', readOnly)
		return table;
	}

	/**
	 * Used to fetch/download assets lists from ServiceNow
	 * @param actionRequest
	 * @return
	 */
	Map fetchAssetList(ActionRequest actionRequest) {

		Map result = serviceNowService.fetchAssetList(actionRequest)
		log.debug 'fetchAssetList() Result of fetch assets. {}', result
		if (result?.status == 'error') {
			throw new RuntimeException(result.cause as String)
		}

		return result
	}

}
