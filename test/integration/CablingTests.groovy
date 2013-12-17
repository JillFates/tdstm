
import grails.test.*

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil

class CablingTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
    /***********************************
     * Test model create functionality
     **********************************/
    void testModelCreate() {
    	def manufacturer = new Manufacturer( name : "Dell" ).save()
    	def modelInstance = new Model( modelName : "PowerEdge 1950", 
    									manufacturer : manufacturer, 
										assetType : "Server",
										poweruse : 1200,
										connectorLabel : "PE5",
										type : "Power",
										connectorPosX : 250,
										connectorPosY : 90)
    	if ( !modelInstance.validate() || !modelInstance.save() ) {
			def etext = "Unable to create modelInstance" +
            GormUtil.allErrorsString( modelInstance )
			println etext
		} else {
			println"Model : ${modelInstance} created"
		}
    	def modelFromDB = Model.findByModelName("PowerEdge 1950")
		
		assertEquals modelInstance.modelName, modelFromDB.modelName
    }
    /***********************************
     * Test AssetCableMap create functionality
     **********************************/
    void testAssetCableMapCreate() {
    	def assetCableMapInstance = new AssetCableMap( cable : "PowerEdge wire", 
						    			assetFrom : AssetEntity.findById(10), 
						    			assetTo : AssetEntity.findById(11),
						    			assetFromPort : 52,
						    			assetToPort : 25,
										state : 1 
										)
    	if ( !assetCableMapInstance.validate() || !assetCableMapInstance.save() ) {
			def etext = "Unable to create assetCableMapInstance" +
            GormUtil.allErrorsString( assetCableMapInstance )
			println etext
		} else {
			println"AssetCableMap : ${assetCableMapInstance} created"
		}
    	def assetCableMapFromDB = AssetCableMap.findByCable("PowerEdge wire")
		
		assertEquals assetCableMapInstance.cable, assetCableMapFromDB?.cable
    }
}
