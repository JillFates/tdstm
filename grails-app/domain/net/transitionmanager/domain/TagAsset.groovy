package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil

class TagAsset {
	Date      dateCreated
	Date      lastUpdated

	static belongsTo = [tag: Tag, asset: AssetEntity]

	static constraints = {
		tag unique: ['asset']
	}

	static mapping = {
		id column: 'tag_asset_id'
		autoTimestamp false
		domainClass enumType: "string"
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT().clearTime()
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT().clearTime()
	}
}
