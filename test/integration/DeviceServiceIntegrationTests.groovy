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

        when: 'creating a VM with assetType VM is created'
            AssetEntity vm = assetHelper.createDevice(project, AssetType.VM.toString())

        then: 'the roomSource should not be null, rackSource and sourceRackPosition properties should be null'
            with(vm){
                roomSource != null
                rackSource == null
                sourceRackPosition == null
            }

        and: 'the roomTarget should not be null, rackTarget and targetRackPosition properties should be null'
            with(vm){
                roomTarget != null
                rackTarget == null
                targetRackPosition == null
            }

        and: 'the count of rooms for the project should be two and named "TBD"'
            isRoomInfoOkay(project)

        and: 'the count of racks for the project should be zero (0)'
            Rack.countByProject(project) == 0

        when: 'creating a VM with assetType Virtual is created'
            AssetEntity virtual = assetHelper.createDevice(project, 'Virtual')

        then: 'the roomSource should not be null, rackSource and sourceRackPosition properties should be null'
            with(virtual){
                roomSource != null
                rackSource == null
                sourceRackPosition == null
            }

        and: 'the roomTarget should not be null, rackTarget and targetRackPosition properties should be null'
            with(virtual){
                roomTarget != null
                rackTarget == null
                targetRackPosition == null
            }

        and: 'the count of rooms for the project should be two and named "TBD"'
            isRoomInfoOkay(project)

        and: 'the count of racks for the project should be zero (0)'
            Rack.countByProject(project) == 0

        when: 'creating a Server with assetType Server is created'
           AssetEntity server = assetHelper.createDevice(project, AssetType.SERVER.toString())
           List racks = GormUtil.findAllByProperties(Rack, [project: project])

        then: 'the roomSource and rackSource should not be null'
            with(server){
                server.roomSource != null
                server.rackSource != null
            }

        and: 'the roomTarget and rackTarget properties should not be null'
            with(server){
                server.roomTarget != null
                server.rackTarget != null
            }

        and: 'the count of rows for the project should be two (2)'
            racks.size() == 2

        and: 'both rack objects tag property should be TBD'
            racks.each{
                it.tag == "TBD"
            }

        and: 'the count of rooms for the project should be two (2) and named TBD'
            isRoomInfoOkay(project)
    }

    /**
     * Checks that two, and only two, rooms are associated with the project
     * and that both are named 'TBD'.
     */
    boolean isRoomInfoOkay(Project project){
        List rooms = GormUtil.findAllByProperties(Room, [project: project])
        boolean isOk = rooms.size() == 2
        rooms.each{
            if (it.roomName != "TBD") {
                isOk = false
            }
        }
        return isOk
    }

}
