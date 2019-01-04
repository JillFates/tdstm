/**
 * This set of Database change to drop aka field from manufacturer and model table as we no more using it.
 */

databaseChangeLog = {	
	changeSet(author: "lokanada", id: "20121214 TM-1132.1") {
		comment('Drop "aka" column from the manufacturer table')
		preConditions(onFail:'HALT', onFailMessage:'The AKA migration script must be run before dropping the manufacturer.aka column') {
			// Make sure that the aka data has been migrated to new table before we drop it
			sqlCheck(expectedResult:"1", "SELECT IF( (SELECT COUNT(*) FROM manufacturer_alias)=0, 0, 1)" )
	    }
	    dropColumn(tableName:'manufacturer', columnName:'aka')
	}
	changeSet(author: "lokanada", id: "20121214 TM-1132.2") {
		comment('Drop "aka" column from the model table')
		preConditions(onFail:'HALT', onFailMessage:'The AKA migration script must be run before dropping the model.aka column') {
			// Make sure that the aka data has been migrated to new table before we drop it
			sqlCheck(expectedResult:"1", "SELECT IF( (SELECT COUNT(*) FROM model_alias)=0, 0, 1)" )
	    }
	    dropColumn(tableName:'model', columnName:'aka')
	}
}
