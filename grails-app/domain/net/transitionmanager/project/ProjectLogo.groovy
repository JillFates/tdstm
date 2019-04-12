package net.transitionmanager.project
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
}
