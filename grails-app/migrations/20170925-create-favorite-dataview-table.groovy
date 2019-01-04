/**
 * Create the table for FavoriteDataView
 */

databaseChangeLog = {
    changeSet(author: "arecordon", id: "20170925 TM-7442-1") {
        comment('Create table favorite_dataview ')

        sql("""
            CREATE TABLE IF NOT EXISTS  favorite_dataview (
                  person_id BIGINT(20) NOT NULL,
                  dataview_id BIGINT(20) NOT NULL,
                  PRIMARY KEY (person_id, dataview_id),
                  FOREIGN KEY FK_PERSON_FAV_DATAVIEW (person_id) REFERENCES person(person_id),
                  FOREIGN KEY FK_DATAVIEW_FAV_DATAVIEW (dataview_id) REFERENCES dataview(id)
			  )
			""")
    }

    changeSet(author: "arecordon", id: "20171019 TM-7442-2") {
        comment('Regenerate FKs using appropriate cascading.')

        grailsChange {
            change {
                Set<String> tdsSchemas = []

                // Retrieve all the FKs for the favorite_dataview in different schemas
                String query = """
                                SELECT constraint_schema AS db, constraint_name AS fk 
                                FROM information_schema.REFERENTIAL_CONSTRAINTS 
                                WHERE table_name="favorite_dataview"
                                """
                // Iterate over each result
                sql.eachRow(query) { row ->
                    String db = row.db
                    String fk = row.fk
                    String dropStatement =
                                """
                                ALTER TABLE ${db}.favorite_dataview DROP FOREIGN KEY ${fk}
                                """
                    // Drop the FK
                    sql.execute(dropStatement)

                    // Add the schema to the set
                    tdsSchemas.add(db)
                }

                // Iterate over the tds schemas regenerating the FKs
                tdsSchemas.each { db ->
                    String personFK = """
                                        ALTER TABLE ${db}.favorite_dataview
                                        ADD FOREIGN KEY (person_id) REFERENCES person(person_id)
                                        ON DELETE CASCADE
                                      """

                    String dataviewFK = """
                                        ALTER TABLE ${db}.favorite_dataview
                                        ADD FOREIGN KEY (dataview_id) REFERENCES dataview(id)
                                        ON DELETE CASCADE
                                        """
                    sql.execute(personFK)
                    sql.execute(dataviewFK)
                }
            }
        }
    }
}
