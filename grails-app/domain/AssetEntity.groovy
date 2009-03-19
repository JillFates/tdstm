class AssetEntity extends com.tdssrc.eav.EavEntity {

	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]

	// This is where we will/would define special details about the class
	// TBD...
	//static eavModel = {
	//	attributeDomain: AssetAttribute
	//	,decendent: this
	//}
}
