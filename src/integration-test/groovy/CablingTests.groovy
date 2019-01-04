import com.tdssrc.grails.GormUtil
import grails.test.spock.IntegrationSpec

import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model


class CablingTests extends IntegrationSpec {
    /***********************************
     * Test model create functionality
     **********************************/

	def "Test model create functionality"(){
		when:
			def manufacturer = new Manufacturer(name : "Dell Enterprise Test")
			if(!manufacturer.validate() || !manufacturer.save(flush: true)){
				def etext = "Unable to create manufacturer" +
					GormUtil.allErrorsString( manufacturer )
				log.error etext
			}

			def modelInstance = new Model(
					modelName      : "PowerEdge 1950",
					manufacturer   : manufacturer,
					assetType      : "Server",
					poweruse       : 1200,
					connectorLabel : "PE5",
					type           : "Power",
					connectorPosX  : 250,
					connectorPosY  : 90
			)
			if(!modelInstance.validate() || !modelInstance.save(flush:true)){
				def etext = "Unable to create modelInstance" +
					GormUtil.allErrorsString( modelInstance )
				log.error etext
			}

		then: "modelInstance isCreated"
		modelInstance.validate() && modelInstance.save()
/*
      def etext = "Unable to create modelInstance" +
            GormUtil.allErrorsString( modelInstance )
      println etext
    } else {
      println"Model : ${modelInstance} created"
    }
*/
		when:
			def modelFromDB = Model.findByModelName("PowerEdge 1950")

		then:
			modelInstance.modelName == modelFromDB.modelName
	}


	/***********************************
	 * Test AssetCableMap create functionality
	 **********************************/
	/*
   void testAssetCableMapCreate() {
	   def assetCableMapInstance = new AssetCableMap( cable : "PowerEdge wire",
									   assetFrom : AssetEntity.get(10),
									   assetTo : AssetEntity.get(11),
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
   */
}
