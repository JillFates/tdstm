import com.tds.asset.AssetEntity
import com.tdsops.etl.DomainClassQueryHelper
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.ImportBatchRecord
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.DataImportService
import net.transitionmanager.service.FileSystemService
import spock.lang.Shared
import test.helper.AssetEntityTestHelper
import test.helper.RackTestHelper
import test.helper.RoomTestHelper

class DomainClassQueryHelperIntegrationSpec extends IntegrationSpec {
	@Shared
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()

	@Shared
	DataImportService dataImportService

	@Shared
	FileSystemService fileSystemService

	@Shared
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()

	@Shared
	PersonTestHelper personTestHelper = new PersonTestHelper()

	@Shared
	RoomTestHelper roomTestHelper = new RoomTestHelper()

	@Shared
	RackTestHelper rackTestHelper = new RackTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	ProviderTestHelper providerTestHelper = new ProviderTestHelper()

	@Shared
	Project project = projectTestHelper.createProject()
	@Shared
	Project otherProject = projectTestHelper.createProject()
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


	void setupSpec() {

	}

	void setup() {
		// project = projectTestHelper.createProject(null)
		// otherProject = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		device2 = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		otherProjectDevice = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, otherProject,
			moveBundleTestHelper.createBundle(otherProject, null))
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


	void '2. can find a Device by sourceRoom'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomSource = roomTestHelper.createRoom(project)
			device.save(failOnError: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [roomSource: device.roomSource.roomName])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '3. can find a Device by targetRoom'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomTarget = roomTestHelper.createRoom(project)
			device.save(failOnError: true)

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
			device.save(failOnError: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [roomSource: device.roomSource.location])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '5. can find a Device by locationTarget'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.roomTarget = roomTestHelper.createRoom(project)
			device.save(failOnError: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [roomSource: device.roomTarget.location])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '6. can find a Room by a by locationTarget'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [roomName: room.roomName])

		then:
			results.size() == 1
			results.first() == room.id
	}

	void '7. can find a Device by locationTarget'() {

		given:
			Room room = roomTestHelper.createRoom(project)
			Rack rack = rackTestHelper.createRack(project, room)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Rack, project, [room: room])

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
			device.save(failOnError: true)

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
			device.save(failOnError: true)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [rackTarget: rack.tag])

		then:
			results.size() == 1
			results.first() == device.id
	}

	void '14. can find a Device by its id and return an instance of Device'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [id: device.id], false)

		then:
			results.size() == 1
			with(results.first()){
				AssetEntity.isAssignableFrom(it.getClass())
				id == device.id
			}
	}


	void '15. can find a Room by a by locationTarget and return an instance of Room'() {

		given:
			Room room = roomTestHelper.createRoom(project)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Room, project, [roomName: room.roomName], false)

		then:
			results.size() == 1
			with(results.first()){
				Room.isAssignableFrom(it.getClass())
				id == room.id
			}
	}


	void '16. can find a Device by Bundle name and return an instance of Device'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [moveBundle: moveBundle.name], false)

		then:
			!results.isEmpty()
			results.contains(device)
	}

//	void '11. can find a Device by manufacturer'() {
//
//		given:
//			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
//
//		when:
//			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [assetClass: AssetClass.DEVICE])
//
//		then:
//			results.size() == 1
//			results.first().id == device.id
//	}

//	void '12. can find a Device by a model instance'() {
//
//		given:
//			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
//
//		when:
//			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [assetClass: AssetClass.DEVICE])
//
//		then:
//			results.size() == 1
//			results.first().id == device.id
//	}

	/*
		More tests:
		def manufacturer = Manufacturer.get(336)
		def results = DomainClassQueryHelper.where(ETLDomain.Device, Project.get(5810), [manufacturer: manufacturer.name])

		def model = Model.get(6941)
		def results = DomainClassQueryHelper.where(ETLDomain.Device, Project.get(5810), [model: model.modelName])

		def moveBundle = MoveBundle.get(5926)
		def results = DomainClassQueryHelper.where(ETLDomain.Device, Project.get(5810), [moveBundle: moveBundle.name])

	 */


}
