import com.tds.asset.AssetEntity
import com.tdssrc.grails.StringUtil

class Rack {
	Project project
	Integer source
	String location
	Room room
	String tag
	Integer roomX = 0
	Integer roomY = 180
	Integer powerA = 3300
	Integer powerB = 3300
	Integer powerC = 0
	String rackType = "Rack"
	String front = "L"

	static hasMany = [sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"rackSource", targetAssets:"rackTarget"]

	static belongsTo = [ manufacturer:Manufacturer, model:Model, room:Room ]

	static constraints = {
		project( nullable:false )
		source( nullable:true )
		location( blank:true, nullable:true )
		room( nullable:true )
		tag( blank:true, nullable:true )
		roomX( nullable:true )
		roomY( nullable:true )
		powerA( nullable:true )
		powerB( nullable:true )
		powerC( nullable:true )
		rackType( blank:true, nullable:true, inList: ["Rack","CRAC","DoorL","DoorR","UPS","Object","block1x1","block1x2","block1x3","block1x4","block1x5","block2x2","block2x3","block2x4","block2x5","block3x3","block3x4","block3x5","block4x4","block4x5","block5x5"] )
		front( blank:true, nullable:true, inList: ["L","R","T","B"] )
		manufacturer( nullable:true )
		model( nullable:true )
	}

	static mapping  = {
		id column:'rack_id'
		sourceAssets sort:'sourceRackPosition'
		targetAssets sort:'targetRackPosition'
		columns {
			rackType sqlType: 'varchar(20)'
		}
	}

	def getAssets() {
		return(source == 1 ? sourceAssets : targetAssets)
	}

	def hasBelongsToMoveBundle( moveBundleId ){
		boolean returnVal = false
		def assets = this.source == 1 ? sourceAssets : targetAssets
		if (!moveBundleId.contains("all")) {
			moveBundleId.each{ id->
				returnVal = returnVal ?: assets.moveBundle.id.contains(id)
			}
		}
		return returnVal
	}

	/**
	 * Updating reference of assetentity's  rackSource and sourceRack to null while deleting rack
	 */
	def beforeDelete = {
		AssetEntity.withNewSession {
			AssetEntity.executeUpdate("Update AssetEntity set rackSource = null where rackSource = :rack",[rack:this])
			AssetEntity.executeUpdate("Update AssetEntity set rackTarget = null where rackTarget = :rack",[rack:this])
		}
	}
}
