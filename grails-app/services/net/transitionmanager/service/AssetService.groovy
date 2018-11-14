package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.asset.DeviceUtils
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.asset.AssetFacade
import grails.transaction.Transactional


class AssetService {

    // DO NOT ADD AssetEntityService due to circular references of services
    ApplicationService applicationService
    AssetOptionsService assetOptionsService
    CustomDomainService customDomainService
    DatabaseService DatabaseService
    DeviceService DeviceService
    SecurityService securityService
    StorageService storageService

    /**
     * Used to return a Map that contains all of the details of the asset and the Options that will be used in the SELECT controls for the Asset CREATE/EDIT
     * @param assetClassName - the name of the asset class
     * @return Map of the asset default values and all of the select control options
     */
	@Transactional(readOnly = true)
    Map getCreateModel(Project project, String assetClassName) {
        Map assetModel
        Map model = [:]
        switch (assetClassName?.toUpperCase()) {
			case "APPLICATION":
				assetModel = applicationService.getModelForCreate()
                model.criticalityOptions = Application.CRITICALITY
				break
			case "DATABASE":
				assetModel = databaseService.getModelForCreate()
				break
			case "DEVICE":
				assetModel = deviceService.getModelForCreate()
                model << DeviceUtils.deviceModelOptions(project)
				break
			case "STORAGE":
				assetModel = storageService.getModelForCreate()
				break
            default:
                throw new InvalidParamException('Unsupported Asset Class type ' + (assetClassName ?: '(undefined)') )
		}

        model.asset = assetModel.assetInstance

        // Set the defaults for the custom fields based on the field specs
        for (spec in assetModel.customs) {
            model.asset[spec.field] = spec.default
        }

        model << commonModelOptions(project)

        return model
    }

    /**
     * Used to return a Map that contains all of the details of the asset and the Options that will be used in the SELECT controls for the Asset CREATE/EDIT
     * NOTE that this is not yet used but should be filling in for WsAssetController.getModel
     * @param assetClassName - the name of the asset class
     * @return Map of the asset default values and all of the select control options
     */
	@Transactional(readOnly = true)
    Map getEditModel(Project project, AssetEntity asset) {
        Map model = [:]

        model << commonModelOptions(project)

        return model
    }

    /**
     * Generates the List<Map> objects that represent the Select Options that are common for all asset classes
     * @param project - the user's current project
     * @return Map of the various option lists
     */
	@Transactional(readOnly = true)
    Map commonModelOptions(Project project) {
        [
            environmentOptions : getAssetEnvironmentOptions(),
            planStatusOptions : getAssetPlanStatusOptions(),
            validationOptions : ValidationType.list,
            dataFlowFreq : AssetDependency.constraints.dataFlowFreq.inList,
            moveBundleList : getMoveBundleOptions(project),
            dependencyMap : dependencyCreateMap(project)
        ]
    }

	/**
	 * Return the map of information for show/create dependencies.
	 * @param project
	 * @return
	 */
	@Transactional(readOnly = true)
	Map dependencyCreateMap(Project project) {
		return [
            assetClassOptions: AssetClass.classOptions,
            dependencyStatus: getDependencyStatusOptions(),
            dependencyType: getDependencyTypeOptions(),
            moveBundleList: getMoveBundleOptions(project),
            nonNetworkTypes: AssetType.nonNetworkTypes,
		]
	}

    /**
     * Used to return the list of MoveBundle Options use in a Select control
     * @param project - the user's current project
     * @return a List of maps consisting of id and name for the bundles
     */
	@Transactional(readOnly = true)
    List<Map> getMoveBundleOptions(Project project) {
        GormUtil.listDomainForProperties(project, MoveBundle, ['id','name'], [['name']])
    }

	/**
	 * Asset Environment options
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getAssetEnvironmentOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
	}

	/**
	 * Asset Status options
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getAssetPlanStatusOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
	}

	/**
	 * The types used to assign to AssetDependency.type
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getDependencyTypeOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
	}

	/**
	 * Asset Priority Options.
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getAssetPriorityOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
	}

	/**
	 * The valid values that can be assigned to AssetDependency.status
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getDependencyStatusOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
	}

    /**
     * Used to set default values for custom fields on the given asset
     * @param asset - the asset to set the default custom fields values into
     * @param fieldSpec - the list of custom fields
     */
    void setCustomDefaultValues(AssetEntity asset) {

        String domain = asset.assetClass.toString().toUpperCase()

        // The asset has to have a project assigned to it to make this all work correctly
        assert asset.project

        // Get list of ALL custom fields
        Map fieldSpecs = customDomainService.customFieldSpecs(asset.project, domain)
        List fieldList = fieldSpecs[domain].fields

        // Loop over the fields and set the default value appropriately
        for (field in fieldList) {
            if ( ! StringUtil.isBlank(field.default) ) {
                asset[field.field] = field.default
            }
        }
    }

   /**
    * Used to delete asset dependencies in bulk with a list of dependency ids for a specific project
    * @param project - the project that the asset dependencies should belong to
    * @param dependencyIds - list of ids for which dependencies are requested to be deleted
    * @return a count of the number of records that are deleted
    */
   String bulkDeleteDependencies(Project project, List<String> dependencyIds) {

      // if no ids given don't process anything.
      if (dependencyIds.isEmpty()) {
         return "0 $type records were deleted"
      }
      List<Long> depIds = NumberUtil.toPositiveLongList(dependencyIds)

      log.debug "bulkDeleteDependencies: $depIds to be deleted"
      int count = depIds.size()

      // Now make sure that the ids are associated to the project
      List<Long> validatedDepIds =
         AssetDependency.where {
            asset.project == project
            dependent.project == project
             id in depIds
         }
         .projections { property 'id' }
         .list()

      if (count != validatedDepIds.size()) {
         List<Long> idsNotInProject = depIds - validatedDepIds
         log.warn "bulkDeleteDependencies called with dependency ids not assigned to project ($depIds)"
      }

      if (validatedDepIds.size() > 0) {
            count = AssetDependency.where {
               id in validatedDepIds
            }.deleteAll()
      } else {
         count = 0
      }

      return "$count record${count==1? ' was' : 's were'} deleted"
   }

    /**
     * Factory method that constructs a AssetFacade populating the custom fields specs for the asset entity type provided
     * @param assetEntity - the assent entity
     * @param readonly - whether the facade needs to be readonly
     * @return
     */
    AssetFacade getAssetFacade(AssetEntity assetEntity, boolean readonly) {
        Map<String, ?> fieldSpecs = customDomainService.customFieldSpecs(assetEntity.project, assetEntity.assetClass.toString(), false)
        return new AssetFacade(assetEntity, fieldSpecs, readonly)
   }
}
