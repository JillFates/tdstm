import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.runtime.TimeCategory
import org.apache.commons.lang.math.NumberUtils

import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil

class AdminController {
	def jdbcTemplate
	def sessionFactory
	def securityService
	def partyRelationshipService
	def userPreferenceService
	def projectService

	def index = { }


	def orphanSummary = {
		def summaryRecords = []
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
								
	def AssetsummaryQuery = """
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
				SELECT 'asset_entity' as mainTable,'target_team_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.target_team_id not in (select pt.project_team_id from project_team pt )
				UNION
				SELECT 'asset_entity' as mainTable,'model_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.model_id not in (select m.model_id from model m)
				UNION
				SELECT 'asset_entity' as mainTable,'manufacturer_id' as refId,'Orphan' as type,count(*) as totalCount FROM asset_entity a where a.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn)) a
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
			/*-----------------------------------ORPHAN RESULTS QUERY FOR MODEL-------------------------------------------*/
			SELECT * FROM (	SELECT 'model' as mainTable,'manufacturer_id' as refId,'Orphan' as type,count(*) as totalCount FROM model m where m.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn) OR m.manufacturer_id is null OR m.manufacturer_id = '' ) aev
			WHERE aev.totalCount > 0
			
			UNION 

			/*-----------------------------------ORPHAN RESULTS QUERY FOR MODEL WHERE Manufacturers with no Models -------------------------------------------*/
			SELECT * FROM (	SELECT 'manufacturer' as mainTable,'manufacturer_id' as refId,'Orphan' as type,count(*) as totalCount FROM manufacturer mn where mn.manufacturer_id not in (select m.manufacturer_id from model m) ) mnm
			WHERE mnm.totalCount > 0

			UNION 

			/*-----------------------------------ORPHAN RESULTS QUERY FOR MODEL WHERE Models with no assets-------------------------------------------*/
			SELECT * FROM (	SELECT 'model' as mainTable,'model_id' as refId,'Orphan' as type,count(*) as totalCount FROM model m where m.model_id not in (select ae.model_id from asset_entity ae where ae.model_id is not null)  ) mae
			WHERE mae.totalCount > 0
			
			UNION 

			/*-----------------------------------ORPHAN RESULTS QUERY FOR MODEL WHERE Models with no assets-------------------------------------------*/
			SELECT * FROM (	SELECT 'model_connector' as mainTable,'model_id' as refId,'Orphan' as type,count(*) as totalCount FROM model_connector mn where mn.model_id not in (select m.model_id from model m)  ) mae
			WHERE mae.totalCount > 0
			"""
		
		summaryRecords << jdbcTemplate.queryForList( AssetsummaryQuery )

		def dataTransferSummaryQuery = """
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
			/*-----------------------------------ORPHAN RESULTS QUERY FOR DATA_TRANSFER_VALUE-------------------------------------------*/
			SELECT * FROM ( SELECT 'data_transfer_value' as mainTable,'data_transfer_batch_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)
				UNION
				SELECT 'data_transfer_value' as mainTable,'data_transfer_batch_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.data_transfer_batch_id is null
				UNION
				SELECT 'data_transfer_value' as mainTable,'eav_attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)
				UNION
				SELECT 'data_transfer_value' as mainTable,'eav_attribute_id' as refId,'Null' as type,count(*) as totalCount FROM data_transfer_value dtv where dtv.eav_attribute_id is null	) dtv
			WHERE dtv.totalCount > 0"""
		
		summaryRecords << jdbcTemplate.queryForList( dataTransferSummaryQuery )				
		
		def eavSummaryQuery = """
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ATTRIBUTE-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_attribute' as mainTable,'entity_type_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_attribute ea where ea.entity_type_id not in (select et.entity_type_id from eav_entity_type et)
				UNION
				SELECT 'eav_attribute' as mainTable,'entity_type_id' as refId,'Null' as type,count(*) as totalCount FROM eav_attribute ea where ea.entity_type_id is null) ea
			WHERE ea.totalCount > 0
				
			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ATTRIBUTE_OPTION-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_attribute_option' as mainTable,'attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_attribute_option eao where eao.attribute_id not in (select ea.attribute_id from eav_attribute ea)
				UNION
				SELECT 'eav_attribute_option' as mainTable,'attribute_id' as refId,'Null' as type,count(*) as totalCount FROM eav_attribute_option eao where eao.attribute_id is null) eao
			WHERE eao.totalCount > 0
				
			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ATTRIBUTE_SET-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_attribute_set' as mainTable,'entity_type_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_attribute_set eas where eas.entity_type_id not in (select et.entity_type_id from eav_entity_type et)
				UNION
				SELECT 'eav_attribute_set' as mainTable,'entity_type_id' as refId,'Null' as type,count(*) as totalCount FROM eav_attribute_set eas where eas.entity_type_id is null) eas
			WHERE eas.totalCount > 0
			
			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ENTITY-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_entity' as mainTable,'attribute_set_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_entity ee where ee.attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)
				UNION
				SELECT 'eav_entity' as mainTable,'attribute_set_id' as refId,'Null' as type,count(*) as totalCount FROM eav_entity ee where ee.attribute_set_id is null) ee
			WHERE ee.totalCount > 0
			
			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ENTITY_ATTRIBUTE-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_entity_attribute' as mainTable,'eav_attribute_set_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_entity_attribute eea where eea.eav_attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)
				UNION
				SELECT 'eav_entity_attribute' as mainTable,'eav_attribute_set_id' as refId,'Null' as type,count(*) as totalCount FROM eav_entity_attribute eea where eea.eav_attribute_set_id is null
				UNION
				SELECT 'eav_entity_attribute' as mainTable,'attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_entity_attribute eea where eea.attribute_id not in (select ea.attribute_id from eav_attribute ea)
				UNION
				SELECT 'eav_entity_attribute' as mainTable,'attribute_id' as refId,'Null' as type,count(*) as totalCount FROM eav_entity_attribute eea where eea.attribute_id is null
				UNION
				SELECT 'eav_entity_attribute' as mainTable,'eav_entity_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_entity_attribute eea where eea.eav_entity_id not in (select ee.entity_id from eav_entity ee)) eea
			WHERE eea.totalCount > 0

			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR EAV_ENTITY_DATATYPE-------------------------------------------*/
			SELECT * FROM ( SELECT 'eav_entity_datatype' as mainTable,'attribute_id' as refId,'Orphan' as type,count(*) as totalCount FROM eav_entity_datatype eed where eed.attribute_id not in (select ea.attribute_id from eav_attribute ea)
				UNION
				SELECT 'eav_entity_datatype' as mainTable,'attribute_id' as refId,'Null' as type,count(*) as totalCount FROM eav_entity_datatype eed where eed.attribute_id is null ) eed
			WHERE eed.totalCount > 0
			"""
		
		summaryRecords << jdbcTemplate.queryForList( eavSummaryQuery )	
		
		def moveSummaryQuery = """ 
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
				SELECT 'move_event_news' as mainTable,'archived_by' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_news men where men.archived_by not in (select p.person_id from person p )) men
			WHERE men.totalCount > 0

			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR MOVE_EVENT_SNAPSHOT-------------------------------------------*/
			SELECT * FROM  ( SELECT 'move_event_snapshot' as mainTable,'move_event_id' as refId,'Orphan' as type,count(*) as totalCount FROM move_event_snapshot mes where mes.move_event_id not in (select me.move_event_id from move_event me )
				UNION
				SELECT 'move_event_snapshot' as mainTable,'move_event_id' as refId,'Null' as type,count(*) as totalCount FROM move_event_snapshot mes where mes.move_event_id is null) mes
			WHERE mes.totalCount > 0
						
			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR STEP_SNAPSHOT-----------------------------------------*/
			SELECT * FROM  ( SELECT 'step_snapshot' as mainTable,'move_bundle_step_id' as refId,'Orphan' as type,count(*) as totalCount FROM step_snapshot ss where ss.move_bundle_step_id not in (select m.id from move_bundle_step m)
				UNION
				SELECT 'step_snapshot' as mainTable,'move_bundle_step_id' as refId,'Null' as type,count(*) as totalCount FROM step_snapshot ss where ss.move_bundle_step_id is null ) ss
			WHERE ss.totalCount > 0	"""
		
		summaryRecords << jdbcTemplate.queryForList( moveSummaryQuery )	
			
		def partySummaryQuery = """
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
			WHERE up.totalCount > 0	"""
			
			summaryRecords << jdbcTemplate.queryForList( partySummaryQuery )

			return[summaryRecords : summaryRecords];	
	}
	/*
	 * 
	 */
	def orphanDetails = {
		def orphanDeatils
		def table = params.table
		def column = params.column
		def type = params.type
		def query = ""
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
		switch (table) {
			case "application" :
				if(type != "Null"){
					query = "SELECT * FROM application app where app.owner_id not in (select p.party_group_id from party_group p )"
				} else {
					query = "SELECT * FROM application app where app.owner_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "application_asset_map" :
				switch (column){
					case "application_id" :
						if(type != "Null"){
							query = "SELECT * FROM application_asset_map asm where asm.application_id not in (select app.app_id from application app )"
						} else {
							query = "SELECT * FROM application_asset_map asm where asm.application_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "asset_id" :
						if(type != "Null"){
							query = "SELECT * FROM application_asset_map asm where asm.asset_id not in (select ae.asset_entity_id from asset_entity ae )"
						} else {
							query = "SELECT * FROM application_asset_map asm where asm.asset_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "asset_comment" :
				switch (column){
					case "asset_entity_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_comment ac where ac.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )"
						} else {
							query = "SELECT * FROM asset_comment ac where ac.asset_entity_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "created_by" :
						if(type != "Null"){
							query = "SELECT * FROM asset_comment ac where ac.created_by not in (select p.person_id from person p )"
						} else {
							query = "SELECT * FROM asset_comment ac where ac.created_by is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "resolved_by" :
						query = "SELECT * FROM asset_comment ac where ac.resolved_by not in (select p.person_id from person p )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "asset_entity" :
				switch (column){
					case "project_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_entity a where a.project_id not in (select p.project_id from project p )"
						} else {
							query = "SELECT * FROM asset_entity a where a.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "owner_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_entity a where a.owner_id not in (select p.party_group_id from party_group p )"
						} else {
							query = "SELECT * FROM asset_entity a where a.owner_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "move_bundle_id" :
						query = "SELECT * FROM asset_entity a where a.move_bundle_id not in (select m.move_bundle_id from move_bundle m )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "source_team_id" :
						query = "SELECT * FROM asset_entity a where a.source_team_id not in (select pt.project_team_id from project_team pt )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "target_team_id" :
						query = "SELECT * FROM asset_entity a where a.target_team_id not in (select pt.project_team_id from project_team pt )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "model_id" :
						query = "SELECT * FROM asset_entity a where a.model_id not in (select m.model_id from model m ) and a.model_id is not null "
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "manufacturer_id" :
						query = "SELECT * FROM asset_entity a where a.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn) and a.manunfacturer_id is not null"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "asset_entity_varchar" :
				switch (column) {
					case "asset_entity_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_entity_varchar aev where aev.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )"
						} else {
							query = "SELECT * FROM asset_entity_varchar aev where aev.asset_entity_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "attribute_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_entity_varchar aev where aev.attribute_id not in (select ea.attribute_id from eav_attribute ea )"
						} else {
							query = "SELECT * FROM asset_entity_varchar aev where aev.attribute_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "asset_transition" :
				switch (column) {
					case "asset_entity_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_transition at where at.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )"
						} else {
							query = "SELECT * FROM asset_transition at where at.asset_entity_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "move_bundle_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_transition at where at.move_bundle_id not in (select m.move_bundle_id from move_bundle m )"
						} else {
							query = "SELECT * FROM asset_transition at where at.move_bundle_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "user_login_id" :
						if(type != "Null"){
							query = "SELECT * FROM asset_transition at where at.user_login_id not in (select ul.user_login_id from user_login ul )"
						} else {
							query = "SELECT * FROM asset_transition at where at.user_login_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "project_team_id" :
						query = "SELECT * FROM asset_transition at where at.project_team_id not in (select pt.project_team_id from project_team pt ) "
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "data_transfer_attribute_map" :
				switch (column){
					case "eav_attribute_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_attribute_map dam where dam.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)"
						} else {
							query = "SELECT * FROM data_transfer_attribute_map dam where dam.eav_attribute_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "data_transfer_set_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_attribute_map dam where dam.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)"
						} else {
							query = "SELECT * FROM data_transfer_attribute_map dam where dam.data_transfer_set_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "data_transfer_batch" :
				switch (column){
					case "user_login_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_batch dtb where dtb.user_login_id not in (select ul.user_login_id from user_login ul)"
						} else {
							query = "SELECT * FROM data_transfer_batch dtb where dtb.user_login_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "data_transfer_set_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_batch dtb where dtb.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)"
						} else {
							query = "SELECT * FROM data_transfer_batch dtb where dtb.data_transfer_set_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "project_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_batch dtb where dtb.project_id not in (select p.project_id from project p)"
						} else {
							query = "SELECT * FROM data_transfer_batch dtb where dtb.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "data_transfer_comment" :
				if(type != "Null"){
					query = "SELECT * FROM data_transfer_comment dtm where dtm.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)"
				} else {
					query = "SELECT * FROM data_transfer_comment dtm where dtm.data_transfer_batch_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "data_transfer_value" :
				switch (column){
					case "data_transfer_batch_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_value dtv where dtv.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)"
						} else {
							query = "SELECT * FROM data_transfer_value dtv where dtv.data_transfer_batch_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "eav_attribute_id" :
						if(type != "Null"){
							query = "SELECT * FROM data_transfer_value dtv where dtv.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)"
						} else {
							query = "SELECT * FROM data_transfer_value dtv where dtv.eav_attribute_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "eav_attribute" :
				if(type != "Null"){
					query = "SELECT * FROM eav_attribute ea where ea.entity_type_id not in (select et.entity_type_id from eav_entity_type et)"
				} else {
					query = "SELECT * FROM eav_attribute ea where ea.entity_type_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "eav_attribute_option" :
				if(type != "Null"){
					query = "SELECT * FROM eav_attribute_option eao where eao.attribute_id not in (select ea.attribute_id from eav_attribute ea)"
				} else {
					query = "SELECT * FROM eav_attribute_option eao where eao.attribute_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
			
			case "eav_attribute_set" :
				if(type != "Null"){
					query = "SELECT * FROM eav_attribute_set eas where eas.entity_type_id not in (select et.entity_type_id from eav_entity_type et)"
				} else {
					query = "SELECT * FROM eav_attribute_set eas where eas.entity_type_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "eav_entity" :
				if(type != "Null"){
					query = "SELECT * FROM eav_entity ee where ee.attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)"
				} else {
					query = "SELECT * FROM eav_entity ee where ee.attribute_set_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
			
			case "eav_entity_attribute" :
				switch (column){
					case "eav_attribute_set_id" :
						if(type != "Null"){
							query = "SELECT * FROM eav_entity_attribute eea where eea.eav_attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)"
						} else {
							query = "SELECT * FROM eav_entity_attribute eea where eea.eav_attribute_set_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "attribute_id" :
						if(type != "Null"){
							query = "SELECT * FROM eav_entity_attribute eea where eea.attribute_id not in (select ea.attribute_id from eav_attribute ea)"
						} else {
							query = "SELECT * FROM eav_entity_attribute eea where eea.attribute_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "eav_entity_id" :
						query = "SELECT * FROM eav_entity_attribute eea where eea.eav_entity_id not in (select ee.entity_id from eav_entity ee)"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "eav_entity_datatype" :
				if(type != "Null"){
					query = "SELECT * FROM eav_entity_datatype eed where eed.attribute_id not in (select ea.attribute_id from eav_attribute ea)"
				} else {
					query = "SELECT * FROM eav_entity_datatype eed where eed.attribute_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
			case "manufacturer" :
				switch (column){
					case "manufacturer_id" :
						query = "SELECT * FROM manufacturer mn where mn.manufacturer_id not in (select m.manufacturer_id from model m)"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			case "model" :
				switch (column){
					case "manufacturer_id" :
						query = "SELECT * FROM model m where m.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn) OR m.manufacturer_id is null OR m.manufacturer_id = ''"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "model_id" :
						query = "SELECT * FROM model m where m.model_id not in (select ae.model_id from asset_entity ae where ae.model_id is not null) "
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			case "model_connector" :
				switch (column){
					case "model_id" :
						query = "SELECT * FROM model_connector mn where mn.model_id not in (select m.model_id from model m ) "
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			case "move_bundle" :
				switch (column){
					case "project_id" :
						if(type != "Null"){
							query = "SELECT * FROM move_bundle m where m.project_id not in (select p.project_id from project p )"
						} else {
							query = "SELECT * FROM move_bundle m where m.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "move_event_id" :
						if(type != "Null"){
							query = "SELECT * FROM move_bundle m where m.move_event_id not in (select me.move_event_id from move_event me )"
						} else {
							query = "SELECT * FROM move_bundle m where m.move_event_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;

				}
			break;
			
			case "move_bundle_step" :
				if(type != "Null"){
					query = "SELECT * FROM move_bundle_step mbs where mbs.move_bundle_id not in (select m.move_bundle_id from move_bundle m )"
				} else {
					query = "SELECT * FROM move_bundle_step mbs where mbs.move_bundle_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "move_event" :
				if(type != "Null"){
					query = "SELECT * FROM move_event me where me.project_id not in (select p.project_id from project p )"
				} else {
					query = "SELECT * FROM move_event me where me.project_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "move_event_news" :
				switch (column){
					case "move_event_id" :
						if(type != "Null"){
							query = "SELECT * FROM move_event_news men where men.move_event_id not in (select me.move_event_id from move_event me )"
						} else {
							query = "SELECT * FROM move_event_news men where men.move_event_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "created_by" :
						if(type != "Null"){
							query = "SELECT * FROM move_event_news men where men.created_by not in (select p.person_id from person p )"
						} else {
							query = "SELECT * FROM move_event_news men where men.created_by is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "archived_by" :
						query = "SELECT * FROM move_event_news men where men.archived_by not in (select p.person_id from person p )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "move_event_snapshot" :
				if(type != "Null"){
					query = "SELECT * FROM move_event_snapshot mes where mes.move_event_id not in (select me.move_event_id from move_event me )"
				} else {
					query = "SELECT * FROM move_event_snapshot mes where mes.move_event_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "step_snapshot" :
				if(type != "Null"){
					query = "SELECT * FROM step_snapshot ss where ss.move_bundle_step_id not in (select m.id from move_bundle_step m)"
				} else {
					query = "SELECT * FROM step_snapshot ss where ss.move_bundle_step_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "party":
				switch (column){
					case "party_id" :
	        		query = """SELECT * FROM party p where p.party_id not in
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
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "" :
					query = "SELECT * FROM party p where p.party_type_id not in (select pt.party_type_code from party_type pt)"
					orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
		break;
		
			case "party_relationship" :
				switch (column){
					case "party_id_from_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_relationship pr where pr.party_id_from_id in ( $orphanParty )"
						} else {
							query = "SELECT * FROM party_relationship pr where pr.party_id_from_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "party_id_to_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_relationship pr where pr.party_id_to_id in ( $orphanParty )"
						} else {
							query = "SELECT * FROM party_relationship pr where pr.party_id_to_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "role_type_code_from_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_from_id not in (select rt.role_type_code from role_type rt )"
						} else {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_from_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "role_type_code_to_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_to_id not in (select rt.role_type_code from role_type rt )"
						} else {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_to_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "party_relationship_type_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_relationship pr where pr.party_relationship_type_id not in (select prt.party_relationship_type_code from party_relationship_type prt )"
						} else {
							query = "SELECT * FROM party_relationship pr where pr.party_relationship_type_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			case "party_role" :
				switch (column){
					case "party_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_role pr where pr.party_id not in (select p.party_id from party p)"
						} else {
							query = "SELECT * FROM party_role pr where pr.party_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "role_type_id" :
						if(type != "Null"){
							query = "SELECT * FROM party_role pr where pr.role_type_id not in (select rt.role_type_code from role_type rt)"
						} else {
							query = "SELECT * FROM party_role pr where pr.role_type_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "project" :
				if(type != "Null"){
					query = "SELECT * FROM project pr where pr.client_id not in (select pg.party_group_id from party_group pg)"
				} else {
					query = "SELECT * FROM project pr where pr.client_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
				
			case "project_asset_map" :
				switch (column){
					case "project_id" :
						if(type != "Null"){
							query = "SELECT * FROM project_asset_map pam where pam.project_id not in (select pr.project_id from project pr)"
						} else {
							query = "SELECT * FROM project_asset_map pam where pam.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "asset_id" :
						if(type != "Null"){
							query = "SELECT * FROM project_asset_map pam where pam.asset_id not in (select ae.asset_entity_id from asset_entity ae)"
						} else {
							query = "SELECT * FROM project_asset_map pam where pam.asset_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "project_logo" :
				switch (column){
					case "party_id" :
						if(type != "Null"){
							query = "SELECT * FROM project_logo pl where pl.party_id not in (select pg.party_group_id from party_group pg)"
						} else {
							query = "SELECT * FROM project_logo pl where pl.party_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "project_id" :
						if(type != "Null"){
							query = "SELECT * FROM project_logo pl where pl.project_id not in (select pr.project_id from project pr)"
						} else {
							query = "SELECT * FROM project_logo pl where pl.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "project_team" :
				switch (column){
					case "move_bundle_id" :
						if(type != "Null"){
							query = "SELECT * FROM project_team pt where pt.move_bundle_id not in (select m.move_bundle_id from move_bundle m)"
						} else {
							query = "SELECT * FROM project_team pt where pt.move_bundle_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
					case "latest_asset_id" :
						query = "SELECT * FROM project_team pt where pt.latest_asset_id not in (select ae.asset_entity_id from asset_entity ae)"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break;
				}
			break;
			
			case "user_login" :
				if(type != "Null"){
					query = "SELECT * FROM user_login ul where ul.person_id not in (select per.person_id from person per)"
				} else {
					query = "SELECT * FROM user_login ul where ul.person_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
			case "user_preference" :
				if(type != "Null"){
					query = "SELECT * FROM user_preference up where up.user_login_id not in (select ul.user_login_id from user_login ul)"
				} else {
					query = "SELECT * FROM user_preference up where up.user_login_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break;
		}// END OF MAIN SWITCH
		def results = [orphanDeatils : orphanDeatils, query:query]  
		render results as JSON
	}
	/**
	 * this method is used to flush imported processed data or unprocessed data or both
	 * @param deleteHistory : the time constraint the end user want to delete the records .
	 * @return : count of record that is deleted.
	 */
	
	def processOldData = {
		def deleteHistory = params.deleteHistory
		StringBuffer queryForData = new StringBuffer("FROM data_transfer_value")
		StringBuffer queryForBatch = new StringBuffer("FROM data_transfer_batch")
		switch(deleteHistory){
			case "anyProcessed" :
				queryForBatch.append(" WHERE status_code = 'completed'")
				queryForData.append(" WHERE data_transfer_batch_id  IN (SELECT batch_id $queryForBatch) ")
			break;
			case "overTwoMonths" :
				queryForBatch.append(" WHERE date_created <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 60 DAY)")
				queryForData.append(" WHERE data_transfer_batch_id IN (SELECT batch_id $queryForBatch )")
			break;
		}
		
		def records = jdbcTemplate.queryForInt("SELECT count(*) FROM (SELECT 1 as count ${queryForData} group by data_transfer_batch_id,row_id) a")
		
		jdbcTemplate.update("DELETE $queryForData")
		jdbcTemplate.update( "DELETE $queryForBatch")
		
		def msg = "Deleted  $records Records"
		render msg
	}
	
	/**
	 * This method is used to get the processed and pending batches counts
	 * @params N/A : 
	 * @return : String formatted to display processed and pending batches and records
	 */
	def getBatchRecords = {
		String queryForRecords ="""SELECT COUNT(*) AS batchCount, IF(SUM(noOfRows),SUM(noOfRows),0) AS records
									  FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows FROM (SELECT data_transfer_batch_id, COUNT(*) AS noOfRows  FROM data_transfer_value dtv
									  LEFT OUTER JOIN data_transfer_batch dtb ON dtv.data_transfer_batch_id = dtb.batch_id
									  WHERE dtb.status_code = 'completed' GROUP BY data_transfer_batch_id, row_id ) a GROUP BY data_transfer_batch_id ) b
								   UNION
								   SELECT COUNT(*) AS batchCount, IF(SUM(noOfRows),SUM(noOfRows),0) AS records
									 FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows  FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows  FROM data_transfer_value dtv
									 LEFT OUTER JOIN data_transfer_batch dtb ON dtv.data_transfer_batch_id = dtb.batch_id
									 WHERE dtb.status_code = 'pending'	GROUP BY data_transfer_batch_id, row_id ) a GROUP BY data_transfer_batch_id ) b;
								"""
		
		def recordsLegend = jdbcTemplate.queryForList( queryForRecords ) 
		
		def pendingInfo = "Current: ${recordsLegend[0].batchCount} batches / ${recordsLegend[0].records} records process,"+
						  (recordsLegend[1] ? "${recordsLegend[1].batchCount} batches / ${recordsLegend[1].records} records pending" : "0 batches / 0 records pending")
		
		render pendingInfo
	}
	
	/**
	 * This method is used to get the Asset type and their respective Asset Count and Model Count
	 * @param:N/A
	 */
	def getAssetTypes ={
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute , [sort:"value"]).value
		def returnMap = []
		assetTypeOptions.remove("Blade") // TODO : temp fix to resolve the below issue
		assetTypeOptions.each{type->
			def modelCount = Model.findAllByAssetType(type)
			def assets = AssetEntity.findAllByAssetType(type) // TODO : getting Exception when type=Blade. assuming data issue. 
			Set projects = assets?.project
			returnMap << [type, assets.size(), modelCount.size(), 
						assets[0]? assets[0]?.project.name : '', projects.size() > 1 ? projects.size()-1 : '']
		}
		render (template:'getAssetTypes', model:['returnMap':returnMap])
	}
	
	/**
	 * This method is used to clean the UnUsed Asset types
	 */
	def cleanAssetTypes = {
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute , [sort:"value"]).value
		def deletedTypes = []
		assetTypeOptions.each{type->
			def assetCount = AssetEntity.countByAssetType(type)
			def modelCount = Model.countByAssetType(type)
			if(assetCount==0 && modelCount==0){
				def eavAttributeOption = EavAttributeOption.findByValue(type)
				if(eavAttributeOption){
					deletedTypes << type
					eavAttributeOption.delete()
				}
			}
		}
		def msg = deletedTypes ? "Removed ${deletedTypes.size()} unused Types: ${WebUtil.listAsMultiValueString(deletedTypes)}" : ""
		render msg
	}

	/**
	 * Just a sample of the Twitter Bootstrap implementation
	 */
	def bootstrap = {
		def contextPath = request.contextPath
		return [contextPath:contextPath]
	}
	

	/**
	 * A controller process to import user accounts
	 */
	def importAccounts = {

		def people

		def project = securityService.getUserCurrentProject()

		def map = [step:'start', projectName:project.toString() ]
 
		def filename = '/tmp/tdstm-account-import.csv'

		// Inline closure to parse the CSV file and return array of mapped fields
		def parseCsv = {file, header ->
			def first = true
			people = []
			file.splitEachLine(",") { fields ->
				if (first && header) {
					first = false
				} else {
					people.add(
						username: fields[0],
						firstName: fields[1],
						middleName: fields[2],
						lastName: fields[3],
						email: fields[7],
						phone: fields[4]
					)
				}
			}
			return people
		}

		def lookForMatches = { 
			def staff = partyRelationshipService.getCompanyStaff( project.client.id )
			def matches = []
			// Look over the people and try to find them in the system and then mark them as existing if they are.
			for (int i=0 ; i < people.size; i++) {

				def person = staff.find {
					it.firstName == people[i].firstName &&
					(it.lastName == people[i].lastName || ( ! it.lastName && ! people[i].lastName)) &&
					(it.middleName == people[i].middleName || ( ! it.middleName && ! people[i].middleName))
				} 

				if (person) {
					people[i].match = 'person'
					matches << people[i]

				} else {
					if (people[i].username) {
						def user = UserLogin.findByUsername(people[i].username)
						if (user) {
							people[i].match = 'username:'+user.id 
							matches << people[i]
						}
					}
				}
			}
			return matches
		}

		switch (params.step) {

			case 'upload':
				if (params.verifyProject != 'Y') {
					flash.message = "You must confirm the project to import into before continuing"
					return map
				}
				
				// Handle the file upload
				def f = request.getFile('myFile')
				if (f.empty) {
					flash.message = 'Upload file appears to be empty'
					return map
				}

				// Save for step 3
				def upload = new File('/tmp/tdstm-account-import.csv')
				// upload.delete()
				f.transferTo(upload)
				def header = params.header == 'Y'

				people = parseCsv(upload, header)
				map.matches = lookForMatches()
				map.people = people
				map.step = 'review'
				map.header = params.header

				break

			case 'post':

				def createUserLogin = params.createUserlogin == 'Y'
				def activateLogin = params.activateLogin == 'Y'
				def randomPassword = params.randomPassword == 'Y'
				def forcePasswordChange = params.forcePasswordChange == 'Y'
				def password = params.password
				def expireDays = NumberUtils.toInt(params.expireDays,90)
				def header = params.header == 'Y'
				def role = createUserLogin ? params.role : ''

				def csv = new File('/tmp/tdstm-account-import.csv')

				people = parseCsv(csv, header)
				lookForMatches()

				if (password) {
					password = securityService.encrypt(password)
				}

				def expiryDate = new Date()

				use(TimeCategory) {
					expiryDate = expiryDate + expireDays.days
				}
				log.info "expiryDate=$expiryDate"

				def failedPeople = []
				def created = 0

				people.each() { p -> 
					if (! p.match ) {
						def failed = false
						def person = new Person(
							firstName:p.firstName, 
							middleName:p.middleName, 
							lastName:p.lastName,
							email:p.email,
							workPhone: p.phone,
							staffType: 'Salary'
							)
					
						if (person.validate() && person.save(flush:true)) {
							log.info "importAccounts() : created person $person"
							partyRelationshipService.addCompanyStaff(project.client, person)

							if (createUserLogin && p.username) {
								def u = new UserLogin(
									username: p.username,
									password: password,
									active: (activateLogin ? 'Y' : 'N'),
									expiryDate: expiryDate,
									person: person
									)

								if (! u.validate() || !u.save(flush:true)) {
									p.error = GormUtil.allErrorsString(u)
									failedPeople << p
									failed = true
								} else {
									if (role) {
										log.info "importAccounts() : creating Role $role for $person"
										userPreferenceService.setUserRoles([role], person.id)
									}

									log.info "importAccounts() : created UserLogin $u"
									def up = new UserPreference(
										userLogin: u,
										preferenceCode: 'CURR_PROJ',
										value: project.id.toString()
									)
									if (! up.validate() || ! up.save()) {
										log.error "importAccounts() : failed creating User Preference for $person : " + GormUtil.allErrorsString(up)
										p.error = "Setting Default Project Errored"
										failedPeople << p
									}
								}
							}
						} else {
							p.error = GormUtil.allErrorsString(person)
							failedPeople << p
							failed = true
						}

						if (! failed) created++

					}

				} // people.each


				map.step = 'results'
				map.failedPeople = failedPeople
				map.created = created
				break

			default: 
				break

		} // switch

		return map

	}
	/**
	 * A action to show project Summary report filters.
	 */
	def projectReport = {
		return
	}
	
	/**
	 * To Generate project Summary Web report
	 */
	def projectSummaryReport = {
		def person  = securityService.getUserLoginPerson().toString()
		def now = TimeUtil.nowGMT()
		def results = projectService.getProjectReportSummary( params )
		render (template :'projectSummaryReport', model:[results:results, person:person, time:now])
	}
}
