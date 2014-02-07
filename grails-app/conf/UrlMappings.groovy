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
		
		"/ws/cookbook/recipe/$id/$version?" {
			controller = "wsCookbook"
			action = [GET:"recipe", POST:"saveRecipeVersion"]
		}

		"/ws/cookbook/recipe/version/$recipeVersionId" {
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

		"/ws/task/$id/publish" {
			controller = "wsTask"
			action = [POST:"publish"]
		}

		"/ws/task/$id/unpublish" {
			controller = "wsTask"
			action = [POST:"unpublish"]
		}

		"/maint/backd00r" {
			controller = "auth"
			action = [GET:"maintMode"]
		}
    	   	
    	
    	"500"(view:'/error')
	}
}
