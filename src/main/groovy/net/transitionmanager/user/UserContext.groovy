package net.transitionmanager.user

import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.UserLogin

class UserContext {

	UserLogin userLogin

	Person person

	Project project

	MoveEvent moveEvent

	MoveBundle moveBundle

	String timezone

	String dateFormat

	String logoUrl

	List<Project> alternativeProjects


	/**
	 * Create and return a map representation of the User's context with only a selection of fields.
	 * @return a map with the values for the context object.
	 */
	Map toMap() {
		Map eventMap = null
		if (moveEvent) {
			eventMap = [
				id: moveEvent.id,
				name: moveEvent.name
			]
		}
		Map bundleMap = null
		if (moveBundle) {
			bundleMap = [
				id: moveBundle.id,
				name: moveBundle.name
			]
		}

		Map projectMap = null
		if (project) {
			projectMap = [
				id: project.id,
				name: project.name,
				status: project.status,
				logoUrl: logoUrl
			]
		}

		List<Map> alternativeProjectsList = alternativeProjects.collect { Project pr ->
			[id: pr.id, name: pr.name, logoUrl: projectService.getProjectLogoUrl(pr), status: pr.status]
		}
		return [
		    user: [
		        id: userLogin.id,
			    username: userLogin.username
		    ],
			person: [
			    id: person.id,
				firstName: person.firstName,
				fullName: person.toString()
			],
			project: projectMap,
			event: eventMap,
			bundle: bundleMap,
			timezone: timezone,
			dateFormat: dateFormat,
			alternativeProjects: alternativeProjectsList
		]
	}

	private static ProjectService getProjectService() {
		return ApplicationContextHolder.getBean('projectService', ProjectService)
	}
}
