/**
 * @author ecantu
 * Removes model_score_bonus column from Person table.
 * See TM-12294 - Move business logic to services for ModelController
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20181024 TM-12294-1") {
        comment("Drop column model_score_bonus")
        preConditions(onFail:'MARK_RAN') {
            columnExists(tableName:'person', columnName:'model_score_bonus' )
        }
        dropColumn(tableName: 'person', columnName: 'model_score_bonus')
    }

}