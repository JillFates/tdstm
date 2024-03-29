import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import com.tdsops.etl.DomainClassQueryHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.FindCondition
import com.tdsops.etl.FindOperator
import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import net.transitionmanager.imports.ImportBatchRecord
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelAlias
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.common.FileSystemService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetEntityTestHelper
import test.helper.MoveEventTestHelper
import test.helper.RackTestHelper
import test.helper.RoomTestHelper

@Integration
@Rollback
class DomainClassQueryHelperIntegrationSpec extends Specification{

	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	DataImportService dataImportService

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	MoveEventTestHelper moveEventTestHelper = new MoveEventTestHelper()

	@Shared
	RoomTestHelper roomTestHelper = new RoomTestHelper()

	@Shared
	RackTestHelper rackTestHelper = new RackTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper

	@Shared
	ProviderTestHelper providerTestHelper = new ProviderTestHelper()

	@Shared
	Project project
	@Shared
	Project otherProject
	@Shared
	MoveBundle moveBundle
	@Shared
	AssetEntity device
	@Shared
	AssetEntity device2
	@Shared
	AssetEntity otherProjectDevice
	@Shared
	Map context

	@Shared
	boolean initialized = false


	void setupSpec() {

	}

	void setup() {
		if(!initialized) {
			projectTestHelper = new test.helper.ProjectTestHelper()
			project = projectTestHelper.createProject()
			otherProject = projectTestHelper.createProject()
			moveBundle = moveBundleTestHelper.createBundle(project, null)
			device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject, moveBundleTestHelper.createBundle(otherProject, null))
			context = dataImportService.initContextForProcessBatch(project, ETLDomain.Dependency)
			context.record = new ImportBatchRecord(sourceRowId: 1)

			device.assetType = 'Server'
			device.save()

			device2.assetType = 'Server'
			device2.save()

			// Create a second project with a device with the same name and type as device above
			otherProjectDevice.assetName = device.assetName
			otherProjectDevice.assetType = device.assetType
			otherProjectDevice.save()

			initialized = true
		}
	}

	void '1. can find a Device by its id'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [id: device.id])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '2. can find a Device by roomSource'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [roomSource: device.roomSource.roomName])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '3. can find a Device by roomTarget'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomTarget = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [roomTarget: device.roomTarget.roomName])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '4. can find a Device by locationSource'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [locationSource: device.roomSource.location])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '5. can find a Device by locationTarget'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomTarget = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [locationTarget: device.roomTarget.location])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '6. can find a Room by a by roomName'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [roomName: room.roomName])

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '7. can find a Rack by room'() {

		given:
			Room room = roomTestHelper.createRoom(project)
			Rack rack = rackTestHelper.createRack(project, room)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Rack, project, [room: room.id])

		then:
			results.size() == 1
			results.first() == rack.id
	}


	void '8. can find a Device by assetType'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [assetType: device.assetType])

		then:
			!results.isEmpty()
			results.contains(device.id)
	}

	void '9. can find a Device by assetClass'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [assetClass: AssetClass.DEVICE])

		then:
			!results.isEmpty()
			results.contains(device.id)
	}

	void '10. can find a Device by Bundle name'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [moveBundle: moveBundle.name])

		then:
			!results.isEmpty()
			results.contains(device.id)
	}

	void '11. can find a Device by project'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [project: project.projectCode])

		then:
			!results.isEmpty()
			results.contains(device.id)
	}

	void '12. can find a Device by rackSource'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			Rack rack = rackTestHelper.createRack(project, device.roomSource, null, null, 'Acme Data Center')
			device.rackSource = rack
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [rackSource: rack.tag])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '13. can find a Device by rackTarget'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			Rack rack = rackTestHelper.createRack(project, device.roomSource, null, null, 'Acme Data Center')
			device.rackTarget = rack
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [rackTarget: rack.tag])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '14. can find a Device by its id returning an instance of Device'() {
		given: 'an asset has been created'
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		when: 'calling the DomainClassQueryHelper.where with flag to return domain instead of id'
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [id: device.id], false)
		then: 'one result should be returned'
			results.size() == 1
		and: 'the return value should be an instance of the expected asset domain and id'
			with(results.first()) {
				AssetEntity.isAssignableFrom(it.getClass())
				id == device.id
			}
	}

	void '15. can find a Room by a by locationTarget returning an instance of Room'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [roomName: room.roomName], false)

		then:
			results.size() == 1
			with(results.first()) {
				Room.isAssignableFrom(it.getClass())
				id == room.id
			}
	}

	void '16. can find a Device by Bundle name returning an instance of Device'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [moveBundle: moveBundle.name], false)

		then:
			!results.isEmpty()
			results.contains(device)
	}


	void '17. can find a Device by its id as String value'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [id: device.id.toString()])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '18. can find a Room by a by id'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [id: room.id])

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '19. can find a Room by id with id as String value'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [id: room.id.toString()])

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '20. can throw an exception if domain does not have alternate key'() {

		given:
			MoveBundle bundle = moveBundleTestHelper.createBundle(project, 'TEST')
			MoveEvent event = moveEventTestHelper.createMoveEvent(project)
			bundle.save(flush: true)

		when:
			DomainClassQueryHelper.where(
				ETLDomain.Bundle, project,
				[
					moveEvent: event.name
				]
			)

		then: 'It throws an Exception because MoveEvent does not have an alternate key'
			RuntimeException e = thrown RuntimeException
			e.message == 'MoveEvent does not have alternate key'
	}

	void '21. can find a Device by its id using a FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('id', device.id, 'eq')
				]
			)

		then:
			results.size() == 1
			results.first() == device.id
	}


	void '22. can find a Device by roomSource using a FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('roomSource', device.roomSource.roomName)
				]
			)

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '23. can find a Device by roomTarget using a FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomTarget = roomTestHelper.createRoom(project)
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('roomTarget', device.roomTarget.roomName)
				]
			)

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '24. can find a Device by using ne in a FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('id', device.id, FindOperator.ne)
				]
			)

		then:
			!results.contains(device.id)
	}

	void '25. can find a Room by a roomName using a like FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition('roomName', room.roomName + '%', FindOperator.like)
				]
			)

		then:
			results.size() == 1
			results.first() == room.id
	}

	// TODO: dcorrea Review with John
	void '26. can find a Room by a roomName using a notLike FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition('roomName', room.roomName + '%', FindOperator.notLike)
				]
			)

		then:
			results.size() == 0
	}


	void '27. can find a Room by a roomName using a contains FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition('roomName', room.roomName, FindOperator.contains)
				]
			)

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '28. can find a Room by a roomName using a notContains FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition('roomName', room.roomName, FindOperator.notContains)
				]
			)

		then:
			results.size() == 0
	}

	void '29. can find a Room by a roomName using a inList FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition(
						'roomName',
						[
							room.roomName, 'anotherValue'
						],
						FindOperator.inList
					)
				]
			)

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '30. can find a Room by a roomName using a notInList FindCondition'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room,
				project,
				[
					new FindCondition(
						'roomName',
						[room.roomName, 'anotherValue'],
						FindOperator.notInList
					)
				]
			)

		then:
			results.size() == 0
	}

	void '31. can find a Room by a roomName using a between FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.priority = 6
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition(
						'priority',
						(4..6),
						FindOperator.between
					)
				]
			)

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '32. can find a Room by a roomName using a notBetween FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.priority = 6
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition(
						'priority',
						(4..6),
						FindOperator.notBetween
					)
				]
			)

		then:
			results.size() == 0
	}

	void '33. can find a Room by a roomName using a null FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.department = null
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition(
						'department',
						null,
						FindOperator.isNull
					)
				]
			)

		then:
			results.size() > 0
			results.contains(device.id)
	}

	void '34. can find a Room by a roomName using a is not null FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.department = null
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition(
						'department',
						null,
						FindOperator.isNotNull
					)
				]
			)

		then:
			results.size() == 0
	}

	void '35. can throw an Exception in case of using an invalid FindCondition'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('id', device.id, 'equality')
				]
			)

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.unrecognizedFindCriteria('equality').message
	}

	void '35. can find Model by a modelName and manufacturer id'() {

		given:
			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(flush: true)

			Model model = new Model(
				modelName: 'BladeCenter HS20',
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('modelName', model.modelName),
					new FindCondition('manufacturer', manufacturer.id)
				]
			)
		then:
			results.size() == 1
	}

	void '36. can find Model by a modelName and manufacturer id Integer value'() {

		given:
			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(flush: true)

			Model model = new Model(
				modelName: 'BladeCenter HS20',
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('modelName', model.modelName),
					new FindCondition('manufacturer', manufacturer.id.intValue())
				]
			)

		then:
			results.size() == 1
	}

	void '37. can find Device by a Model id'() {

		given:

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(flush: true)

			Model model = new Model(
				modelName: 'BladeCenter HS20',
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(flush: true)

			device.model = model
			device.manufacturer = manufacturer
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', model.id)
				]
			)

		then:
			results.size() == 1
	}

	void '38. can find Device by a Model id'() {

		given:

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(flush: true)

			Model model = new Model(
				modelName: 'BladeCenter HS20',
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(flush: true)

			device.model = model
			device.manufacturer = manufacturer
			device.save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', model.modelName)
				]
			)

		then:
			results.size() == 1
	}

	void '39. can skip query if Conditions contains null values'() {

		given: 'a list of conditions with null values'
			List<FindCondition> conditions = [new FindCondition('model', null)]

		when: 'where method is evaluated'
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, conditions)

		then: 'results are an empty list'
			results.size() == 0
	}

	void '40. can find a Device by manufacturer id, manufacturer name or manufacturer alias'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.manufacturer = manufacturer
			device.save(flush: true)
			List results

		when: "find Device by 'manufacturer' eq 1122l into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('manufacturer', manufacturer.id)
				]
			)

		then: """select D.id
                  from AssetEntity D
                  left outer join D.manufacturer
                 where D.project = :project
                   and D.assetClass = :assetClass
                   and D.manufacturer.id = :manufacturer_id
			"""
			results.size() == 1

		when: "find Device by 'manufacturer' eq 'HP custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('manufacturer', manufacturerName)
				],
				false
			)

		then: """select D
		 	       from AssetEntity D
		 	       left outer join D.manufacturer
		 		  where D.project = :project
		     		and D.assetClass = :assetClass
		     		and ( D.manufacturer.name = :manufacturer_name
		      		  		or D.manufacturer in (
		      					select MFG_ALIAS.manufacturer
		      			  		  from ManufacturerAlias MFG_ALIAS
		      			 		 where MFG_ALIAS.name = :manufacturer_name )
		    )"""
			results.size() == 1
			results[0].id == device.id
			results[0].manufacturer.id == manufacturer.id
			results[0].manufacturer.name == manufacturer.name

		when: "find Device by 'manufacturer' eq 'Hewlett Packard custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('manufacturer', manufacturerAliasName)
				],
				false
			)

		then: """select D
		 	       from AssetEntity D
		 	       left outer join D.manufacturer
		 		  where D.project = :project
		     		and D.assetClass = :assetClass
		     		and ( D.manufacturer.name = :manufacturer_name
		      		  		or D.manufacturer in (
		      					select MFG_ALIAS.manufacturer
		      			  		  from ManufacturerAlias MFG_ALIAS
		      			 		 where MFG_ALIAS.name = :manufacturer_name )
		    )"""
			results.size() == 1
			results[0].id == device.id
			results[0].manufacturer.id == manufacturer.id
			results[0].manufacturer.name == manufacturer.name

		when: "find Device by 'manufacturer' eq 'ProLiant BL460c G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('manufacturer', modelName)
				]
			)

		then: "There is not results with manufacturer name or alias as 'ProLiant BL460c G1 custom'"
			results.isEmpty()
	}

	void '41. can find a Device by model id, model name or model alias'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.model = model
			device.save(flush: true)
			List results

		when: "find Device by 'model' eq model.id into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', model.id)
				],
				false
			)

		then: """select D
				   from AssetEntity D
		   		   left outer join D.model
				  where D.project = :project
				    and D.assetClass = :assetClass
		  			and D.model.id = :model_id
		"""
			results.size() == 1
			results[0].id == device.id
			results[0].model.id == model.id
			results[0].model.modelName == model.modelName

		when: "find Device by 'model' eq 'ProLiant BL460c G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', modelName)
				],
				false
			)

		then: """select D
                   from AssetEntity D
                   left outer join D.model
                  where D.project = :project
                    and D.assetClass = :assetClass
                    and ( D.model.modelName = :model_modelName	
                    	or
                    	  D.model in (
                    	  	select MDL_ALIAS.model
                    	  	  from ModelAlias MDL_ALIAS
                    	  	 where MDL_ALIAS.name = :model_modelName
                    	  )
                  )
			"""
			results.size() == 1
			results[0].id == device.id
			results[0].model.id == model.id
			results[0].model.modelName == model.modelName

		when: "find Device by 'model' eq 'BL460C G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', modelAliasName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].model.id == model.id
			results[0].model.modelName == model.modelName

		when: "find Device by 'model' eq 'HP custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', manufacturerName)
				]
			)

		then: "There is not results with model name or alias as 'HP custom'"
			results.isEmpty()
	}

	void '42. can find a Manufacturer by manufacturer name or manufacturer alias'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)

		when: "find Manufacturer by 'name' eq 'HP custom' into 'id'"
			List results = DomainClassQueryHelper.where(ETLDomain.Manufacturer,
				project,
				[
					new FindCondition('name', manufacturerName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == manufacturer.id
			results[0].name == manufacturer.name

		when: "find Manufacturer by 'name' eq 'Hewlett Packard custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Manufacturer,
				project,
				[
					new FindCondition('name', manufacturerAliasName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == manufacturer.id
			results[0].name == manufacturer.name

		when: "find Manufacturer by 'name' eq 'ProLiant BL460c G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Manufacturer,
				project,
				[
					new FindCondition('name', modelName)
				]
			)

		then: "There is not results with Manufacturer name or alias as 'ProLiant BL460c G1 custom'"
			results.isEmpty()
	}

	void '43. can find a Model by model name or model alias'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)
			List results = []

		when: "find Model by 'modelName' eq 'ProLiant BL460c G1' custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('modelName', modelName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == model.id
			results[0].modelName == modelName

		when: "find Model by 'modelName' eq 'BL460C G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('modelName', modelAliasName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == model.id
			results[0].modelName == modelName
	}

	void '44. can find a Model by manufacturer by model name or alias'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)

			List results

		when: "find Model by manufacturer eq 'HP custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('manufacturer', manufacturerName)
				],
				false
			)
		then:
			results.size() == 1
			results[0].id == model.id
			results[0].modelName == modelName

		when: "find Model by manufacturer eq 'Hewlett Packard custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('manufacturer', manufacturerAliasName)
				],
				false
			)
		then:
			results.size() == 1
			results[0].id == model.id
			results[0].modelName == modelName

		when: "find Model by manufacturer eq 'ProLiant BL460c G1 custom' into 'id'"
			results = DomainClassQueryHelper.where(ETLDomain.Model,
				project,
				[
					new FindCondition('manufacturer', modelName)
				]
			)
		then:
			results.isEmpty()
	}

	void '45. can find a Device by manufacturer and model names or aliases'() {

		given:
			String manufacturerAliasName = 'Hewlett Packard custom'
			String manufacturerName = 'HP custom'
			String modelAliasName = 'BL460C G1 custom'
			String modelName = 'ProLiant BL460c G1 custom'

			Manufacturer manufacturer = initializeManufacturer(manufacturerName, manufacturerAliasName)
			Model model = initializeModel(modelName, modelAliasName, manufacturer)

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.model = model
			device.manufacturer = manufacturer
			device.save(flush: true)

			List results

		when: """find Device by 'model' eq 'ProLiant BL460c G1 custom'\\
					        and 'manufacturer' eq 'HP custom'\\
					       into 'id'
		"""
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', modelName),
					new FindCondition('manufacturer', manufacturerName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == device.id
			results[0].model.id == model.id
			results[0].model.modelName == modelName
			results[0].manufacturer.id == manufacturer.id
			results[0].manufacturer.name == manufacturerName

		when: """find Device by 'model' eq 'BL460C G1 custom'\\
					        and 'manufacturer' eq 'Hewlett Packard custom'\\
					       into 'id'
		"""
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', modelAliasName),
					new FindCondition('manufacturer', manufacturerAliasName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == device.id
			results[0].model.id == model.id
			results[0].model.modelName == modelName
			results[0].manufacturer.id == manufacturer.id
			results[0].manufacturer.name == manufacturerName

		when: """find Device by 'model' eq 'ProLiant BL460c G1 custom'\\
					        and 'manufacturer' eq 'Hewlett Packard custom'\\
					       into 'id'
		"""
			results = DomainClassQueryHelper.where(ETLDomain.Device,
				project,
				[
					new FindCondition('model', modelName),
					new FindCondition('manufacturer', manufacturerAliasName)
				],
				false
			)

		then:
			results.size() == 1
			results[0].id == device.id
			results[0].model.id == model.id
			results[0].model.modelName == modelName
			results[0].manufacturer.id == manufacturer.id
			results[0].manufacturer.name == manufacturerName
	}

	void '46. can find Dependency by asset using asset.id'() {
		given:
			AssetEntity asset = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			AssetEntity dependent = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

			AssetDependency dependency = new AssetDependency(
				asset: asset,
				dependent: dependent,
				status: AssetDependencyStatus.VALIDATED
			).save(flush: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Dependency,
				project,
				[
					new FindCondition('asset', asset.id, FindOperator.eq)
				]
			)

		then:
			results.size() > 0
			results.contains(dependency.id)
	}

	/**
	 * Creates a Manufacturer and an alias in case that it doesn't exist for testing
	 * @param manufacturerName
	 * @param manufacturerAliasName
	 * @return
	 */
	private Manufacturer initializeManufacturer(String manufacturerName, String manufacturerAliasName) {
		ManufacturerAlias manufacturerAlias = ManufacturerAlias.findByName(manufacturerAliasName)

		Manufacturer manufacturer
		if (manufacturerAlias) {
			manufacturer = manufacturerAlias.manufacturer
		} else {
			manufacturer = Manufacturer.findOrSaveWhere(name: manufacturerName)
			new ManufacturerAlias(
				name: manufacturerAliasName, manufacturer: manufacturer
			).save(flush: true)
		}

		return manufacturer
	}

	/**
	 * Create a Model and alias linked to a Manufacturer in case that it doesn't exist for testing
	 * @param modelName
	 * @param modelAliasName
	 * @param manufacturer
	 * @return
	 */
	private Model initializeModel(String modelName, String modelAliasName, Manufacturer manufacturer) {
		ModelAlias modelAlias = ModelAlias.findByName(modelAliasName)

		Model model
		if (modelAlias) {
			model = modelAlias.model
		} else {
			model = Model.findOrSaveWhere(modelName: modelName, manufacturer: manufacturer)
			new ModelAlias(
				name: modelAliasName, model: model, manufacturer: manufacturer
			).save(flush: true)
		}

		return model
	}

}
