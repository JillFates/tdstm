package net.transitionmanager.asset


import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.service.ServiceMethods

class AssetService implements ServiceMethods{

    // DO NOT ADD AssetEntityService due to circular references of services
	ApplicationService  applicationService
	AssetOptionsService assetOptionsService
	CustomDomainService customDomainService
	DatabaseService     DatabaseService
	DeviceService       DeviceService
	StorageService      storageService

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
				model.personList = assetModel.personList
				break
			case "DATABASE":
				assetModel = databaseService.getModelForCreate()
				break
			case "COMMON":
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
            dataFlowFreq : GormUtil.getConstrainedProperties(AssetDependency).dataFlowFreq.inList,
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
		return AssetUtils.getEnvironmentOptions()
	}

	/**
	 * Asset Status options
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getAssetPlanStatusOptions() {
		return AssetUtils.getPlanStatusOptions()
	}

	/**
	 * The types used to assign to AssetDependency.type
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getDependencyTypeOptions() {
		return AssetUtils.getDependencyTypeOptions()
	}

	/**
	 * Asset Priority Options.
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getAssetPriorityOptions() {
		return DeviceUtils.getPriorityOptions()
	}

	/**
	 * The valid values that can be assigned to AssetDependency.status
	 * @return the values
	 */
	@Transactional(readOnly = true)
	List<String> getDependencyStatusOptions() {
		return AssetUtils.getDependencyStatusOptions()
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
	 * Used to add the Standard and Custom Field Specs to a CRUD Model which relies on the model having
	 * a number of other properties preloaded. This will inject the select options into the field spec constraints
	 * values appropriately.
	 *
	 * The end result of this method is that the standardFieldSpecs properties will be added to the model Map.
	 *
	 * @param project - the project that the field specs are for
	 * @param assetClass - the Asset Class to get the field specs for
	 * @param model
	 */
	void addFieldSpecsToCrudModel(Project project, String assetClass, Map model) {
		// Populate all of the various sets of Select Controls Options Lists in the field spect constraints
		Map standard = customDomainService.standardFieldSpecsByField(project, assetClass)
		standard.environment.constraints.values = addBlankToNonRequiredOptions(standard.environment, model.environmentOptions)
		standard.planStatus.constraints.values = addBlankToNonRequiredOptions(standard.planStatus, model.planStatusOptions)
		standard.validation.constraints.values = addBlankToNonRequiredOptions(standard.validation, ValidationType.list)

		if (assetClass == 'APPLICATION') {
			standard.criticality.constraints.values = addBlankToNonRequiredOptions(standard.criticality, Application.CRITICALITY)
		} else if (assetClass == 'DEVICE') {
			standard.priority.constraints.values = addBlankToNonRequiredOptions(standard.priority, model.priorityOption)
			// TODO : JPM 2/2020 : Double-check to see if the following are even used for the select dropdowns in the UI
			standard.railType.constraints.values = addBlankToNonRequiredOptions(standard.railType, model.railTypeOption)
			standard.roomSource.constraints.values = addBlankToNonRequiredOptions(standard.roomSource, model.sourceRoomSelect)
			standard.roomTarget.constraints.values = addBlankToNonRequiredOptions(standard.roomTarget, model.targetRoomSelect)
			standard.rackSource.constraints.values = addBlankToNonRequiredOptions(standard.rackSource, model.sourceRackSelect)
			standard.rackTarget.constraints.values = addBlankToNonRequiredOptions(standard.rackTarget, model.targetRackSelect)
			standard.sourceChassis.constraints.values = addBlankToNonRequiredOptions(standard.sourceChassis, model.sourceChassisSelect)
			standard.targetChassis.constraints.values = addBlankToNonRequiredOptions(standard.targetChassis, model.targetChassisSelect)
		}
		model.put('standardFieldSpecs', standard)

	}

	/**
	 * Used by addFieldSpecsToCrudModel to prepend a blank option if it doesn't exist in the option list of
	 * non-required fields. This will return the list of options from the
	 * @param spec - The individual field spec
	 * @return the array of options from the model options attributes appropriately
	 */
	private List<String> addBlankToNonRequiredOptions(Map spec, List<String> options) {
		if (! spec.constraints?.required && options) {
			if (! options.contains('')) {
				return [''] + options
			}
		}
		return options
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
        AssetFacade facade
        if (assetEntity) {
            Map<String, ?> fieldSpecs = customDomainService.customFieldSpecs(assetEntity.project, assetEntity.assetClass.toString(), false)
            facade = new AssetFacade(assetEntity, fieldSpecs, readonly)
        }
        return facade
	}


	/**
	 * Create and return a map with the information regarding dependencies for the Asset Show views.
	 *
	 * @param project - user's current project.
	 * @param assetId - the given asset used to search dependencies by.
	 * @return a map with the info about the dependents and supporting assets.
	 */
	Map getDependenciesMapForAsset(Project project, Long assetId) {
		AssetEntity asset = get(AssetEntity, assetId, project)

		Closure transformDependency = { String status, String type, AssetEntity depAsset ->
			return [
					assetClass: AssetClass.getClassOptionValueForAsset(depAsset),
					id: depAsset.id,
					moveBundle: depAsset.moveBundleName,
					name: depAsset.assetName,
			        status: status,
					type: type
			]
		}

		List<Map> supports = asset.supportedDependencies().collect { AssetDependency dependency ->
			transformDependency(dependency.status, dependency.type, dependency.asset)
		}

		List<Map> dependents = asset.requiredDependencies().collect { AssetDependency dependency ->
			transformDependency(dependency.status, dependency.type, dependency.dependent)
		}

		return [
		        supports: supports,
				dependents: dependents
		]

	}
}
