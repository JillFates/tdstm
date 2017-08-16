/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Report
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Service class with main database operations for Report.
 * @see Report
 */
class ReportService implements ServiceMethods {

	SecurityService securityService

	/**
	 * Query for getting all projects where: belong to current project and either shared, system or are owned by
	 * current person in session.
	 * @param project Filter reports by this project.
	 * @param person Filter by owned reports.
	 * @return
	 */
	List<Report> list(Project project, Person person) {
		def query = Report.where {
			project == project && (isSystem == true || isShared == true || person == person)
		}
		return query.list()
	}

	/**
	 * Get a report by specific id.
	 * @param id
	 * @return Report object.
	 */
	Report fetch(Integer id) {
		return Report.get(id)
	}

	/**
	 * Updates a database report object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param report original report object.
	 * @param json JSONObject to take changes from.
	 */
	def update(Report report, JSONObject json) {
		report.reportSchema = json.schema
		report.isShared = json.isShared
		if(!report.save()) {
			report.errors.each {
				log.error(it)
			}
		}
	}

	/**
	 * Creates a database report object.
	 * Report person and project are taken from current session.
	 * @param json JSONObject to take changes from.
	 */
	def create(JSONObject json) {
		Report report = new Report()
		report.person = securityService.loadCurrentPerson()
		report.project = securityService.userCurrentProject
		report.name = json.name
		report.isSystem = json.isSystem
		report.isShared = json.isShared
		report.reportSchema = json.schema
		if(!report.save()) {
			report.errors.each {
				log.error(it)
			}
		}
	}
}