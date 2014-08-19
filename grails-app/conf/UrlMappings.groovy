class UrlMappings {
    static mappings = {
      
    	"/$controller/$action?/$id?"{
    		constraints {
			 // apply constraints here
	      	}
    	}

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
			action = [GET: "getTaskBatch", DELETE:"deleteBatch"]
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
			action = [GET:"getTasksOfTaskBatch"]
		}
		
		"/ws/progress/$id" {
			controller = "wsProgress"
			action = [GET:"get"]
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
		
		"/ws/progress" {
			controller = "wsProgress"
			action = [GET:"list"]
		}
		
		"/ws/public/sequence/$contextId/$name" {
			controller = "wsSequence"
			action = [GET:"get"]
		}

		"/ws/project/userProjects" {
			controller = "wsProject"
			action = [GET:"userProjects"]
		}

		"/maint/backd00r" {
			controller = "auth"
			action = [GET:"maintMode"]
		}
    	   	
    	
    	"500"(view:'/error')
		"401"(view:'/unauthorized')
	}
}
