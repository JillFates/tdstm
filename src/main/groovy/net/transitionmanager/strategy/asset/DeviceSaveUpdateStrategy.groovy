package net.transitionmanager.strategy.asset

import net.transitionmanager.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.util.Holders
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.project.ProjectService

class DeviceSaveUpdateStrategy extends AssetSaveUpdateStrategy{


	protected static ProjectService projectService = Holders.grailsApplication.mainContext.getBean('projectService')

	/**
	 * Constructor that takes a command as argument.
	 * @param assetCommand
	 */
	DeviceSaveUpdateStrategy(AssetCommand assetCommand) {
		super(assetCommand)
	}

	/**
	 * Create a new Device
	 * @return
	 */
	@Override
	AssetEntity createAssetInstance() {
		return new AssetEntity()
	}

	@Override
	protected void formatCommandFields() {
		super.formatCommandFields()
		// Parse Integer fields.
		AssetEntityService.ASSET_INTEGER_PROPERTIES.each { p ->
			if (command.asset.containsKey(p)) {
				command.asset[p] = NumberUtil.toInteger(command.asset[p])
			}
		}
	}

	/**
	 * Populate the fields of a device with the values from the command object.
	 * @param device
	 */
	@Override
	protected void populateAsset(AssetEntity device) {
		// Assign all of the standard properties to the device class
		(AssetEntityService.ASSET_PROPERTIES + AssetEntityService.CUSTOM_PROPERTIES + AssetEntityService.DEVICE_PROPERTIES).each { p ->
			if (command.asset.containsKey(p)) {
				if (AssetEntityService.ASSET_DATE_PROPERTIES.contains(p)) {
					if (command.asset[p] instanceof String) {
						if (StringUtil.isBlank(command.asset[p])) {
							device[p] = null
						} else {
							device[p] = TimeUtil.parseDate(command.asset[p])
						}
					} else {
						device[p] = command.asset[p]
					}
				} else {
					device[p] = command.asset[p]
				}
			}
		}

		if (!device.assetTag?.size()) {
			device.assetTag = projectService.getNextAssetTag(project)
		}


		// Update the Manufacturer and/or Model for the asset based on what the user has selected. If the model was selected
		// then we'll use that for everything otherwise we will set the manufacturer from the form.

		if (command.asset.modelId) {
			// Check to see if the user gave us a valid model and then assign to the asset
			def model = GormUtil.findInProject(project, Model, command.asset.modelId)
			device.model = model
			device.manufacturer = model.manufacturer
			device.assetType = model.assetType
		} else {
			device.model = null
			if (command.asset.manufacturerId) {
				// Attempt to assign just the manufacturer and asset type to the device
				def manu = GormUtil.findInProject(project, Manufacturer, command.asset.manufacturerId)
				device.manufacturer = manu
				// assetType was applied in the bind above
			} else {
				device.manufacturer = null
			}

			device.assetType = command.asset.currentAssetType ?: null
		}

		// Set the source/target location/room which creates the Room if necessary
		assetEntityService.assignAssetToRoom(project, device, command.asset.roomSourceId, command.asset.locationSource, command.asset.roomSource, true)
		assetEntityService.assignAssetToRoom(project, device, command.asset.roomTargetId, command.asset.locationTarget, command.asset.roomTarget, false)

		assetEntityService.assignDeviceToChassisOrRack(project, device, command.asset)

        if (!device.moveBundle) {
            assetEntityService.assignAssetToBundle(project, device, command.asset['moveBundleId'].toString())
        }
	}
}
