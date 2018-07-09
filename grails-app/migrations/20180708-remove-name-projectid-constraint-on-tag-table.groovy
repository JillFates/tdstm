/**
 * @author ecantu
 * Remove unique constraint on Tag table for name and project_id.
 * See TM-11127 - Copy Tags to asset being cloned
 */

databaseChangeLog = {

    changeSet(author: "ecantu", id: "20180708 TM-11127-1") {
        comment("Remove 'UK_tag_name_project' constraint")
        sql("ALTER TABLE tag DROP INDEX UK_tag_name_project")
    }
}
