
/**
 * This change set is used to add ITIL classifications to the Application criticality property
 */
databaseChangeLog = {
	
	changeSet(author: "rmacfarlane", id: "20160712 TM-5101-1") {
		comment('Add ITIL classifications to the Application criticality property')
		sql("ALTER TABLE application MODIFY COLUMN criticality varchar(25)")
	}
}
