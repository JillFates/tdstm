import grails.converters.JSON
import com.tdsops.tm.enums.domain.ValidationType
import com.tds.asset.FieldImportance
import net.transitionmanager.domain.Project

/**
 * Set custom fields to hidden by default in default project
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20151020 TM-3934-1") {
		comment('Set custom fields to hidden by default in default project')

		grailsChange {
			change {
				def defaultProject = Project.getDefaultProject()

				def entityTypes = ["Application", "AssetEntity", "Database", "Files"]
				def phases = ValidationType.getListAsMap().keySet()
				def attrValues

				// TM-6622 - Below code has been commented because FieldImportance table and domain class no longer exists.
				/*entityTypes.each { entityType ->
					def fieldImportance = FieldImportance.findByProjectAndEntityType(defaultProject, entityType)
					def configMap = JSON.parse(fieldImportance.config)
					(1..Project.CUSTOM_FIELD_COUNT).each { i ->
						def pmap = phases.inject([:]) { map, item ->
							map[item]="H"
							return map
						}
						configMap['custom' + i] = ['phase': pmap]
					}

					fieldImportance.config = (configMap as JSON).toString()
					fieldImportance.save(flush: true)
				}*/
			}
		}

	}
}