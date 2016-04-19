import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject

import groovy.time.TimeCategory
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.lang.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook

import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.WorkbookUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.common.security.*
import com.tdsops.common.os.Shell
// import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.SecurityUtil



//import org.springframework.web.multipart.*
//import org.springframework.web.multipart.commons.*

import java.util.UUID

class AdminController {
	def jdbcTemplate
	def sessionFactory
	def grailsApplication

	def accountImportExportService
	def auditService
	def coreService
	def controllerService
	def partyRelationshipService
	def projectService
	def securityService
	def userPreferenceService
	def userService

	private static String DEFAULT_ROLE = 'USER'
	private static String APP_RESTART_CMD_PROPERTY = 'admin.serviceRestartCommand'

	def index() { }

	/**
	 * Used to render the Application Restart Form
	 * @Permission RestartApplication
	 */
	def restartAppServiceForm(){
		if (!controllerService.checkPermission(this, 'RestartApplication')) { 
			return 
		}

		String restartCmd = coreService.getAppConfigSetting(APP_RESTART_CMD_PROPERTY)
		boolean restartable = restartCmd != null
		int activityTimeLimit = 5
		List users = userService.usernamesWithRecentActivity(activityTimeLimit)

		model:[ restartable: restartable, users: users, activityTimeLimit: activityTimeLimit ]
	}	

	/**
	 * Action to invoke a Application Restart process if the property has been configured with a proper command. This will 
	 * look for the tdstm.admin.serviceRestartCommand property and attempt to shell out to the OS and run if defined.
	 * @Permission RestartApplication
	 */
	def restartAppServiceAction(){
		String cmd = coreService.getAppConfigSetting(APP_RESTART_CMD_PROPERTY)

		if (! controllerService.checkPermission(this, 'RestartApplication')) {
			render(status: 401)
			return
		} else if (cmd == null) {
			render(status: 400, text:g.message(code:"tdstm.admin.serviceRestartCommand.error"))
			return
		} 

		def username = securityService.getUserLogin().username

		// Log to the app log as well as to the system log that we're going to restart the app
		def logStr = g.message(code:"tdstm.admin.serviceRestartCommand.log", args:[username, cmd])		 
		Shell.systemLog(logStr)
		log.info(logStr)

		List results = Shell.executeCommand(cmd)

		// Now log the results
		logStr = g.message(code:'tdstm.admin.serviceRestartCommand.results', args:[results.toString()])
		Shell.systemLog(logStr)
		log.info(logStr)

		render "OK"
	}
	
	def orphanSummary() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
			SELECT * FROM ( SELECT 'project_logo' as mainTable,'project_id' as refId,'Orphan' as type,count(*) as totalCount FROM project_logo pl where pl.project_id not in (select pr.project_id from project pr)
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
	def orphanDetails() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
	
	def processOldData() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
	def retrieveBatchRecords() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
	def retrieveAssetTypes() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
	def cleanAssetTypes() {
		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

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
	 * This method renders the Export Accounts form
	 */
	def exportAccounts() {
		def (project, user) = controllerService.getProjectAndUserForPage(this, 'PersonExport')
		if (!project) {
			return
		}
		def company = user.person.company
		Map model = [
			project: project.name,
			client:project.client.name, 
			company:company
		]

		Map paramDefaults = [
			'staffType': 'PROJ_STAFF',
			'includeLogin': 'Y',
			'loginChoice': '1'
		]
		paramDefaults.each { key, defVal ->
			model[key] = (params.containsKey(key) ? params[key] : defVal)
		}


		render(view:"exportAccounts", model:model)
	}

	/**
	 * Used to export project staff and users to a spreadsheet
	 * @response Excel Spreadsheet 
	 */
	def exportAccountsProcess() {
		def (project, byWhom) = controllerService.getProjectAndUserForPage(this, 'PersonExport')
		if (!project) {
			return
		}

		Map formOptions = [
			staffType: params.staffType,				// CLIENT_STAFF | AVAIL_STAFF | PROJ_STAFF
			includeLogin: params.includeLogin,			// Y)es if checked
			loginChoice: params.loginChoice				// 0=All, 1=Active, 2=Inactive
		]

		try {
			// def session = getSession()
			accountImportExportService.generateAccountsExportToBrowser(session, response, byWhom, project, formOptions) 
			return

		} catch (InvalidParamException e) {
			flash.message = e.getMessage()
		} catch (EmptyResultException e) {
			flash.message = e.getMessage()
		} catch (e) { 
			log.error "Exception occurred while exporting data" + e.printStackTrace()
			flash.message = "An error occurred while attempting to export accounts"
		}

		redirect (action:"exportAccounts", params:formOptions)	
	}

	/**
	 * Used to download the spreadsheet import template
	 */
	def importAccountsTemplate() {
		def (project, byWhom) = controllerService.getProjectAndUserForPage(this, 'PersonImport')
		if (!project) {
			return
		}

		try {
			// Formulate the download filename ExportAccounts + ProjectCode + yyyymmdd sans the extension
			String projectName = project.projectCode.replaceAll(' ','')
			String formattedDate = TimeUtil.formatDateTime(session, new Date(), TimeUtil.FORMAT_DATE_TIME_5)
			String filename = "${accountImportExportService.IMPORT_FILENAME_PREFIX}-$projectName-$formattedDate"

			accountImportExportService.generateImportTemplateToBrowser(session, response, byWhom, project, filename)
			return
		} catch (InvalidRequestException e) {
			flash.message = e.getMessage()
		} catch (DomainUpdateException e) {
			flash.message = e.getMessage()
		} catch (InvalidParamException e) {
			flash.message = e.getMessage()
		} catch (EmptyResultException e) {
			flash.message = e.getMessage()
		} catch (e) {
			log.error "Exception occurred while downloading the Account Import template : " + e.printStackTrace()
			flash.message = "An error occurred while attempting to download the Account Import template"
		}

		forward (action:'importAccounts')
	}
	
	/**
	 * Used to retrieve the account information during the import process after it has been read in from the 
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON { accounts: List of accounts }
	 */
	def importAccountsReviewData() {
		def (project, user) = controllerService.getProjectAndUserForPage(this, 'PersonExport')
		if (!project) {
			ServiceResults.respondWithError(response, flash.message)
			flash.message = ''
			return
		}
		if (! params.filename) {
			ServiceResults.respondWithError(response, "Request was missing the required filename reference")
			return
		}
		try {
			// TODO : JPM 4/2016 : importAccountsReviewData This method should be refactored so that the bulk of the logic
			// is implemented in the service.

			Map formOptions = accountImportExportService.importParamsToOptionsMap(params)
			List accounts = accountImportExportService.generateReviewData(session, user, project, params.filename, formOptions)

			ServiceResults.respondAsJson(response, accounts )

		} catch(e) {
			log.error "Exception occurred while importing data: " + e.printStackTrace()

			ServiceResults.respondWithError(response,
				[ "An error occurred while attempting to import accounts", e.getMessage()]  )
		}
	}

	/**
	 * Used to retrieve the account information during the import process after it has been read in from the 
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON { accounts: List of accounts }
	 */
	def importAccountsPostResultsData() {
		def (project, user) = controllerService.getProjectAndUserForPage(this, 'PersonExport')
		if (!project) {
			ServiceResults.respondWithError(response, flash.message)
			flash.message = ''
			return
		}
		if (! params.filename) {
			ServiceResults.respondWithError(response, "Request was missing the required filename reference")
			return
		}
		try {
			Map formOptions = accountImportExportService.importParamsToOptionsMap(params)
			accountImportExportService.generatePostResultsDataToBrowser(session, response, user, project, params.filename, formOptions)
			return 

		} catch(e) {
			log.error "importAccountsPostResultsData() Exception occurred while retrieving import accounts post results: " + e.printStackTrace()

			ServiceResults.respondWithError(response,
				[ "An error occurred while attempting to retrieve import accounts post results", e.getMessage()]  )
		}
	}

	/**
	 * Used to import accounts including persons and optionally their userLogin accounts. This is a three
	 * step form that take param.step to track at what point the user is in the process. The steps include:
	 *     start  - The user is presented a form
	 *     upload - The user has uploaded the spreadsheet which is saved to a temporary random filename and the user
	 *              is presented with the validation results
	 *     post   - The previously confirmed and this submission will reload the saved spreadsheet and post the 
	 *              changes to the database and delete the spreadsheet.
	 */	 
	def importAccounts() {
		// TODO : JPM 4/2016 : importAccounts - check permissions based on importing person and users (options if person but not user should update the import form as well)
		def (project, user) = controllerService.getProjectAndUserForPage(this, 'EditUserLogin')
		if (!project) {
			return
		}
		String formAction='importAccounts'
		String currentStep = (params.step ?: 'start')

		// fileParamName is the name of the parameter that the file will be uploaded as
		String fileParamName = 'importSpreadsheet'
		Map model = [ step:currentStep, projectName:project.name, fileParamName:fileParamName ]
		
		Map options = accountImportExportService.importParamsToOptionsMap(params)
		String view = 'importAccounts'
		def session = getSession()

		// There is a bug or undocumented feature that doesn't allow overriding params when forwarding which is used
		// in the upload step to forward to the review so we look for the stepAlt and use it if found.
		String step = params.stepAlt ?: params.step
		try {

			switch (step) {

				case 'upload':
					// This step will save the spreadsheet that was posted to the server after reading it 
					// and verifying that it has some accounts in it. If successful it will do a forward to 
					// the review step.

					if (params.verifyProject != 'Y') {
						flash.message = "You must confirm the project to import into before continuing"
						return model
					}

					options.fileParamName = fileParamName
					model = accountImportExportService.processFileUpload(session, request, user, project, options)
					// Redirect the user to the Review step
					forward( action:formAction, params: [stepAlt:'review', filename:model.filename, importOption: params.importOption] )
					return
					break

				case 'review':
					// This step will serve up the review template that in turn fetch the review data 
					// via an Ajax request.
					model << accountImportExportService.generateModelForReview(session, user, project, options)
					// log.debug "importAccounts() case 'review':\n\toptions=$options\n\tmodel=$model"
					if (!options.filename && model.filename) {
						// log.debug "importAccounts() step=$step set filename=${model.filename}"
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]

					// log.debug "importAccounts() in step 'review' model=$model"
					//log.debug "importAccounts() step=$step -- params=$params -- model=$model"

					view = "${formAction}Review"
					break

				case 'post':
					// This is the daddy of the steps in that it is going to post the changes back to the 
					// database either creating or updating Person and/or UserLogin records.

					List optionErrors = accountImportExportService.validateImportOptions(options)

					if (optionErrors) {
						throw new InvalidParamException(optionErrors.toString())
					}

					options.testMode = params.testMode == 'Y'

					// Here's the money maker call that will update existing accounts and create new ones accordingly
					model.results = accountImportExportService.postChangesToAccounts(session, user, project, options)
					
					log.debug "importAccounts() post results = ${model.results}"

					model << accountImportExportService.generateModelForPostResults(session, user, project, options)
					if (!options.filename && model.filename) {
						// log.debug "importAccounts() step=$step set filename=${model.filename}"
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]

					view = "${formAction}Results"
					log.debug "importAccounts() view = $view"



					// render "<h1>Results of the POST</h1><pre>${results.toString()}</pre>"

					// return
					break

				default:
					// The default which is the first step to prompt for the spreadsheet to upload
					break

			}

		} catch (e) {
			switch (e) {
				case InvalidRequestException:
				case DomainUpdateException:
				case InvalidParamException:
				case EmptyResultException:
					log.debug "importAccounts() exception ${e.getClass().getName()} ${e.getMessage()}"
					flash.message = e.getMessage()
					break
				default: 
					log.error "Exception occurred while importing data (step $currentStep)" + e.printStackTrace()
					flash.message = "An error occurred while attempting to import accounts"				
			}
			// Attempt to delete the temporary uploaded worksheet if an exception occurred
			if (options.filename) {
				accountImportExportService.deletePreviousUpload(user, options)
			}

		}

		// log.debug "importAccounts() Finishing up controller step=$step, view=$view, model=$model"
		render view:view, model:model
	}

	/**
	 * Used to cancel an account import process that is in flight. This basically will delete the uploaded spreadsheet
	 * and then redirect the user back to the Import Account view.
	 * @param params.filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON { accounts: List of accounts }
	 */
	def cancelImport() {
		try {
			def (project, user) = controllerService.getProjectAndUserForPage(this, 'PersonImport')
			if (!project) {
				return
			}

			Map options = [filename: params.id]

			accountImportExportService.cancelPreviousUpload(user, project, options)
			flash.message = "The previous import was cancelled"

		} catch (InvalidRequestException e) {
			flash.message = e.getMessage()
		} catch (DomainUpdateException e) {
			flash.message = e.getMessage()
		} catch (InvalidParamException e) {
			flash.message = e.getMessage()
		} catch (EmptyResultException e) {
			flash.message = e.getMessage()
		} catch (e) {
			log.error "cancelImport() Unexpected exception occurred while cancelling Account Import" + e.printStackTrace()
			flash.message = "An error occurred while attempting to cancel Account Import"
		}

		redirect (action:'importAccounts' )
	}

	/**
	 * A action to show project Summary report filters.
	 */
	def projectReport() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

		return
	}
	
	/**
	 * To Generate project Summary Web report
	 */
	def projectSummaryReport() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

		def person  = securityService.getUserLoginPerson().toString()
		def now = TimeUtil.nowGMT()
		def results = projectService.getProjectReportSummary( params )
		render (template :'projectSummaryReport', model:[results:results, person:person, time:now])
	}
	
	// Gets the number of assets with a desync between the device's assetType and its model's assetType
	def countAssetsOutOfSync() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

		def query = """ -- Case sensitive - assets that assetType don't match
			SELECT COUNT(*)
			FROM asset_entity a
			JOIN model m ON m.model_id = a.model_id
			WHERE a.asset_class='DEVICE' AND BINARY a.asset_type <> BINARY m.asset_type; """
		def assetCount = jdbcTemplate.queryForLong(query)
		render assetCount
	}
	
	/**
	 * Used to reconcile assetTypes of devices with the assetType of their models
	 */
	def reconcileAssetTypes() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')

		if (project) {
			def query = """ -- Update DEVICES with their propery asset_type from their respective model record
				UPDATE asset_entity a
				JOIN model m ON a.model_id = m.model_id
				SET a.asset_type = m.asset_type
				WHERE a.asset_class='DEVICE';"""
			jdbcTemplate.execute(query)
		} else {
			response.sendError( 401, "Unauthorized Error")
		}
		render 0
	}

	/**
	 * Used to encrypt a value
	 */
	def encryptValue() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

		def toEncryptString = params.toEncryptString
		def encryptAlghoritm = params.encryptAlghoritm
		def encryptSalt = params.encryptSalt
		def encodedValue = ""
		switch (encryptAlghoritm) {
			case "AES":
				encodedValue = AESCodec.encode(toEncryptString, encryptSalt)
				break;
			case "DES":
			default:
				encodedValue = DESCodec.encode(toEncryptString, encryptSalt)
		}
		render encodedValue
	}

	/**
	 * Used to display the application memory consumption along with runtime configuration and performance data
	 */
	def systemInfo() {

		Project project = controllerService.getProjectForPage(this, 'AdminMenuView')
		if (!project) 
			return

		int MegaBytes = 1024;

		Runtime rt = Runtime.getRuntime()
		long freeMemory = rt.freeMemory()/MegaBytes
		long totalMemory = rt.totalMemory()/MegaBytes
		long maxMemory = rt.maxMemory()/MegaBytes
		long usedMemory = totalMemory - freeMemory

		java.lang.management.MemoryMXBean memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean()
		java.lang.management.MemoryUsage memNonHeap=memoryMXBean.getNonHeapMemoryUsage()
		java.lang.management.MemoryUsage memHeap=memoryMXBean.getHeapMemoryUsage()

		java.lang.management.OperatingSystemMXBean osMxBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean()
		java.lang.management.RuntimeMXBean rtMXBean = java.lang.management.ManagementFactory.getRuntimeMXBean()

		long heapUsed = memHeap.getUsed()/MegaBytes
		long heapCommitted = memHeap.getCommitted()/MegaBytes
		long heapMax = memHeap.getMax() / MegaBytes

		long nonHeapUsed = memNonHeap.getUsed()/MegaBytes
		long nonHeapCommitted = memNonHeap.getCommitted()/MegaBytes
		long nonHeapMax = memNonHeap.getMax() / MegaBytes

		long sysMemSize = osMxBean.getTotalPhysicalMemorySize() / MegaBytes
		long sysMemFree = osMxBean.getFreePhysicalMemorySize() / MegaBytes
		long swapSize = osMxBean.getTotalSwapSpaceSize() / MegaBytes
		long swapFree = osMxBean.getFreeSwapSpaceSize() / MegaBytes
		long virtMemCommit = osMxBean.getCommittedVirtualMemorySize() / MegaBytes

		Map sysProps = rtMXBean.getSystemProperties()

		render (template:'systemInfo', model:
			['freeMemory':freeMemory,
			 'totalMemory':totalMemory,
			 'maxMemory':maxMemory,
			 'usedMemory':usedMemory,
			 'memoryMXBean':memoryMXBean,
			 'memNonHeap':memNonHeap,
			 'memHeap':memHeap,
			 'osMxBean':osMxBean,
			 'rtMXBean':rtMXBean,
			 'heapUsed':heapUsed,
			 'heapCommitted':heapCommitted,
			 'heapMax':heapMax,
			 'nonHeapUsed':nonHeapUsed,
			 'nonHeapCommitted':nonHeapCommitted,
			 'nonHeapMax':nonHeapMax,
			 'sysMemSize':sysMemSize,
			 'sysMemFree':sysMemFree,
			 'swapSize':swapSize,
			 'swapFree':swapFree,
			 'virtMemCommit':virtMemCommit,
			 'sysProps':sysProps,
			 'osMxBean':osMxBean,
			 'rt': rt,
			 'groovyVersion': GroovySystem.getVersion()
			]
		)
	}

	/*
	 *  Action to navigate the admin control home page
	 */
	def home() {
		if (!controllerService.checkPermission(this, 'AdminMenuView')) 
			return

		def dateNow = TimeUtil.nowGMT()
		def timeNow = dateNow.getTime()
		def dateNowSQL = TimeUtil.nowGMTSQLFormat()
		
		// retrive the list of 20 usernames with the most recent login times
		def recentUsers = UserLogin.findAll("FROM UserLogin ul WHERE ul.lastLogin is not null ORDER BY ul.lastPage DESC",[max:20])

		// retrive the list of events in progress
		def currentLiveEvents = MoveEvent.findAll()
		def moveEventsList = []
		def thirtyDaysInMS = 60 * 24 * 30 * 1000
		
		currentLiveEvents.each{ moveEvent  ->
			def completion = moveEvent.getEventTimes()?.completion?.getTime()
			if(moveEvent.newsBarMode == "on" || (completion && completion < timeNow && completion + thirtyDaysInMS > timeNow)){
				def query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ?  ORDER BY mes.dateCreated DESC"
				def moveEventSnapshot = MoveEventSnapshot.findAll( query , [moveEvent] )[0]
				def status =""
				def dialInd = moveEventSnapshot?.dialIndicator 
				if( dialInd && dialInd < 25){
					status = "Red($dialInd)"
				} else if( dialInd && dialInd >= 25 && dialInd < 50){
					status = "Yellow($dialInd)"
				} else if(dialInd){
					status = "Green($dialInd)"
				}
				moveEventsList << [moveEvent : moveEvent, status : status, startTime : moveEvent.getEventTimes()?.start, completionTime : moveEvent.getEventTimes()?.completion]
			}
		}
		// retrive the list of 10 upcoming bundles
		def upcomingBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.startTime > '$dateNowSQL' ORDER BY mb.startTime",[max:10])
		
		render( view:'home', model:[ recentUsers:recentUsers, moveEventsList:moveEventsList, upcomingBundles:upcomingBundles] )
	}

}
