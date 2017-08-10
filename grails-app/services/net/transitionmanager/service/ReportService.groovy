package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Report

class ReportService implements ServiceMethods {

	SecurityService securityService

	List<Report> list(Project project, Person person) {
		def query = Report.where {
			project == project && (isShared == true || isSystem == true || person == person)
		}
		return query.list()
	}
}