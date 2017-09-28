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
}