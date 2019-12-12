/**
 * @author oluna
 *
 * Remove all offending records that were introduced in dataview
 * See TM-16497
 */
databaseChangeLog = {

    changeSet(author: "oluna", id: "20191201 TM-16497 ") {
        comment('remove wrong formatted report Schemas in dataview')
        sql("DELETE FROM dataview WHERE report_schema LIKE '[columns%';")
    }

}
