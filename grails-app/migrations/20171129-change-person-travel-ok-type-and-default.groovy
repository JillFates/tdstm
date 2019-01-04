/**
 * Change the Person's Travel Ok column to tinyint and default it to 0
 */

databaseChangeLog = {
    changeSet(author: "arecordon", id: "20171129 TM-8148-1") {
        comment("Change the Person's Travel Ok column to tinyint and default it to 0")

        sql("""
            UPDATE person set travelok = 0 WHERE travelok IS NULL
            """)

        sql("""
            ALTER TABLE person MODIFY COLUMN travelok TINYINT(1) NOT NULL DEFAULT 0;
			""")
    }
}
