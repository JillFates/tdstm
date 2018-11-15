class UrlMappings {

	static mappings = {

		/**
		 * View Controller
		 */
		"/task/userTask/$id" {
			controller = "task"
			action = [GET:"userTask"]
		}

		/**
		 * TM-8842  Dependency Analyzer drill-in from Asset Show Details
		 */
		name dependencyConsoleMap: "/moveBundle/dependencyConsole/$subsection?/$groupId?" {
			controller = "moveBundle"
			action = [GET:"dependencyConsole",
					  POST:"dependencyConsole"]
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

		"/ws/asset" {
			controller = "wsAsset"
			action = [
			    POST: "saveAsset"
			]
		}

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
					GET:"getAsset",
					PUT: "updateAsset"
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
					GET: "fetch",
					PUT: "update"
			]
		}

		"/ws/apiAction" {
			controller = "wsApiAction"
			action = [
					GET: "list",
					POST: "create"
			]
		}

		"/ws/apiAction/enums" {
			controller = "wsApiAction"
			action = [
					GET: "enums"
			]
		}

		"/ws/asset/retrieveBundleChange" {
			controller = 'wsAsset'
			action = [
			    POST: 'retrieveBundleChange'
			]
		}

		"/ws/asset/retrieveChassisSelectOptions/$id" {
			controller = 'wsAsset'
			action = [
			    GET: 'retrieveChassisSelectOptions'
			]
		}

		"/ws/asset/retrieveRackSelectOptions/$id" {
			controller = 'wsAsset'
			action = [
				GET: 'retrieveRackSelectOptions'
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

		"/ws/asset/createTemplate/$domainName" (controller:'wsAsset', action:'getCreateTemplate', method:'GET')

		"/ws/asset/showModel/$id" (controller:'wsAsset', action:'getModel', method:'GET') {
			mode = 'show'
		}

		"/ws/asset/editModel/$id" (controller:'wsAsset', action:'getModel', method:'GET') {
			mode = 'edit'
		}

		"/ws/asset/defaultCreateModel/$assetClass" (controller:'wsAsset', action:'getDefaultCreateModel', method:'GET')

		"/ws/asset/deleteAssets" {
			controller = "wsAsset"
			action = [
			        POST: "deleteAssets"
			]
		}

		"/ws/asset/classOptions" {
			controller = "wsAsset"
			action = [
			    GET: "retrieveAssetClassOptions"
			]
		}

		"/ws/task/assetCommentCategories" {
			controller = "wsTask"
			action = [
			    GET : 'assetCommentCategories'
			]
		}


		'/ws/bulkChange' {
			controller = 'wsBulkAssetChange'
			action = [
			    PUT:'change'
			]
		}

		'/ws/bulkChange/fields' {
			controller = 'wsBulkAssetChange'
			action = [
				GET: 'fields'
			]
		}

		'/ws/bulkChange/actions' {
			controller = 'wsBulkAssetChange'
			action = [
				GET: 'actions'
			]
		}
		/******************************************************/

		"/ws/moveEvent/list" {
			controller = "wsEvent"
			action = [GET: "listEvents"]
		}

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

		name wsDepAnalyzer: "/ws/depAnalyzer/peopleAssociatedToDepGroup/$id?" {
			controller = 'wsDepAnalyzer'
			action = [GET: ' peopleAssociatedToDepGroup']
		}

		"/ws/depAnalyzer/filteredAssetList/$id?" {
			controller = 'wsDepAnalyzer'
			action = [POST: ' filteredAssetList']
		}

		"/ws/event/listBundles/$id?" {
			controller = "wsEvent"
			action = [GET:"listBundles"]
		}


		/***************************/

		"/ws/assetImport/invokeFetchAction/$id" {
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

		"/ws/assetImport/initiateTransformData" {
			controller = 'wsAssetImport'
			action = [POST: 'initiateTransformData']
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

		/**
		 * AssetComment CRUD endpoints
		 */

		"/ws/task/comment/$id" {
			controller = "wsTask"
			action = [
				DELETE:'deleteComment',
				PUT: 'updateComment'
			]
		}

		"/ws/task/comment" {
			controller = 'wsTask'
			action = [
			    POST: 'saveComment'
			]
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
			action = [
					GET:"retrieveStatus",
					DELETE: "interruptJob"
			]
		}

		"/ws/progress/demo" {
			controller = "wsProgress"
			action = [GET:"demo"]
		}

		"/ws/progress/demo/failed" {
			controller = "wsProgress"
			action = [GET:"demoFailed"]
		}

		"/ws/user" {
			controller = "wsUser"
			action = [GET: "getUser"]
		}

		"/ws/user/preferencesForEdit" {
			controller = "wsUser"
			action = [ GET: "preferencesForEdit" ]
		}

		"/ws/user/resetPreferences" {
			controller = "wsUser"
			action = [ DELETE: "resetPreferences" ]
		}

		"/ws/user/preferences/$id?" {
			controller = "wsUser"
			action = [	GET:"preferences",
						DELETE: "removePreference"]
		}

		"/ws/user/preference/$id?" {
			controller = "wsUser"
			action = [	GET:"preferences",
						POST:"savePreference",
						DELETE: "removePreference"]
		}

		"/ws/user/person" {
			controller = "wsUser"
			action = [GET:"getPerson"]
		}

		"/ws/progress" {
			controller = "wsProgress"
			action = [GET:"list"]
		}

		"/ws/public/sequence/$contextId/$name" {
			controller = "wsSequence"
			action = [GET:"retrieveNext"]
		}

		"/ws/projects" {
			controller = "wsProject"
			action = [GET: "projects"]
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

		name adminPortal: "/admin/home" {
			controller = 'admin'
			action = [
				GET: "home"
			]
		}

		"/admin/restart" {
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

		"/ws/fileSystem/uploadText" {
			controller = "wsFileSystem"
			action = [
			        POST: "uploadText"
			]
		}

		"/ws/fileSystem/uploadTextETLDesigner" {
			controller = "wsFileSystem"
			action = [
					  POST: "uploadTextETLDesigner"
			]
		}

		"/ws/fileSystem/uploadTextETLAssetImport" {
			controller = "wsFileSystem"
			action = [
					  POST: "uploadTextETLAssetImport"
			]
		}

		"/ws/fileSystem/uploadFile" {
			controller = "wsFileSystem"
			action = [
					POST: "uploadFile"
			]
		}

		"/ws/fileSystem/uploadFileETLDesigner" {
			controller = "wsFileSystem"
			action = [
					  POST: "uploadFileETLDesigner"
			]
		}

		"/ws/fileSystem/uploadFileETLAssetImport" {
			controller = "wsFileSystem"
			action = [
					  POST: "uploadFileETLAssetImport"
			]
		}

		"/ws/fileSystem/delete" {
			controller = "wsFileSystem"
			action = [
					DELETE: "deleteFile"
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

		"/ws/assetExplorer/validateUnique" {
			controller = "wsAssetExplorer"
			action = [
							POST: "validateUniqueName",
			]
		}

		"/ws/dataingestion/datascript/list" {
			controller = "wsDataScript"
			action = [
			        GET: "list"
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

		/*
		 * Get the sample filename related to a DataScript Object
		 */
		"/ws/dataingestion/datascript/$id/sampleData/$filename" {
			controller = "wsDataScript"
			action = [
					  GET: "sampleData"
			]
		}

		"/ws/dataingestion/datascript/validateDelete/$id?" {
			controller = "wsDataScript"
			action = [
					GET: "validateDelete",
			]
		}

		"/ws/dataingestion/datascript/validateUnique" {
			controller = "wsDataScript"
			action = [
					POST: "validateUniqueName"
			]
		}

		"/ws/dataScript/sampleData/$filename" {
			controller = "wsDataScript"
			action = [
					  GET: "sampleData"
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

		"/ws/dataingestion/provider/validateUnique/$name" {
			controller = "wsProvider"
			action = [
					POST: "validateUniqueName"
			]
		}

		//
		// ApiAction
		//
		"/ws/apiAction/connector/$id" {
			controller = "wsApiAction"
			action = [
			        GET: "connectorDictionary"
			]
		}

		"/ws/apiAction/validateSyntax" {
			controller = "wsApiAction"
			action = [
					POST: "validateSyntax"
			]
		}

		"/ws/apiAction/fields" {
			controller = "wsApiAction"
			action = [
					GET: "domainFields"
			]
		}

		//
		// DataScript
		//
		"/ws/dataScript/initiateTestScript" {
			controller = "wsDataScript"
			action = [
					POST: "initiateTestScript"
			]
		}

		"/ws/dataScript/checkSyntax" {
			controller = "wsDataScript"
			action = [
					POST: "checkSyntax"
			]
		}

		"/ws/dataScript/saveScript" {
			controller = "wsDataScript"
			action = [
					// TODO : This should be a PUT
					POST: "saveScript"
			]
		}

		//
		// Filename
		//
		"/ws/filename" {
			controller = "wsAsset"
			action = [
					POST: "exportFilename"
			]
		}

		//
		// Import Batch Actions
		//

		// List all Import Batches | Bulk Delete Import Batches
		"/ws/import/batches" {
			controller = "wsImportBatch"
			action = [
					GET: "listImportBatches",
					DELETE: "bulkDeleteImportBatches",
					PATCH: "patchActionOnBatches"
			]
		}

		// Get/Delete on a single ImportBatch
		"/ws/import/batch/$id" {
			controller = "wsImportBatch"
			action = [
					GET: "fetchImportBatch",
					DELETE: "deleteImportBatch",
					PUT: "updateImportBatch"
			]
		}

		// Get list of Batch Detail Records for a single batch
		"/ws/import/batch/$id/records" {
			controller = "wsImportBatch"
			action = [
					GET: "listBatchRecords",
					PATCH: "patchActionOnBatchRecords"
			]
		}

		// Dealing with individual ImportBatchRecord detail rows
		"/ws/import/batch/$id/record/$recordId" {
			controller = "wsImportBatch"
			action = [
					GET: "fetchImportBatchRecord",
					PUT: "updateImportBatchRecord"
			]
		}
		// Retrieve info of the Import Batch
		"/ws/import/batch/$id/$info" {
			controller = "wsImportBatch"
			action = [
				GET: "getInfoOfBatch"
			]
		}

		//
		// Credentials
		//
		"/ws/credential/$id" {
			controller = "wsCredential"
			action = [
				GET: "fetch",
				PUT: "update",
				DELETE: "delete"
			]
		}

		"/ws/credential" {
			controller = "wsCredential"
			action = [
				GET: "list",
				POST: "create"
			]
		}

		"/ws/credential/enums" {
			controller = "wsCredential"
			action = [
				GET: "enums"
			]
		}

		"/ws/credential/test/$id" {
			controller = "wsCredential"
			action = [
				POST: "testAuthentication"
			]
		}

		"/ws/credential/checkValidExprSyntax" {
			controller = "wsCredential"
			action = [
				POST: "checkValidExprSyntax"
			]
		}

		"/ws/tag" {
			controller = "wsTag"
			action = [
				GET : "list",
				POST: "create"
			]
		}

		"/ws/tag/search" {
			controller = "wsTag"
			action = [
				POST: "search"
			]
		}

		"/ws/tag/$id" {
			controller = "wsTag"
			action = [
				PUT : "update",
				DELETE: "delete"
			]
		}

		"/ws/tag/$targetId/merge/$sourceId" {
			controller = "wsTagAsset"
			action = [
				PUT: "merge",
			]
		}

		"/ws/tag/asset" {
			controller = "wsTagAsset"
			action = [
				POST: "create",
				DELETE: "delete"
			]
		}

		"/ws/tag/asset/$id" {
			controller = "wsTagAsset"
			action = [
				GET : "list"
			]
		}

		"/ws/tag/event" {
			controller = "wsTagEvent"
			action = [
				POST  : "create",
				DELETE: "delete"
			]
		}

		"/ws/tag/event/$id" {
			controller = "wsTagEvent"
			action = [
				GET: "list"
			]
		}

		// Angular 1.5
		"/app/**/*" ( controller: 'app', action: 'index' )
		// Angular 6 and future latest version
		"/module/" ( controller: 'singleApp', action: 'index' )
		"/module/**" ( controller: 'singleApp', action: 'index' )
		"/module/**/*" ( controller: 'singleApp', action: 'index' )

		// Angular Single Page App Named mappings
		name assetViewShow: "/module/asset/views/$id/show" {
			controller = 'singleApp'
			action = 'index'
		}

		//ROOT map to the auth/index action
		"/" (controller: "auth")

		// API via /ws/ endpoints
		"/ws/${controller}"(version: "1.0", namespace: "v1", method: "GET")
		"/ws/${controller}/$id(.$format)?"(version: "1.0", action: "show", namespace:"v1", method: "GET")
		"/ws/${controller}/$id/$action(.$format)?"(version: "1.0", namespace:"v1", method: "GET")

		// REST API
		"/api/projects/heartbeat"(controller: 'project', action: 'heartbeat', namespace:"v1", method: "GET")

		"/api/${controller}"(version: "1.0", namespace: "v1", method: "GET")
		"/api/${controller}/$id(.$format)?"(version: "1.0", action: "show", namespace:"v1", method: "GET")
		"/api/${controller}/$id/$action(.$format)?"(version: "1.0", namespace:"v1", method: "GET")

		"/api/${controller}/$id(.$format)?"(action: "delete", version: "1.0", namespace:"v1", method: "DELETE")
		"/api/${controller}/$id(.$format)?"(action: "update", version: "1.0", namespace:"v1", method: "PUT")
		"/api/${controller}(.$format)?"(action: "save", version: "1.0", namespace:"v1", method: "POST")

		"/api/${controller}"(version: "2.0", namespace: "v2", method: "GET")
		"/api/${controller}/$id(.$format)?"(version: "2.0", action: "show", namespace: "v2", method: "GET")
		"/api/${controller}/$id(.$format)?"(action: "delete", version: "2.0", namespace: "v2", method: "DELETE")
		"/api/${controller}/$id(.$format)?"(action: "update", version: "2.0", namespace: "v2", method: "PUT")
		"/api/${controller}(.$format)?"(action: "save", version: "2.0", namespace: "v2", method: "POST")
		// End: REST API

		// Various error pages
		"401" ( controller: 'errorHandler', action: 'unauthorized' )
		"403" ( controller: 'errorHandler', action: 'forbidden' )
		"404" ( controller: 'errorHandler', action: 'notFound' )
		// These were commented out as part of TM-8782 - the exceptions in ControllerMethods should catch these now
		// "500" ( controller: 'errorHandler', action: 'notFound', exception: NotFoundException)
		// "500" ( controller: 'errorHandler', action: 'forbidden', exception: AccessDeniedException)
		// "500" ( controller: 'errorHandler', action: 'licensing', exception: InvalidLicenseException)
		"500" ( controller: 'errorHandler', action: 'error' )

	}
}
