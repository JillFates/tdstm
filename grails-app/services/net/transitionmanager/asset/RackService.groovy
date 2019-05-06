package net.transitionmanager.asset

import net.transitionmanager.asset.AssetCableMap
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.service.ServiceMethods

class RackService implements ServiceMethods {

	/**
	 * Used to find or create a Rack automatically. If the rack name is blank then it will default to 'TBD'
	 * @param project - the project to find/create the rack for
	 * @param room - the room to place the rack in
	 * @param rackName - the name (aka tag) of the rack
	 * @param model - the Model of the rack if null then it defaults to an 'Generic 42U Rack' rack will be added
	 */
	@Transactional
	Rack findOrCreateRack(Room room, String rackName, Model model=null) {
		if (!room?.project) {
			log.error "findOrCreate() called with invalid project or room objects (room: $room)"
			return
		}

		Rack rack
		String query = "from Rack r where r.project=:project and room=:room and tag=:tag"
		def params = [
			project: room.project,
			room: room,
			tag: StringUtil.defaultIfEmpty(rackName, 'TBD')
		]

		rack = Rack.find(query, params)
		if (!rack) {
			// Attempt to create the rack
			if (!model) {
				model = getDefaultRackModel()
			}
			if (model) {
				params.model = model
				params.manufacturer = model.manufacturer
				params.model = getDefaultRackModel()
				// TODO : JPM 9/2014 - source and location should be normalized out of this domain
				params.source = room.source
				params.location = room.location
			}

			// TODO : JPM 9/2014 : Adjust the roomX/Y coord so that racks don't land on top of one another when creating new ones

			rack = new Rack(params)
			if (!rack.save(failOnError: false)) {
				log.error "Unable to create room project:$project, room:$room, rackName:$rackName, $model:$model ${GormUtil.allErrorsString(rack)}"
				room = null
			}
		}

		return rack
	}

	/**
	 * Used to retrieve the default model of a rack which is manufacturer 'Generic' and model name '42U Rack', which will create it if it doesn't exist
	 * @return The model object if found or created, or NULL if there was an error
	 */
	@Transactional
	Model getDefaultRackModel() {
		Model model

		Map names = [
			manuName: 'Generic',
			modelName: '42U Rack'
		]

		model = Model.find("from Model as m inner join fetch m.manufacturer as a where a.name=:manuName and m.modelName=:modelName and m.assetType='Rack'", names)
		if (! model) {
			// Let's attempt to create the model

			// Start by finding/creating the manufacturer
			Manufacturer manu = Manufacturer.findByName(names.manuName)
			if (! manu) {
				manu = new Manufacturer([ name: names.manuName])
				if (! manu.save(flush:true, failOnError: false)) {
					log.error "getDefaultRackModel() failed to create Manufacturer $manuName : ${GormUtil.allErrorsString(manu)}"
					return null
				}
				log.info "getDefaultRackModel() Created manufacturer $manu"
			}

			Map modelProps = [
				manufacturer: manu,
				modelName: names.modelName,
				assetType: 'Rack',
				modelStatus: 'valid',
				usize: 42,
				roomObject: true,
				height: 80,
				width: 24,
				depth: 34
			]
			model = new Model(modelProps)
			if (! model.save(flush:true, failOnError: false)) {
				log.error "getDefaultRackModel() failed to create model $names.manuName/$names.modelName : ${GormUtil.allErrorsString(model)}"
				return null
			}
			log.info "getDefaultRackModel() Created model $model"
		}

		return model
	}

	/**
	 * Returns the list of racks for a given room sorted by tag
	 * @param project - the project that the room is associated with
	 * @param room - the room object
	 * @param isSource - flag indicating to return source racks (true) or target racks (false) default (true)
	 * @return List of racks associated to the room specified
	 */
	List getRacksOfRoom(Project project, Room room, Boolean isSource=true) {
		def racks = Rack.findAllByRoom(room, [sort : "tag"])
		// filter out room objects
		racks = racks.findAll { e -> !e.model?.roomObject }
		return racks
	}

	/**
	 * Overloaded version of the getRacksOfRoom that takes the id of the room object instead of the room object itself
	 * @param project - the project that the room is associated with
	 * @param roomId - the id of the room
	 * @param isSource - flag indicating to return source racks (true) or target racks (false) default (true)
	 * @return List of racks associated to the room specified
	 */
	List getRacksOfRoom(Project project, roomId, Boolean isSource=true) {
		def racks = []
		Long id = NumberUtil.toLong(roomId)
		if (id) {
			Room room = Room.findByIdAndSource(id, isSource)
			if (room) {
				if (room.project == project) {
					racks = getRacksOfRoom(project, room)
				} else {
					securityService.reportViolation("Attempted to access Room/Rack ($roomId) of unassociated project ($project.id)")
				}
			}
		}
		return racks
	}

	/**
	 * This method is used  give power connection to a selected rack.
	 * @param rackId - id of requested rack.
	 * @return -  rack
	 */
	@Transactional
	def assignPowerForRack(Long rackId) {
		Integer portsCabled = 0

		// TODO : JPM 11/2018 : Should be validating rack against the project
		Rack rack = Rack.read(rackId)
		List<String> toPowers = ["A", "B", "C"]

		rack.assets.each { asset ->
			List<AssetCableMap> assetCablePowerList = AssetCableMap.findAllByAssetFrom(asset).findAll { it.assetFromPort?.type == "Power" }

			// Guessing that this is dealing with A/B/C power feeds?
			assetCablePowerList = assetCablePowerList.size() > 3 ? assetCablePowerList[0..2] : assetCablePowerList

			assetCablePowerList.eachWithIndex { assetCablePower, i ->
				if (!assetCablePower.toPower) {
					assetCablePower.assetTo = assetCablePower.assetFrom
					assetCablePower.assetToPort = null
					assetCablePower.toPower = toPowers[i]
					assetCablePower.cableColor = 'Black'
					assetCablePower.cableStatus = 'Cabled'

					if (!assetCablePower.save(flush: true)) {
						assetCablePower.errors.allErrors.each { println it }
					}
					portsCabled++
				}
			}
		}
		log.debug "assignPowerForRack() cabled ${portsCabled} power ports on rack $rack"
		return rack
	}
}
