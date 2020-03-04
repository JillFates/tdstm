package version.v5_0_0

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.DatabaseMigrationService
import net.transitionmanager.common.Setting

databaseChangeLog = {
	changeSet(author: 'tpelletier', id: '20200110-TM-16721-1') {
		comment('Add the "dependencyGroup", "conflict", "tbd" columns to the filed settings')

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// Asset Class field definition
				Map dependencyGroupMap = [
					'constraints': [
						'required': 1
					],
					'control'    : 'Number',
					'default'    : '',
					'field'      : 'dependencyGroup',
					'imp'        : 'N',
					'label'      : 'Dependency Group',
					'order'      : 0,
					'shared'     : 0,
					'show'       : 1,
					'tip'        : 'Dependency Group',
					'udf'        : 0
				]

				Map conflictMap = [
					'constraints': [
						'required': 1
					],
					'control'    : 'Number',
					'default'    : '',
					'field'      : 'conflict',
					'imp'        : 'N',
					'label'      : 'Conflict',
					'order'      : 0,
					'shared'     : 0,
					'show'       : 1,
					'tip'        : 'Conflict',
					'udf'        : 0
				]

				Map tbdMap = [
					'constraints': [
						'required': 1
					],
					'control'    : 'Number',
					'default'    : '',
					'field'      : 'tbd',
					'imp'        : 'N',
					'label'      : 'TBD',
					'order'      : 0,
					'shared'     : 0,
					'show'       : 1,
					'tip'        : 'TBD',
					'udf'        : 0
				]

				// This closure adds the definition of the tags field to the settings for a given domain.
				Closure changeScript = { settingsJson ->
					// Add the field to the list of fields for this domain
					if (settingsJson.fields) {
						settingsJson.fields << dependencyGroupMap
						settingsJson.fields << conflictMap
						settingsJson.fields << tbdMap
					}
					return settingsJson
				}


				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different settings with the Asset Class fields.
				databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)

			}
		}
	}

	changeSet(author: 'tpelletier', id: '20200110-TM-16721-2') {
		comment('Add new views for the depdency analyzer')

		sql('''INSERT INTO `tdstm`.`dataview` (id,project_id,person_id,name,is_system,is_shared,report_schema,date_created) 
					VALUES (8,2,5662,'Dependency Analyzer All',true,false,
					'{"columns":[{"filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"common","property":"assetClass","width":110,"label":"Asset Class","locked":false},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","application","database","device","storage"],"sort":{"domain":"common","property":"assetName","order":"a"}}', 
					CURDATE());''')

		sql('''INSERT INTO `tdstm`.`dataview` (id,project_id,person_id,name,is_system,is_shared,report_schema,date_created) 
					VALUES (9,2,5662,'Dependency Analyzer Servers',true,false,
					'{"columns":[{"filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"device","property":"model","width":110,"label":"Model","locked":false},{"filter":"","edit":false,"domain":"device","property":"locationSource","width":110,"label":"Source Location","locked":false},{"filter":"","edit":false,"domain":"device","property":"roomSource","width":110,"label":"Source Room","locked":false},{"filter":"","edit":false,"domain":"device","property":"rackSource","width":110,"label":"Source Rack","locked":false},{"filter":"","edit":false,"domain":"device","property":"locationTarget","width":110,"label":"Target Location","locked":false},{"filter":"","edit":false,"domain":"device","property":"roomTarget","width":110,"label":"Target Room","locked":false},{"filter":"","edit":false,"domain":"device","property":"rackTarget","width":110,"label":"Target Rack","locked":false},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","device"],"sort":{"domain":"common","property":"assetName","order":"a"}}', 
					CURDATE());''')

		sql('''INSERT INTO `tdstm`.`dataview` (id,project_id,person_id,name,is_system,is_shared,report_schema,date_created) 
					VALUES (10,2,5662,'Dependency Analyzer Apps',true,false,
					'{"columns":[{"filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"application","property":"sme","width":110,"label":"SME1","locked":false},{"filter":"","edit":false,"domain":"application","property":"sme2","width":110,"label":"SME2","locked":false},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","application"],"sort":{"domain":"common","property":"assetName","order":"a"}}',
					CURDATE());''')

		sql('''INSERT INTO `tdstm`.`dataview` (id,project_id,person_id,name,is_system,is_shared,report_schema,date_created) 
					VALUES (11,2,5662,'Dependency Analyzer Databases',true,false,
					'{"columns":[{"filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"database","property":"dbFormat","width":110,"label":"Format","locked":false},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","database"],"sort":{"domain":"common","property":"assetName","order":"a"}}',
					CURDATE());''')

		sql('''INSERT INTO `tdstm`.`dataview` (id,project_id,person_id,name,is_system,is_shared,report_schema,date_created) 
					VALUES (12,2,5662,'Dependency Analyzer Storage',true,false,
					'{"columns":[{" filter":"","edit":false,"domain":"common","property":"assetName","width":110,"label":"Name","locked":false},{"filter":"","edit":false,"domain":"storage","property":"fileFormat","width":110,"label":"Format","locked":false},{"filter":"","edit":false,"domain":"common","property":"assetClass","width":110,"label":"Asset Class","locked":false},{"filter":"","edit":false,"domain":"common","property":"validation","width":110,"label":"Validation","locked":false},{"filter":"","edit":false,"domain":"common","property":"moveBundle","width":110,"label":"Bundle","locked":false},{"filter":"","edit":false,"domain":"common","property":"dependencyGroup","width":110,"label":"Dependency Group","locked":false},{"filter":"","edit":false,"domain":"common","property":"planStatus","width":110,"label":"Plan Status","locked":false},{"filter":"","edit":false,"domain":"common","property":"tbd","width":110,"label":"TBD","locked":false},{"filter":"","edit":false,"domain":"common","property":"conflict","width":110,"label":"Conflict","locked":false}],"domains":["common","storage"],"sort":{"domain":"common","property":"assetName","order":"a"}}',
					CURDATE());''')
	}

}
