package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetClass
import grails.transaction.Transactional
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.PartyRelationshipService

/**
 * The application service handles the logic for CRUD applications
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ApplicationService implements ServiceMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	PartyRelationshipService partyRelationshipService

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned
	 * applications for the user's current project
	 *
	 * @param bundleId the id of the bundle
	 * @return the list of applications associated with the bundle
	 */
	def listInBundle(bundleId) {
		Project project = controllerService.requiredProject

		if (bundleId != null && !bundleId.isNumber()) {
			throw new IllegalArgumentException('Not a valid number')
		}

		bundleId = bundleId.toInteger()
		def mb
		if (bundleId > 0) {
			mb = MoveBundle.get(bundleId)
			if (mb != null) {
				securityService.assertCurrentProject mb.project
			} else {
				log.info('Move bundle is null')
				throw new EmptyResultException()
			}
		}

		def result = []
		def applications
		if (mb != null) {
			applications = Application.findAllByMoveBundle(mb)
		} else {
			applications = Application.findAllByMoveBundleIsNullAndOwner(project.client)
		}

		for (application in applications) {
			result.add(id: application.id, name: application.assetName)
		}

		return result
	}

	/**
	 * Used to get the model map used to render the show view of an Application domain asset
	 * @param project - the project of the user
	 * @param app - the instance of the application to pull attributes from
	 * @param params - request parameters
	 * @return a map of the properties
	 */
	Map getModelForShow(Project ignored, Application app, Map params) {
		Project project = app.project

		def appMoveEvent = AppMoveEvent.findAllByApplication(app)
		def appMoveEventlist = AppMoveEvent.findAllByApplication(app)?.value
		def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])

		def shutdownBy = app.shutdownBy  ? assetEntityService.resolveByName(app.shutdownBy) : ''
		def startupBy = app.startupBy  ? assetEntityService.resolveByName(app.startupBy) : ''
		def testingBy = app.testingBy  ? assetEntityService.resolveByName(app.testingBy) : ''

		def shutdownById = shutdownBy instanceof Person ? shutdownBy.id : -1
		def startupById = startupBy instanceof Person ? startupBy.id : -1
		def testingById = testingBy instanceof Person ? testingBy.id : -1

		def personList = partyRelationshipService.getProjectApplicationStaff(project)
		def availableRoles = partyRelationshipService.getStaffingRoles()
		def partyGroupList = partyRelationshipService.getCompaniesList()
		Map commonModel = this.getCommonModel(false, project, app, params)

		return [applicationInstance: app, appMoveEvent: appMoveEvent, appMoveEventlist: appMoveEventlist,
		        moveEventList: moveEventList, shutdownBy: shutdownBy, startupBy: startupBy, testingBy: testingBy,
		        shutdownById: shutdownById, startupById: startupById, testingById: testingById,
				availableRoles: availableRoles, partyGroupList: partyGroupList, staffTypes: Person.constraints.staffType.inList,
		        personList: personList, currentUserId: securityService.currentPersonId] +
				commonModel

	}

	/**
	 * Used to get the model map used to render the create view of an Application domain asset
	 * @param project - the project of the user
	 * @param params - request parameters
	 * @return a map of the properties
	 */
	Map getModelForCreate(Map params=null) {
		Project project = securityService.getUserCurrentProject()
		Application application = new Application(project: project)
		List moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])

		List personList = partyRelationshipService.getProjectApplicationStaff(project)
		List availableRoles = partyRelationshipService.getStaffingRoles()
		List partyGroupList = partyRelationshipService.getCompaniesList()
		Map commonModel = this.getCommonModel(true, project, application, params)

		return [
			assetInstance: application,
			moveEventList: moveEventList,
			availableRoles: availableRoles,
			partyGroupList: partyGroupList,
			staffTypes: Person.constraints.staffType.inList,
			personList: personList
		] + commonModel
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param project - the user's selected project
	 * @param params - the request parameters
	 */
	@Transactional
	Application saveAssetFromForm(Project project, Map params) {
		saveUpdateAssetFromForm(project, params, new Application())
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param project - the user's selected project
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 */
	Application updateAssetFromForm(Project project, Map params, Long id) {
		def asset
		if (id) {
			asset = Application.get(id)
		} else {
			throw new RuntimeException("updateAssetFromForm() was missing required id parameter")
		}
		if (! asset) {
			throw new DomainUpdateException("updateAssetFromForm() unable to locate asset id $id")
		}

		return saveUpdateAssetFromForm(project, params, asset)
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param project  the user's selected project
	 * @param params - the request parameters
	 * @param asset - the asset object to update
	 */
	private Application saveUpdateAssetFromForm(Project project, Map params, Application asset) {
		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			// Removed by TM-6779 - this is not being used since field specs fields implementation
			// asset.attributeSet = EavAttributeSet.get(2)	// Application attributeSet
			asset.assetClass = AssetClass.APPLICATION
			asset.assetType = AssetType.APPLICATION

			// asset.createdBy = userLogin.person
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $id not belonging to current project $project.id")
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		asset.sme = null
		asset.sme2 = null
		asset.appOwner = null

		asset.properties = params

		asset.shutdownFixed = params.shutdownFixed ?  1 : 0
		asset.startupFixed = params.startupFixed ?  1 : 0
		asset.testingFixed = params.testingFixed ?  1 : 0

		asset.modifiedBy = securityService.loadCurrentPerson()

		assetEntityService.assignAssetToBundle(project, asset, (String) params['moveBundle.id'])

		assetEntityService.createOrUpdateAssetEntityAndDependencies(asset.project, asset, params)

		// Save which events that an application might be approprate
		/*
		def appMoveEventList = AppMoveEvent.findAllByApplication(asset)?.moveEvent?.id
		if (appMoveEventList.size()>0) {
			for (int i : appMoveEventList) {
				def okToMove = params["okToMove_"+i]
				def appMoveInstance = AppMoveEvent.findByMoveEventAndApplication(MoveEvent.get(i),asset)
				    appMoveInstance.value = okToMove
				    appMoveInstance.save(flush:true)
			}
		}
		*/

		return asset
	}

	/**
	 * This method deletes an application and all related data.
	 * @param application
	 */
	void deleteApplication(Application application) {
		assetEntityService.deleteAsset(application)
	}

	/**
	 * Used to get the model map used to render the create view of an Application domain asset
	 * @param forCreate - is model for create or show/edit
	 * @param project - the project of the user
	 * @param application - current application
	 * @param params - request parameters
	 * @return a map of the properties containing the list values to populate the list controls
	 */
	private Map getCommonModel(Boolean forCreate, Project project, Application application, Map params) {
		Map commonModel = assetEntityService.getCommonModel(forCreate, project, application, 'Application', params)
		commonModel.standardFieldSpecs.criticality.constraints.put('values', Application.CRITICALITY)
		return commonModel
	}
}
