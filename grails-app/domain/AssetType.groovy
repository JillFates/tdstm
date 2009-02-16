class AssetType {
	
	String id
	String description
	
	/*
	 * Fields Validations
	 */
	static constraints = {
		id( blank:false, nullable:false, maxLength:20 )
		description( blank:true, nullable:true )
	}

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {
		version false
		id column: 'asset_type_code', generator: 'assigned', sqlType:'varchar(20)'
	}
	
	String toString(){
		"$id : $description"
	}

}
