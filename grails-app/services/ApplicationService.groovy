import com.tds.asset.Application
import com.tdssrc.eav.EavAttributeSet
//import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.AssetType


/**
 * The application service handles the logic for CRUD applications
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ApplicationService {

	boolean transactional = true
	
	def assetEntityService

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned
	 * applications for the user's current project
	 * 
	 * @param bundleId the id of the bundle
	 * @param user the current user
	 * @param currentProject the current project
	 * @return the list of applications associated with the bundle
	 */
	def listInBundle(bundleId, user, currentProject) {
		if (currentProject == null) {
			log.info('Current project is null')
			throw new EmptyResultException()
		}
		
		if (bundleId != null && !bundleId.isNumber()) {
			throw new IllegalArgumentException('Not a valid number')
		}
		
		bundleId = bundleId.toInteger()
		def mb = null
		
		if (bundleId > 0) {
			mb = MoveBundle.get(bundleId)
			if (mb != null) {
				if (!mb.project.equals(currentProject)) {
					throw new IllegalArgumentException('The current project and the Move event project doesn\'t match')
				}
			} else {
				log.info('Move bundle is null')
				throw new EmptyResultException()
			}
		}
		
		def result = []
		def applications = []
		
		if (mb != null) {
			applications = Application.findAllByMoveBundle(mb)	
		} else {
			applications = Application.findAllByMoveBundleIsNullAndOwner(currentProject.client)
		}
		
		for (application in applications) {
			def applicationMap = [
				'id' : application.id,
				'name' : application.assetName
			];
		
			result.add(applicationMap)
		}
		
		return result
	}


	/**
	 * Used to get the model map used to render the show view of an Application domain asset
	 * @param project - the project of the user
	 * @param app - the instance of the application to pull attributes from
	 * @param params - parameters from the controller
	 * @return a map of the properties
	 */
	Map getModelForShow(Project, app, params) {
			Project project = app.project

			def appMoveEvent = AppMoveEvent.findAllByApplication(app)
			def appMoveEventlist = AppMoveEvent.findAllByApplication(app)?.value
			def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])

			def shutdownBy = app.shutdownBy  ? assetEntityService.resolveByName(app.shutdownBy) : ''
			def startupBy = app.startupBy  ? assetEntityService.resolveByName(app.startupBy) : ''
			def testingBy = app.testingBy  ? assetEntityService.resolveByName(app.testingBy) : ''

			def model = [
				applicationInstance : app,
				appMoveEvent:appMoveEvent, 
				appMoveEventlist:appMoveEventlist, 
				moveEventList:moveEventList, 
				shutdownBy:shutdownBy, 
				startupBy:startupBy, 
				testingBy:testingBy
			]

			model.putAll( assetEntityService.getCommonModelForShows('Application', project, params, app) )

			return model
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the http seesion object
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Application saveAssetFromForm(controller, session, Long projectId, Long userId, params ) {
		Application asset = new Application()
		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}		

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Application updateAssetFromForm(controller, session, Long projectId, Long userId, params, Long id ) {
		def asset
		if (id) {
			asset = Application.get(id)
		} else {
			throw new RuntimeException("updateAssetFromForm() was missing required id parameter")
		}
		if (! asset) {
			throw new DomainUpdateException("updateAssetFromForm() unable to locate asset id $id")
		}

		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @param asset - the asset object to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	private Application saveUpdateAssetFromForm(controller, session, Long projectId, Long userId, params, Application asset ) {
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userId)

		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			asset.attributeSet = EavAttributeSet.get(2)	// Application attributeSet
			asset.assetClass = AssetClass.APPLICATION
			asset.assetType = AssetType.APPLICATION

			// asset.createdBy = userLogin.person
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $id not belonging to current project $project.id", userLogin)
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}		

		asset.sme = null
		asset.sme2 = null
		asset.appOwner = null
		
		asset.properties = params

		asset.shutdownFixed = params.shutdownFixed ?  1 : 0
		asset.startupFixed = params.startupFixed ?  1 : 0
		asset.testingFixed = params.testingFixed ?  1 : 0
		
		// Save who changed it
		asset.modifiedBy = userLogin.person

		assetEntityService.assignAssetToBundle(project, asset, params['moveBundle.id'])

		if (! asset.validate() || ! asset.save(flush:true)) {
			log.debug "Unable to update device ${GormUtil.allErrorsString(asset)}"
			throw new DomainUpdateException("Unable to update asset ${GormUtil.allErrorsString(asset)}".toString())
		}

		def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, asset, userLogin, asset.project)
		if (errors) {
			throw new DomainUpdateException("Unable to update dependencies : $errors".toString())
		}

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

}
