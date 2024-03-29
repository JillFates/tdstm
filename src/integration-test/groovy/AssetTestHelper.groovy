/**
 * AssetTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */



import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import grails.util.Holders
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.SecurityService
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
	 * Simple method for creating a Device with a bare minimum fields set.
	 * @param project - the project the device will belong to
	 * @param moveBundle - the bundle the device will be assigned to.
	 * @param assetType - the asset type for the device.
	 * @return a device instance
	 */
	AssetEntity createDevice(Project project, MoveBundle moveBundle, AssetType assetType) {
		AssetEntity device = new AssetEntity(
			assetName: RandomStringUtils.randomAlphabetic(15),
			project: project,
			moveBundle: moveBundle,
			assetType: assetType.toString(),
			custom7: "something"
		)

		if (!device.save(flush: true)) {
			throw new RuntimeException("createDevice() failed because " + GormUtil.allErrorsString(device))
		}
		return device
	}


	/**
	 * Creates, saves and return a new Database with some basic configuration.
	 * @param project - the project the database will be assigned to.
	 * @param bundle - The MoveBundle the application is assigned to. If null, the project's default bundle will be used.
	 * @return a new Database instance.
	 */
	Database createDatabase(Project project, MoveBundle bundle = null) {
		Database database = new Database(
			assetName: RandomStringUtils.randomAlphabetic(15),
			project: project,
			moveBundle: bundle?: project.projectDefaultBundle,
			custom7: "something"
		)

		if (!database.save(flush: true)) {
			throw new RuntimeException("createDabase() failed because " + GormUtil.allErrorsString(database))
		}

		return database
	}


	/**
	 * Creates, saves and return a new Files with some basic configuration.
	 * @param project - the project the storage will be assigned to.
	 * @param bundle - The MoveBundle the application is assigned to. If null, the project's default bundle will be used.
	 * @return a new Files instance.
	 */
	Files createStorage(Project project, MoveBundle bundle = null) {
		Files storage = new Files(
			assetName: RandomStringUtils.randomAlphabetic(15),
			project: project,
			moveBundle: bundle?: project.projectDefaultBundle,
			custom7: 'something'
		)

		if (!storage.save(flush: true)) {
			throw new RuntimeException("createStorage() failed because " + GormUtil.allErrorsString(storage))
		}

		return storage
	}


	/**
	 * Used to create an application and reference the person in all possible properties
	 * @param person  The person to be referenced
	 * @param project  The Project to be assigned
	 * @param bundle - The MoveBundle the application is assigned to. If null, the project's default bundle will be used.
	 * @return  The newly created Application
	 */
	Application createApplication(Person person, Project project, MoveBundle bundle = null) {
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
			moveBundle: bundle?: projectService.getDefaultBundle(project),
			custom7: "something"
		)

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
	AssetEntity createDevice(Project project, String assetType, Map params = [:]) {
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
			custom7            : "something",
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

		return asset.save(flush: true)
	 }

}
