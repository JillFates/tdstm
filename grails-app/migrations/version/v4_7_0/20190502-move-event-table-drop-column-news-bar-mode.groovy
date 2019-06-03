/**
 * @author ecantu
 * Removes news_bar_mode column from move_event table.
 * See TM-14226 - Remove the News Bar functionality from Events and the News ribbon from the Menu
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20190502 TM-14226-1") {
        comment("Drop column news_bar_mode")
        preConditions(onFail:'MARK_RAN') {
            columnExists(tableName:'move_event', columnName:'news_bar_mode' )
        }
        dropColumn(tableName: 'move_event', columnName: 'news_bar_mode')
    }

}