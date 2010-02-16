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
    	   	
    	
    	"500"(view:'/error')
	}
}
