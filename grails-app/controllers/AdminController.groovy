import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.os.Shell
import com.tdsops.common.security.AESCodec
import com.tdsops.common.security.DESCodec
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.ServiceResults
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventSnapshot
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AccountImportExportService
import net.transitionmanager.service.AssetOptionsService
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.UserService

import org.springframework.jdbc.core.JdbcTemplate

import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage
import java.lang.management.OperatingSystemMXBean
import java.lang.management.RuntimeMXBean

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class AdminController implements ControllerMethods {

	AccountImportExportService accountImportExportService
	AuditService auditService
	ControllerService controllerService
	CoreService coreService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	UserService userService
	AssetOptionsService assetOptionsService

	static final String APP_RESTART_CMD_PROPERTY = 'admin.serviceRestartCommand'

	private static final int thirtyDaysInMS = 60 * 24 * 30 * 1000

	@HasPermission(Permission.AdminUtilitiesAccess)
	def index() {}

	/**
	 * Renders the Application Restart Form
	 */
	@HasPermission(Permission.ApplicationRestart)
	def restartAppServiceForm() {
		String restartCmd = coreService.getAppConfigSetting(APP_RESTART_CMD_PROPERTY)
		int activityTimeLimit = 5

		[activityTimeLimit: 5, restartable: restartCmd != null,
		 users: userService.usernamesWithRecentActivity(activityTimeLimit)]
	}

	/**
	 * Invokes a Application Restart process if the property has been configured with
	 * a proper command. Looks for the tdstm.admin.serviceRestartCommand property and
	 * attempts to shell out to the OS and run if defined.
	 */
	@HasPermission(Permission.ApplicationRestart)
	def restartAppServiceAction() {
		String cmd = coreService.getAppConfigSetting(APP_RESTART_CMD_PROPERTY)
		if (cmd == null) {
			render(status: 400, text: message(code: 'tdstm.admin.serviceRestartCommand.error'))
			return
		}

		// Log to the app log as well as to the system log that we're going to restart the app
		String logStr = message(code: 'tdstm.admin.serviceRestartCommand.log',
				args: [securityService.currentUsername, cmd])
		Shell.systemLog(logStr)

		auditService.saveUserAudit(UserAuditBuilder.restartedApplication())

		log.warn(logStr)

		Map results = Shell.executeCommand(cmd)

		// Now log the results
		logStr = message(code: 'tdstm.admin.serviceRestartCommand.results', args: [results.toString()])
		Shell.systemLog(logStr)
		log.warn(logStr)

		if (results.exitValue == 0) {
			renderSuccessJson(results)
		} else {
			renderFailureJson(results)
		}
		println "restartAppServiceAction() 5"
		
	}

	@HasPermission(Permission.AdminUtilitiesAccess)
	def orphanSummary() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		def summaryRecords = []
		String orphanParty = '''
			SELECT party_id
			FROM party
			WHERE party_id not in (
				SELECT distinct party_id from (
					      SELECT party_group_id  as party_id from party_group
					UNION SELECT person_id       as party_id from person
					UNION SELECT move_bundle_id  as party_id from move_bundle
					UNION SELECT project_id      as party_id from project
					UNION SELECT project_team_id as party_id from project_team
					UNION SELECT app_id          as party_id from application) pr)'''

		String assetSummaryQuery = '''
			/*-----------------------------------ORPHAN RESULTS QUERY FOR APPLICATION_ASSET_MAP---------------------------------*/
			SELECT * FROM (SELECT 'application_asset_map' as mainTable, 'application_id' as refId, 'Orphan' as type, count(*) as totalCount
			               from application_asset_map where application_id NOT IN (SELECT app_id from application)
				UNION
					SELECT 'application_asset_map' as mainTable,'application_id' as refId,'Null' as type,count(*) as totalCount from application_asset_map asm where asm.application_id IS NULL
				UNION
				SELECT 'application_asset_map' as mainTable,'asset_id' as refId,'Orphan' as type,count(*) as totalCount from application_asset_map asm where asm.asset_id NOT IN (SELECT ae.asset_entity_id from asset_entity ae )
				UNION
				SELECT 'application_asset_map' as mainTable,'asset_id' as refId,'Null' as type,count(*) as totalCount from application_asset_map asm where asm.asset_id IS NULL) asm
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
			'''

		summaryRecords << jdbcTemplate.queryForList( assetSummaryQuery )

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

		summaryRecords << jdbcTemplate.queryForList(dataTransferSummaryQuery)

		def eavSummaryQuery = '''
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
			'''

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

		summaryRecords << jdbcTemplate.queryForList(moveSummaryQuery)

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
			SELECT * FROM  ( SELECT 'user_login' as mainTable, 'person_id' as refId, 'Orphan' as type, count(*) as totalCount FROM user_login ul where ul.person_id not in (select person_id from person)
				UNION
				SELECT 'user_login' as mainTable,'person_id' as refId,'Null' as type,count(*) as totalCount FROM user_login ul where ul.person_id is null ) ul
			WHERE ul.totalCount > 0

			UNION
			/*-----------------------------------ORPHAN RESULTS QUERY FOR USER_PREFERENCE-----------------------------------------*/
			SELECT * FROM  (  SELECT 'user_preference' as mainTable,'user_login_id' as refId,'Orphan' as type,count(*) as totalCount FROM user_preference up where up.user_login_id not in (select ul.user_login_id from user_login ul)
				UNION
				SELECT 'user_preference' as mainTable,'user_login_id' as refId,'Null' as type,count(*) as totalCount FROM user_preference up where up.user_login_id is null) up
			WHERE up.totalCount > 0	"""

		summaryRecords << jdbcTemplate.queryForList(partySummaryQuery)

		[summaryRecords: summaryRecords]
	}

	@HasPermission(Permission.AdminUtilitiesAccess)
	def orphanDetails() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def orphanDeatils
		def table = params.table
		def column = params.column
		def type = params.type
		def query = ''
		def orphanParty = '''SELECT party_id as party_id FROM party p where p.party_id not in
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
				select app_id as party_id from application ) pr)'''
		switch (table) {
			case 'application':
				if (type != 'Null') {
					query = 'SELECT * FROM application app where app.owner_id not in (select p.party_group_id from party_group p )'
				} else {
					query = 'SELECT * FROM application app where app.owner_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'application_asset_map':
				switch (column) {
					case 'application_id':
						if (type != 'Null') {
							query = 'SELECT * FROM application_asset_map where application_id not in (select app.app_id from application app )'
						} else {
							query = 'SELECT * FROM application_asset_map where application_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'asset_id':
						if (type != 'Null') {
							query = 'SELECT * FROM application_asset_map where asset_id not in (select ae.asset_entity_id from asset_entity ae )'
						} else {
							query = 'SELECT * FROM application_asset_map where asset_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'asset_comment':
				switch (column) {
					case 'asset_entity_id':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_comment ac where ac.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )'
						} else {
							query = 'SELECT * FROM asset_comment ac where ac.asset_entity_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'created_by':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_comment ac where ac.created_by not in (select p.person_id from person p )'
						} else {
							query = 'SELECT * FROM asset_comment ac where ac.created_by is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'resolved_by':
						query = 'SELECT * FROM asset_comment ac where ac.resolved_by not in (select p.person_id from person p )'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'asset_entity':
				switch (column) {
					case 'project_id':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_entity a where a.project_id not in (select p.project_id from project p )'
						} else {
							query = 'SELECT * FROM asset_entity a where a.project_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'owner_id':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_entity a where a.owner_id not in (select p.party_group_id from party_group p )'
						} else {
							query = 'SELECT * FROM asset_entity a where a.owner_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'move_bundle_id':
						query = 'SELECT * FROM asset_entity a where a.move_bundle_id not in (select m.move_bundle_id from move_bundle m )'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'model_id':
						query = 'SELECT * FROM asset_entity a where a.model_id not in (select m.model_id from model m ) and a.model_id is not null '
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'manufacturer_id':
						query = 'SELECT * FROM asset_entity a where a.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn) and a.manunfacturer_id is not null'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'asset_entity_varchar':
				switch (column) {
					case 'asset_entity_id':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_entity_varchar aev where aev.asset_entity_id not in (select ae.asset_entity_id from asset_entity ae )'
						} else {
							query = 'SELECT * FROM asset_entity_varchar aev where aev.asset_entity_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'attribute_id':
						if (type != 'Null') {
							query = 'SELECT * FROM asset_entity_varchar aev where aev.attribute_id not in (select ea.attribute_id from eav_attribute ea )'
						} else {
							query = 'SELECT * FROM asset_entity_varchar aev where aev.attribute_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'data_transfer_attribute_map':
				switch (column) {
					case 'eav_attribute_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_attribute_map dam where dam.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)'
						} else {
							query = 'SELECT * FROM data_transfer_attribute_map dam where dam.eav_attribute_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'data_transfer_set_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_attribute_map dam where dam.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)'
						} else {
							query = 'SELECT * FROM data_transfer_attribute_map dam where dam.data_transfer_set_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'data_transfer_batch':
				switch (column) {
					case 'user_login_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.user_login_id not in (select ul.user_login_id from user_login ul)'
						} else {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.user_login_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'data_transfer_set_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.data_transfer_set_id not in (select dts.data_transfer_id from data_transfer_set dts)'
						} else {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.data_transfer_set_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'project_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.project_id not in (select p.project_id from project p)'
						} else {
							query = 'SELECT * FROM data_transfer_batch dtb where dtb.project_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'data_transfer_comment':
				if (type != 'Null') {
					query = 'SELECT * FROM data_transfer_comment dtm where dtm.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)'
				} else {
					query = 'SELECT * FROM data_transfer_comment dtm where dtm.data_transfer_batch_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'data_transfer_value':
				switch (column) {
					case 'data_transfer_batch_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_value dtv where dtv.data_transfer_batch_id not in (select dtb.batch_id from data_transfer_batch dtb)'
						} else {
							query = 'SELECT * FROM data_transfer_value dtv where dtv.data_transfer_batch_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'eav_attribute_id':
						if (type != 'Null') {
							query = 'SELECT * FROM data_transfer_value dtv where dtv.eav_attribute_id not in (select ea.attribute_id from eav_attribute ea)'
						} else {
							query = 'SELECT * FROM data_transfer_value dtv where dtv.eav_attribute_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'eav_attribute':
				if (type != 'Null') {
					query = 'SELECT * FROM eav_attribute ea where ea.entity_type_id not in (select et.entity_type_id from eav_entity_type et)'
				} else {
					query = 'SELECT * FROM eav_attribute ea where ea.entity_type_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'eav_attribute_option':
				if (type != 'Null') {
					query = 'SELECT * FROM eav_attribute_option eao where eao.attribute_id not in (select ea.attribute_id from eav_attribute ea)'
				} else {
					query = 'SELECT * FROM eav_attribute_option eao where eao.attribute_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'eav_attribute_set':
				if (type != 'Null') {
					query = 'SELECT * FROM eav_attribute_set eas where eas.entity_type_id not in (select et.entity_type_id from eav_entity_type et)'
				} else {
					query = 'SELECT * FROM eav_attribute_set eas where eas.entity_type_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'eav_entity':
				if (type != 'Null') {
					query = 'SELECT * FROM eav_entity ee where ee.attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)'
				} else {
					query = 'SELECT * FROM eav_entity ee where ee.attribute_set_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'eav_entity_attribute':
				switch (column) {
					case 'eav_attribute_set_id':
						if (type != 'Null') {
							query = 'SELECT * FROM eav_entity_attribute eea where eea.eav_attribute_set_id not in (select eas.attribute_set_id from eav_attribute_set eas)'
						} else {
							query = 'SELECT * FROM eav_entity_attribute eea where eea.eav_attribute_set_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'attribute_id':
						if (type != 'Null') {
							query = 'SELECT * FROM eav_entity_attribute eea where eea.attribute_id not in (select ea.attribute_id from eav_attribute ea)'
						} else {
							query = 'SELECT * FROM eav_entity_attribute eea where eea.attribute_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'eav_entity_id':
						query = 'SELECT * FROM eav_entity_attribute eea where eea.eav_entity_id not in (select ee.entity_id from eav_entity ee)'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'eav_entity_datatype':
				if (type != 'Null') {
					query = 'SELECT * FROM eav_entity_datatype eed where eed.attribute_id not in (select ea.attribute_id from eav_attribute ea)'
				} else {
					query = 'SELECT * FROM eav_entity_datatype eed where eed.attribute_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break
			case 'manufacturer':
				switch (column) {
					case 'manufacturer_id':
						query = 'SELECT * FROM manufacturer mn where mn.manufacturer_id not in (select m.manufacturer_id from model m)'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break
			case 'model':
				switch (column) {
					case 'manufacturer_id':
						query = 'SELECT * FROM model m where m.manufacturer_id not in (select mn.manufacturer_id from manufacturer mn) OR m.manufacturer_id is null OR m.manufacturer_id = '
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'model_id':
						query = 'SELECT * FROM model m where m.model_id not in (select ae.model_id from asset_entity ae where ae.model_id is not null) '
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break
			case 'model_connector':
				switch (column) {
					case 'model_id':
						query = 'SELECT * FROM model_connector mn where mn.model_id not in (select m.model_id from model m ) '
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break
			case 'move_bundle':
				switch (column) {
					case 'project_id':
						if (type != 'Null') {
							query = 'SELECT * FROM move_bundle m where m.project_id not in (select p.project_id from project p )'
						} else {
							query = 'SELECT * FROM move_bundle m where m.project_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'move_event_id':
						if (type != 'Null') {
							query = 'SELECT * FROM move_bundle m where m.move_event_id not in (select me.move_event_id from move_event me )'
						} else {
							query = 'SELECT * FROM move_bundle m where m.move_event_id is null'
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break

				}
			break

			case 'move_bundle_step':
				if (type != 'Null') {
					query = 'SELECT * FROM move_bundle_step mbs where mbs.move_bundle_id not in (select m.move_bundle_id from move_bundle m )'
				} else {
					query = 'SELECT * FROM move_bundle_step mbs where mbs.move_bundle_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'move_event':
				if (type != 'Null') {
					query = 'SELECT * FROM move_event me where me.project_id not in (select p.project_id from project p )'
				} else {
					query = 'SELECT * FROM move_event me where me.project_id is null'
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'move_event_news':
				switch (column) {
					case "move_event_id":
						if (type != "Null") {
							query = "SELECT * FROM move_event_news men where men.move_event_id not in (select me.move_event_id from move_event me )"
						}
						else {
							query = "SELECT * FROM move_event_news men where men.move_event_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "created_by":
						if (type != "Null") {
							query = "SELECT * FROM move_event_news men where men.created_by not in (select p.person_id from person p )"
						}
						else {
							query = "SELECT * FROM move_event_news men where men.created_by is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "archived_by":
						query = "SELECT * FROM move_event_news men where men.archived_by not in (select p.person_id from person p )"
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case "move_event_snapshot":
				if (type != "Null") {
					query = "SELECT * FROM move_event_snapshot mes where mes.move_event_id not in (select me.move_event_id from move_event me )"
				}
				else {
					query = "SELECT * FROM move_event_snapshot mes where mes.move_event_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case "step_snapshot":
				if (type != "Null") {
					query = "SELECT * FROM step_snapshot ss where ss.move_bundle_step_id not in (select m.id from move_bundle_step m)"
				}
				else {
					query = "SELECT * FROM step_snapshot ss where ss.move_bundle_step_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'party':
				switch (column) {
					case "party_id":
						query = '''SELECT * FROM party p where p.party_id not in
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
						select app_id as party_id from application ) pr)'''
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "":
						query = "SELECT * FROM party p where p.party_type_id not in (select pt.party_type_code from party_type pt)"
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
				}
		break

			case 'party_relationship':
				switch (column) {
					case "party_id_from_id":
						if (type != "Null") {
							query = "SELECT * FROM party_relationship pr where pr.party_id_from_id in ( $orphanParty )"
						}
						else {
							query = "SELECT * FROM party_relationship pr where pr.party_id_from_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "party_id_to_id":
						if (type != "Null") {
							query = "SELECT * FROM party_relationship pr where pr.party_id_to_id in ( $orphanParty )"
						}
						else {
							query = "SELECT * FROM party_relationship pr where pr.party_id_to_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "role_type_code_from_id":
						if (type != "Null") {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_from_id not in (select rt.role_type_code from role_type rt )"
						}
						else {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_from_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "role_type_code_to_id":
						if (type != "Null") {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_to_id not in (select rt.role_type_code from role_type rt )"
						}
						else {
							query = "SELECT * FROM party_relationship pr where pr.role_type_code_to_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "party_relationship_type_id":
						if (type != "Null") {
							query = "SELECT * FROM party_relationship pr where pr.party_relationship_type_id not in (select prt.party_relationship_type_code from party_relationship_type prt )"
						}
						else {
							query = "SELECT * FROM party_relationship pr where pr.party_relationship_type_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break
			case 'party_role':
				switch (column) {
					case "party_id":
						if (type != "Null") {
							query = "SELECT * FROM party_role pr where pr.party_id not in (select p.party_id from party p)"
						}
						else {
							query = "SELECT * FROM party_role pr where pr.party_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "role_type_id":
						if (type != "Null") {
							query = "SELECT * FROM party_role pr where pr.role_type_id not in (select rt.role_type_code from role_type rt)"
						}
						else {
							query = "SELECT * FROM party_role pr where pr.role_type_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case "project":
				if (type != "Null") {
					query = "SELECT * FROM project pr where pr.client_id not in (select pg.party_group_id from party_group pg)"
				}
				else {
					query = "SELECT * FROM project pr where pr.client_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break

			case 'project_asset_map':
				switch (column) {
					case "project_id":
						if (type != "Null") {
							query = "SELECT * FROM project_asset_map pam where pam.project_id not in (select pr.project_id from project pr)"
						}
						else {
							query = "SELECT * FROM project_asset_map pam where pam.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
						break
					case "asset_id":
						if (type != "Null") {
							query = "SELECT * FROM project_asset_map pam where pam.asset_id not in (select ae.asset_entity_id from asset_entity ae)"
						}
						else {
							query = "SELECT * FROM project_asset_map pam where pam.asset_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'project_logo':
				switch (column) {
					case "project_id":
						if (type != "Null") {
							query = "SELECT * FROM project_logo pl where pl.project_id not in (select pr.project_id from project pr)"
						}
						else {
							query = "SELECT * FROM project_logo pl where pl.project_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case 'project_team':
				switch (column) {
					case "move_bundle_id":
						if (type != "Null") {
							query = "SELECT * FROM project_team pt where pt.move_bundle_id not in (select m.move_bundle_id from move_bundle m)"
						}
						else {
							query = "SELECT * FROM project_team pt where pt.move_bundle_id is null"
						}
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
					case 'latest_asset_id':
						query = 'SELECT * FROM project_team pt where pt.latest_asset_id not in (select ae.asset_entity_id from asset_entity ae)'
						orphanDeatils = jdbcTemplate.queryForList(query)
					break
				}
			break

			case "user_login":
				if (type != "Null") {
					query = "SELECT * FROM user_login ul where ul.person_id not in (select per.person_id from person per)"
				}
				else {
					query = "SELECT * FROM user_login ul where ul.person_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
				break
			case "user_preference":
				if (type != "Null") {
					query = "SELECT * FROM user_preference up where up.user_login_id not in (select ul.user_login_id from user_login ul)"
				}
				else {
					query = "SELECT * FROM user_preference up where up.user_login_id is null"
				}
				orphanDeatils = jdbcTemplate.queryForList(query)
			break
		}// END OF MAIN SWITCH

		renderAsJson(orphanDeatils: orphanDeatils, query: query)
	}

	/**
	 * Flush imported processed data or unprocessed data or both
	 * @param deleteHistory : the time constraint the end user want to delete the records .
	 * @return : count of record that is deleted.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def processOldData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		StringBuilder queryForData = new StringBuilder('FROM data_transfer_value')
		StringBuilder queryForBatch = new StringBuilder('FROM data_transfer_batch')

		switch (params.deleteHistory) {
			case 'anyProcessed':
				queryForBatch << ''' where status_code='completed' '''
				queryForData << ' WHERE data_transfer_batch_id IN (SELECT batch_id ' << queryForBatch << ') '
				break
			case 'overTwoMonths':
				queryForBatch << ' WHERE date_created <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 60 DAY)'
				queryForData << ' WHERE data_transfer_batch_id IN (SELECT batch_id ' << queryForBatch << ' )'
				break
		}

		def records = jdbcTemplate.queryForObject('SELECT count(*) FROM (SELECT 1 as count ' + queryForData +
				' group by data_transfer_batch_id, row_id) a', Integer)

		jdbcTemplate.update('DELETE ' + queryForData)
		jdbcTemplate.update('DELETE ' + queryForBatch)

		render 'Deleted ' + records + ' Records'
	}

	/**
	 * Get the processed and pending batches counts.
	 * @params N/A :
	 * @return : String formatted to display processed and pending batches and records
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def retrieveBatchRecords() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		String queryForRecords = '''
			SELECT COUNT(*) as batchCount, if (SUM(noOfRows),SUM(noOfRows),0) as records
			FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows  FROM data_transfer_value dtv
			LEFT OUTER JOIN data_transfer_batch dtb ON dtv.data_transfer_batch_id = dtb.batch_id
			WHERE dtb.status_code = 'completed' GROUP BY data_transfer_batch_id, row_id ) a GROUP BY data_transfer_batch_id ) b
			UNION
			SELECT COUNT(*) as batchCount, if (SUM(noOfRows),SUM(noOfRows),0) as records
			FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows  FROM (SELECT data_transfer_batch_id, COUNT(*) as noOfRows  FROM data_transfer_value dtv
			LEFT OUTER JOIN data_transfer_batch dtb ON dtv.data_transfer_batch_id = dtb.batch_id
			WHERE dtb.status_code = 'pending' GROUP BY data_transfer_batch_id, row_id ) a GROUP BY data_transfer_batch_id ) b;'''

		def recordsLegend = jdbcTemplate.queryForList(queryForRecords)

		String pendingInfo = "Current: ${recordsLegend[0].batchCount} batches / ${recordsLegend[0].records} records process," +
				(recordsLegend[1] ? "${recordsLegend[1].batchCount} batches / ${recordsLegend[1].records} records pending" : "0 batches / 0 records pending")

		render pendingInfo
	}

	/**
	 * Get the Asset type and their respective Asset Count and Model Count.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def retrieveAssetTypes() {
		Project project = controllerService.getProjectForPage(this)

		if (!project) {
			return
		}

		//List<String> assetTypeOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ASSET_TYPE, [sort: 'value']).value
		List<String> assetTypeOptions = assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.ASSET_TYPE)
		List assetTypes = []
		assetTypeOptions.remove('Blade') // TODO : temp fix to resolve the below issue

		assetTypeOptions.each { type ->
			def modelCount = Model.findAllByAssetType(type)
			def assets = AssetEntity.findAllByAssetType(type) // TODO : getting Exception when type=Blade. assuming data issue.
			Set projects = assets?.project
			int assetRef = assets.size()
			int modelRef = modelCount.size()
			String projectRef = assets[0]?.project?.name ?: ''
			String projectCnt = projects.size() > 1 ? projects.size() - 1 : ''
			boolean toPurge = (assetRef == 0 && modelRef == 0)
			assetTypes << [type, assetRef, modelRef, projectRef, projectCnt, toPurge]
		}

		render(template: 'getAssetTypes', model: [assetTypes: assetTypes])
	}

	/**
	 * Clean the Unused Asset types.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def cleanAssetTypes() {
		Project project = controllerService.getProjectForPage(this)

		if (!project) {
			return
		}

		def deletedTypes = []
		List<String> assetTypeOptions = assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.ASSET_TYPE)

		assetTypeOptions.each { type ->
			int assetCount = AssetEntity.countByAssetType(type)
			int modelCount = Model.countByAssetType(type)

			if (!assetCount && !modelCount) {
				AssetOptions assetOption = assetOptionsService.findByValue(type)

				if (assetOption) {
					deletedTypes << type
					assetOption.delete()
				}
			}
		}

		render !deletedTypes ? 'No asset types found that are unreferenced by assets or models.' :
			'Removed ' + deletedTypes.size() + ' unused Types: ' +
			WebUtil.listAsMultiValueString(deletedTypes)
	}

	/**
	 * Renders the Export Accounts form.
	 */
	@HasPermission(Permission.PersonExport)
	def exportAccounts() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		Map model = [project: project.name, client: project.client.name, company: securityService.userLoginPerson.company]
		Map paramDefaults = [staffType: 'PROJ_STAFF', includeLogin: 'Y', loginChoice: '1']
		paramDefaults.each { key, defVal -> model[key] = params[key] ?: defVal }

		model
	}

	/**
	 * Exports project staff and users to a spreadsheet.
	 * @response Excel Spreadsheet
	 */
	@HasPermission(Permission.PersonExport)
	def exportAccountsProcess() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		Map formOptions = [
			exportFormat: params.exportFormat ?: 'xlsx', staffType: params.staffType,// CLIENT_STAFF | AVAIL_STAFF | PROJ_STAFF
			includeLogin: params.includeLogin, // Y)es if checked
		                   loginChoice : params.loginChoice]   // 0=All, 1=Active, 2=Inactive

		try {
			accountImportExportService.generateAccountsExportToBrowser(response, project, formOptions)
			return
		}
		catch (InvalidParamException | EmptyResultException e) {
			flash.message = e.message
		}
		catch (e) {
			log.error 'Exception occurred while exporting data', e
			flash.message = 'An error occurred while attempting to export accounts'
		}

		redirect action: 'exportAccounts', params: formOptions
	}

	/**
	 * Downloads the spreadsheet import template
	 */
	// /('P_PersonImport')
	@HasPermission(Permission.PersonImport)
	def importAccountsTemplate() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		try {
			// Formulate the download filename ExportAccounts + ProjectCode + yyyymmdd sans the extension
			String projectName = project.projectCode.replaceAll(' ', '')
			String formattedDate = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
			String filename = accountImportExportService.IMPORT_FILENAME_PREFIX + '-' + projectName + '-' + formattedDate
			accountImportExportService.generateImportTemplateToBrowser(response, project, filename)
			return
		}
		catch (InvalidRequestException | DomainUpdateException | InvalidParamException | EmptyResultException e) {
			flash.message = e.message
		}
		catch (e) {
			log.error 'Exception occurred while downloading the Account Import template', e
			flash.message = 'An error occurred while attempting to download the Account Import template'
		}

		forward action: 'importAccounts'
	}

	/**
	 * Retrieve the account information during the import process after it has been read in from the
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON{ accounts: List of accounts }
	 */
	@HasPermission(Permission.PersonImport)
	def importAccountsReviewData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			renderErrorJson(flash.message)
			flash.message = ''
			return
		}

		if (!params.filename) {
			renderErrorJson('Request was missing the required filename reference')
			return
		}

		try {
			// TODO : JPM 4/2016 : importAccountsReviewData This method should be refactored so that
			// the bulk of the logic is implemented in the service.

			Map formOptions = accountImportExportService.importParamsToOptionsMap(params)
			renderAsJson(accountImportExportService.generateReviewData(project, params.filename, formOptions))
		}
		catch (e) {
			log.error 'Exception occurred while importing data: ', e

			renderErrorJson(['An error occurred while attempting to import accounts', e.message])
		}
	}

	/**
	 * Retrieves the account information during the import process after it has been read in from the
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON{ accounts: List of accounts }
	 */
	@HasPermission(Permission.PersonImport)
	def importAccountsPostResultsData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			renderErrorJson(flash.message)
			flash.message = ''
			return
		}
		if (!params.filename) {
			renderErrorJson('Request was missing the required filename reference')
			return
		}

		try {
			Map formOptions = accountImportExportService.importParamsToOptionsMap(params)
			File spreadsheetFile = accountImportExportService.generatePostResultsData(params.filename, formOptions)
			ServiceResults.respondAsJson response, spreadsheetFile
		}
		catch (e) {
			log.error 'importAccountsPostResultsData() Exception occurred while retrieving import accounts post results: ', e

			renderErrorJson(['An error occurred while attempting to retrieve import accounts post results', e.message])
		}
	}

	/**
	 * Imports accounts including persons and optionally their userLogin accounts. This is a three-step
	 * form that take param.step to track at what point the user is in the process. The steps include:
	 *     start  - The user is presented a form
	 *     upload - The user has uploaded the spreadsheet which is saved to a temporary random filename and the user
	 *              is presented with the validation results
	 *     post   - The previously confirmed and this submission will reload the saved spreadsheet and post the
	 *              changes to the database and delete the spreadsheet.
	 */
	@HasPermission(Permission.UserImport)
	def importAccounts() {
		// TODO : JPM 4/2016 : importAccounts - check permissions based on importing person and users (options if person but not user should update the import form as well)
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		String formAction = 'importAccounts'
		String currentStep = (params.step ?: 'start')

		// fileParamName is the name of the parameter that the file will be uploaded as
		String fileParamName = 'importSpreadsheet'
		Map model = [step: currentStep, projectName: project.name, fileParamName: fileParamName]

		Map options = accountImportExportService.importParamsToOptionsMap(params)
		String view = 'importAccounts'

		// There is a bug or undocumented feature that doesn't allow overriding params when forwarding which is used
		// in the upload step to forward to the review so we look for the stepAlt and use it if found.
		String step = params.stepAlt ?: params.step
		try {

			switch (step) {

				case 'upload':
					// saves the spreadsheet that was posted to the server after reading it and verifying that
					// it has some accounts in it. If successful it will do a forward to the review step.

					options.fileParamName = fileParamName
					model = accountImportExportService.processFileUpload(request, project, options)

					Map forwardParams = [stepAlt: 'review', filename: model.filename, importOption: params.importOption,
					                     accountsToRemoveFromProject: model.accountsToRemoveFromProject]

					// Redirect the user to the Review step
					forward action: formAction, params: forwardParams
					return

				case 'review':
					// Serves up the review template that in turn fetch the review data via an Ajax request.
					model << accountImportExportService.generateModelForReview(project, options)
					// log.debug 'importAccounts() case 'review':\n\toptions=$options\n\tmodel=$model'
					if (!options.filename && model.filename) {
						// log.debug 'importAccounts() step=$step set filename=$model.filename'
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename: model.filename, importOption: params.importOption]
					view = formAction + 'Review'
					break

				case 'post':
					// The daddy of the steps in that it is going to post the changes back to the
					// database either creating or updating Person and/or UserLogin records.

					List optionErrors = accountImportExportService.validateImportOptions(options)
					if (optionErrors) {
						throw new InvalidParamException(optionErrors.toString())
					}

					options.testMode = params.testMode == 'Y'

					// Here's the money maker call that will update existing accounts and create new ones accordingly
					model.results = accountImportExportService.postChangesToAccounts(project, options)

					log.debug 'importAccounts() post results = $model.results'

					model << accountImportExportService.generateModelForPostResults(project, options)
					if (!options.filename && model.filename) {
						// log.debug 'importAccounts() step=$step set filename=$model.filename'
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename: model.filename, importOption: params.importOption]

					view = formAction + 'Results'
					log.debug 'importAccounts() view = $view'

					break

				default:
					// The default which is the first step to prompt for the spreadsheet to upload
					break
			}
		}
		catch (e) {
			switch (e) {
				case InvalidRequestException:
				case DomainUpdateException:
				case InvalidParamException:
				case EmptyResultException:
					log.debug 'importAccounts() exception ${e.getClass().name} $e.message'
					flash.message = e.message
					break
				default:
					log.error 'Exception occurred while importing data (step $currentStep)', e
					flash.message = 'An error occurred while attempting to import accounts'
			}
			// Attempt to delete the temporary uploaded worksheet if an exception occurred
			if (options.filename) {
				accountImportExportService.deletePreviousUpload(options)
			}

		}

		// log.debug 'importAccounts() Finishing up controller step=$step, view=$view, model=$model'
		render view: view, model: model
	}

	/**
	 * Cancels an account import process that is in flight; deletes the uploaded spreadsheet
	 * and then redirects the user back to the Import Account view.
	 * @param params.filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON{ accounts: List of accounts }
	 */
	@HasPermission(Permission.PersonImport)
	def cancelImport() {
		try {
			Project project = controllerService.getProjectForPage(this)
			if (!project) {
				return
			}

			accountImportExportService.cancelPreviousUpload(project, [filename: params.id])
			flash.message = 'The previous import was cancelled'
		}
		catch (InvalidRequestException | DomainUpdateException | InvalidParamException | EmptyResultException e) {
			flash.message = e.message
		}
		catch (e) {
			log.error 'cancelImport() Unexpected exception occurred while cancelling Account Import', e
			flash.message = 'An error occurred while attempting to cancel Account Import'
		}

		redirect(action: 'importAccounts')
	}

	/**
	 * Shows project Summary report filters.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def projectReport() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}
	}

	/**
	 * Generates the project Summary Web report
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def projectSummaryReport() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		render template: 'projectSummaryReport',
				model: [results: projectService.getProjectReportSummary(params),
				        person : securityService.userLoginPerson.toString(), time: TimeUtil.nowGMT()]
	}

	// Gets the number of assets with a desync between the device's assetType and its model's assetType
	@HasPermission(Permission.AdminUtilitiesAccess)
	def countAssetsOutOfSync() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		String query = """ -- Case sensitive - assets that assetType don't match
			SELECT COUNT(*)
			FROM asset_entity a
			JOIN model m ON m.model_id = a.model_id
			WHERE a.asset_class='DEVICE' AND BINARY a.asset_type <> BINARY m.asset_type """
		Long assetCount = jdbcTemplate.queryForLong(query)
		render assetCount.toString()
	}

	/**
	 * Used to reconcile assetTypes of devices with the assetType of their models
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def reconcileAssetTypes() {
		Project project = controllerService.getProjectForPage(this)

		if (project) {
			def query = ''' -- Update DEVICES with their propery asset_type from their respective model record
				UPDATE asset_entity a
				JOIN model m ON a.model_id = m.model_id
				SET a.asset_type = m.asset_type
				WHERE a.asset_class='DEVICE';'''
			jdbcTemplate.execute(query)
		}
		else {
			response.sendError(401, 'Unauthorized Error')
		}
		render 0
	}

	/**
	 * Hash a value.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def encryptValue() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		def toHashString = params.toEncryptString
		def hashAlgorithm = params.encryptAlghoritm
		def hashSalt = params.encryptSalt
		def encodedValue
		switch (hashAlgorithm) {
			case 'AES':
				encodedValue = AESCodec.instance.encode(toHashString, hashSalt)
				break
			case 'DES':
			default:
				encodedValue = DESCodec.encode(toHashString, hashSalt)
		}
		render encodedValue
	}

	/**
	 * Used to display the application memory consumption along with runtime configuration and performance data
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def systemInfo() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		int MegaBytes = 1024

		Runtime rt = Runtime.getRuntime()
		long freeMemory = rt.freeMemory() / MegaBytes
		long totalMemory = rt.totalMemory() / MegaBytes
		long maxMemory = rt.maxMemory() / MegaBytes
		long usedMemory = totalMemory - freeMemory

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean()
		MemoryUsage memNonHeap = memoryMXBean.getNonHeapMemoryUsage()
		MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage()

		OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean()
		RuntimeMXBean rtMXBean = ManagementFactory.getRuntimeMXBean()

		long heapUsed = memHeap.getUsed() / MegaBytes
		long heapCommitted = memHeap.getCommitted() / MegaBytes
		long heapMax = memHeap.getMax() / MegaBytes

		long nonHeapUsed = memNonHeap.getUsed() / MegaBytes
		long nonHeapCommitted = memNonHeap.getCommitted() / MegaBytes
		long nonHeapMax = memNonHeap.getMax() / MegaBytes

		long sysMemSize = osMxBean.getTotalPhysicalMemorySize() / MegaBytes
		long sysMemFree = osMxBean.getFreePhysicalMemorySize() / MegaBytes
		long swapSize = osMxBean.getTotalSwapSpaceSize() / MegaBytes
		long swapFree = osMxBean.getFreeSwapSpaceSize() / MegaBytes
		long virtMemCommit = osMxBean.getCommittedVirtualMemorySize() / MegaBytes

		Map sysProps = rtMXBean.getSystemProperties()

			[freeMemory      : freeMemory,
			 totalMemory     : totalMemory,
			 maxMemory       : maxMemory,
			 usedMemory      : usedMemory,
			 memoryMXBean    : memoryMXBean,
			 memNonHeap      : memNonHeap,
			 memHeap         : memHeap,
			 osMxBean        : osMxBean,
			 rtMXBean        : rtMXBean,
			 heapUsed        : heapUsed,
			 heapCommitted   : heapCommitted,
			 heapMax         : heapMax,
			 nonHeapUsed     : nonHeapUsed,
			 nonHeapCommitted: nonHeapCommitted,
			 nonHeapMax      : nonHeapMax,
			 sysMemSize      : sysMemSize,
			 sysMemFree      : sysMemFree,
			 swapSize        : swapSize,
			 swapFree        : swapFree,
			 virtMemCommit   : virtMemCommit,
			 sysProps        : sysProps,
			 osMxBean        : osMxBean,
			 rt              : rt,
			 groovyVersion   : GroovySystem.getVersion()]
	}

	/**
	 *  The admin control home page.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def home() {
		long timeNow = TimeUtil.nowGMT().time
		def dateNowSQL = TimeUtil.nowGMTSQLFormat()

		// retrieve the list of 20 usernames with the most recent login times
		def recentUsers = UserLogin.executeQuery('FROM UserLogin WHERE lastLogin is not null ORDER BY lastPage DESC', [max: 20])

		// retrieve the list of events in progress
		def moveEventsList = []

		MoveEvent.list().each { MoveEvent moveEvent ->
			Long completion = moveEvent.getEventTimes()?.completion?.time
			if (moveEvent.newsBarMode == 'on' || (completion && completion < timeNow && completion + thirtyDaysInMS > timeNow)) {
				MoveEventSnapshot snapshot = MoveEventSnapshot.findByMoveEvent(moveEvent, [sort: 'dateCreated', order: 'DESC'])
				String status = ''
				if (snapshot) {
					String color
					int indicator = snapshot.dialIndicator
					if (indicator > 0 && indicator < 25) {
						color = 'Red'
					}
					else if (indicator >= 25 && indicator < 50) {
						color = 'Yellow'
					}
					else if (indicator >= 50) {
						color = 'Green'
					}
					if (color) {
						status = color + '(' + indicator + ')'
					}
				}
				moveEventsList << [moveEvent: moveEvent, status: status, startTime: moveEvent.eventTimes.start,
				                   completionTime: moveEvent.eventTimes.completion]
			}
		}
		// retrieve the list of 10 upcoming bundles
		def upcomingBundles = MoveBundle.executeQuery(
				'FROM MoveBundle WHERE startTime>:now ORDER BY startTime',
				[now: TimeUtil.nowGMT()], [max: 10])

		[recentUsers: recentUsers, moveEventsList: moveEventsList, upcomingBundles: upcomingBundles]
	}

	/**
	 * Model Alias Conflicts
	 * @return list of model alias conflicts (result set)
	 */
	@HasPermission(Permission.ModelEdit)
	def modelConflicts() {

		String modelConflictsQuery = """
			SELECT
            	mfg.name as mfg,
            	m.model_id as model_id, m.name as model_name,
            	m2.model_id as alias_model_id,
            	if(m.model_id=m2.model_id, '', m2.name) as alternate_model
            FROM model m
            JOIN model_alias ma ON ma.manufacturer_id = m.manufacturer_id and ma.name=m.name
            JOIN model m2 ON m2.model_id = ma.model_id
            JOIN manufacturer mfg on mfg.manufacturer_id = m.manufacturer_id
            ORDER BY mfg.name, m.name """

		def conflictList = jdbcTemplate.queryForList(modelConflictsQuery)

		[conflictList: conflictList]
	}
}
