package net.transitionmanager

import com.tds.asset.AssetEntity
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.asset.DeviceUtils
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import test.helper.MoveBundleTestHelper

@Integration
@Rollback
class DeviceUtilsIntegrationSpec extends Specification{

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
	@Shared
	MoveBundleTestHelper bundleHelper = new MoveBundleTestHelper()

	@Shared
	Project project
	@Shared
	MoveBundle moveBundle

	def setup() {
		project = projectTestHelper.createProject()
		bundleHelper.createBundle(project)

		Room room1 = new Room(project: project, location: 'Location 1', roomName: 'Room 1', source: 1).save(flush: true, failOnError: true)
		Room room2 = new Room(project: project, location: 'Location 2', roomName: 'Room 2', source: 0).save(flush: true, failOnError: true)

		Manufacturer manufacturer1 = new Manufacturer(project: project, name: RandomStringUtils.randomAlphabetic(10)).save(flush: true, failOnError: true)
		Manufacturer manufacturer2 = new Manufacturer(project: project, name: RandomStringUtils.randomAlphabetic(10)).save(flush: true, failOnError: true)

		Model model1 = new Model(project: project, manufacturer: manufacturer1, assetType: 'Rack', modelName: 'Model 1').save(flush: true, failOnError: true)
		Model model2 = new Model(project: project, manufacturer: manufacturer2, assetType: 'Rack', modelName: 'Model 2').save(flush: true, failOnError: true)
		Model model3 = new Model(project: project, manufacturer: manufacturer1, assetType: 'Chassis', modelName: 'Model 3').save(flush: true, failOnError: true)
		Model model4 = new Model(project: project, manufacturer: manufacturer2, assetType: 'Blade Chassis', modelName: 'Model 4').save(flush: true, failOnError: true)

		Rack rack1 = new Rack(project: project, room: room1, model: model1, location: 'Location 1', source: 1, tag: 'Rack 1').save(flush: true, failOnError: true)
		Rack rack2 = new Rack(project: project, room: room2, model: model2, location: 'Location 2', source: 0, tag: 'Rack 2').save(flush: true, failOnError: true)

		AssetEntity assetEntity1 = new AssetEntity(project: project, moveBundle: moveBundle, roomSource: room1, roomTarget: room1, model: model3, assetName: 'Asset 1', assetTag: 'AT1', assetType: 'Chassis').save(flush: true, failOnError: true)
		AssetEntity assetEntity2 = new AssetEntity(project: project, moveBundle: moveBundle, roomSource: room2, roomTarget: room2, model: model4, assetName: 'Asset 2', assetTag: 'AT2', assetType: 'Blade Chassis').save(flush: true, failOnError: true)

		room1.addToRacks(rack1).save(flush: true, failOnError: true)
		room2.addToRacks(rack2).save(flush: true, failOnError: true)
		room1.addToSourceAssets(assetEntity1).save(flush: true, failOnError: true)
		room2.addToTargetAssets(assetEntity2).save(flush: true, failOnError: true)
	}

	@See('TM-13021')
	void '01. Test get asset rail type options return expected list of rail types'() {
		when: 'getting asset rail types'
			List<String> railTypesOptions = DeviceUtils.getAssetRailTypeOptions()
		then: 'the list is returned and it is exactly the same as AssetEntity RAIL_TYPES'
			railTypesOptions == AssetEntity.RAIL_TYPES
	}

	@See('TM-13021')
	void '02. Test get room select options'() {
		when: 'getting source room list'
			List sourceRooms = DeviceUtils.getRoomSelectOptions(project, true, false)
		then: 'source room select list is returned with expected values'
			with(sourceRooms) {
				size() == 1
				with(get(0)) {
					id
					value == 'Location 1 / Room 1'
				}
			}
		when: 'getting target room list'
			List targetRooms = DeviceUtils.getRoomSelectOptions(project, false, false)
		then: 'target room select list is returned with expected values'
			with(targetRooms) {
				size() == 1
				with(get(0)) {
					id
					value == 'Location 2 / Room 2'
				}
			}
	}

	@See('TM-13021')
	void '03. Test get rack select options'() {
		setup: 'preparing list of test rooms'
			Project thatProject = project
			List<Room> rooms = Room.where { project == thatProject }.list()
		when: 'getting source rack list'
			List sourceRacks = DeviceUtils.getRackSelectOptions(project, rooms[0].id, false)
		then: 'source rack select list is returned with expected values'
			with(sourceRacks) {
				size() == 1
				with(get(0)) {
					id
					value == 'Rack 1'
				}
			}
		when: 'getting target rack list'
			List targetRacks = DeviceUtils.getRackSelectOptions(project, rooms[1].id, false)
		then: 'target rack select list is returned with expected values'
			with(targetRacks) {
				size() == 1
				with(get(0)) {
					id
					value == 'Rack 2'
				}
			}
	}

	@See('TM-13021')
	void '04. Test get chassis select options'() {
		setup: 'preparing list of test rooms'
			Project thatProject = project
			List<Room> rooms = Room.where { project == thatProject }.list()
		when: 'getting source chassis list'
			List sourceRacks = DeviceUtils.getChassisSelectOptions(project, rooms[0].id)
		then: 'source chassis select list is returned with expected values'
			with(sourceRacks) {
				size() == 1
				with(get(0)) {
					id
					value == 'Asset 1/AT1'
				}
			}
		when: 'getting target chassis list'
			List targetRacks = DeviceUtils.getChassisSelectOptions(project, rooms[1].id)
		then: 'target chassis select list is returned with expected values'
			with(targetRacks) {
				size() == 1
				with(get(0)) {
					id
					value == 'Asset 2/AT2'
				}
			}

	}

	@See('TM-13021')
	void '05. Test get all device model options'() {
		setup: 'preparing list of test rooms'
			Project thatProject = project
			List<AssetEntity> assetEntities = AssetEntity.where { project == thatProject }.list()
		when: 'retrieving source device select options'
			Map sourceSelectOptions = DeviceUtils.deviceModelOptions(project, assetEntities[0])
		then: 'source select device options are returned as expected'
			with(sourceSelectOptions) {
				get('railTypeOption') != null
				get('sourceRoomSelect') != null
				get('sourceRackSelect') != null
				get('sourceChassisSelect') != null

				with(get('railTypeOption')) {
					size() == 6
					it == AssetEntity.RAIL_TYPES
				}

				with(get('sourceRoomSelect')) {
					size() == 2
					with(get(0)) {
						id == -1
						value == 'Add Room...'
					}
					with(get(1)) {
						id
						value == 'Location 1 / Room 1'
					}
				}

				with(get('sourceRackSelect')) {
					size() == 2
					with(get(0)) {
						id == -1
						value == 'Add Rack...'
					}
					with(get(1)) {
						id
						value == 'Rack 1'
					}
				}

				with(get('sourceChassisSelect')) {
					size() == 1
					with(get(0)) {
						id
						value == 'Asset 1/AT1'
					}
				}

			}
		when: 'retrieving target device select options'
			Map targetSelectOptions = DeviceUtils.deviceModelOptions(project, assetEntities[1])
		then: 'target select device options are returned as expected'
			with(targetSelectOptions) {
				get('railTypeOption') != null
				get('targetRoomSelect') != null
				get('targetRackSelect') != null
				get('targetChassisSelect') != null

				with(get('railTypeOption')) {
					size() == 6
					it == AssetEntity.RAIL_TYPES
				}

				with(get('targetRoomSelect')) {
					size() == 2
					with(get(0)) {
						id == -1
						value == 'Add Room...'
					}
					with(get(1)) {
						id
						value == 'Location 2 / Room 2'
					}
				}

				with(get('targetRackSelect')) {
					size() == 2
					with(get(0)) {
						id == -1
						value == 'Add Rack...'
					}
					with(get(1)) {
						id
						value == 'Rack 2'
					}
				}

				with(get('targetChassisSelect')) {
					size() == 1
					with(get(0)) {
						id
						value == 'Asset 2/AT2'
					}
				}
			}
	}
}
