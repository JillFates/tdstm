package net.transitionmanager.imports

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.command.dataview.DataviewManageImportBatchesExcel
import net.transitionmanager.common.ControllerService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import org.springframework.jdbc.core.JdbcTemplate

import java.text.DateFormat

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class DataTransferBatchController implements ControllerMethods {

	static allowedMethods = [save:'POST', update:'POST']
	static defaultAction = 'list'

	private static final Map<Class, String> assetClassMap = [AssetEntity: 'device', Application: 'app',
	                                                         Database: 'db', Files: 'files']

	AssetEntityService assetEntityService
	ControllerService controllerService
	ImportService importService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService personService
	UserPreferenceService userPreferenceService
	DataTransferBatchService dataTransferBatchService

	@HasPermission(Permission.AssetImport)
	def importResults() {
		DataTransferBatch dtb = fetchDomain(DataTransferBatch, params)
		renderSuccessJson(importResults: dtb.importResults)
	}

	/**
	 * Return list of dataTransferBatchs for associated Project and Mode = Import
	 * @return dataTransferBatchList
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def list() {
		if (params.message) {
			flash.message = params.message
		}

		Project project = controllerService.getProjectForPage(this, 'to view Manage Batches')
		if (!project) return

		def dataTransferBatchList =  DataTransferBatch.findAllByProjectAndTransferMode( project, "I",
			[sort: "dateCreated", order: "desc", max: params.int('max', 25), offset: params.int('offset', 0)])

		def userAgent = request.getHeader("User-Agent")
		[dataTransferBatchList: dataTransferBatchList, projectId: project.id,
		 isMSIE: userAgent.contains("MSIE") || userAgent.contains("Firefox")]
	}

	// TODO : JPM : 10/2013 - remove this function after done testing
	// Working on an improved version of the GormUtil function to get human readable error messages
	static allErrorsString = { domain, separator= " : " ->
		def text = new StringBuilder()
		def first = true
		domain.errors.allErrors.each {
			text.append( (first ? '' : separator) + message(error:it) )
		}
		text.toString()
	}

	/**
	 * Process DataTransfervalues corresponding to a DataTransferBatch for a given batch
	 * @param params.id - the id of the DataTransferBatch
	 * @return JSON response containing the information and other attributes regarding the process
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def processImportBatch() {
		String error
		Map results

		while (true) {
			try {
				Project project = controllerService.getProjectForPage(this)
				if (!project || !controllerService.checkPermission(this, Permission.AssetImport)) {
					error = flash.message
					flash.message = null
					break
				}

				Long id = params.long('id')
				if (id == null || id < 1) {
					error = 'Invalid batch id was submitted'
					break
				}

				String tzId = userPreferenceService.timeZone

				// Call the service stub that knowns which actual service method to run
				results = importService.invokeAssetImportProcess(project.id, securityService.currentUserLoginId, id, tzId, session)
				error = results.error

			} catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
				error = e.message
			} catch (RuntimeException e) {
				error = 'An error occurred while processing the import. Please contact support for assistance.'
				if (log.debugEnabled) {
					error = "$error $e.message"
				}
				log.error "deviceProcess() failed : $e.message : userLogin $securityService.currentUsername : batchId $params.id"
				log.error ExceptionUtil.stackTraceToString(e)
			}
			break
		}

		if (error) {
			renderErrorJson(error)
		}
		else {
			renderSuccessJson(results: results)
		}
	}

	/**
	 * Used to generate the List for Manage Asset Import Batches using Kendo Grid.
	 * @return : list of batches process as JSON
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def retrieveManageBatchList() {
		Project project = securityService.userCurrentProject
		if (!project) {
			flash.message = "Please select project to view Manage Batches"
		}

		DataviewManageImportBatchesExcel dataviewParams = populateCommandObject(DataviewManageImportBatchesExcel)

		Map findAllParams = [sort: "dateCreated", order: "desc"]
		if(dataviewParams.max > 0) {
			findAllParams << [ max: dataviewParams.max, offset: dataviewParams.offset]
		}

		List<DataTransferBatch> dataTransferBatchList = DataTransferBatch.
			findAllByProjectAndTransferMode(project, "I", findAllParams)

		def result = []
		for (DataTransferBatch entry in dataTransferBatchList) {

			def domainName
			def className

			// String entityTypeDomainName = entry.eavEntityType.domainName
			String entityTypeDomainName = entry.assetClass
			if (entityTypeDomainName) {
				if (entityTypeDomainName == 'Files') {
					domainName = 'Logical Storage'
				} else if (entityTypeDomainName == 'AssetEntity') {
					domainName = 'Device'
				} else {
					domainName = entityTypeDomainName
				}

				className = assetClassMap[entityTypeDomainName]
			}

			DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

			result.add(
				batchId: entry.id,
				importedAt: entry.dateCreated,
				importedBy: entry.userLogin?.person ? entry.userLogin.person.firstName + ' ' + entry.userLogin.person.lastName : '',
				attributeSet: entry.dataTransferSet?.title ?: '',
				class: domainName,
				assets: DataTransferValue.executeQuery('''
					select count(d.id)
					from DataTransferValue d
					where d.dataTransferBatch=:dataTransferBatch
					group by rowId
				''', [dataTransferBatch: entry]).size(),
				status: fieldValue(bean: entry, field: 'statusCode'),
				action: '',
				className: className,
				hasErrors: entry.hasErrors,
				importResults: entry.importResults as Boolean)
		}

		render result as JSON
	}

	/**
	 * Called by the Asset Post page via Ajax in order to show progress of how many assets have been posted
	 * @param  : processed and total assts from session
	 *	@return : processed data for Batch progress bar
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def retrieveProgress() {
		def progressData = [[processed: session.getAttribute("TOTAL_PROCESSES_ASSETS"),
		                     total: session.getAttribute("TOTAL_BATCH_ASSETS")]]
		render progressData as JSON
	 }

	/**
	 * data transfer batch error list
	 */
	@HasPermission(Permission.DataTransferBatchView)
	def errorsListView() {
		long dataTransferBatchId = params.long('id')

		List<DataTransferValue> dataTransferErrorList = DataTransferValue.where {
			dataTransferBatch.id == dataTransferBatchId && hasError == 1
		}.list()

		def currentValues

		List completeDataTransferErrorList = dataTransferErrorList.collect { DataTransferValue dataTransferError ->
			AssetEntity assetEntity = AssetEntity.read(dataTransferError.assetEntityId)
			currentValues = assetEntity?.(dataTransferError.fieldName)

			println dataTransferError.inspect()
			println "*** dataTransferError.assetEntityId = ${dataTransferError.assetEntityId}"
			println "*** assetEntity=$assetEntity, dataTransferError.fieldName=${dataTransferError.fieldName}"

			return [
				assetName   : assetEntity.assetName,
				assetTag    : assetEntity.assetTag ?: '',
				attribute   : dataTransferError.fieldName,
				error       : dataTransferError.errorText,
				currentValue: currentValues,
				importValue : dataTransferError.importValue
			]
		}

		[completeDataTransferErrorList: completeDataTransferErrorList.sort { it.assetTag + it.field_name }]
	}

	/**
	 * Delete the Data Transfer Batch Instance
	 */
	@HasPermission(Permission.DataTransferBatchDelete)
	def delete() {
		// Legacy code. We have support for both option: params.id or params.batchId
		if(params.batchId) params.id = params.batchId
		DataTransferBatch dataTransferBatch = fetchDomain(DataTransferBatch, params)
		dataTransferBatchService.delete(dataTransferBatch)

		flash.message = "DataTransferBatch $params.batchId deleted"
		redirect(action: "list")
	}
}
