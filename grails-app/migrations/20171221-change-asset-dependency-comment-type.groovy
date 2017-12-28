/**
 * Change the type for the asset_dependency comment to text.
 */
databaseChangeLog = {
    changeSet(author: "arecordon", id: "20171221 TM-8299-1") {
        comment("Change the type for the asset_dependency comment to text.")
        sql("ALTER TABLE asset_dependency MODIFY comment TEXT")
    }

}
