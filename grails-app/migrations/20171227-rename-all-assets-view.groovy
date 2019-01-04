databaseChangeLog = {
    changeSet(author: "arecordon", id: "TM-8470-1") {
        comment('Rename the All assets view to All Assets')

        sql("""
             UPDATE dataview SET name='All Assets' WHERE id = 1;
			""")
    }
}
