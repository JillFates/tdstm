import grails.converters.JSON

import java.text.SimpleDateFormat

import org.apache.commons.lang.math.NumberUtils
import java.util.regex.Matcher
// import org.apache.shiro.SecurityUtils

import com.tds.asset.Application
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.WebUtil

class DataTransferBatchController {

	// Objects to be injected
	def sessionFactory
	def assetEntityService
	def assetEntityAttributeLoaderService
	def controllerService
	def importService
	def jdbcTemplate
	def securityService
	def partyRelationshipService
	def personService

	def messageSource

	// Data used within some of the controller methods static vars bundleMoveAndClientTeams and bundleTeamRoles
	// TODO : JPM 9/2014 : Need to remove the 
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static bundleTeamRoles = ['sourceTeamMt':'MOVE_TECH','targetTeamMt':'MOVE_TECH',
		'sourceTeamLog':'CLEANER','targetTeamLog':'CLEANER',
		'sourceTeamSa':'SYS_ADMIN','targetTeamSa':'SYS_ADMIN',
		'sourceTeamDba':'DB_ADMIN','targetTeamDba':'DB_ADMIN'
	]

	protected static formatter = new SimpleDateFormat("M-d-yyyy")

	/**
	 * The default index page loads the list page
	 */
	def index = { redirect(action:list, params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [save:'POST', update:'POST']


	/**
	 * Return list of dataTransferBatchs for associated Project and Mode = Import
	 * @param projectId
	 * @return dataTransferBatchList
	 */
	def list = {
		if(params.message){
			flash.message = params.message
		}
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view Manage Batches"
			redirect(controller:'project',action:'list')
			return
		}
		if( !params.max ) params.max = 10
		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( project, "I", 
			[sort:"dateCreated", order:"desc", max:params.max,offset:params.offset ? params.offset : 0] )
		
		def isMSIE = false
		def userAgent = request.getHeader("User-Agent")
		if (userAgent.contains("MSIE") || userAgent.contains("Firefox"))
			isMSIE = true
		return [ dataTransferBatchList:dataTransferBatchList, projectId:projectId, isMSIE:isMSIE ]
	}

// TODO : JPM : 10/2013 - remove this function after done testing
// Working on an improved version of the GormUtil function to get human readable error messages
	def public static allErrorsString = { domain, separator= " : " ->
		def text = new StringBuilder()
		def first = true
		domain.errors.allErrors.each { 
			println message(error: it)
//			text.append( (first ? '' : separator) + messageSource.getMessage(it, null) ) 
			text.append( (first ? '' : separator) + message(error:it) ) 
		}
		
		text.toString()
	}

	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for a given batch
	 * @param params.id - the id of the DataTransferBatch
	 * @return JSON response containing the information and other attributes regarding the process
	 */
	def processImportBatch = {
		String error
		Map results
		Project project
		UserLogin userLogin

		while (true) {
			try {
				(project, userLogin) = controllerService.getProjectAndUserForPage( this, 'Import' )
				if (! project) { 
					error = flash.message
					flash.message = null
					break
				}

				Long id = NumberUtil.toLong(params.id)
				if (id == null || id < 1) {
					error = 'Invalid batch id was submitted'
					break
				}

				def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ

				// Call the service stub that knowns which actual service method to run
				results = importService.invokeAssetImportProcess(project.id, userLogin.id, id, tzId, session)
				error = results.error
				
			} catch (UnauthorizedException e) {
				error = e.getMessage()
			} catch (InvalidParamException e) {
				error = e.getMessage()
			} catch (DomainUpdateException e) {
				error = e.getMessage()
			} catch (RuntimeException e) {
				error = 'An error occurred while processing the import. Please contact support for assistance.'
				if (log.isDebugEnabled()) {
					error = "$error ${e.getMessage()}"
				}
				log.error "deviceProcess() failed : ${e.getMessage()} : userLogin $userLogin : batchId ${params.id}"
				log.error ExceptionUtil.stackTraceToString(e)
			}
			break
		}

		if (error)
			render ServiceResults.errors(error) as JSON
		else
			render ServiceResults.success([results:results]) as JSON
	}

	/**
	 * Called by the Asset Post page via Ajax in order to show progress of how many assets have been posted
	 * 	@param  : processed and total assts from session 
	 *	@return : processed data for Batch progress bar
	 */
	def getProgress = {
		def progressData = []
		def total = session.getAttribute("TOTAL_BATCH_ASSETS") 
		def processed = session.getAttribute("TOTAL_PROCESSES_ASSETS")
		progressData << [processed:processed, total:total]
		render progressData as JSON
	 }
	
	/* --------------------------------------
	 * 	@author : Mallikarjun
	 *	@return : data transfer batch error list
	 * -------------------------------------- */
	def errorsListView = {
		def dataTransferBatchInstance = DataTransferBatch.get( params.id )
		def query = new StringBuffer(" select d.asset_entity_id,d.import_value,d.row_id,a.attribute_code,d.error_text FROM data_transfer_value d ")
		query.append(" left join eav_attribute a on (d.eav_attribute_id = a.attribute_id) where d.data_transfer_batch_id = ${dataTransferBatchInstance.id} ")
		query.append(" and has_error = 1 ")
		def dataTransferErrorList = jdbcTemplate.queryForList( query.toString() )
		def completeDataTransferErrorList = []
		def currentValues
		dataTransferErrorList.each {
			def assetNameId = EavAttribute.findByAttributeCode("assetName")?.id
			def assetTagId = EavAttribute.findByAttributeCode("assetTag")?.id
			def assetName = DataTransferValue.find("from DataTransferValue where rowId=$it.row_id and eavAttribute=$assetNameId "+
													"and dataTransferBatch=$dataTransferBatchInstance.id")?.importValue
			def assetTag = DataTransferValue.find("from DataTransferValue where rowId=$it.row_id and eavAttribute=$assetTagId "+
													"and dataTransferBatch=$dataTransferBatchInstance.id")?.importValue
			def assetEntity = AssetEntity.find("from AssetEntity where id=${it.asset_entity_id}")
			if( bundleMoveAndClientTeams.contains(it.attribute_code) ) {
				currentValues = assetEntity?.(it.attribute_code).name
			} else {
				currentValues = assetEntity?.(it.attribute_code)
			}
			completeDataTransferErrorList << ["assetName":assetName, "assetTag":assetTag, "attribute":it.attribute_code, "error":it.error_text,  
											  "currentValue":currentValues, "importValue":it.import_value]
		}
		completeDataTransferErrorList.sort{it.assetTag + it.attribute}
		return [ completeDataTransferErrorList : completeDataTransferErrorList ]
	 }

	/**
	 * Update Asset Racks once import batch process done.
	 */
/*	 
	def updateAssetRacks = {
		def assetsList = session.getAttribute("IMPORT_ASSETS")
		assetsList.each { assetId ->
			AssetEntity.get(assetId)?.updateRacks()
		}
		session.setAttribute("IMPORT_ASSETS",null)
		render ""
	}
*/
	/**
	 *     Delete the Data Transfer Batch Instance
	 */
	def delete = {
		try{
			def dataTransferBatchInstance = DataTransferBatch.get(params.batchId)
			if(dataTransferBatchInstance) {
				dataTransferBatchInstance.delete(flush:true,failOnError:true)
				flash.message = "DataTransferBatch ${params.batchId} deleted"
				redirect(action:list)
			}else {
				flash.message = "DataTransferBatch not found with id ${params.batchId}"
				redirect(action:list)
		   }
		} catch(Exception e) {
			e.printStackTrace()
		}
	}
	
	
}