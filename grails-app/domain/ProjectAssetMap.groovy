/**
 * This domain is used to manage the many-to-many relationship of assets that are owned 
 * by a company but can be associated to one or more projects.
 **/
class ProjectAssetMap {
    Project	project
    AssetEntity	asset
    Date createdDate

	static mapping  = {
		version false
	}
	
	String toString(){
		"${project.name} : ${asset.assetName}" 
	}
}
