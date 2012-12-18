/**
 * This changeLog will migrate the aka comma delimited lists of aka fields in the manufacturer and model tables into their own 
 * normalized alias tables appropriately.
 */

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20121218 TM-1132.1") {
		comment('Drop manufacturer_alias.version column')
		preConditions(onFail:'MARK_RAN') {
			// Make sure that the aka data has been migrated to new table before we drop it
			columnExists(schemaName:'tdstm', tableName:'manufacturer_alias', columnName:'version' )
	    }
	    dropColumn(tableName:'manufacturer_alias', columnName:'version')
	}
		
	changeSet(author: "jmartin", id: "20121218 TM-1132.2") {
		comment('Move the manufacturer.aka values to new manufacturer_alias table')
		grailsChange {
	    	change {
				sql.eachRow('SELECT manufacturer_id AS id, aka, date_created FROM manufacturer ORDER by name') { manufacturer->
					def akas = manufacturer.aka
					def akaList = akas?.split(",")
					if (akas) {
						akaList.each{ aka->
							aka = aka.trim().toLowerCase()
							
							// See if the alias is already a manufacturer or an existing alias before inserting a new alias 
							def manuSearch = sql.firstRow("SELECT manufacturer_id AS id, aka FROM manufacturer WHERE name=${aka}")
							if (manuSearch) {
								println "Manufacturer alias '${aka}' references existing manufacturer (id=${manuSearch.id}) - alias dropped"
							} else {
								manuSearch = sql.firstRow("SELECT manufacturer_id AS id FROM manufacturer_alias WHERE name=${aka}")
								if (manuSearch) {
									println "Manufacturer alias '${aka}' alias already exists"									
								} else {
									sql.execute("""INSERT INTO manufacturer_alias (manufacturer_id, name, date_created) 
										VALUES(${manufacturer.id}, ${aka}, ${manufacturer.date_created})""")
								}
							}
						}
					}
				}
			}
		}		
	}
	
	changeSet(author: "jmartin", id: "20121218 TM-1132.3") {
		comment('Move the model.aka values to new model_alias table')
		grailsChange {
	    	change {
				sql.eachRow('SELECT model_id AS id, manufacturer_id AS mid, aka, date_created FROM model ORDER by name') { model ->
					def akas = model.aka
					def akaList = akas?.split(",")
					if (akas) {
						akaList.each{ aka->
							aka = aka.trim().toLowerCase()
							
							// See if the alias is already a model or an existing alias before inserting a new alias 							
							def modelSearch = sql.firstRow("SELECT model_id AS id, aka FROM model WHERE manufacturer_id=${model.mid} AND name=${aka}")
							if (modelSearch) {
								println "Model alias '${aka}' references existing model (id=${modelSearch.id}) - alias dropped"
							} else {
								modelSearch = sql.firstRow("SELECT manufacturer_id AS mid FROM model_alias WHERE manufacturer_id=${model.mid} AND name=${aka}")
								if (modelSearch) {
									println "Model alias '${aka}' already exists for manufacturer id ${model.mid}"
								} else {
									sql.execute("""INSERT INTO model_alias (model_id, manufacturer_id, name, date_created) 
										VALUES(${model.id}, ${model.mid}, ${aka}, ${model.date_created})""")
								}
							}
						}
					}
				}
			}
		}		
	}
}
