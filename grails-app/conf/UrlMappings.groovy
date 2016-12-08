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

		"/ws/event/listBundles/$id" {
			controller = "wsEvent"
			action = [GET:"listBundles"]
		}

		"/ws/event/listEventsAndBundles" {
			controller = "wsEvent"
			action = [GET:"listEventsAndBundles"]
		}

		//Named link to render the actual URLMapping
		name qzSignLink: "/ws/task/qzsign" {
			controller = "wsTask"
			action = [GET:"qzSignMessage", POST:"qzSignMessage"]
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
			controller = "wsLicense"
			action = [
					GET: "getLicenses"
			]
		}

		"/ws/license/$id" {
			controller = "wsLicense"
			action = [
					GET: "getLicense",
					DELETE: "deleteLicense"
			]
		}

		//Gets que request Hash  --- OLB 161207 Change Hash to request...
		"/ws/license/$id/hash" {
			controller = "wsLicense"
			action = [
					GET: "getLicenseRequestHash"
			]
		}


		//Requests
		"/ws/license/request" {
			controller = "wsLicense"
			action = [
			        POST: "generateRequest"
			]
		}

		//Load License
		"/ws/license/$id/load" {
			controller = "wsLicense"
			action = [
					POST: "loadLicense"
			]
		}



		///// LICENSES Manager API (BackOffice) /////////

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
					DELETE: "deleteLicense"
			]
		}

		"/ws/manager/license/$id/key" {
			controller = "wsLicenseManager"
			action = [
					GET: "getLicenseKey"
			]
		}

		///// LICENSES Common API /////////

		"/ws/license/environment" {
			controller = "wsLicense"
			action = [
					GET: "fetchEnvironments"
			]
		}

		"/ws/license/type" {
			controller = "wsLicense"
			action = [
					GET: "fetchTypes"
			]
		}

		"/ws/license/status" {
			controller = "wsLicense"
			action = [
					GET: "fetchStatus"
			]
		}

		"/ws/license/method" {
			controller = "wsLicense"
			action = [
					GET: "fetchMethods"
			]
		}

		"/ws/license/project" {
			controller = "wsLicense"
			action = [
					GET: "fetchProjects"
			]
		}

		///// LICENSES Admin API (Client Side) /////////


		//ROOT map to the auth/index action
		"/"(controller: "auth")

		"500"(view:'/error')
		"401"(view:'/unauthorized')
	}
}
