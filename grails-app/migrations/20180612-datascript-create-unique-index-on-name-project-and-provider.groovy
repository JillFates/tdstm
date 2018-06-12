/**
 * @author ecantu
 * TM-10194 Fix the ugly hibernate error that appears when you try and create a DataScript with a duplicate name.
 * The existing unique index `IX_DATASCRIPT_PROJECT_NAME` it's preventing insertion of duplicates by 'name' and 'project_id',
 * but in the front end checks when creating a Datascript (DatascriptService.validateUniqueName()) the check is
 * by 'name', 'project_id' and 'provider_id', so this index has to be dropped and recreated to include 'provider_id' as well.
 * As the dropped index is more restrictive than the new one, there is no treatment for previous data needed.
 */
databaseChangeLog = {

    changeSet(author: 'ecantu', id: 'TM-10194-1') {
        comment('Drop unique index IX_DATASCRIPT_PROJECT_NAME')
        dropIndex(tableName:'data_script', indexName:'IX_DATASCRIPT_PROJECT_NAME')
    }

    changeSet(author: 'ecantu', id: 'TM-10194-2') {
        comment('re-create unique index on name, Project and Provider')
        createIndex(indexName:'IX_DATASCRIPT_PROJECT_NAME', tableName:'data_script', unique:true) {
            column(name:'name')
            column(name:'project_id')
            column(name:'provider_id')
        }

    }
}
