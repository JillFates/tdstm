package net.transitionmanager.project

import net.transitionmanager.asset.AssetEntity
import com.tdssrc.grails.TimeUtil

/**
 * Manages the many-to-many relationship of assets that are owned
 * by a company but can be associated to one or more projects.
 */
class ProjectAssetMap {

	Project project
	AssetEntity asset
	Integer currentStateId
	Date createdDate
	Date lastModified

	static constraints = {
		createdDate nullable: true
		lastModified nullable: true
	}

	static mapping = {
		version false
		autoTimestamp false
	}

	String toString() {
		"$project.name : $asset.assetName"
	}

	def beforeInsert = {
		createdDate = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
