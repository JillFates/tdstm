/**
 * This migration is to delete obsolete permission ViewSupervisorConsoles
 */ 
import com.tdssrc.grails.GormUtil

databaseChangeLog = {

	def permCode =	'ViewSupervisorConsoles'

	// this changeset is used to Rename "ShowMoveTechsAndAdmins" permission to "ViewSupervisorConsoles" in permission table
	changeSet(author: "jmartin", id: "20140903 TM-3220-1") {
		comment('Delete obsolete permission ViewSupervisorConsoles in permission tables')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'1', "select count(*) from permissions where permission_group='CONSOLE' and permission_item='$permCode'")
		}
		grailsChange {
			change {
				def perm = Permissions.findByPermissionItem(permCode)
				if (perm) {
					perm.delete(flush: true)
				} else {
					throw new RuntimeException("Unable to delete permission $permCode")
				}
			}
		}
	}
}
