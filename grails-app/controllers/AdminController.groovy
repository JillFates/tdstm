class AdminController {
	def jdbcTemplate
    def index = { }
    
    def orphanSummary = {
		def orphanParty = """SELECT party_id as party_id FROM party p where p.party_id not in
								(select distinct pr.party_id from (select party_group_id as party_id from party_group
								union
								select person_id as party_id from person
								union
								select move_bundle_id as party_id from move_bundle
								union
								select project_id as party_id from project
								union
								select project_team_id as party_id from project_team
								union
								select app_id as party_id from application ) pr)"""
								
    	def summaryQuery = """
						/*-----------------------------------ORPHAN RESULTS QUERY FOR APPLICATION-------------------------------------------*/	
						SELECT * FROM ( SELECT 'application' as mainTable,'owner_id' as refId,'Orphan' as type,count(*) as totalCount FROM application app where app.owner_id not in (select p.party_group_id from party_group p )
							UNION
							SELECT 'application' as mainTable,'owner_id' as refId,'Null' as type,count(*) as totalCount FROM application app where app.owner_id is null ) ap
						WHERE ap.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR APPLICATION_ASSET_MAP---------------------------------*/
						SELECT * FROM (SELECT 'application_asset_map' as mainTable,'application_id' as refId,'Orphan' as type,count(*) as totalCount FROM application_asset_map asm where asm.application_id not in (select app.app_id from application app )
							UNION
							SELECT 'application_asset_map' as mainTable,'application_id' as refId,'Null' as type,count(*) as totalCount FROM application_asset_map asm where asm.application_id is null
							UNION 
							SELECT 'application_asset_map' as mainTable,'asset_id' as refId,'Orphan' as type,count(*) as totalCount FROM application_asset_map asm where asm.asset_id not in (select ae.asset_entity_id from asset_entity ae )
							UNION
							SELECT 'application_asset_map' as mainTable,'asset_id' as refId,'Null' as type,count(*) as totalCount FROM application_asset_map asm where asm.asset_id is null) asm
						WHERE asm.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR ASSET_COMMENT-------------------------------------------*/
						SELECT * FROM ( SELECT 'asset_comment' as mainTable,'asset_entity_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_comment ac where ac.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )
							UNION
							SELECT 'asset_comment' as mainTable,'asset_entity_id' as refId,'Null' as type,count(*) as totalCount FROM asset_comment ac where ac.asset_entity_id is null
							UNION
							SELECT 'asset_comment' as mainTable,'created_by' as refId,'Orphan' as type,count(*) as totalCount FROM asset_comment ac where ac.created_by not in (select p.person_id from person p )
							UNION
							SELECT 'asset_comment' as mainTable,'created_by' as refId,'Null' as type,count(*) as totalCount FROM asset_comment ac where ac.created_by is null
							UNION
							SELECT 'asset_comment' as mainTable,'resolved_by' as refId,'Orphan' as type,count(*) as totalCount FROM asset_comment ac where ac.resolved_by not in (select p.person_id from person p )) ac
							WHERE ac.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR ASSET_ENTITY-------------------------------------------*/
						SELECT * FROM (SELECT 'asset_entity' as mainTable,'project_id' as refId,'Orphan' as type, count(*) as totalCount FROM asset_entity a where a.project_id not in (select p.project_id from project p )
							UNION
							SELECT 'asset_entity' as mainTable,'project_id' as refId ,'Null' as type,count(*) as totalCount FROM asset_entity a where a.project_id is null
							UNION
							SELECT 'asset_entity' as mainTable,'owner_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.owner_id not in (select p.party_group_id from party_group p )
							UNION
							SELECT 'asset_entity' as mainTable,'owner_id' as refId,'Null' as type,count(*) as totalCount FROM asset_entity a where a.owner_id is null
							UNION
							SELECT 'asset_entity' as mainTable,'move_bundle_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.move_bundle_id not in (select m.move_bundle_id from move_bundle m )
							UNION
							SELECT 'asset_entity' as mainTable,'source_team_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.source_team_id not in (select pt.project_team_id from project_team pt )
							UNION
							SELECT 'asset_entity' as mainTable,'target_team_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.target_team_id not in (select pt.project_team_id from project_team pt )) a
						WHERE a.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR ASSET_ENTITY_VARCHAR-------------------------------------------*/
						SELECT * FROM (	SELECT 'asset_entity_varchar' as mainTable,'asset_entity_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity_varchar aev where aev.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )
							UNION
							SELECT 'asset_entity_varchar' as mainTable,'asset_entity_id' as refId,'Null' as type,count(*) as totalCount FROM asset_entity_varchar aev where aev.asset_entity_id is null
							UNION
							SELECT 'asset_entity_varchar' as mainTable,'attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity_varchar aev where aev.attribute_id not in (select ea.attribute_id from eav_attribute ea )
							UNION
							SELECT 'asset_entity_varchar' as mainTable,'attribute_id' as refId,'Null' as type,count(*) as totalCount FROM asset_entity_varchar aev where aev.attribute_id is null ) aev
						WHERE aev.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR ASSET_TRANSITION-------------------------------------------*/
						SELECT * FROM (	SELECT 'asset_transition' as mainTable,'asset_entity_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_transition at where at.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )
							UNION
							SELECT 'asset_transition' as mainTable,'asset_entity_id' as refId,'Null' as type,count(*) as totalCount FROM asset_transition at where at.asset_entity_id is null
							UNION
							SELECT 'asset_transition' as mainTable,'move_bundle_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_transition at where at.move_bundle_id not in (select m.move_bundle_id from move_bundle m )
							UNION
							SELECT 'asset_transition' as mainTable,'move_bundle_id' as refId,'Null' as type,count(*) as totalCount FROM asset_transition at where at.move_bundle_id is null
							UNION
							SELECT 'asset_transition' as mainTable,'user_login_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_transition at where at.user_login_id not in (select ul.user_login_id from user_login ul )
							UNION
							SELECT 'asset_transition' as mainTable,'user_login_id' as refId,'Null' as type,count(*) as totalCount FROM asset_transition at where at.user_login_id is null
							UNION
							SELECT 'asset_transition' as mainTable,'project_team_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_transition at where at.project_team_id not in (select pt.project_team_id from project_team pt ) ) at
						WHERE at.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR DATA_TRANSFER_ATTRIBUTE_MAP-------------------------------------------*/
						SELECT * FROM ( SELECT 'data_transfer_attribute_map' as mainTable,'eav_attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_attribute_map dam where dam.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)
							UNION
							SELECT 'data_transfer_attribute_map' as mainTable,'eav_attribute_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_attribute_map dam where dam.eav_attribute_id is null
							UNION
							SELECT 'data_transfer_attribute_map' as mainTable,'data_transfer_set_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_attribute_map dam where dam.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)
							UNION
							SELECT 'data_transfer_attribute_map' as mainTable,'data_transfer_set_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_attribute_map dam where dam.data_transfer_set_id is null) dam
						WHERE dam.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR DATA_TRANSFER_BATCH-------------------------------------------*/
						SELECT * FROM ( SELECT 'data_transfer_batch' as mainTable,'user_login_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.user_login_id not in (select ul.user_login_id from user_login ul)
							UNION
							SELECT 'data_transfer_batch' as mainTable,'user_login_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.user_login_id is null
							UNION
							SELECT 'data_transfer_batch' as mainTable,'data_transfer_set_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)
							UNION
							SELECT 'data_transfer_batch' as mainTable,'data_transfer_set_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.data_transfer_set_id is null
							UNION
							SELECT 'data_transfer_batch' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.project_id not in (select p.project_id from project p)
							UNION
							SELECT 'data_transfer_batch' as mainTable,'project_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_batch dtb where dtb.project_id is null) dtb
						WHERE dtb.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR DATA_TRANSFER_COMMENT-------------------------------------------*/
						SELECT * FROM ( SELECT 'data_transfer_comment' as mainTable,'data_transfer_batch_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_comment dtm where dtm.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)
							UNION
							SELECT 'data_transfer_comment' as mainTable,'data_transfer_batch_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_comment dtm where dtm.data_transfer_batch_id is null) dtm
						WHERE dtm.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR DATA_TRANSFER_COMMENT-------------------------------------------*/
						SELECT * FROM ( SELECT 'data_transfer_value' as mainTable,'data_transfer_batch_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)
							UNION
							SELECT 'data_transfer_value' as mainTable,'data_transfer_batch_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.data_transfer_batch_id is null
							UNION
							SELECT 'data_transfer_value' as mainTable,'eav_attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)
							UNION
							SELECT 'data_transfer_value' as mainTable,'eav_attribute_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.eav_attribute_id is null	) dtv
						WHERE dtv.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_BUNDLE-------------------------------------------*/
						SELECT * FROM  ( SELECT 'move_bundle' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_bundle m where m.project_id not in (select p.project_id from project p )
							UNION
							SELECT 'move_bundle' as mainTable,'project_id' as refId,'Null' as type,count(*) as totalCount FROM move_bundle m where m.project_id is null
							UNION
							SELECT 'move_bundle' as mainTable,'move_event_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_bundle m where m.move_event_id not in (select me.move_event_id from move_event me )
							UNION
							SELECT 'move_bundle' as mainTable,'move_event_id' as refId,'Null' as type,count(*) as totalCount FROM move_bundle m where m.move_event_id is null ) m
						WHERE m.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_BUNDLE_STEP-------------------------------------------*/
						SELECT * FROM  ( SELECT 'move_bundle_step' as mainTable,'move_bundle_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_bundle_step mbs where mbs.move_bundle_id not in (select m.move_bundle_id from move_bundle m )
							UNION
							SELECT 'move_bundle_step' as mainTable,'move_bundle_id' as refId,'Null' as type,count(*) as totalCount FROM move_bundle_step mbs where mbs.move_bundle_id is null ) mbs
						WHERE mbs.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_EVENT-------------------------------------------*/
						SELECT * FROM  ( SELECT 'move_event' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_event me where me.project_id not in (select p.project_id from project p )
							UNION
							SELECT 'move_event' as mainTable,'project_id' as refId,'Null' as type,count(*) as totalCount FROM move_event me where me.project_id is null ) me
						WHERE me.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_EVENT_NEWS-------------------------------------------*/
						SELECT * FROM ( SELECT 'move_event_news' as mainTable,'move_event_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_news men where men.move_event_id not in (select me.move_event_id from move_event me )
							UNION
							SELECT 'move_event_news' as mainTable,'move_event_id' as refId,'Null' as type,count(*) as totalCount FROM move_event_news men where men.move_event_id is null
							UNION
							SELECT 'move_event_news' as mainTable,'created_by' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_news men where men.created_by not in (select p.person_id from person p )
							UNION
							SELECT 'move_event_news' as mainTable,'created_by' as refId,'Null' as type,count(*) as totalCount FROM move_event_news men where men.created_by is null
							UNION
							SELECT 'move_event_news' as mainTable,'resolved_by' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_news men where men.archived_by not in (select p.person_id from person p )) men
						WHERE men.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_EVENT_SNAPSHOT-------------------------------------------*/
						SELECT * FROM  ( SELECT 'move_event_snapshot' as mainTable,'move_event_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_snapshot mes where mes.move_event_id not in (select me.move_event_id from move_event me )
							UNION
							SELECT 'move_event_snapshot' as mainTable,'move_event_id' as refId,'Null' as type,count(*) as totalCount FROM move_event_snapshot mes where mes.move_event_id is null) mes
						WHERE mes.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PARTY-------------------------------------------*/
						SELECT * FROM  ( SELECT 'party' as mainTable,'party_id' as refId,'Orphan' as type,count(*) as totalCount FROM party p 
										where p.party_id not in (select distinct pr.party_id from 
													( select party_group_id as party_id from party_group
													union
													select person_id as party_id from person
													union
													select move_bundle_id as party_id from move_bundle
													union
													select project_id as party_id from project
													union
													select project_team_id as party_id from project_team
													union
													select app_id as party_id from application ) pr	)
							UNION
							SELECT 'party' as mainTable,'party_type_id' as refId,'Orphan' as type,count(*) as totalCount FROM party p where p.party_type_id not in (select pt.party_type_code from party_type pt) ) p
						WHERE p.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PARTY_RELATIONSHIP-----------------------------------------*/
						SELECT * FROM  ( SELECT 'party_relationship' as mainTable,'party_id_from_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_relationship pr where pr.party_id_from_id in ( $orphanParty )
							UNION
							SELECT 'party_relationship' as mainTable,'party_id_from_id' as refId,'Null' as type,count(*) as totalCount FROM party_relationship pr where pr.party_id_from_id is null
							UNION
							SELECT 'party_relationship' as mainTable,'party_id_to_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_relationship pr where pr.party_id_to_id in ( $orphanParty )
							UNION
							SELECT 'party_relationship' as mainTable,'party_id_to_id' as refId,'Null' as type,count(*) as totalCount FROM party_relationship pr where pr.party_id_to_id is null
							UNION
							SELECT 'party_relationship' as mainTable,'role_type_code_from_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_relationship pr where pr.role_type_code_from_id not in (select rt.role_type_code from role_type rt )
							UNION
							SELECT 'party_relationship' as mainTable,'role_type_code_from_id' as refId,'Null' as type,count(*) as totalCount FROM party_relationship pr where pr.role_type_code_from_id is null
							UNION
							SELECT 'party_relationship' as mainTable,'role_type_code_to_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_relationship pr where pr.role_type_code_to_id not in (select rt.role_type_code from role_type rt )
							UNION
							SELECT 'party_relationship' as mainTable,'role_type_code_to_id' as refId,'Null' as type,count(*) as totalCount FROM party_relationship pr where pr.role_type_code_to_id is null
							UNION
							SELECT 'party_relationship' as mainTable,'party_relationship_type_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_relationship pr where pr.party_relationship_type_id not in (select prt.party_relationship_type_code from party_relationship_type prt )
							UNION
							SELECT 'party_relationship' as mainTable,'party_relationship_type_id' as refId,'Null' as type,count(*) as totalCount FROM party_relationship pr where pr.party_relationship_type_id is null ) pr
						WHERE pr.totalCount > 0	
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PARTY_ROLE-----------------------------------------*/
						SELECT * FROM  (SELECT 'party_role' as mainTable,'party_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_role pr where pr.party_id not in (select p.party_id from party p)
							UNION
							SELECT 'party_role' as mainTable,'party_id' as refId,'Null' as type,count(*) as totalCount FROM party_role pr where pr.party_id is null
							UNION
							SELECT 'party_role' as mainTable,'role_type_id' as refId,'Orphan' as type,count(*) as totalCount FROM party_role pr where pr.role_type_id not in (select rt.role_type_code from role_type rt)
							UNION
							SELECT 'party_role' as mainTable,'role_type_id' as refId,'Null' as type,count(*) as totalCount FROM party_role pr where pr.role_type_id is null ) pr
						WHERE pr.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PROJECT-----------------------------------------*/
						SELECT * FROM  (SELECT 'project' as mainTable,'client_id' as refId,'Orphan' as type,count(*) as totalCount FROM project pr where pr.client_id not in (select pg.party_group_id from party_group pg)
							UNION
							SELECT 'project' as mainTable,'client_id' as refId,'Null' as type,count(*) as totalCount FROM project pr where pr.client_id is null ) pr
						WHERE pr.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PROJECT_ASSET_MAP-----------------------------------------*/
						SELECT * FROM  ( SELECT 'project_asset_map' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_asset_map pam where pam.project_id not in (select pr.project_id from project pr)
							UNION
							SELECT 'project_asset_map' as mainTable,'project_id' as refId,'Null' as type,count(*) as totalCount FROM project_asset_map pam where pam.project_id is null
							UNION
							SELECT 'project_asset_map' as mainTable,'asset_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_asset_map pam where pam.asset_id not in (select ae.asset_entity_id from asset_entity ae)
							UNION
							SELECT 'project_asset_map' as mainTable,'asset_id' as refId,'Null' as type,count(*) as totalCount FROM project_asset_map pam where pam.asset_id is null ) pam
						WHERE pam.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PROJECT_LOGO-----------------------------------------*/
						SELECT * FROM  ( SELECT 'project_logo' as mainTable,'party_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_logo pl where pl.party_id not in (select pg.party_group_id from party_group pg)
							UNION
							SELECT 'project_logo' as mainTable,'party_id' as refId,'Null' as type,count(*) as totalCount FROM project_logo pl where pl.party_id is null
							UNION
							SELECT 'project_logo' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_logo pl where pl.project_id not in (select pr.project_id from project pr)
							UNION
							SELECT 'project_logo' as mainTable,'project_id' as refId,'Null' as type,count(*) as totalCount FROM project_logo pl where pl.project_id is null) pl
						WHERE pl.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR PROJECT_TEAM-----------------------------------------*/
						SELECT * FROM  ( SELECT 'project_team' as mainTable,'move_bundle_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_team pt where pt.move_bundle_id not in (select m.move_bundle_id from move_bundle m)
							UNION
							SELECT 'project_team' as mainTable,'move_bundle_id' as refId,'Null' as type,count(*) as totalCount FROM project_team pt where pt.move_bundle_id is null
							UNION
							SELECT 'project_team' as mainTable,'latest_asset_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_team pt where pt.latest_asset_id not in (select ae.asset_entity_id from asset_entity ae)) pt
						WHERE pt.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR STEP_SNAPSHOT-----------------------------------------*/
						SELECT * FROM  ( SELECT 'step_snapshot' as mainTable,'move_bundle_step_id' as refId,'Orphan' as type,count(*) as totalCount FROM step_snapshot ss where ss.move_bundle_step_id not in (select m.id from move_bundle_step m)
							UNION
							SELECT 'step_snapshot' as mainTable,'move_bundle_step_id' as refId,'Null' as type,count(*) as totalCount FROM step_snapshot ss where ss.move_bundle_step_id is null ) ss
						WHERE ss.totalCount > 0
						
						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR USER_LOGIN-----------------------------------------*/
						SELECT * FROM  ( SELECT 'user_login' as mainTable,'person_id' as refId,'Orphan' as type,count(*) as totalCount FROM user_login ul where ul.person_id not in (select per.person_id from person per)
							UNION
							SELECT 'user_login' as mainTable,'person_id' as refId,'Null' as type,count(*) as totalCount FROM user_login ul where ul.person_id is null ) ul
						WHERE ul.totalCount > 0

						UNION
						/*-----------------------------------ORPHAN RESULTS QUERY FOR USER_PREFERENCE-----------------------------------------*/
						SELECT * FROM  (  SELECT 'user_preference' as mainTable,'user_login_id' as refId,'Orphan' as type,count(*) as totalCount FROM user_preference up where up.user_login_id not in (select ul.user_login_id from user_login ul)
							UNION
							SELECT 'user_preference' as mainTable,'user_login_id' as refId,'Null' as type,count(*) as totalCount FROM user_preference up where up.user_login_id is null) up
						WHERE up.totalCount > 0

						"""
			def summaryRecords = jdbcTemplate.queryForList( summaryQuery )
    		return[summaryRecords : summaryRecords];	
    }
}
