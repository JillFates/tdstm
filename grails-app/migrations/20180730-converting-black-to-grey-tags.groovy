/**
 * This changelog convert any black tags from testing to grey tags.
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20180730 TM-11593-1") {
		comment("Change all black tags to grey tags.")
		sql("UPDATE tag SET color = 'Grey' WHERE color = 'Black';")
	}
}
