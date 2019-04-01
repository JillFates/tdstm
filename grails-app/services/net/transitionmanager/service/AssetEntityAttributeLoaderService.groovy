package net.transitionmanager.service

import net.transitionmanager.asset.AssetCableMap
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.ControlType
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import grails.util.GrailsClassUtils
import net.transitionmanager.imports.DataTransferBatch
import net.transitionmanager.imports.DataTransferValue
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelAlias
import net.transitionmanager.model.ModelConnector
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import org.apache.commons.lang3.math.NumberUtils
import org.apache.poi.ss.usermodel.Sheet

class AssetEntityAttributeLoaderService implements ServiceMethods {

	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	MoveBundleService moveBundleService
	AssetOptionsService assetOptionsService

	private static final String DEFAULT_DEVICE_TYPE = 'Server'
	private static final String UNKNOWN_MFG_MODEL = 'Unknown'

	/**
	 * Check the sheet headers.
	 */
	private boolean checkHeader(int column, Map map, Sheet sheet) {
		for (int c = 0; c < column; c++) {
			def cellContent = WorkbookUtil.getStringCellValue(sheet, c, 0)
			if (map.containsKey(cellContent)) {
				map[cellContent] = c
			}
		}
		!map.containsValue(null)
	}

	/**
	 * To Validate the Import Process If any Errors update DataTransferBatch and DataTransferValue
	 * @author Srinivas
	 * @param DataTransferBatch - the record of the transfer batch
	 * @param AssetEntity - the asset to validate
	 * @param Map of the property attributes from the import
	 * @param fieldSpecs - fields specification list
	 * @return Map of [flag, errorConflictCount]
	 *     flag being true indicates that the asset was updated since the export was generated
	 *     errorConflictCount indicates the number of fields that have conflicts
	 */
	@Transactional
	def importValidation(dataTransferBatch, asset, dtvList, List<Map<String, ?>> fieldSpecs) {
		//Export Date Validation
		def errorConflictCount = 0

		def modifiedSinceExport = (asset.lastUpdated && asset.lastUpdated >= dataTransferBatch.exportDatetime)
		log.info 'importValidation() asset lastUpdated={}, exportDatetime={}, modifiedSinceExport={}', asset.lastUpdated, dataTransferBatch.exportDatetime, modifiedSinceExport

		if (modifiedSinceExport) {
			log.info 'importValidation() Asset $asset was modified at {}, after the export at {}', asset, asset.lastUpdated, dataTransferBatch.exportDatetime
			// If the asset has been modified, see how many of the fields are in conflict
			dtvList.each { dtValue ->
				String attribName = dtValue.fieldName
				Map<String, ?> fieldSpec = fieldSpecs.find { field -> (field["field"] == attribName || field["label"] == attribName) }

				if (attribName == "moveBundle") {
					if (asset?.moveBundle?.name!= dtValue.correctedValue && asset?.moveBundle?.name!= dtValue.importValue){
						updateChangeConflicts(dataTransferBatch, dtValue)
						errorConflictCount++
					}
				} else if (attribName in ["usize", "modifiedBy", "lastUpdated"]){
					// skip the validation
				} else if (fieldSpec["control"] == ControlType.NUMBER.toString()) {
					def correctedPos
					try {
						if (dtValue.correctedValue) {
							correctedPos = Integer.parseInt(dtValue.correctedValue.trim())
						} else if (dtValue.importValue) {
							correctedPos = Integer.parseInt(dtValue.importValue.trim())
						}
						if (asset."$attribName"!= correctedPos){
							updateChangeConflicts(dataTransferBatch, dtValue)
							errorConflictCount++
						}
					} catch (Exception ex) {
						// TODO - JM 10/2013 - nothing? really? if the parseInt fails shouldn't it be an error?
						log.error 'importValidation() failed parseInt most likely'
					}
				} else {
					if (attribName.contains('.')) {
						// These are the dot notation properties for master/child relationships
						// TODO : 9/2014 JPM : Need to figure out what to do as part of the validation of master.child properties
					} else if (asset."$attribName"!= dtValue.correctedValue && asset."$attribName"!= dtValue.importValue){
						updateChangeConflicts(dataTransferBatch, dtValue)
						errorConflictCount++
					}

				}
			}
		}

		return [flag:modifiedSinceExport, errorConflictCount:errorConflictCount]
	}

	/*
	 * Update ChangeConficts if value is changed in spreadsheet
	 * @param dataTransferBatch, datatransfervalue
	 * @author srinivas
	 */
	@Transactional
	def updateChangeConflicts(def dataTransferBatch, def dtValue) {
		if (dataTransferBatch.hasErrors == 0) {
			dataTransferBatch.hasErrors = 1
		}
		dtValue.hasError = 1
		dtValue.errorText = "change conflict"
		dtValue.save(flush:true)
		log.warn 'Conflict in change: {}', dtValue
	}

	/* To get DataTransferValue Asset MoveBundle
	 * @param dtv - a Map or a DataTransferValue object
	 * @param project - the project to get the bundle reference for
	 * @return the appropriate MoveBundle
	 */
	 private MoveBundle getDtvMoveBundle(def dtv, Project project) {
		if (dtv.correctedValue && dtv.correctedValue.toUpperCase().trim() != "NULL") {
			return moveBundleService.createBundleIfNotExist(dtv.correctedValue, project)
		}
		if (dtv.importValue && dtv.importValue.toUpperCase().trim() != "NULL") {
			return moveBundleService.createBundleIfNotExist(dtv.importValue, project)
		}
		if (!dtv.importValue) {
			return projectService.getDefaultBundle(project)
		}
	}

	/**
	 * Used to find or create both the manufacturer and/or model based on the values from the DataTransferValue objects for the two properties. This
	 * method will also update the model and manufacturer properties of the device
	 * @param userLogin - the UserLogin object of the person invoking this method
	 * @param device - the AssetEntity object that is being created or updated
	 * @param mfgNameParam - name of the manufacturer
	 * @param modelNameParam - name of the model
	 * @param deviceTypeParam - the Device Type of the device that is used to help resolve the model at times
	 * @param deviceTypeMap - a map of all of the existing types that the user's import must match in order to insert/update devices' models
	 * @param usize - the Usize of the asset used when creating new models
	 * @param canCreateMfgAndModel - a flag indicating that the user has the permission to create new mfg and models
	 * @return A map containing the following values:
	 * 		errorMsg - one or more error messages that also is used to signal that the assignment failed for some reason
	 * 		warningMsg - one or more warning messages that might be usesful to the user
	 * 		modelWasCreated - flag to indicate that a new Model was created as a result of this call
	 * 		mfgWasCreated - flag to indicate that a new Manufacturer was created as a result of this call
	 * TODO : JPM 11/2014 : Refactor function assignMfgAndModelToDevice into the ImportService class
	 */
	@Transactional
	Map assignMfgAndModelToDevice(UserLogin userLogin, AssetEntity device, String mfgNameParam,
	                              String modelNameParam, String deviceTypeParam, Map<String, String> deviceTypeMap,
	                              String usize, boolean canCreateMfgAndModel) {
		String methodName = 'assignMfgAndModelToDevice()'
		String errorMsg
		String warningMsg
		boolean deviceExists = device.id > 0
		boolean mfgWasCreated = false
		boolean modelWasCreated = false
		// Flag to control if a combination should be cachable (e.g. blank mfg/model/type should NOT be cached)
		boolean cachable = true

		String mfgName = mfgNameParam
		String modelName = modelNameParam
		String deviceType = deviceTypeParam
		boolean haveMfgName = mfgName
		boolean haveModelName = modelName
		boolean haveDeviceType = deviceType

		String delim = '. '

		// Flag when deviceType is supplied and is invalid which in most cases will result in an error or warning
		boolean invalidDeviceType = true

		// Get the Unknown Mfg in case we're doing a partial Mfg/Model reference and will go with the Unknown Mfg and corresponding Model
		Manufacturer unknownMfg = Manufacturer.findByName('Unknown')

		// Double check the device type and set the deviceType to the proper case if found so we can use it below correctly
		if (haveDeviceType) {
			String dtlc = deviceType.toLowerCase()
			if (deviceTypeMap.containsKey(dtlc)) {
				deviceType = deviceTypeMap[dtlc]
				log.debug '{} Found {} in deviceTypeMap', methodName, dtlc
				invalidDeviceType = false
				haveDeviceType = true
			}
		}

		log.debug '**** {} mfgNameParam={}, modelNameParam={}, deviceType=({}/{}), deviceExists={}, haveDeviceType={}, invalidDeviceType={}',
				methodName, mfgNameParam, modelNameParam, deviceTypeParam, deviceType, deviceExists, haveDeviceType, invalidDeviceType

		// Some common error/warning messages used below
		String DEVICE_TYPE_INVALID = "Device Type ($deviceTypeParam) is invalid"
		String DEVICE_TYPE_BLANK = "Device Type is needed but is blank"
		String MODEL_BLANK = "Model name is needed but is blank"
		String LACK_INFO_NO_CREATE = 'Incomplete Mfg/Model/Type therefore did not create device'
		String UNEXPECTED_CONDITION = "An unexpected condition occurred for the Mfg/Model/Type combination"

		// Get the device's current mfg/model
		Manufacturer mfg = device.model?.manufacturer ?: device.manufacturer
		Model model = device.model

		// Helper closure used that will assign an existing Mfg/Model to the the asset using the supplied model object
		def performAssignment = { modelObj ->
			device.model = modelObj
			device.manufacturer = modelObj.manufacturer
			device.assetType = modelObj.assetType

			log.debug '{}.performAssignment() model={}', methodName, modelObj

			// Add a few possible warning messages
//			if (! device.isaBlade() && usize?.size() && usize!= modelObj.usize) {
			if (usize && usize != modelObj.usize.toString())  {
				warningMsg = StringUtil.concat(warningMsg, "Specified u-size ($usize) differs from existing model ($modelObj.usize)", delim)
			}
			if (haveDeviceType && deviceType!= modelObj.assetType) {
				warningMsg = StringUtil.concat(warningMsg,
					"Specified device type ($deviceTypeParam) differs from existing model type ($modelObj.assetType) for $device.model", delim)
			} else if (invalidDeviceType) {
				warningMsg = StringUtil.concat(warningMsg,
					"Specified device type ($deviceTypeParam) was invalid, defaulted to existed model type ($modelObj.assetType)", delim)
			}
		}

		// Helper closure used to create a model and possibly a manufacturer
		// @param mfgObj - the name of the Mfg if creating a new Mfg or the existing Mfg record
		// @param createModelName - the name of the model to create
		// @note This will assume the usize and deviceType from the local scope variables
		def performCreateMfgModel = { mfgObj, createModelName, createDeviceType, createUsize ->
			log.debug '{}.performCreateMfgModel() mfg={}, createModelName={}, createDeviceType={}, createUsize={}',
					methodName, mfgObj, createModelName, createDeviceType, createUsize

			if (mfgObj instanceof String) {
				mfgName = mfgObj

				if (canCreateMfgAndModel) {
					mfg = new Manufacturer(name: mfgName)
					save mfg, true

					if (mfg.hasErrors()) {
						errorMsg = "An error occured while trying to create the new manufacturer ($mfgName)"
						return
					}

					log.info '{}.performCreateMfgModel() Manufacturer {} was just created (${})', methodName, mfgName, mfg.id
					mfgWasCreated = true
				} else {
					errorMsg = "You do not have permission to create manufacturer ($mfgName)"
					return
				}
			} else {
				mfgName = mfgObj.name
				mfg = mfgObj
			}

			if (canCreateMfgAndModel) {
				modelName = createModelName

				try {
					model = Model.createModelByModelName(modelName, mfg, createDeviceType,  NumberUtil.toInteger(createUsize), userLogin?.person)
					modelWasCreated = true
					performAssignment(model)
					assetOptionsService.findOrCreate(AssetOptions.AssetOptionsType.ASSET_TYPE, createDeviceType)
					log.info '{}.performCreateMfgModel() Model {} was created (id {})', methodName, modelName, model.id
				} catch (e) {
					errorMsg = e.message
				}
			} else {
				errorMsg = "You do not have permission to create model ($mfgName/$modelName)"
			}
		}

		// Helper closure used to setup various variables for when we'll create/assign an Unknown Mfg/Model
		def performUnknownAssignment = {
			log.debug '{}.performUnknownAssignment() modelName={}, deviceType={}', methodName, modelName, deviceType
			if (! unknownMfg) {
				errorMsg = "Unable to find the 'Unknown' manufacturer"
			} else {
				modelName = "$UNKNOWN_MFG_MODEL - $deviceType"
				model = Model.findWhere(modelName:modelName, manufacturer:unknownMfg, assetType:deviceType)
				if (model) {
					performAssignment(model)
				} else {
					performCreateMfgModel(unknownMfg, modelName, deviceType, '1')
				}
			}
		}

		// Handle the off-chance that the model.manufacturer doesn't match device.manufacturer. If so, set the device mfg to that of the model
		if (model && mfg && device.manufacturer!= mfg) {
			device.manufacturer = mfg
		}

		while (true) {

			// If we don't have any of the information to lookup the Mfg/Model or just deviceType
			if (! haveMfgName &&! haveModelName) {
				if (device.model) {
					if (haveDeviceType && deviceType!= device.model.assetType) {
						warningMsg = "Specific device type ($deviceTypeParam) does not match the asset's exiting model type ($device.model.assetType})"
					}
					log.debug '{} CASE 112/114', methodName
					cachable = false
				} else {
					if (haveDeviceType &&!invalidDeviceType) {
						log.debug '{} CASE 112', methodName
						performUnknownAssignment()
					} else {
						if (haveDeviceType && invalidDeviceType) {
							log.debug '{} CASE 114', methodName
							errorMsg = "An invalid device type ($deviceTypeParam) was specified"
						} else {
							log.debug '{} CASE 111', methodName
							errorMsg = 'A Model Name plus Mfg Name or Device Type are required'
						}
					}
				}
				break
			}

			// Handle the NULLing situation which will set the mfg/model to Unknow/Unknown - DeviceType, which will be created as necessary
			if (mfgName == ImportService.NULL_INDICATOR || modelName == ImportService.NULL_INDICATOR) {
				if (! haveDeviceType)
					deviceType = DEFAULT_DEVICE_TYPE

				performUnknownAssignment()
				//cachable = false
				break
			}

			List mfgList = []
			if (haveMfgName)
				mfgList = findManufacturersByName(mfgName)
			if (mfgList.size() > 1) {
				// Check to see if we found more than one manufacturer / model combination
				// Note that this should be nearly impossible unless someone screws up with the aliases some how
				log.error 'Manufacturer name ({}) is not unique and should be corrected : {}', mfgName, mfgList
				errorMsg = "Manufacturer ($mfgNameParam) must be unique"
				break
			}

			List modelList = []
			if (haveModelName)
				modelList = findModelsByName(modelName)
			int modelListCount = modelList.size()

			List filteredModels
			int filteredCount

			//
			// Case when user has supplied a valid/existing Mfg
			//
			if (mfgList) {
				log.debug '{} We have manufacturer(s) {}', methodName, mfgList.size()
				//
				// Case when we don't have a model
				//
				if (! haveModelName) {
					if (haveDeviceType &&!invalidDeviceType) {
						warningMsg = StringUtil.concat(warningMsg, "Incomplete Mfg/Model/Type therefore it was set to 'Unknown/Unknown - $deviceType'", delim)
						log.debug '{} CASE 212', methodName
						performUnknownAssignment()
					} else {
						if (haveDeviceType && invalidDeviceType) {
							log.debug '{} CASE 214', methodName
							errorMsg = StringUtil.concat(warningMsg, "Device Type ($deviceTypeParam) is invalid", delim)
						} else {
							log.debug '{} CASE 211', methodName
							errorMsg = StringUtil.concat(errorMsg, LACK_INFO_NO_CREATE, delim)
						}
					}
					break
				}

				// Look for the models by Mfg with/without the device type
				filteredModels = modelList.findAll { it.manufacturer.id == mfgList[0].id }
				filteredCount = filteredModels.size()

				//
				// Case when we have no mfg / model matches
				//
				if (filteredCount == 0) {
					log.debug '{} we have ZERO models matching Mfg', methodName
					if (modelListCount==0) {
						// No models found but we have a model name so we can create as long as we have a valid type
						if (haveDeviceType &&! invalidDeviceType) {
							log.debug '{} CASE 232', methodName
							performCreateMfgModel(mfgList[0], modelName, deviceType, usize)
							break
						} else {
							log.debug '{} CASE 231 or 234', methodName
							if (invalidDeviceType) {
								warningMsg = StringUtil.concat(warningMsg, DEVICE_TYPE_INVALID, delim)
							}
							errorMsg = StringUtil.concat(errorMsg, LACK_INFO_NO_CREATE, delim)
							break
						}
					} else {
						// Cases 321,322,323,324 - We have a list of existing models by name
						if (invalidDeviceType) {
							log.debug '{} CASE 324', methodName
							errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_INVALID, delim)
							break
						}
						if (!haveDeviceType) {
							log.debug '{} CASE 321', methodName
							errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_BLANK, delim)
							break
						}

						// Try filtering down the original model list (w/o Mfg filtering) to get a model for the specified device type
						filteredModels = modelList.findAll { it.assetType == deviceType }
						filteredCount = filteredModels.size()

						if (filteredCount == 0) {
							log.debug '{} CASE 323', methodName
							errorMsg = StringUtil.concat(errorMsg, "Device type ($deviceTypeParam) doesn't match any existing model of same name", delim)
						} else if (filteredCount == 1) {
							log.debug '{} CASE 322', methodName
							warningMsg = StringUtil.concat(warningMsg, "Mfg ($mfgNameParam) doesn't match existing model's Mfg ($filteredModels.manufacturer.name)", delim)
							performAssignment(filteredModels[0])
						} else {
							log.debug '{} CASE 322 alternate', methodName
							errorMsg = StringUtil.concat(errorMsg, "Multiple models have same name and device type but don't match specified Mfg ($mfgNameParam)", delim)
						}
						break
					}
				} else if (filteredCount==1) {
					// We found a unique match for existing mfg/model - our favorite case!
					log.debug '{} CASE 232', methodName
					performAssignment(filteredModels[0])
					break

				} else {
					log.debug '{} fell into the mfg/model else section', methodName
					//
					// Case when we found multiple mfg/models matches so we need to narrow down the deviceType to try and get unique
					//
					if (haveDeviceType) {
						if (invalidDeviceType) {
							log.debug '{} CASE 234', methodName
							errorMsg = "Invalid device type ($deviceTypeParam) specified"
							break
						}

						// Try refining the list to include the Type
						filteredModels = filteredModels.findAll { it.assetType == deviceType }
						filteredCount = filteredModels.size()

						if (filteredCount == 0) {
							log.debug '{} CASE 232', methodName
							performCreateMfgModel(mfgList[0], modelName, deviceType, usize)
						} else if (filteredCount == 1) {
							log.debug '{} CASE 211', methodName
							performAssignment(filteredModels[0])
						} else {
							// Non-unique match - bad
							errorMsg = "Mfg/Model/DeviceType combination found $filteredCount matches, which must be unique"
						}
						break
					} else {
						log.debug '{} CASE 231', methodName
						errorMsg = "Mfg/Model/DeviceType combination found $filteredCount matches, which must be unique"
						break
					}
				}

				log.error '{} Reached condition with existing Mfg that was unhandled', methodName
				errorMsg = UNEXPECTED_CONDITION + ' for known Mfg'
				break
			}

			//
			// Case when user has supplied a non-existing Mfg Name
			//
			// TODO : JPM 11/2014 : Should add logic to compare mfg name against model aliases to see if mfg name in the model name
			if (haveMfgName) {
				if (! haveDeviceType) {
					log.debug '{} CASE 411, 421, 431', methodName
					errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_BLANK, delim)
					if (! haveModelName) {
						errorMsg = StringUtil.concat(errorMsg, MODEL_BLANK, delim)
					}
					break
				} else if (invalidDeviceType) {
					log.debug '{} CASE 414, 424, 434', methodName
					errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_INVALID, delim)
					break
				} else if (!haveModelName) {
					if (! invalidDeviceType) {
						log.debug '{} CASE 412', methodName
						performUnknownAssignment()
					} else {
						log.error '{} reached if/else that was unexpected mfgNameParam={}, modelNameParam={}, deviceType=({}/{})',
								methodName, mfgNameParam, modelNameParam, deviceTypeParam, deviceType
						errorMsg = StringUtil.concat(errorMsg, UNEXPECTED_CONDITION + ' without Mfg name', delim)
					}
					break
				}

				// Okay so we have a model name and a legit device type so try filtering the model list on the deviceType
				filteredModels = modelList.findAll { it.assetType == deviceType }
				filteredCount = filteredModels.size()

				if (filteredCount == 0) {
					log.debug '{} CASE 423', methodName
					performCreateMfgModel(mfgName, modelName, deviceType, usize)
				} else if (filteredCount == 1) {
					log.debug '{} CASE 422', methodName
					warningMsg = StringUtil.concat(warningMsg, "Specified Mfg ($mfgNameParam) does not match the existing Model Mfg (${filteredModels[0].manufacturer.name})", delim)
					performAssignment(filteredModels[0])
				} else {
					log.debug '{} CASE 422 multiple matches ({})', methodName, filteredCount
					errorMsg = StringUtil.concat(errorMsg, "Mfg/Model/DeviceType combination found $filteredCount matches, which must be unique", delim)
				}
				break
			}

			//
			// Case when user has NOT supplied the Mfg Name
			//
			if (! haveMfgName) {
				if (!haveDeviceType) {
					if (modelListCount == 1) {
						log.debug '{} CASE 121', methodName
						warningMsg = StringUtil.concat(warningMsg, "No Mfg/Type specified therefore assuming you meant Mfg (${modelList[0].manufacturer.name})/Type (${modelList[0].assetType})", delim)
						performAssignment(modelList[0])
					} else if (modelListCount > 0) {
						log.debug '{} CASE 121 multiple model matches', methodName
						errorMsg = StringUtil.concat(errorMsg, "Multiple models matched by name without a type specified", delim)
					} else {
						log.debug '{} CASE 121, 131', methodName
						errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_BLANK, delim)
					}
					break
				} else if (invalidDeviceType) {
					log.debug '{} CASE 124, 134', methodName
					errorMsg = StringUtil.concat(errorMsg, DEVICE_TYPE_INVALID, delim)
					break
				}

				if (modelListCount==0) {
					// No Models found
					log.debug '{} CASE 132', methodName
					errorMsg = StringUtil.concat(errorMsg, LACK_INFO_NO_CREATE, delim)
					break
				} else {
					// Try to find a match of the models by the deviceType
					filteredModels = modelList.findAll { it.assetType == deviceType }
					filteredCount = filteredModels.size()
					if (filteredCount == 0) {
						// Didn't find any matched after filtering on the device type so go back to the master model list
						if (modelListCount == 1) {
							log.debug '{} CASE 123', methodName
							// We'll assign the model regardless of the deviceType but may warn
							performAssignment(modelList[0])
						} else {
							log.debug '{} CASE 123 multiple model matches', methodName
							errorMsg = StringUtil.concat(errorMsg, "Multiple models matched by name but none had the specified type", delim)
						}
					} else if (filteredCount == 1) {
						log.debug '{} CASE 122', methodName
						warningMsg = StringUtil.concat(warningMsg, "No Mfg specified therefore assuming you meant Mfg (${filteredModels[0].manufacturer.name})", delim)
						performAssignment(filteredModels[0])
					} else {
						log.debug '{} CASE 122 multiple model/type matches', methodName
					}
					break
				}
			}

			errorMsg = UNEXPECTED_CONDITION + ' at the end of conditions'
			log.error '{} {}', methodName, errorMsg
			break
		} // while (true)

		return [errorMsg: errorMsg, warningMsg: warningMsg, mfgWasCreated: mfgWasCreated, modelWasCreated: modelWasCreated, cachable: cachable]
	}

	/**
	 * Used to retrieve a list of all manufacturers and their aliases that have the same name
	 * @param name - the name to lookup
	 * @param The list of models found
	 */
	List findManufacturersByName(String name) {
		List list = Manufacturer.findAllByName(name)
		list.addAll(ManufacturerAlias.findAllByName(name).manufacturer)
		list = list.unique({ a, b -> a.id <=> b.id })
		return list
	}

	/**
	 * Used to retrieve a list of all models and their aliases that have the same name
	 * @param modelName - the name to lookup
	 * @param The list of models found
	 */
	List findModelsByName(String name) {
		List list = Model.findAllByModelName(name)
		list.addAll(ModelAlias.findAllByName(name).model)
		list = list.unique({ a, b -> a.id <=> b.id })
		return list
	}

	// TODO: Move to AssetEntityService
	/**
	 * Method used to find model by manufacturrName as well as create model if modelnot exist and manufacturer exist.
	 * @param manufacturerName : name of manufacturer
	 * @param modelName : name of model
	 * @param type : asset's asset type
	 * @param create : a boolean flag to determine if model don't exist create model or not.
	 * @param usize : usize of model (default 1)
	 * @params dtvList : dataTransferValueList
	 * @return model instance
	 */
	@Transactional
	List findOrCreateModel(UserLogin userLogin, Manufacturer mfg, String modelName, String deviceType, String usize, boolean canCreateMfgAndModel) {
		Model model
		String errorMsg

		try {
			if (mfg) {
				// if modelValue exist using that else using 'unknown' as modelValue
				modelName = modelName ?: 'unknown'
				// if manufacturer searching in model table if found assigning .
				model = Model.findByModelNameAndManufacturer(modelName, mfg)
				if (!model) {
					// if imported value is not in model table then search in model alias table .
					model = ModelAlias.findByNameAndManufacturer(modelName,mfg)?.model
					if (! model) {
						if (canCreateMfgAndModel) {
							if (! deviceType)
								deviceType = 'Server'

							model = Model.createModelByModelName(modelName, mfg, deviceType, NumberUtil.toInteger(usize), userLogin?.person)
						} else {
							errorMsg = "Unable to find model ($modelName) for mfg ($mfg)"
						}
					}
				}
			}
		} catch(Exception e) {
			errorMsg = "Unable to create model $modelName - $e"
			log.error 'Unable to create model {} - {} : {}', modelName, e.message, ExceptionUtil.stackTraceToString(e)
		}
		return [model, errorMsg]
	}

	// TODO: Move to AssetEntityService and change the code to check for existing connectors (see TM-3308)
	/*
	*  Create asset_cabled_Map for all asset model connectors
	*/
	@Transactional
	def createModelConnectors(assetEntity){
		if (assetEntity.model){
			def assetConnectors = ModelConnector.findAllByModel(assetEntity.model)
			assetConnectors.each {
				def assetCableMap = new AssetCableMap(
					cable : "Cable"+it.connector,
					assetFrom: assetEntity,
					assetFromPort : it,
					cableStatus : it.status
				)
				if (assetEntity?.rackTarget && it.type == "Power" && it.label?.toLowerCase() == 'pwr1'){
					assetCableMap.assetTo = assetEntity
					assetCableMap.assetToPort = null
					assetCableMap.toPower = "A"
				}
				save assetCableMap, true
			}
		}
	}

	// TODO: Move to AssetEntityService
	/*
	 *  Create asset_cabled_Map for all asset model connectors
	 */
	@Transactional
	def updateModelConnectors(assetEntity) {
		if (assetEntity.model) {
			// Set to connectors to blank if associated
			AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='$AssetCableStatus.UNKNOWN',assetTo=null,
				assetToPort=null where assetTo = ? """,[assetEntity])
			// Delete AssetCableMap for this asset
			AssetCableMap.executeUpdate("delete from AssetCableMap where assetFrom = ?",[assetEntity])
			// Create new connectors
			def assetConnectors = ModelConnector.findAllByModel(assetEntity.model)
			assetConnectors.each {
				def assetCableMap = new AssetCableMap(
					cable : "Cable"+it.connector,
					assetFrom: assetEntity,
					assetFromPort : it,
					cableStatus : it.status
				)
				if (assetEntity?.rackTarget && it.type == "Power" && it.label?.toLowerCase() == 'pwr1'){
					assetCableMap.assetTo = assetEntity
					assetCableMap.assetToPort = null
					assetCableMap.toPower = "A"
				}
				save assetCableMap
			}
		}
	}

	/**
	 * A helper closure used to set property to null or blank if the import value equals "NULL" and the property supports NULL or is a String.
	 * In the case of being a String, if not blankable, then it sets the field to "NULL"
	 * @param The asset instance that is being updated
	 * @param The name of the property
	 * @param The import value
	 * @param The list of properties that support NULL
	 * @param The list of properties that support blank
	 * @return String message
	 * @usage setToNull().call(assetInstance, property, value)
	 * @references
	 *    - nullFProps
	 *    - blankFProps
	 *
	 */
	String setToNullOrBlank(AssetEntity asset, String propertyName, value, List<String> nullProps, List<String> blankProps) {
		String msg = ''
		if (value == "NULL") {
			log.debug 'setToNullOrBlank() for {} {} presently {}', asset.getClass().name, propertyName, asset[propertyName]
			//If imported "NULL" and field allows blank and null updating value to null
			Class type = GrailsClassUtils.getPropertyType(asset.getClass(), propertyName)
			if (nullProps.contains(propertyName)) {
				asset[propertyName] = null
			} else if (type == String) {
				asset[propertyName] = blankProps.contains(propertyName) ? '' : 'NULL'
			} else {
				log.warn '''setToNullOrBlank() Imported invalid value 'NULL' which is not allowed for {} property.''', propertyName
				msg = "Unable to set $propertyName to NULL"
			}
		}
		return msg
	}

	/**
	 * A helper method used to do the initial lookup of an asset. If the asset was not found then it will
	 * create a new asset and initialize various properties. If the asset was modified since the export and import then it will return null
	 * @param The class to use (e.g. AssetEntity or Application)
	 * @param The asset id to lookup
	 * @return The asset that was looked up or a new one. If the asset exists and was modified since the import then it will return null
	 * @references
	 *   - errorCount
	 *   - errorConflictCount
	 *   - ignoredAssets
	 *   - dataTransferBatch
	 *   - dtvList
	 *   - project
	 */
	def findAndValidateAsset(
		Project project,
		UserLogin userLogin,
		Class clazz,
		Long assetId,
		DataTransferBatch dataTransferBatch,
		List<DataTransferValue> dtvList,
		Integer errorCount,
		Integer errorConflictCount,
		List<String> ignoredAssets,
		Integer rowNum,
		List<Map<String, ?>> fieldSpecs
	) {
		// Try loading the application and make sure it is associated to the current project
		def asset
		def clazzName = clazz.name.tokenize('.')[-1]
		def clazzMap = [AssetEntity: 'Server', Database: 'Database', Application: 'Application', Files:'Files']

		if (assetId) {
			asset = clazz.get(assetId)
			if (asset) {
				log.debug 'findAndValidateAsset() Found {} id ({}) {}', clazzName, asset.id, asset.assetName
				if (asset.project.id == project.id) {
					if (dataTransferBatch?.dataTransferSetId == 1L) {
						// Validate that the AE fields are valid
						def validateResultList = importValidation(dataTransferBatch, asset, dtvList, fieldSpecs)
						if (validateResultList.flag) {
							// The asset has been updated since the last export so we don't want to overwrite any possible changes
							errorCount++
							errorConflictCount += validateResultList.errorConflictCount
							ignoredAssets << "$asset.id $asset.assetName (row $rowNum)".toString()
							log.warn 'findAndValidateAsset() Field validation error for {} (id:{}, assetName:{})', clazzName, asset.id, asset.assetName
							asset = false
						}
					}
				} else {
					// If id is not associated to the project then we'll just ignore it and handle as a new asset
					securityService.reportViolation("import referenced $clazzName asset ($assetId) not associated with project ($project.id)", userLogin.toString())
					asset.clear()
					asset = null
				}
			}
		} else {

			// Look for the id property and see if there is an error on it
			// Map idDtv = dtvList.find { it.fieldName == 'id' }
//println "findAndValidateAsset() $idDtv"
//def x=5/0
			// if (idDtv && idDtv.hasError) {
			if (dtvList[0].hasError) {
				errorCount++
				// ignoredAssets << "${idDtv.errorText} (row $rowNum)".toString()
				ignoredAssets << "${dtvList[0].errorText} (row $rowNum)".toString()
				// log.warn 'findAndValidateAsset() Field validation error for {} (id:{}, assetName:{})', clazzName, asset.id, asset.assetName
				asset = false
			}
		}

		if (asset == null) {
			asset = clazz.newInstance()
			asset.project = project
			asset.owner = project.client
			asset.assetType = clazzMap[clazzName]

			log.debug 'findAndValidateAsset() Created {}', clazzName
		}

		// If there were conflicts above, set the object to null
		asset == false ? null : asset
	}

	/**
	 * Used by the import process to save the assets and update various vars used to track status
	 * @param
	 * @return
	 */
	@Transactional
	List<Integer> saveAssetChanges(AssetEntity asset, List<Long> assetList, int rowNum, int insertCount,
	                               int updateCount, int errorCount, List<String> warnings) {
		String methodName = 'saveAssetChanges()'
		boolean saved
		if (asset.id) {
			if (asset.dirtyPropertyNames) {
				// Check to see if dirty
				log.info '{} Updated asset {} {} - Dirty properties: {}', methodName, asset.id, asset.assetName, asset.dirtyPropertyNames
				save(asset)
				saved = !asset.hasErrors()
				if (saved) {
					updateCount++
					assetList << asset.id
				}
			} else {
				saved = true 	// Mark as saved even though it wasn't changed
			}
		} else {
			// Handle a new asset
			save asset
			saved = !asset.hasErrors()
			if (saved) {
				insertCount++
				assetList << asset.id // Once asset saved to DB it will provide ID for that.
				log.debug '{} saved new asset id:{}, insertCount:{}', methodName, asset.id, insertCount
			}
		}
		if (! saved) {
			log.warn '{} Performing discard for rowNum {}', methodName, rowNum
			warnings << "Asset $asset.assetName, row $rowNum had an error and was not updated. ${GormUtil.errorsAsUL(asset)}"
			asset.discard()
			errorCount++
		}

		log.debug '{} saved={}, asset={}', methodName, saved, asset
		return [insertCount, updateCount, errorCount]
	}

	/**
	 * Used by the asset import process to set a value on an asset property or a default if it wasn't already set
	 * @param Object the asset to update
	 * @param String the name of the property to update
	 * @param Object the value to set on the property
	 * @param Object the default value (optional)
	 */
	def setValueOrDefault(asset, property, value, defValue = null) {
		if ((value?.size() && value!= asset[property]) ||! asset[property]) {
			if (value) {
				asset[property] = value
			} else if (defValue) {
				asset[property] = defValue
			}
		}
	}

	/**
	 * Used by the asset import process to set the various properties that a common across the various asset classes
	 * @param project - the project that is being processed
	 * @param asset - the asset that is being updated
	 * @param dtv - a Map or DataTransferValue object containing the name/value to update
	 * @param rowNum - the row number being processed
	 * @param warnings - List the list of warning messages
	 * @param errorConflictCount - an Integer counter for error conflicts that is updated on errors
	 * @param tzId - the Timezone id of the user
	 * @param dtFormat - the date time format of the current user
	 */
	@Transactional
	void setCommonProperties(
		Project project,
		AssetEntity asset,
		def dtv,
		Integer rowNum,
		List<String> warnings,
		Integer errorConflictCount,
		String tzId,
		String dtFormat,
		Map<String, ?> fieldSpec
	) {
		// def handled = true
		String property = fieldSpec["field"]
		String value = dtv.importValue
		String newVal
		String classSimpleName = asset.getClass().name.tokenize('.')[-1]

		if (value || ['assetName','assetTag'].contains(property)) {
			log.debug 'setCommonProperties() updating {}.{} with [{}] (row {})', classSimpleName, property, value, rowNum
		}

		switch (property) {
			case 'tagAssets':
				// This is for the collection of tags, non implemented
				break
			case 'assetTag':
				// This is a special case when the clazz is AssetEntity as we construct the assetName & assetTag if not presented
				if (classSimpleName == 'AssetEntity') {
					if (! asset[property] &&! value) {
						newVal = projectService.getNewAssetTag(project, asset)
					} else {
						newVal = value ?: null
					}
				} else {
					newVal = value ?: null
				}
				if (newVal)
					asset[property] = newVal
				break
			case 'moveBundle':
				if (!asset.id || dtv.importValue) {
					asset[property] = getDtvMoveBundle(dtv, project)
				}
				break
			case 'maintExpDate':
			case 'retireDate':
				log.debug 'setCommonProperties() Have {} with value "{}"', property, value
				if (value) {
					try {
						asset[property] = TimeUtil.parseDate(dtFormat, value, TimeUtil.FORMAT_DATE)
					} catch (e) {
						warnings << "Invalid date ($value) for $property on row $rowNum" +
							(asset.assetName ? ", asset '$asset.assetName'" : '') +
							', proper format mm/dd/yyyy'
						errorConflictCount++
						log.error('Cannot parse date value for property {} {}', property, e.message, e)
					}
				}
				break
			case "owner":
				// TODO : JPM 10/13 - what the heck is this doing?  - This in the spreadsheet refers to the AppOwner?
				// asset[property] = asset.owner
				break
			case "planStatus":
				setValueOrDefault(asset, property, value, 'Unassigned')
				break
			case 'rateOfChange':
			case 'size':
				Integer ival = NumberUtil.toInteger(value, 0)
				if (asset.size != ival) {
					asset.size = ival
				}
				break
			case 'scale':
				newVal = SizeScale.asEnum(value)
				if (value.size() &&!newVal) {
					// Value wrong
					warnings << "Invalid value ($value) for Scale on row $rowNum, valid values ${SizeScale.getKeys()}"
					errorConflictCount++
				} else if (newVal!= asset[property]) {
					asset[property] = newVal
				}
				break
			case "validation":
				setValueOrDefault(asset, property, value, ValidationType.UNKNOWN)
				break
			case 'version':
			case 'modifiedBy':
			case 'lastUpdated':
				// Do not want to all user to modify these properties
				break
			default:
				if (value.size()) {
					if (fieldSpec["control"] == ControlType.NUMBER.toString()) {
						def correctedPos
						try {
							if (dtv.correctedValue) {
								correctedPos = NumberUtils.toDouble(dtv.correctedValue.trim(), 0).round()
							} else if (dtv.importValue) {
								correctedPos = NumberUtils.toDouble(value, 0).round()
							}
							//correctedPos = dtv.correctedValue
							if (asset[property]!= correctedPos ||! asset.id) {
								asset[property] = correctedPos
							}
						} catch (e) {
							log.error 'setCommonProperties() exception 1 : {}', e.message
							e.printStackTrace()
							warnings << "Unable to update $property with value [$value] on $asset.assetName (row $rowNum)"
							errorConflictCount++
							dtv.hasError = 1
							dtv.errorText = "format error"
							dtv.save()
						}
					} else {
						try {
							asset[property] = dtv.correctedValue ?: dtv.importValue
						} catch (e) {
							log.error e.message, e
							warnings << "Unable to update $property with value [$value] on $asset.assetName (row $rowNum)"
							errorConflictCount++
							dtv.hasError = 1
							dtv.errorText = e.message
							dtv.save()
						}
					}
				}
		}
	}
}
