import grails.converters.JSON
import com.tdsops.tm.enums.domain.ValidationType
import net.transitionmanager.domain.Project

/**
 * Set custom fields to hidden by default in default project
 *
 * dontiveros: this script has been removed from the changelog.groovy file,
 * as part of TM-6622 work of removing old legacy Field Settings.
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

				//  FieldImportance table no longer exists
				/*
				entityTypes.each { entityType ->
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
				}
				*/
			}
		}

	}
}
