/**
 * AssetTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */






import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import grails.util.Holders
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import org.apache.commons.lang3.RandomStringUtils

class AssetTestHelper {
	SecurityService securityService
	ProjectService projectService

	Long adminPersonId = 100

	AssetTestHelper(){
		projectService = Holders.applicationContext.getBean('projectService')
		securityService = Holders.applicationContext.getBean('securityService')
	}

	/**
	 * Used to create an application and reference the person in all possible properties
	 * @param person  The person to be referenced
	 * @param project  The Project to be assigned
	 * @return  The newly created Application
	 */
	Application createApplication(Person person, Project project) {
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
			moveBundle: projectService.getDefaultBundle(project)
		)

		app.moveBundle = project.getDefaultBundle()

		if (!app.save(flush: true)) {
			throw new RuntimeException("createApplication() failed because " + GormUtil.allErrorsString(app))
		}

		return app
	}


	/**
	 * This method creates a random device using the deviceService.saveAssetFromForm
	 * @param project: project to be assigned.
	 * @param assetType: what kind of asset it should be as an Enum
	 * @return  The newly created AssetEntity
	 */
	public AssetEntity createDevice(Project project, AssetType assetType, Map params = [:]) {
	 	return createDevice(project, assetType.toString(), params)
	}

	/**
	 * This method creates a random device using the deviceService.saveAssetFromForm
	 * @param project: project to be assigned.
	 * @param assetType: what kind of asset it should be. It has to be a String because
	 *    not all the possible values have a corresponding keyword.
	 * @return  The newly created AssetEntity
	 */
	public AssetEntity createDevice(Project project, String assetType, Map params = [:]) {
		 /* Most the values in this map replicate what the front-end sends to the
		 back-end when creating a device. */
		Map defaultValues = [
			assetName          : RandomStringUtils.randomAlphabetic(15),
			currentAssetType   : assetType,
			moveBundle         : projectService.getDefaultBundle(project),
			"moveBundle.id"    : params.moveBundle ? params.moveBundle.id.toString() : projectService.getDefaultBundle(project).id.toString(),
			roomSourceId       : "-1",
			locationSource     : "TBD",
			roomSource         : "",
			roomTargetId       : "-1",
			locationTarget     : "TBD",
			roomTarget         : "",
			rackSourceId       : "-1",
			rackSource         : "",
			newRackSourceId    : "-1",
			rackTargetId       : "-1",
			rackTarget         : "",
			newRackTargetId    : "-1",
			sourceChassis      : null,
			targetChassis      : null,
			sourceRackPosition : "",
			targetRackPosition : "",
			sourceBladePosition: "",
			targetBladePosition: "",
			project            : project,
			owner              : project.client,
			assetClass         : AssetClass.DEVICE,

		]

		 defaultValues.each{ key, val->
			 if(!params.containsKey(key)) {
				 params[key] = val
			 }
		 }
		AssetEntity asset = new AssetEntity(params)

		return asset.save(flush: true, failOnError: true)
	 }

}
