/**
 * This migration will remove any model/manufacturer aliases with names that are identical to the model/manufacturer they alias
 * @author rmacfarlane
 * TM-5279
 */
databaseChangeLog = {
	changeSet(author: "rmacfarlane", id: "20170209 TM-5279-1") {
		comment('Remove any model aliases that match the name of the model they alias.')
		sql("""
			DELETE ma FROM model_alias ma
			LEFT OUTER JOIN model m ON m.model_id = ma.model_id
			WHERE BINARY ma.name = m.name
		""")
	}
	changeSet(author: "rmacfarlane", id: "20170209 TM-5279-2") {
		comment('Remove any manufacturer aliases that match the name of the manufacturer they alias.')
		sql("""
			DELETE ma FROM manufacturer_alias ma
			LEFT OUTER JOIN manufacturer m on m.manufacturer_id = ma.manufacturer_id
			WHERE BINARY ma.name = m.name
		""")
	}
}
