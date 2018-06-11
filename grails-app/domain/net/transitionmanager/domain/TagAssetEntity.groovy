package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil

class TagAssetEntity {
	Date      dateCreated
	Date      lastUpdated

	static belongsTo = [tag: Tag, assetEntity: AssetEntity]

	static constraints = {
		tag unique: ['assetEntity']
	}

	static mapping = {
		id column: 'tag_asset_entity_id'
		autoTimestamp false
		domainClass enumType: "string"
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT().clearTime()
	}

	def beforeUpdSate = {
		lastUpdated = TimeUtil.nowGMT().clearTime()
	}
}
