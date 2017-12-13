import com.tdsops.common.exceptions.InvalidLicenseException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException

class UrlMappings {

	static mappings = {

		/**
		 * View Controller
		 */
		"/task/userTask/$id" {
			controller = "task"
			action = [GET:"userTask"]
		}

		/*
		TM-5299 Commenting out the line below because it's interfering
		with requests that pass a 'format' parameter.
		*/
		// "/$controller/$action?/$id?(.$format)?" {
		"/$controller/$action?/$id?" {}

		/**
		 * Web Service Controller
		 */
		"/ws/application/listInBundle/$id" {
			controller = "wsApplication"
			action = [GET:"listInBundle"]
		}

		/********************************************************
		 * Asset WS
		 ********************************************************/

		"/ws/asset/checkForUniqueName" {
			controller = "wsAsset"
			action = [
			        POST:"checkForUniqueName"
			]
		}

		"/ws/asset/clone" {
			controller = "wsAsset"
			action = [
					POST:"clone"
			]
		}

		"/ws/asset/$id" {
			controller = "wsAsset"
			action = [
					GET:"getAsset"
			]
		}

		"/ws/asset/dependencies" {
			controller = "wsAsset"
			action = [
					POST:"getAssetDependencies",
					PUT:"updateCommonAssetDependencyFields",
					DELETE:"deleteAssetDependency",
			]
		}

		"/ws/apiAction/$id" {
			controller = "wsApiAction"
			action = [
			        DELETE: "delete",
					GET: "fetch"
			]
		}

		"/ws/apiAction/" {
			controller = "wsApiAction"
			action = [
					GET: "list"
			]
		}

		"/ws/asset/showTemplate" (controller:'wsAsset', action:'getTemplate', method:'GET') {
			mode = 'show'
		}

		"/ws/asset/showTemplate/$id" (controller:'wsAsset', action:'getTemplate', method:'GET') {
			mode = 'show'
		}

		"/ws/asset/editTemplate/$id" (controller:'wsAsset', action:'getTemplate', method:'GET') {
			mode = 'edit'
		}

		"/ws/asset/showModel/$id" (controller:'wsAsset', action:'getModel', method:'GET') {
			mode = 'show'
		}

		"/ws/asset/editModel/$id" (controller:'wsAsset', action:'getModel', method:'GET') {
			mode = 'edit'
		}

		"/ws/asset/deleteAssets" {
			controller = "wsAsset"
			action = [
			        POST: "deleteAssets"
			]
		}		
		/******************************************************/

		"/ws/moveEventNews/$id?" {
			controller = "moveEventNews"
			action = [GET:"list", PUT:"update", DELETE:"delete", POST:"save"]
		}

		"/ws/dashboard/bundleData/$id?" {
			controller = "wsDashboard"
			action = [GET:"bundleData"]
		}

		"/ws/cookbook/recipe/list" {
			controller = "wsCookbook"
			action = [GET:"recipeList"]
		}

		"/ws/cookbook/recipeVersion/list/$id" {
			controller = "wsCookbook"
			action = [GET:"recipeVersionList"]
		}

		"/ws/cookbook/recipe/revert/$id" {
			controller = "wsCookbook"
			action = [POST:"revert"]
		}

		"/ws/cookbook/recipe/archive/$id" {
			controller = "wsCookbook"
			action = [POST:"archiveRecipe"]
		}

		"/ws/cookbook/recipe/unarchive/$id" {
			controller = "wsCookbook"
			action = [POST:"unarchiveRecipe"]
		}

		"/ws/cookbook/recipe/$id/$version?" {
			controller = "wsCookbook"
			action = [GET:"recipe", POST:"saveRecipeVersion", PUT:"updateRecipe", DELETE:"deleteRecipeOrVersion"]
		}

		"/ws/cookbook/recipe/release/$recipeId" {
			controller = "wsCookbook"
			action = [POST:"releaseRecipe"]
		}

		"/ws/cookbook/recipe/" {
			controller = "wsCookbook"
			action = [POST:"createRecipe"]
		}

		"/ws/cookbook/recipe/validateSyntax" {
			controller = "wsCookbook"
			action = [POST:"validateSyntax"]
		}

		"/ws/cookbook/recipe/clone" {
			controller = "wsCookbook"
			action = [POST:"cloneRecipe"]
		}

		"/ws/cookbook/recipe/context/$recipeId" {
			controller = "wsCookbook"
			action = [POST:"defineRecipeContext", DELETE: "deleteRecipeContext"]
		}

		"/ws/cookbook/groups" {
			controller = "wsCookbook"
			action = [POST:"groups"]
		}

		name wsDepAnalyzer:
		"/ws/depAnalyzer/$action?/$id?" {
			controller = 'wsDepAnalyzer'
		}

		"/ws/event/listBundles/$id" {
			controller = "wsEvent"
			action = [GET:"listBundles"]
		}


		/***************************/

		"/ws/assetImport/invokeFetchAction" {
			controller = 'wsAssetImport'
			action = [POST: 'invokeFetchAction']
		}

		"/ws/assetImport/loadData" {
			controller = 'wsAssetImport'
			action = [POST: 'loadData']
		}

		"/ws/assetImport/manualFormOptions" {
			controller = 'wsAssetImport'
			action = [GET: 'manualFormOptions']
		}

		"/ws/assetImport/transformData" {
			controller = 'wsAssetImport'
			action = [POST: 'transformData']
		}

		"/ws/assetImport/viewData" {
			controller = 'wsAssetImport'
			action = [GET: 'viewData']
		}

		/***************************/

		"/ws/event/listEventsAndBundles" {
			controller = "wsEvent"
			action = [GET:"listEventsAndBundles"]
		}

		//Named link to render the actual URLMapping
		name qzSignLink: "/ws/task/qzsign" {
			controller = "wsTask"
			action = [GET:"qzSignMessage", POST:"qzSignMessage"]
		}

		"/ws/task/taskCreateDefaults" {
			controller = "wsTask"
			action = [GET:"taskCreateDefaults"]
		}

		"/ws/qzCertificate" {
			controller = 'wsApplication'
			action = [GET:'qzCertificate']
		}

		"/ws/task/generateTasks" {
			controller = "wsTask"
			action = [POST:"generateTasks"]
		}

		"/ws/task/findTaskBatchByRecipeAndContext" {
			controller = "wsTask"
			action = [GET:"findTaskBatchByRecipeAndContext"]
		}

		"/ws/task/listTaskBatches" {
			controller = "wsTask"
			action = [GET:"listTaskBatches"]
		}

		"/ws/task/$id" {
			controller = "wsTask"
			action = [GET: "retrieveTaskBatch", DELETE:"deleteBatch"]
		}

		"/ws/task/$id/publish" {
			controller = "wsTask"
			action = [POST:"publish"]
		}

		"/ws/task/$id/unpublish" {
			controller = "wsTask"
			action = [POST:"unpublish"]
		}

		"/ws/task/$id/taskReset" {
			controller = "wsTask"
			action = [POST:"taskReset"]
		}

		"/ws/task/$id/tasks" {
			controller = "wsTask"
			action = [GET:"retrieveTasksOfTaskBatch"]
		}

		"/ws/task/$id/invokeAction" {
			controller = "wsTask"
			action = [POST:"invokeAction"]
		}

		"/ws/task/$id/resetAction" {
			controller = "wsTask"
			action = [POST:"resetAction"]
		}

		"/ws/progress/$id" {
			controller = "wsProgress"
			action = [GET:"retrieveStatus"]
		}

		"/ws/progress/demo" {
			controller = "wsProgress"
			action = [GET:"demo"]
		}

		"/ws/progress/demo/failed" {
			controller = "wsProgress"
			action = [GET:"demoFailed"]
		}

		"/ws/user/preferences/$id" {
			controller = "wsUser"
			action = [GET:"preferences"]
		}

		"/ws/user/preference" {
			controller = "wsUser"
			action = [POST:"savePreference"]
		}

		"/ws/progress" {
			controller = "wsProgress"
			action = [GET:"list"]
		}

		"/ws/public/sequence/$contextId/$name" {
			controller = "wsSequence"
			action = [GET:"retrieveNext"]
		}

		"/ws/project/userProjects" {
			controller = "wsProject"
			action = [GET:"userProjects"]
		}

		"/ws/manufacturer/merge" {
			controller = "wsManufacturer"
			action = [POST:"merge"]
		}

		"/maint/backd00r" {
			controller = "auth"
			action = [GET:"maintMode"]
		}

		"/auth/resetPassword/$token" {
			controller = "auth"
			action = [GET:"resetPassword"]
		}

		"/ws/admin/unlockAccount" {
			controller = "wsAdmin"
			action = [PUT:"unlockAccount"]
		}

		"/admin/restartAppService" {
			controller = "admin"
			action = [
				GET: "restartAppServiceForm",
				POST:"restartAppServiceAction"
			]
		}

		///// NOTICES API /////////
		"/ws/notices/$id" {
			controller = "wsNotice"
			action = [
					GET:  "fetchById",
					DELETE: "delete",
					PUT: "update"
			]
		}

		"/ws/notices" {
			controller = "wsNotice"
			action = [
					GET:  "fetch",
					POST: "create"
			]
		}

		"/ws/notices/$id/ack" {
			controller = "wsNotice"
			action = [
					POST: "ack"
			]
		}

		///// LICENSES Admin API (Client Side) /////////
		"/ws/license" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "getLicenses"
			]
		}

		"/ws/license/$id" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "getLicense",
					DELETE: "deleteLicense"
			]
		}

		//Gets que request Hash  --- OLB 161207 Change Hash to request...
		"/ws/license/$id/hash" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "getLicenseRequestHash"
			]
		}

		//Requests
		"/ws/license/request" {
			controller = "wsLicenseAdmin"
			action = [
			        POST: "generateRequest"
			]
		}

		//Resubmit License Request
		"/ws/license/$id/email/request" {
			controller = "wsLicenseAdmin"
			action = [
					GET:  "emailRequestData",
					POST: "emailRequest"
			]
		}

		//Load License
		"/ws/license/$id/load" {
			controller = "wsLicenseAdmin"
			action = [
					POST: "loadLicense"
			]
		}

		///// LICENSES Manager API (BackOffice) /////////

		// TODO: OLB 20170124:Can we add Conditional logic to disable the Manager entry points when is not a manager???

		//load a request HASH from a client, returns JSON object
		"/ws/manager/license/request" {
			controller = "wsLicenseManager"
			action = [
					POST: "loadRequest"
			]
		}

		//load a request HASH from a client, returns JSON object
		"/ws/manager/license" {
			controller = "wsLicenseManager"
			action = [
					GET: "getLicenses"
			]
		}

		"/ws/manager/license/$id" {
			controller = "wsLicenseManager"
			action = [
					GET: "getLicense",
					PUT: "updateLicense",
					DELETE: "revokeLicense"
			]
		}

		"/ws/manager/license/$id/key" {
			controller = "wsLicenseManager"
			action = [
					GET: "getLicenseKey"
			]
		}

		"/ws/manager/license/${id}/delete" {
			controller = "wsLicenseManager"
			action = [
					DELETE: "deleteLicense"
			]
		}

		"/ws/manager/license/${id}/activate" {
			controller = "wsLicenseManager"
			action = [
					POST: "activateLicense"
			]
		}

		"/ws/manager/license/${id}/activitylog" {
			controller = "wsLicenseManager"
			action = [
					GET: "activityLog"
			]
		}

		//Email License
		"/ws/manager/license/$id/email/send" {
			controller = "wsLicenseManager"
			action = [
					POST: "emailLicense"
			]
		}

		"/ws/manager/status" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "managerActive"
			]
		}

		///// LICENSES Common API /////////
		"/ws/license/environment" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "fetchEnvironments"
			]
		}

		"/ws/license/type" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "fetchTypes"
			]
		}

		"/ws/license/status" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "fetchStatus"
			]
		}

		"/ws/license/method" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "fetchMethods"
			]
		}

		"/ws/license/project" {
			controller = "wsLicenseAdmin"
			action = [
					GET: "fetchProjects"
			]
		}

		"/ws/customDomain/fieldSpec/$domain" {
			controller = "wsCustomDomain"
			action = [
					GET: "getFieldSpec",
					POST: "saveFieldSpec"
			]
		}

		"/ws/customDomain/distinctValues/$id" {
			controller = "wsCustomDomain"
			action = [
					POST: "distinctValues"
			]
		}

		"/ws/customDomain/fieldSpecsWithCommon" {
			controller = "wsCustomDomain"
			action = [
				GET: "fieldSpecsWithCommon"
			]
		}

		"/ws/security/permissions" {
			controller = "wsSecurity"
			action = [
			        GET: "permissions"
			]
		}

		"/ws/assetExplorer/views" {
			controller = "wsAssetExplorer"
			action = [
					GET: "listDataviews"
			]
		}

		"/ws/assetExplorer/view/$id?" {
			controller = "wsAssetExplorer"
			action = [
					GET: "getDataview",
					POST: "createDataview",
					PUT: "updateDataview",
					DELETE: "deleteDataview"
			]
		}

		"/ws/assetExplorer/previewQuery" {
			controller = "wsAssetExplorer"
			action = [
					POST: "previewQuery"
			]
		}

		"/ws/assetExplorer/query/$id" {
			controller = "wsAssetExplorer"
			action = [
					POST: "query"
			]
		}

		"/ws/assetExplorer/favoriteDataviews" {
			controller = "wsAssetExplorer"
			action = [
					GET: "favoriteDataviews"
			]
		}

		"/ws/assetExplorer/favoriteDataview/$id?" {
			controller = "wsAssetExplorer"
			action = [
					POST: "addFavoriteDataview",
					DELETE: "deleteFavoriteDataview"
			]
		}

		"/ws/dataingestion/datascript/list" {
			controller = "wsDataScript"
			action = [
			        GET: "getDataScripts"
			]
		}

		"/ws/dataingestion/datascript/" {
			controller = "wsDataScript"
			action = [
					POST: "createDataScript"
			]
		}

		"/ws/dataingestion/datascript/$id?" {
			controller = "wsDataScript"
			action = [
					GET: "getDataScript",
			        PUT: "updateDataScript",
					DELETE: "deleteDataScript"
			]
		}

		"/ws/dataingestion/datascript/validateunique/$name" {
			controller = "wsDataScript"
			action = [
					POST: "validateUniqueName"
			]
		}

		"/ws/dataingestion/provider/list" {
			controller = "wsProvider"
			action = [
					GET: "getProviders"
			]
		}

		"/ws/dataingestion/provider/" {
			controller = "wsProvider"
			action = [
					POST: "createProvider"
			]
		}

		"/ws/dataingestion/provider/$id?" {
			controller = "wsProvider"
			action = [
					GET: "getProvider",
					PUT: "updateProvider",
					DELETE: "deleteProvider"
			]
		}

		"/ws/dataingestion/provider/validateunique/$name" {
			controller = "wsProvider"
			action = [
					POST: "validateUniqueName"
			]
		}

		// Angular 1.5
		"/app/**/*" ( controller: 'app', action: 'index' )
		// Angular 2 and future latest version
		"/module/" ( controller: 'singleApp', action: 'index' )
		"/module/**/*" ( controller: 'singleApp', action: 'index' )

		// Angular Single Page App Named mappings
		name assetViewShow: "/module/asset/views/$id/show" {
			controller = 'singleApp'
			action = 'index'
		}

		//ROOT map to the auth/index action
		"/" (controller: "auth")

		// REST API
		"/api/projects/heartbeat"(controller: 'project', action: 'heartbeat', namespace:"v1", method: "GET")

		"/api/${controller}s"(version: "1.0", namespace: "v1", method: "GET")
		"/api/${controller}s/$id(.$format)?"(version: "1.0", action: "show", namespace:"v1", method: "GET")
		"/api/${controller}s/$id(.$format)?"(action: "delete", version: "1.0", namespace:"v1", method: "DELETE")
		"/api/${controller}s/$id(.$format)?"(action: "update", version: "1.0", namespace:"v1", method: "PUT")
		"/api/${controller}s(.$format)?"(action: "save", version: "1.0", namespace:"v1", method: "POST")

		"/api/${controller}s"(version: "2.0", namespace: "v2", method: "GET")
		"/api/${controller}s/$id(.$format)?"(version: "2.0", action: "show", namespace: "v2", method: "GET")
		"/api/${controller}s/$id(.$format)?"(action: "delete", version: "2.0", namespace: "v2", method: "DELETE")
		"/api/${controller}s/$id(.$format)?"(action: "update", version: "2.0", namespace: "v2", method: "PUT")
		"/api/${controller}s(.$format)?"(action: "save", version: "2.0", namespace: "v2", method: "POST")
		// End: REST API

		// Various error pages
		"401" ( controller: 'errorHandler', action: 'unauthorized' )
		"403" ( controller: 'errorHandler', action: 'forbidden' )
		"404" ( controller: 'errorHandler', action: 'notFound' )
		"500" ( controller: 'errorHandler', action: 'notFound', exception: NotFoundException)
		"500" ( controller: 'errorHandler', action: 'forbidden', exception: AccessDeniedException)
		"500" ( controller: 'errorHandler', action: 'licensing', exception: InvalidLicenseException)
		"500" ( controller: 'errorHandler', action: 'error' )

	}
}
