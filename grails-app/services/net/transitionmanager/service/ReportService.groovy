/**
 * @author David Ontiveros
 */
package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Report

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
}