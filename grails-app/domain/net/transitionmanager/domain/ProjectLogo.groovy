package net.transitionmanager.domain

import com.tdssrc.grails.GormUtil
import org.hibernate.Hibernate

import java.sql.Blob

/**
 * Persists the logo that is presented in the UI at the project level.
 */
class ProjectLogo {

	String name
	byte[] partnerImage
	Project project

	static constraints = {
		partnerImage nullable: true
	}

	static mapping = {
		id column: 'project_logo_id'
	}

	static transients = ['size', 'data']

	void setData(InputStream is) {
		partnerImage = is.getBytes()
	}

	long getSize() {
		partnerImage?.length() ?: 0
	}

	/**
	 * A factory method to create/update a ProjectLogo object from an File which will be associated to a project. This will
	 * attempt to lookup an existing ProjectLogo by project first and only create one if it doesn't already exist. It will then
	 * update the object with the content from the file object.
	 *
	 * @param project - the project to save logo to
	 * @param file - the File resource to reference
	 * @return the ProjectLogo object
	 */
	static ProjectLogo createOrUpdate(Project project, file) {
		assert file
		assert project

		String origFileName = file.originalFilename
		int slashIndex = Math.max(origFileName.lastIndexOf('/'), origFileName.lastIndexOf('\\'))
		if (slashIndex > -1) {
			origFileName = origFileName.substring(slashIndex + 1)
		}

		ProjectLogo pl = findByProject(project) ?: new ProjectLogo(name: origFileName, project: project)
		pl.setData file.inputStream

		if (!pl.save(flush: true)) {
			// TODO : JPM : 3/3/2016 : need to figure out how we are going to handle the failure case for createOrUpdate()
			println "createOrUpdate() unable to save ProjectLogo for project $project : ${GormUtil.allErrorsString(pl)}"
		}

		return pl
	}
}
