
import com.tds.asset.AssetEntity;
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def entityType = EavEntityType.findByEntityTypeCode('AssetEntity')
/*
 * Set Server as AssetEntity default type
 */
AssetEntity.executeUpdate("UPDATE from AssetEntity set assetType = 'Server' where assetType is null")
def eavAttribute = EavAttribute.findByAttributeCodeAndEntityType("assetType",entityType)
EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[eavAttribute])
def assetTypes = AssetEntity.findAll("From AssetEntity group by assetType")
assetTypes?.assetType?.each{ option->
   try{
      def integerAssetType =  Integer.parseInt(option)
	  option = 'Server'
	  AssetEntity.executeUpdate("Update AssetEntity set assetType = 'Server' where assetType = '${integerAssetType}'")
	  println "Error : AssetType has wrong value::::::::::"+integerAssetType
    }  catch(Exception ex){
		if(!option){
			option = 'Server'
		}
    }
	def eavAttributeOption = EavAttributeOption.findByValueAndAttribute(option,eavAttribute)
	if( !eavAttributeOption ){
		eavAttributeOption = new EavAttributeOption(
													value : option,
													attribute : eavAttribute
													)
		if ( !eavAttributeOption.validate() || !eavAttributeOption.save(flush:true) ) {
			def etext = "Unable to create eavAttributeOption" +
			GormUtil.allErrorsString( eavAttributeOption )
			println etext
		}
	}
}
println"**********************Create Rack asset entity option***************************************"
def eavAttributeOption = EavAttributeOption.findByValueAndAttribute("Rack",eavAttribute)
if( !eavAttributeOption ){
	eavAttributeOption = new EavAttributeOption(
			value : "Rack",
			attribute : eavAttribute
			)
	if ( !eavAttributeOption.validate() || !eavAttributeOption.save(flush:true) ) {
		def etext = "Unable to create eavAttributeOption" +
				GormUtil.allErrorsString( eavAttributeOption )
		println etext
	 }
}