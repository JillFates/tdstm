/*
 *
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130125 TM-1191.2") {
		comment("Set the validation='Discovery' and planStatus ='Unassigned' where both are null")
		sql("UPDATE asset_entity SET validation='Discovery' WHERE (validation IS NULL OR validation = '')")
		sql("UPDATE asset_entity SET new_or_old='Unassigned' WHERE (new_or_old IS NULL OR new_or_old = '')")
	}
}