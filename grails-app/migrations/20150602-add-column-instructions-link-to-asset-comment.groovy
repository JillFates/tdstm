/*Adds a new column in asset_comment "instructions_link"*/
databaseChangeLog = {
	changeSet(author: "jdanahy", id: "20150602 TM-3845-1") {
		comment('Create column instructions_link in asset comment')
		 addColumn(tableName: "asset_comment") {
            column(name: "instructions_link", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
	}
}
