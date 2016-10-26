import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.RoleType

/**
 * This set of database changes will create the person that represent the automated service used in task completion
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130510 TM-1905-1") {
		comment('Create Automated Task person account')

		grailsChange {
			change {
				def personType = PartyType.read("PERSON")
				def person =  new Person (
					firstName:'Automated', lastName:'Task',
					title:'Account for task completions',
					active: 'Y',
					staffType: 'Salary',
					partyType:personType)
				if ( ! person.validate() || ! person.save(flush:true) ) {
					throw new RuntimeException('Creating person failed')
				}
			}
		}
	}
	changeSet(author: "jmartin", id: "20130510 TM-1905-2") {
		comment('Add new Staff : Automatic roll type')

		grailsChange {
			change {
				def role = new RoleType()
				role.id = AssetComment.AUTOMATIC_ROLE
				role.description = 'Staff : Automatic'
				if ( ! role.validate() || ! role.save(flush:true) ) {
					throw new RuntimeException('Creating RoleType failed: ' + GormUtil.allErrorsString(role))
				}
			}
		}
	}

}
