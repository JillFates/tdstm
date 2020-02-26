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
			List<AssetEntity> assets = getAssets(ids, queryFilter, currentProject)
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
	 * @param currentProject, the project to get assets for.
	 *
	 * @return A list Of assets to use in the bulk ETL.
	 */
	List<AssetEntity> getAssets(List<Long> ids, Map queryFilter, Project currentProject) {
		String queryForAssetIds
		Map params = [:]


		if (ids && !queryFilter) {
			queryForAssetIds = ':assetIds'
			params.assetIds = ids
		} else {
			queryForAssetIds = queryFilter.query
			params << queryFilter.params
		}
		params.project = currentProject

		String query = """
			FROM AssetEntity a
			WHERE a.id in ($queryForAssetIds) AND project = :project
		"""

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

			fieldMapping[asset.assetClass.name()].each { String key, Map mapValue ->
				String label = mapValue?.label ?: key

				if (asset.hasProperty(key)) {
					def value = asset."$key"

					switch (value) {
						case String || Number || Date || Boolean:
							break
						case Enum:
							value = value.name()
							break
						case { GormUtil.isDomainClass(it) }:
							value = value.id
							break
					}

					updatedRow[label] = value

				}
			}

			return updatedRow
		}
	}
}
