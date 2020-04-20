package net.transitionmanager.model

import net.transitionmanager.asset.AssetCableMap
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil

import static com.tdsops.tm.enums.domain.AssetCableStatus.UNKNOWN

class ModelConnector {

	String connector
	String label
	String type
	String labelPosition
	Integer connectorPosX
	Integer connectorPosY
	String status = 'missing'
	String option
	Date dateCreated
	Date lastModified

	static belongsTo = [model: Model]

	static constraints = {
		connector blank: false
		connectorPosX nullable: true
		connectorPosY nullable: true
		dateCreated nullable: true
		label nullable: true
		labelPosition nullable: true
		lastModified nullable: true
		option nullable: true
		status blank: false, inList: ['missing', 'empty', 'cabled', 'cabledDetails']
		type nullable: true, inList: ['Ether', 'Serial', 'Power', 'Fiber', 'SCSI', 'USB', 'KVM', 'ILO', 'Management', 'SAS', 'Other']
	}

	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column: 'model_connectors_id'
			option column: 'connector_option'
			type sqltype: 'varchar(20)'
		}
	}

	def beforeInsert = {
		dateCreated = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

	def beforeDelete = {
			executeUpdate('delete AssetCableMap where assetFromPort=?0', [this])
			executeUpdate('update AssetCableMap set cableStatus=?0, assetTo=null, assetToPort=null ' +
			              'where assetToPort=?1', [UNKNOWN, this])
	}

	List<AssetCableMap> getFromConnectorCableMaps() {
		AssetCableMap.findAllByAssetFromPort(this)
	}

	String toString() {
		"${model?.modelName} : $connector"
	}
}
