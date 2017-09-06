import net.transitionmanager.domain.RoleType

/**
 * @author: Diego Correa
 *
 * Ticket TM-5161
 *
 * Remove the "Staff : " prefix from the Team descriptions and hide other types from the Role Type CRUD pages
 *
 *
 */
databaseChangeLog = {

    changeSet(author: "dcorrea", id: "20170830 TM-5161") {
        comment('This script removes "Staff : " prefix from TEAM type descriptions.')

        grailsChange {
            change {

                updateRoleTypeDescriptions()
            }
        }

    }
}


void updateRoleTypeDescriptions() {

    List<RoleType> roleTypes = RoleType.where { type == RoleType.TEAM }.list()

    roleTypes.each { RoleType roleType ->

        String newDescription = roleType.toString().trim()
        roleType.description = newDescription

        if (roleType.validate()) {
            roleType.save(flush: true)
        } else {
            throw new RuntimeException("Couldn't update role type: ${roleType.toString()} id: ${roleType.id}")
        }
    }
}
