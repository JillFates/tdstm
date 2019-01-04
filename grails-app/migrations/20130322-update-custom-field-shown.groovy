/*
 *
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130322 TM-1263-3") {
		comment("Set the custom_fields_shown='8'  where it is null")
		sql("UPDATE project SET custom_fields_shown='8' WHERE (custom_fields_shown IS NULL OR custom_fields_shown = '')")
	}
}
