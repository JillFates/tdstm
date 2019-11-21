package net.transitionmanager.asset


import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.bulk.BulkETLCommand
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.imports.DataviewService
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
/**
 * This handles taking in a bulk change ETL and executing it.
 */
@Transactional
class BulkAssetETLService implements ServiceMethods {
	FileSystemService fileSystemService
	DataviewService   dataviewService
	DataImportService dataImportService


	/**
	 * Handles the bulk change json, and delegates the bulk change to the appropriate service.
	 *
	 * @param currentProject used fot checking security.
	 * @param bulkETL the command object that holds the bulk change json.
	 */
	Map runBulkETL(Project currentProject, BulkETLCommand bulkETL) {
		List ids = []
		Map queryFilter = [:]
		Map<String, Map> fieldMapping

		//For some reason adding the customDomainService causes a dependency loop, and crashed the app so I'm accessing it through the dataviewService
		fieldMapping = dataviewService.projectService.customDomainService.fieldToBulkChangeMapping(currentProject)

		if (bulkETL.allIds) {
			queryFilter = dataviewService.getAssetIdsHql(currentProject, bulkETL.dataViewId, bulkETL.userParams)
		} else {
			ids = bulkETL.ids
			int validAssetCount = AssetEntity.where { id in ids && project == currentProject }.count()

			if (validAssetCount != ids.size()) {
				throw new InvalidParamException("Only $validAssetCount of the ${ids.size()} records specified were found so the changes were not applied. Please repeat the bulk change process accordingly.")
			}
		}

		try {
			List<Map> assets = getAssets(ids, queryFilter)
			List<Map> assetsUsingLabels = replaceLabels(assets, fieldMapping)
			List<Object> tempFileObject = fileSystemService.createTemporaryFile('','json')

			OutputStream outputStream = tempFileObject[1]
			outputStream.write((assetsUsingLabels as JSON).toString().getBytes())
			outputStream.flush()
			outputStream.close()

			Map result = dataImportService.scheduleETLTransformDataJob(
				currentProject,
				bulkETL.dataScriptId,
				tempFileObject[0],
				bulkETL.sendEmail
			)

			return result

		} catch (InvalidParamException e) {
			throw e
		} catch (Exception e) {
			log.error('An unexpected error occurred while invoking the bulk change action', e)
			throw new DomainUpdateException('An unexpected error occurred while invoking the bulk change action', e)
		}
	}


	/**
	 * Gets the list of assets based on Ids or a qureryFilter passed in.
	 *
	 * @param ids The asset Ids to look up other wise use the queryFilter.
	 * @param queryFilter The query filter to use if ther are no assetIds.
	 *
	 * @return A list Of assets to use in the builk ETL.
	 */
	List<AssetEntity> getAssets(List<Long> ids, Map queryFilter) {
		String queryForAssetIds
		Map params = [:]

		//Map assetQueryParams = [:]

		if (ids && !queryFilter) {
			queryForAssetIds = ':assetIds'
			params.assetIds = ids
			//assetQueryParams['assetIds'] = ids
		} else {
			queryForAssetIds = queryFilter.query
			params << queryFilter.params
			//assetQueryParams = queryFilter.params
		}

		String query = """
			FROM AssetEntity a
			WHERE a.id in ($queryForAssetIds)
		"""
		//TODO should we do this?
		//assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)

		return AssetEntity.executeQuery(query, params)
	}

	/**
	 * This goes through a list of assets and creates a list of maps replacing property names with there labels from the FieldMapping
	 *
	 * @param assets The assets to convert to a List of maps.
	 * @param fieldMapping The field mapping to use to look up the property names to replace.
	 *
	 * @return A list of Maps containing the asset properties with the labels from the field mappings as keys.
	 */
	List<Map> replaceLabels(List<AssetEntity> assets, Map<String, Map> fieldMapping) {
		Map updatedRow

		return assets.collect { AssetEntity asset ->
			updatedRow = [:]

			GormUtil.persistentProperties(asset).each { property ->
				String label = fieldMapping[property]?.label ?: property
				def value = asset."$property"
				if(value  &&(value instanceof String || value instanceof  Long || value instanceof Integer || value instanceof Date || value instanceof Double)) {
					updatedRow[label] = value
				} else if(value && value instanceof Enum){
					updatedRow[label] = value.name()
				} else if(GormUtil.isDomainClass(value)){
					updatedRow[label] = value.id
				}
			}

			return updatedRow
		}
	}
}
