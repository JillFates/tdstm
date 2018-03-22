package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.service.ServiceNowService
import groovy.transform.CompileStatic

/**
 * Methods to interact with ServiceNow fetch/download assets lists
 */
@Slf4j(value='logger')
@Singleton(strict=false)
@CompileStatic
class ServiceNowAgent extends AbstractAgent {

	public ServiceNowService serviceNowService

	private static final List<LinkedHashMap> COMMON_PARAMS = [
		[
			paramName: 'sysparm_display_value',
			desc: 'Set to true will return all fields, false returns only the fields in sysparm_fields (option true|false, default false)',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'false',
			required:1,
			readonly:0,
			encoded:1
		],
		[
			paramName: 'sysparm_limit',
			desc: 'Limit to be applied on pagination. The default is 10000',
			type: 'Integer',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 10000,
			required:1,
			readonly:0,
			encoded:1
		],
		[
			paramName: 'sysparm_offset',
			desc: 'Use this parameter to obtain more records than specified in sysparm_limit',
			type: 'Integer',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 0,
			required:1,
			readonly:0,
			encoded:1
		],
		[
			paramName: 'sysparm_exclude_reference_link',
			desc: 'Used to suppress link to resource provided for reference fields such as the URI',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'true',
			required:1,
			readonly:1,
			encoded:1
		],
		[
			paramName: 'sysparm_suppress_pagination_header',
			desc: 'Set this value to true to remove the Link header from the response (option true|false)',
			type: 'String',
			context: ContextType.USER_DEF,
			required:1,
			readonly:1,
			encoded:1,
			fieldName: null,
			value: 'true'
		],
		[
			paramName: 'CSV',
			desc: 'Used to indicate the list format as CSV',
			type: 'String',
			context: ContextType.USER_DEF,
			fieldName: null,
			value: 'true',
			required: 1,
			readonly: 1,
			encoded:1
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
					// https://dev33184.service-now.com/cmdb_ci_appl.do?CSV&sysparm_fields=name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn&sysparm_display_value=false
					agentMethod: 'ApplicationList',
					name: 'Application List',
					description: 'Retrieves a list of applications from ServiceNow',
					endpointUrl: 'https://YOUR-HOST.service-now.com/cmdb_ci_appl.do',
					docUrl: 'https://developer.servicenow.com/app.do#!/rest_api_doc?v=jakarta&id=r_TableAPI-GET',
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'name,short_description,used_for,sys_id,sys_updated_on,vendor,sys_class_name,department,supported_by,owned_by,warranty_expiration,fqdn',
							required:1,
							readonly:0,
							encoded: 0
						]
					] + COMMON_PARAMS
				]),
			ServerListWindows:
				new DictionaryItem(	[
					// https://dev23132.service-now.com/cmdb_ci_win_server.do?CSV=true&sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_fields=sys_id,host_name,short_description,asset_tag,chassis_type,location,os,vendor,manufacturer,model_id,serial_number,department,ip_address,cpu_count,ram,form_factor,classification&sysparm_limit=10
					agentMethod: 'ServerListWindows',
					name: 'Windows Server List',
					description: 'Retrieves a list of Windows Servers from ServiceNow',
					method: 'fetchAssets',
					endpointUrl: ' https://YOUR-HOST.service-now.com/cmdb_ci_win_server.do',
					docUrl: 'https://developer.servicenow.com/app.do#!/rest_api_doc?v=jakarta&id=r_TableAPI-GET',
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,host_name,short_description,asset_tag,chassis_type,location,os,vendor,manufacturer,model_id,serial_number,department,ip_address,cpu_count,ram,form_factor,classification',
							required:1,
							readonly:0,
							encoded: 0
						]
					] + COMMON_PARAMS
				] ),
			ServerListLinux:
				new DictionaryItem( [
					// https://dev23132.service-now.com/api/now/table/cmdb_ci_linux_server?sysparm_display_value=true&sysparm_exclude_reference_link=true&sysparm_suppress_pagination_header=true&sysparm_limit=10&sysparm_fields=sys_id%2Cname%2Casset_tag%2Chost_name%2Cserial_number%2Cshort_description%2Cclassification%2Cdepartment%2Clocation%2Cmanufacturer%2Cmodel_id%2Cos%2Ckernel_release%2Ccpu_count%2Cram%2Cdisk_space%2Cvirtual%2Cchassis_type%2Cmac_address%2Cip_address
					agentMethod: 'ServerListLinux',
					name: 'Linux Server List',
					description: 'Retrieves a list of Linux Servers from ServiceNow',
					method: 'fetchAssets',
					endpointUrl: 'https://YOUR-HOST.service-now.com/cmdb_ci_linux_server.do',
					docUrl: 'ServiceNow REST API for Tables|https://developer.servicenow.com/app.do#!/rest_api_doc?v=jakarta&id=r_TableAPI-GET',
					method: 'fetchAssetList',
					producesData: 1,
					results: invokeResults(),
					params: [
						[
							paramName: 'sysparm_fields',
							desc: 'Comma-separated field names to return in the response',
							type: 'String',
							context: ContextType.USER_DEF,
							fieldName: null,
							value: 'sys_id,name,asset_tag,host_name,serial_number,short_description,classification,department,location,manufacturer,model_id,os,kernel_release,cpu_count,ram,disk_space,virtual,chassis_type,mac_address,ip_address',
							required:1,
							readonly:0,
							encoded: 0
						]

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
	 * Used to fetch/download assets lists from ServiceNow
	 * @param payload
	 * @return
	 */
	Map fetchAssetList(Object payload) {
		Map result = serviceNowService.fetchAssetList(payload)
		log.debug 'Result of fetch assets. {}', result

		return result
	}

}
