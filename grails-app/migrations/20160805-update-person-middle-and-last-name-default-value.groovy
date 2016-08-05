
/**
 * Sets the default value for a Person's middle and last name to
 * an empty string.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20160805 TM-5189-1") {
		comment("Sets the default value for a Person's middle and last name to an empty string.")

		def columnsInfo = [middle:[name: "middle_name", size: 20], last:[name: "last_name", size: 34]]
		columnsInfo.each{ key, value ->
    		String updateStatement = "ALTER TABLE person CHANGE COLUMN ${value.name} ${value.name} VARCHAR(${value.size}) NOT NULL DEFAULT ''"
    		sql(updateStatement)
		}
	}
}
