class UrlMappings {
    static mappings = {
      
    	"/$controller/$action?/$id?"{
    		constraints {
			 // apply constraints here
	      	}
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

		"/ws/task/generateTasks" {
			controller = "wsTask"
			action = [POST:"generateTasks"]
		}
		
		"/ws/task/$id" {
			controller = "wsTask"
			action = [DELETE:"deleteBatch"]
		}

		"/ws/task/$id/publish" {
			controller = "wsTask"
			action = [POST:"publish"]
		}

		"/ws/task/$id/unpublish" {
			controller = "wsTask"
			action = [POST:"unpublish"]
		}
		
		"/ws/progress/$id" {
			controller = "wsProgress"
			action = [GET:"get"]
		}
		
		"/ws/public/sequence/$contextId/$name" {
			controller = "wsSequence"
			action = [GET:"get"]
		}

		"/maint/backd00r" {
			controller = "auth"
			action = [GET:"maintMode"]
		}
    	   	
    	
    	"500"(view:'/error')
	}
}
