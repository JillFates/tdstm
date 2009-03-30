class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	// Temp Attributes
	String serverName
	String model
	String sourceLocation
	String targetLocation
	String sourceRack
	String targetRack
	String position
	String unitSize
	
	
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]
	
	static constraints = {
		serverName( blank:false, nullable:false)
		model( blank:true, nullable:true)
		sourceLocation( blank:true, nullable:true )
		targetLocation( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		position( blank:true, nullable:true )
		unitSize( blank:true, nullable:true )
	}
	// This is where we will/would define special details about the class
	// TBD...
	//static eavModel = {
	//	attributeDomain: AssetAttribute
	//	,decendent: this
	//}
}
