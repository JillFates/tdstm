import com.tds.asset.AssetCableMap
import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.AssetCableStatus


class ModelConnector {
	// Declare propertied
	String connector
	String label
	String type
	String labelPosition
	Integer connectorPosX
	Integer connectorPosY
	String status
	String option
	Date dateCreated
	Date lastModified
	
	
	static belongsTo = [ model : Model ]
	
    static constraints = {
		connector( blank:false, nullable:false, unique:'model' )
		label( blank:true, nullable:true, unique:'model' )
		type( blank:true, nullable:true, inList: ["Ether", "Serial", "Power", "Fiber", "SCSI", "USB", "KVM","ILO","Management","SAS","Other"] )
		labelPosition( blank:true, nullable:true )
		connectorPosX( nullable:true )
		connectorPosY( nullable:true )
		status( blank:false, nullable:false, inList: ['missing','empty','cabled','cabledDetails'] )
		option( blank:true, nullable:true )
        lastModified( nullable:true )
        dateCreated( nullable:true )
    }
	
	static mapping  = {	
		version false
        autoTimestamp false
		columns {
			id column:'model_connectors_id'
			option column:'connector_option'
			type sqltype: 'varchar(20)'
		}
	}

	def beforeInsert = {
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
    
    def beforeDelete = {
        AssetCableMap.withNewSession { fromConnectorCableMaps*.delete() }
        AssetCableMap.withNewSession{
            AssetCableMap.executeUpdate("Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}', assetTo=null, \
                                                        assetToPort=null where assetToPort = :connector",[connector:this])
        }
    }
    
    def getFromConnectorCableMaps(){
        AssetCableMap.findAllByAssetFromPort(this)
    }
	
	String toString(){
		"${model?.modelName} : ${connector}"
	}
}
