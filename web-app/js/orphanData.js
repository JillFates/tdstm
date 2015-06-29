function getDeatiledReport( records, table ){
	var dataLength = records.length;
	var timeZone = $("#tzId").val()
	var userDTFormat = $("#userDTFormat").val()
	var tbody = ""
	if(dataLength != 0){
		switch(table){
			
			/* for application */
			case "application" :
				var thead ="<tr><th>app_id</th><th>app_code</th><th>environment</th><th>owner_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.app_id+"</td><td>"+record.app_code+"</td>"+
							"<td>"+record.environment+"</td><td>"+record.owner_id+"</td></tr>"
				}
			break;
			/* for application_asset_map */
			case "application_asset_map" :
				var thead ="<tr><th>id</th><th>application_id</th><th>asset_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.application_id+"</td><td>"+record.asset_id+"</td></tr>"
				}
			break;
			/* for application_asset_map */
			case "asset_comment" :
				var thead ="<tr><th>asset_comment_id</th><th>asset_entity_id</th><th>category</th><th>comment</th><th>comment_type</th>"+
							"<th>created_by</th><th>date_created</th><th>resolved_by</th><th>date_resolved</th><th>resolution</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.asset_comment_id+"</td><td>"+record.asset_entity_id+"</td><td>"+record.category+"</td>"+
							"<td>"+record.comment+"</td><td>"+record.comment_type+"</td><td>"+record.created_by+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td><td>"+record.resolved_by +"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.date_resolved, userDTFormat, timeZone)+"</td><td>"+record.resolution +"</td></tr>"
				}
			break;
			/* for asset_entity */
			case "asset_entity" :
				var thead ="<tr><th>asset_entity_id</th><th>project_id</th><th>owner_id</th><th>move_bundle_id</th><th>source_team_id</th>"+
							"<th>target_team_id</th><th>application</th><th>asset_name</th><th>model</th><th>manufacturer</th><th>asset_tag</th><th>asset_type</th><th>serial_number</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.asset_entity_id+"</td><td>"+record.project_id+"</td><td>"+record.owner_id+"</td>"+
							"<td>"+record.move_bundle_id+"</td><td>"+record.source_team_id+"</td><td>"+record.target_team_id+"</td>"+
							"<td>"+record.application+"</td><td>"+record.asset_name +"</td><td>"+record.model_id +"</td><td>"+record.manufacturer_id +"</td>"+
							"<td>"+record.asset_tag+"</td><td>"+record.asset_type +"</td><td>"+record.serial_number +"</td></tr>"
				}
			break;
			/* for asset_entity_varchar */
			case "asset_entity_varchar" :
				var thead ="<tr><th>id</th><th>asset_entity_id</th><th>attribute_id</th><th>audit_action</th><th>value</th>"
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.asset_entity_id+"</td><td>"+record.attribute_id+"</td>"+
							"<td>"+record.audit_action+"</td><td>"+record.value+"</td><td></tr>"
				}
			break;
			/* for data_transfer_attribute_map */
			case "data_transfer_attribute_map" :
				var thead ="<tr><th>id</th><th>eav_attribute_id</th><th>data_transfer_set_id</th><th>column_name</th><th>is_required</th>"+
							"<th>sheet_name</th><th>validation</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.eav_attribute_id+"</td><td>"+record.data_transfer_set_id+"</td>"+
							"<td>"+record.column_name+"</td><td>"+record.is_required+"</td><td>"+record.sheet_name+"</td><td>"+record.validation +"</td></tr>"
				}
			break;
			/* for data_transfer_batch */
			case "data_transfer_batch" :
				var thead ="<tr><th>batch_id</th><th>project_id</th><th>data_transfer_set_id</th><th>user_login_id</th><th>date_created</th>"+
							"<th>export_datetime</th><th>last_modified</th><th>has_errors</th><th>status_code</th><th>transfer_mode</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.batch_id+"</td><td>"+record.project_id+"</td><td>"+record.data_transfer_set_id+"</td>"+
							"<td>"+record.user_login_id+"</td><td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td><td nowrap>"+getConvertedTimeFormate(record.export_datetime, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.last_modified, userDTFormat, timeZone) +"</td><td>"+record.has_errors +"</td><td>"+record.status_code +"</td><td>"+record.transfer_mode +"</td></tr>"
				}
			break;
			/* for data_transfer_comment */
			case "data_transfer_comment" :
				var thead ="<tr><th>id</th><th>asset_id</th><th>data_transfer_batch_id</th><th>comment</th><th>comment_id</th>"+
							"<th>comment_type</th><th>must_verify</th><th>row_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.asset_id+"</td><td>"+record.data_transfer_batch_id+"</td>"+
							"<td>"+record.comment+"</td><td>"+record.comment_id +"</td><td>"+record.comment_type +"</td><td>"+record.must_verify +"</td>"+
							"<td>"+record.row_id+"</td></tr>"
				}
			break;
			/* for data_transfer_value */
			case "data_transfer_value" :
				var thead ="<tr><th>value_id</th><th>eav_attribute_id</th><th>data_transfer_batch_id</th><th>asset_entity_id</th><th>import_value</th>"+
							"<th>corrected_value</th><th>error_text</th><th>has_error</th><th>row_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.value_id+"</td><td>"+record.eav_attribute_id+"</td><td>"+record.data_transfer_batch_id+"</td>"+
							"<td>"+record.asset_entity_id+"</td><td>"+record.import_value+"</td><td>"+record.corrected_value+"</td><td>"+record.error_text+"</td>"+
							"<td>"+record.has_error+"</td><td>"+record.row_id+"</td></tr>"
				}
			break;
			/* for eav_attribute */
			case "eav_attribute" :
				var thead ="<tr><th>attribute_id</th><th>entity_type_id</th><th>attribute_code</th><th>backend_type</th><th>default_value</th>"+
							"<th>frontend_input</th><th>frontend_label</th><th>is_required</th><th>sort_order</th><th>validation</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.attribute_id+"</td><td>"+record.entity_type_id+"</td><td>"+record.attribute_code+"</td>"+
							"<td>"+record.backend_type+"</td><td>"+record.default_value+"</td><td>"+record.frontend_input+"</td><td>"+record.frontend_label+"</td>"+
							"<td>"+record.is_required+"</td><td>"+record.sort_order+"</td><td>"+record.validation+"</td></tr>"
				}
			break;
			/* for eav_attribute_option */
			case "eav_attribute_option" :
				var thead ="<tr><th>option_id</th><th>attribute_id</th><th>sort_order</th><th>value</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.option_id+"</td><td>"+record.attribute_id+"</td>"+
							"<td>"+record.sort_order+"</td><td>"+record.value+"</td></tr>"
				}
			break;
			/* for eav_attribute_set */
			case "eav_attribute_set" :
				var thead ="<tr><th>attribute_set_id</th><th>attribute_set_name</th><th>entity_type_id</th><th>sort_order</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.attribute_set_id+"</td><td>"+record.attribute_set_name+"</td>"+
							"<td>"+record.entity_type_id+"</td><td>"+record.sort_order+"</td></tr>"
				}
			break;
			/* for eav_entity */
			case "eav_entity" :
				var thead ="<tr><th>entity_id</th><th>attribute_set_id</th><th>date_created</th><th>last_updated</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.entity_id+"</td><td>"+record.attribute_set_id+"</td>"+
							"<td>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td><td>"+getConvertedTimeFormate(record.last_updated, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for eav_entity_attribute */
			case "eav_entity_attribute" :
				var thead ="<tr><th>entity_attribute_id</th><th>attribute_id</th><th>eav_attribute_set_id</th><th>eav_entity_id</th><th>sort_order</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.entity_attribute_id+"</td><td>"+record.attribute_id+"</td>"+
							"<td>"+record.eav_attribute_set_id+"</td><td>"+record.eav_entity_id+"</td><td>"+record.sort_order+"</td></tr>"
				}
			break;
			/* for eav_entity_datatype */
			case "eav_entity_datatype" :
				var thead ="<tr><th>value_id</th><th>attribute_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.value_id+"</td><td>"+record.attribute_id+"</td></tr>"
				}
			break;
			/* for manufacturer */
			case "manufacturer" :
				var thead ="<tr><th>manufacturer_id</th><th>Name</th><th>Description</th><th>AKA</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.manufacturer_id+"</td><td>"+record.name+"</td>"+
							"<td>"+record.description+"</td><td>"+record.aka+"</td></tr>"
				}
			break;
			/* for model */
			case "model" :
				var thead ="<tr><th>model_id</th><th>description</th><th>manufacturer_id</th><th>name</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.model_id+"</td><td>"+record.description+"</td>"+
							"<td>"+record.manufacturer_id+"</td><td>"+record.name+"</td></tr>"
				}
			break;
			/* for model */
			case "model_connector" :
				var thead ="<tr><th>model_connector_id</th><th>model_id</th><th>label</th><th>type</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.model_connectors_id+"</td><td>"+record.model_id+"</td>"+
							"<td>"+record.label+"</td><td>"+record.type+"</td></tr>"
				}
			break;
			/* for move_bundle */
			case "move_bundle" :
				var thead ="<tr><th>move_bundle_id</th><th>name</th><th>move_event_id</th><th>project_id</th><th>description</th><th>operational_order</th><th>start_time</th><th>completion_time</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.move_bundle_id+"</td><td>"+record.name+"</td><td>"+record.move_event_id+"</td>"+
							"<td>"+record.project_id+"</td><td>"+record.description+"</td><td>"+record.operational_order+"</td>"+
							"<td>"+getConvertedTimeFormate(record.start_time, userDTFormat, timeZone)+"</td><td>"+getConvertedTimeFormate(record.completion_time, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for move_bundle_step */
			case "move_bundle_step" :
				var thead ="<tr><th>id</th><th>move_bundle_id</th><th>date_created</th><th>last_updated</th><th>plan_start_time</th>"+
							"<th>plan_completion_time</th><th>actual_start_time</th><th>actual_completion_time</th><th>calc_method</th>"+
							"<th>label</th><th>show_on_dashboard</th><th>transition_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.move_bundle_id+"</td><td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.last_updated, userDTFormat, timeZone)+"</td><td nowrap>"+getConvertedTimeFormate(record.plan_start_time, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.plan_completion_time, userDTFormat, timeZone)+"</td><td nowrap>"+getConvertedTimeFormate(record.actual_start_time, userDTFormat, timeZone)+"</td>"+
							"<tdnowrap>"+getConvertedTimeFormate(record.actual_completion_time, userDTFormat, timeZone)+"</td><td>"+record.calc_method+"</td><td>"+record.label+"</td>"+
							"<td>"+record.show_on_dashboard+"</td><td>"+record.transition_id+"</td></tr>"
				}
			break;
			/* for move_event */
			case "move_event" :
				var thead ="<tr><th>move_event_id</th><th>project_id</th><th>name</th><th>description</th><th>actual_start_time</th>"+
							"<th>actual_completion_time</th><th>revised_completion_time</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.move_event_id+"</td><td>"+record.project_id+"</td><td>"+record.name+"</td><td>"+record.description+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.actual_start_time, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.actual_completion_time, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.revised_completion_time, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for move_event_news */
			case "move_event_news" :
				var thead ="<tr><th>move_event_news_id</th><th>move_event_id</th><th>message</th><th>created_by</th><th>date_created</th>"+
							"<th>is_archived</th><th>archived_by</th><th>date_archived</th><th>resolution</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.move_event_news_id+"</td><td>"+record.move_event_id+"</td><td>"+record.message+"</td>"+
							"<td>"+record.created_by+"</td><td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td>"+
							"<td>"+record.is_archived+"</td><td>"+record.archived_by+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.date_archived, userDTFormat, timeZone)+"</td><td nowrap>"+record.resolution+"</td></tr>"
				}
			break;
			/* for move_event_snapshot */
			case "move_event_snapshot" :
				var thead ="<tr><th>id</th><th>move_event_id</th><th>dial_indicator</th><th>plan_delta</th><th>date_created</th>"+
							"<th>type</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.move_event_id+"</td><td>"+record.dial_indicator+"</td>"+
							"<td>"+record.plan_delta+"</td><td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for party */
			case "party" :
				var thead ="<tr><th>party_id</th><th>party_type_id</th><th>date_created</th><th>last_updated</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.party_id+"</td><td>"+record.party_type_id+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.last_updated, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for party_relationship */
			case "party_relationship" :
				var thead ="<tr><th>party_relationship_type_id</th><th>party_id_from_id</th><th>party_id_to_id</th><th>role_type_code_from_id</th>"+
							"<th>role_type_code_to_id</th><th>comment</th><th>status_code</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.party_relationship_type_id+"</td><td>"+record.party_id_from_id+"</td>"+
							"<td>"+record.party_id_to_id+"</td><td>"+record.role_type_code_from_id+"</td><td>"+record.role_type_code_to_id+"</td>"+
							"<td>"+record.comment+"</td><td>"+record.status_code+"</td></tr>"
				}
			break;
			/* for party_role */
			case "party_role" :
				var thead ="<tr><th>party_id</th><th>party_id</th><th>role_type_id</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.party_id+"</td><td>"+record.role_type_id+"</td></tr>"
				}
			break;
			/* for project */
			case "project" :
				var thead ="<tr><th>project_id</th><th>client_id</th><th>project_code</th><th>description</th>"+
							"<th>track_changes</th><th>workflow_code</th><th>start_date</th><th>completion_date</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.project_id+"</td><td>"+record.client_id+"</td>"+
							"<td>"+record.project_code+"</td><td>"+record.description+"</td><td>"+record.track_changes+"</td><td>"+record.workflow_code+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.start_date, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.completion_date, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for project_asset_map */
			case "project_asset_map" :
				var thead ="<tr><th>id</th><th>project_id</th><th>asset_id</th><th>current_state_id</th>"+
							"<th>created_date</th><th>last_modified</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.project_id+"</td>"+
							"<td>"+record.asset_id+"</td><td>"+record.current_state_id+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.created_date, userDTFormat, timeZone)+"</td>"+
							"<td nowrap>"+getConvertedTimeFormate(record.last_modified, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for project_logo */
			case "project_logo" :
				var thead ="<tr><th>project_logo_id</th><th>project_id</th><th>party_id</th><th>name</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.project_logo_id+"</td><td>"+record.project_id+"</td>"+
							"<td>"+record.party_id+"</td><td>"+record.name+"</td></tr>"
				}
			break;
			/* for project_team */
			case "project_team" :
				var thead ="<tr><th>project_team_id</th><th>move_bundle_id</th><th>team_code</th><th>latest_asset_id</th>"+
							"<th>current_location</th><th>is_disbanded</th><th>is_idle</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.project_team_id+"</td><td>"+record.move_bundle_id+"</td>"+
							"<td>"+record.team_code+"</td><td>"+record.latest_asset_id+"</td><td>"+record.current_location+"</td>"+
							"<td>"+record.is_disbanded+"</td><td>"+record.is_idle+"</td></tr>"
				}
			break;
			/* for step_snapshot */
			case "step_snapshot" :
				var thead ="<tr><th>id</th><th>move_bundle_step_id</th><th>tasks_count</th><th>tasks_completed</th>"+
							"<th>date_created</th><th>dial_indicator</th><th>duration</th><th>plan_delta</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.id+"</td><td>"+record.move_bundle_step_id+"</td>"+
							"<td>"+record.tasks_count+"</td><td>"+record.tasks_completed+"</td>"+
							"<td>"+getConvertedTimeFormate(record.date_created, userDTFormat, timeZone)+"</td>"+
							"<td>"+record.dial_indicator+"</td><td>"+convertIntoHHMM(record.duration)+"</td><td>"+record.plan_delta+"</td></tr>"
				}
			break;
			/* for user_login */
			case "user_login" :
				var thead ="<tr><th>user_login_id</th><th>username</th><th>person_id</th><th>active</th>"+
							"<th>created_date</th><th>last_login</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.user_login_id+"</td><td>"+record.username+"</td>"+
							"<td>"+record.person_id+"</td><td>"+record.active+"</td>"+
							"<td>"+getConvertedTimeFormate(record.created_date, userDTFormat, timeZone)+"</td>"+
							"<td>"+getConvertedTimeFormate(record.last_login, userDTFormat, timeZone)+"</td></tr>"
				}
			break;
			/* for user_preference */
			case "user_preference" :
				var thead ="<tr><th>user_login_id</th><th>preference_code</th><th>value</th><tr>"	
				for( i = 0; i < dataLength; i++){
					var cssClass = 'odd'
					if(i % 2 == 0){
						cssClass = 'even'
					}
					var record = records[i]
					tbody +="<tr class='"+cssClass+"'><td>"+record.user_login_id+"</td><td>"+record.preference_code+"</td>"+
							"<td>"+record.value+"</td></tr>"
				}
			break;
		}
	}
	return thead + tbody
}
function getConvertedTimeFormate( dateString, userDTFormat, timeZone ){
	var timeString = ""
	if(dateString){
		if (timeZone == null) {
			timeZone = "GMT"
		}
		var format = "MM/DD/YYYY hh:mm a"
		if (userDTFormat == "DD/MM/YYYY") {
			format = "DD/MM/YYYY hh:mm a"
		}
		// Convert zulu datetime to a specific timezone/format
		timeString = moment(dateString).tz(timeZone).format(format)
	}
   return timeString;
}
function convertIntoHHMM( seconds ){
	var timeFormate 
    var hours = parseInt(seconds / 3600) 
    	timeFormate = hours >= 10 ? hours : '0'+hours
    var minutes =  parseInt((seconds % 3600 ) / 60 )
    	timeFormate += ":"+(minutes >= 10 ? minutes : '0'+minutes)
    	return timeFormate
}