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
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.DeviceService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project

class AssetTestHelper {
	DeviceService deviceService
	PersonService personService
	SecurityService securityService

	Long adminPersonId = 100

	AssetTestHelper() {
		deviceService = ApplicationContextHolder.getService('deviceService')
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
		app.moveBundle = project.getDefaultBundle()

		if (! app.save(flush:true) ) {
			throw new RuntimeException("createApplication() failed because " + GormUtil.allErrorsString(app))
		}

		return app
	}


	/**
	 * This method creates a random device using the deviceService.saveAssetFromForm
	 * @param project: prroject to be assigned.
	 * @param assetType: what kind of asset it should be as an Enum
	 */
	public AssetEntity createDevice(Project project, AssetType assetType, Map params = [:]) {
	 	return createDevice(project, assetType.toString(), params)
	}

	/**
	 * This method creates a random device using the deviceService.saveAssetFromForm
	 * @param project: prroject to be assigned.
	 * @param assetType: what kind of asset it should be. It has to be a String because
	 *    not all the possible values have a corresponding keyword.
	 */
	public AssetEntity createDevice(Project project, String assetType, Map params = [:]) {
		 /* Most the values in this map replicate what the front-end sends to the
		 back-end when creating a device. */
		 Map defaultValues = [
		 		assetName: RandomStringUtils.randomAlphabetic(15),
				currentAssetType: assetType,
				moveBundle: project.projectDefaultBundle,
				"moveBundle.id": params.moveBundle ? params.moveBundle.id.toString() : project.projectDefaultBundle.id.toString(),
				roomSourceId: "-1",
				sourceLocation: "",
				sourceRoom: "",
				roomTargetId: "-1",
				targetLocation: "",
				targetRoom: "",
				rackSourceId: "-1",
				sourceRack: "",
				newRackSourceId: "-1",
				rackTargetId: "-1",
				targetRack: "",
				newRackTargetId: "-1",
				sourceChassis: "0",
				targetChassis: "0",
				sourceRackPosition: "",
				targetRackPosition: "",
				sourceBladePosition: "",
				targetBladePosition: "",
				sourceLocation: "TBD",
				targetLocation: "TBD",
		 ]
		 defaultValues.each{ key, val->
			 if(!params.containsKey(key)) {
				 params[key] = val
			 }
		 }
		return deviceService.saveAssetFromForm(project, params)
	 }

}
