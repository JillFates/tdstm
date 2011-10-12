/**
 * This domain is used to manage the many-to-many relationship of assets that are owned 
 * by a company but can be associated to one or more projects.
 * */
import com.tds.asset.AssetEntity;
import com.tdssrc.grails.GormUtil
class ProjectAssetMap {
	Project project
	AssetEntity asset
	Integer currentStateId
	Date createdDate
	Date lastModified
	
	static constraints = {
		createdDate( nullable:true )
		lastModified( nullable:true )
	}
	
	static mapping = {
		version false
		autoTimestamp false
	}

	String toString() {
		"${project.name} : ${asset.assetName}"
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		createdDate = GormUtil.convertInToGMT( "now", "EDT" )
		lastModified = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastModified = GormUtil.convertInToGMT( "now", "EDT" )
	}
}
