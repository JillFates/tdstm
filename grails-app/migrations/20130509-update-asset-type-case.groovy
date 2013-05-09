
/**
 * This set of Database change that is required to update faulty assetType e.g. (blade, BLADE) to 'Blade'
 */

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130509 TM-1903 ") {
		comment('update "asset_type" in "model" and "asset_entity" table to use correct "asset_type"')
		def faultyType = ['Blade':'blade', 'Server':'server', 'Switch':'switch']
		faultyType.each{key, value->
			sql("UPDATE model SET asset_type = '"+key+"' where asset_type = '"+value+"'")
			sql("UPDATE asset_entity SET asset_type = '"+key+"' where asset_type = '"+value+"'")
		}
	}
}