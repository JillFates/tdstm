import spock.lang.*
import spock.lang.Specification

import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdssrc.grails.GormUtil

class DeviceServiceIntegrationTests extends Specification {

    private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private AssetTestHelper assetHelper = new AssetTestHelper()
    private Project project

    def setup() {
        projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()
    }


    def "01. Test no TBD racks or rooms are created for virtual devices"() {

        // Creating a vm, which shouldn't have any room/rack associated.
        when: 'creating a VM with assetType VM is created'
            AssetEntity vm = assetHelper.createDevice(project, AssetType.VM.toString())
        then: 'the roomSource, rackSource and sourceRackPosition properties should be null'
            vm
            vm.roomSource == null
            vm.rackSource == null
            vm.sourceRackPosition == null

        and: 'the roomTarget, rackTarget and targetRackPosition properties should be null'
            vm.roomTarget == null
            vm.rackTarget == null
            vm.targetRackPosition == null
        and: 'the count of rooms for the project should be zero (0)'
            Room.countByProject(project) == 0
        and: 'the count of racks for the project should be zero (0)'
            Rack.countByProject(project) == 0

        // Creating a virtual device, which shouldn't have any room/rack associated.
        when: 'creating a VM with assetType Virtual is created'
            AssetEntity virtual = assetHelper.createDevice(project, 'Virtual')
            then: 'the roomSource, rackSource and sourceRackPosition properties should be null'
                virtual
                virtual.roomSource == null
                virtual.rackSource == null
                virtual.sourceRackPosition == null

            and: 'the roomTarget, rackTarget and targetRackPosition properties should be null'
                virtual.roomTarget == null
                virtual.rackTarget == null
                virtual.targetRackPosition == null
            and: 'the count of rooms for the project should be zero (0)'
                Room.countByProject(project) == 0
            and: 'the count of racks for the project should be zero (0)'
                Rack.countByProject(project) == 0

        // Creating a simple server.
        when: 'creating a Server with assetType Server is created'
            AssetEntity server = assetHelper.createDevice(project, AssetType.SERVER.toString())
            List rooms = GormUtil.findAllByProperties(Room, [project: project])
            List racks = GormUtil.findAllByProperties(Rack, [project: project])
        then: 'the roomSource and rackSource should not be null'
            server
            server.roomSource != null
            server.rackSource != null
        and: 'the roomTarget and rackTarget properties should not be null'
            server.roomTarget != null
            server.rackTarget != null
        and: 'the count of rows for the project should be two (2)'
            racks.size() == 2
        and: 'both rack objects tag property should be TBD'
            racks.each{
                it.tag == "TBD"
            }
        and: 'the count of rooms for the project should be two (2)'
            rooms.size() == 2
        and: 'both room objects roomName property should be TBD'
            rooms.each{
                it.roomName == "TBD"
            }
    }

}
