/**
 * AssetTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import com.tdsops.common.grails.ApplicationContextHolder
import org.apache.commons.lang3.RandomStringUtils

import com.tds.asset.Application
import com.tdssrc.grails.GormUtil
import com.tdssrc.eav.EavAttributeSet
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project

class AssetTestHelper {
	PersonService personService
	SecurityService securityService

	Long adminPersonId = 100

	AssetTestHelper() {
		personService = ApplicationContextHolder.getService('personService')
		securityService = ApplicationContextHolder.getService('securityService')
		assert (personService instanceof PersonService)
		assert (securityService instanceof SecurityService)
	}



	/**
	 * Used to create an application and reference the person in all possible properties
	 * @param person - the person to be referenced
	 * @return the newly created Application
	 */
	private Application createApplication(Person person, Project project) {
		String pRef = person.id.toString()
		Application app = new Application(
			assetName: RandomStringUtils.randomAlphabetic(15),
			project: project,
			modifiedBy: person,
			appOwner: person,
			sme: person,
			sme2: person,
			shutdownBy: pRef,
			startupBy: pRef,
			testingBy: pRef,
			moveBundle: project.projectDefaultBundle
		)
		app.attributeSet = EavAttributeSet.get(2)
		app.moveBundle = project.getDefaultBundle()

		if (! app.save(flush:true) ) {
			throw new RuntimeException("createApplication() failed because " + GormUtil.allErrorsString(app))
		}

		return app
	}

}