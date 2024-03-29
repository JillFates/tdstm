import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import com.tdssrc.grails.GormUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import spock.lang.Specification

@Integration
@Rollback
class DeviceServiceIntegrationTests extends Specification{

    private ProjectTestHelper projectHelper
	private AssetTestHelper assetHelper
    private Project project

    def setup() {
        projectHelper = new ProjectTestHelper()
        assetHelper = new AssetTestHelper()
        projectHelper = new ProjectTestHelper()
		project = projectHelper.createProject()
    }


    def "01. Test no TBD racks or rooms are created for virtual devices"() {

        when: 'a Device with assetType VM is created'
            AssetEntity vm = assetHelper.createDevice(project, AssetType.VM.toString())

        then: 'the roomSource, rackSource and sourceRackPosition properties should be null'
            with(vm){
                roomSource == null
                rackSource == null
                sourceRackPosition == null
            }
        and: 'the roomTarget, rackTarget and targetRackPosition properties should be null'
            with(vm){
                roomTarget == null
                rackTarget == null
                targetRackPosition == null
            }

        and: 'the count of racks for the project should be zero (0)'
            Rack.countByProject(project) == 0

        when: 'a Device with assetType Virtual is created'
            AssetEntity virtual = assetHelper.createDevice(project, 'Virtual')

        then: 'the roomSource, rackSource and sourceRackPosition properties should be null'
            with(virtual){
                roomSource == null
                rackSource == null
                sourceRackPosition == null
            }

        and: 'the roomTarget, rackTarget and targetRackPosition properties should be null'
            with(virtual){
                roomTarget == null
                rackTarget == null
                targetRackPosition == null
            }

        and: 'the count of racks for the project should be zero (0)'
            Rack.countByProject(project) == 0

        when: 'a Server with assetType Server is created'
           AssetEntity server = assetHelper.createDevice(project, AssetType.SERVER.toString())
           List racks = GormUtil.findAllByProperties(Rack, [project: project])

        then: 'the roomSource and rackSource should be null'
            with(server){
                server.roomSource == null
                server.rackSource == null
            }

        and: 'the roomTarget and rackTarget properties should be null'
            with(server){
                server.roomTarget == null
                server.rackTarget == null
            }

        and: 'the count of rows for the project should be zero (0)'
            racks.size() == 0
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
