import net.transitionmanager.domain.RoleType

/**
 * @author: Diego Correa
 *
 * Ticket TM-5161
 *
 * Removes all prefixes from the Team descriptions from the Role Type domain class.
 *
 *
 */
databaseChangeLog = {

    changeSet(author: "dcorrea", id: "20170907 TM-5161") {
        comment('This script removes ["Staff : ", "App :", "Party : ", "Project : ", "System : ", "Team : "] prefixes from TEAM type descriptions in role types.')

        sql("UPDATE role_type SET description = REPLACE(description, 'App : ', '');")
        sql("UPDATE role_type SET description = REPLACE(description, 'Party : ', '');")
        sql("UPDATE role_type SET description = REPLACE(description, 'Project : ', '');")
        sql("UPDATE role_type SET description = REPLACE(description, 'Staff : ', '');")
        sql("UPDATE role_type SET description = REPLACE(description, 'System : ', '');")
        sql("UPDATE role_type SET description = REPLACE(description, 'Team : ', '');")

    }
}

