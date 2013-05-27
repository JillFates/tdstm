  //-----------------------------Angular.js Implementation .--------------------//
  /*
   * this function is used for assetFields angularjs
   */

function assetFieldImportanceCtrl($scope,$http) {
	$scope.Discovery={}; 
	$scope.Validated={};
	$scope.DependencyReview={};
	$scope.DependencyScan={};// initializing jsonObject
	$scope.BundleReady={};
	$scope.fields = returnMap
	
  
	$scope.showAssets = function(forWhom, type) {
		if (forWhom == 'show') {
			$scope.showAssetForm(type);
		} else {
			$(".assetDivId").hide();
			$("#hideId").hide();
			$("#showId").show();
		}
	}
	
	$scope.editAssets = function() {
		$(".radioShow").show();
		$(".radioEdit").hide();
		$("#update").show();
		$("#edit").hide();
	}
	
	$scope.showAssetForm = function(type) {
		$http({
			url : contextPath+"/project/showFieldImportance",
			params:{'entityType':type},
			method: "GET"
			}).success (function(resp) {
				if(!resp.errorMsg){
					var importSign = {"C":'!!!', 'V':'!!', 'I':'!'} 
					Object.keys(resp).forEach(function(key) {
						var data = resp[ key ]
						var phase = key
						if(data){
							if(phase=="Discovery")
								$scope.Discovery=data
							else if(phase=="Validated")
								$scope.Validated=data
							else if(phase=="DependencyReview")
								$scope.DependencyReview=data
							else if(phase=="DependencyScan")
								$scope.DependencyScan=data
							else if(phase=="BundleReady")
								$scope.BundleReady=data
						}
						
						Object.keys(data).forEach(function(key) {
					        var value = data [key]
					        $("#"+phase+"_"+key).html(importSign[value])
					        $("#td_"+phase+"_"+key).addClass(value);
					    });
					})
					
					
					$(".assetDivId").show();
					$("#showId").hide();
					$("#hideId").show();
				} else {
					alert(resp.errorMsg)
				}
			}).error(function(resp, status, headers, config) {
				alert("An Unexpected error while showing the asset fields.")
			});
	}
	
	
	$scope.updateAssetForm = function(type) {
		$http({
			url : contextPath+"/project/updateFieldImportance",
			method: "POST",
			data:{'config':{'Discovery_jsonString':$("#disJsonId").val(), 'Validated_jsonString':$("#vlJsonId").val(),
				  'DependencyReview_jsonString':$("#drJsonId").val(),'DependencyScan_jsonString':$("#dsJsonId").val(),
				  'BundleReady_jsonString':$("#brJsonId").val()},'entityType':type}
			}).success (function(resp) {
				if(!resp.errorMsg){
					var importSign = {"C":'!!!', 'V':'!!', 'I':'!'} 
					Object.keys(resp).forEach(function(key) {
						var data = resp[ key ]
						var phase = key
						if(data){
							if(phase=="Discovery")
								$scope.Discovery=data
							else if(phase=="Validated")
								$scope.Validated=data
							else if(phase=="DependencyReview")
								$scope.DependencyReview=data
							else if(phase=="DependencyScan")
								$scope.DependencyScan=data
							else if(phase=="BundleReady")
								$scope.BundleReady=data
						}
						
						Object.keys(data).forEach(function(key) {
					        var value = data [key]
					        $("#"+phase+"_"+key).html(importSign[value])
					        $("#td_"+phase+"_"+key).addClass(value);
					    });
					})
					
					$(".radioEdit").show()
					$(".radioShow").hide();
					$("#edit").show();
					$("#update").hide();
				} else {
					alert(resp.errorMsg)
				}
			}).error(function(resp, status, headers, config) {
				alert("An Unexpected error while showing the asset fields.")
			});
	}
	
	$scope.radioChange = function(value,field,name) {
		if(value=='C')
			$(".tdClass_"+field+"_"+name).addClass(value);
		else
			$(".tdClass_"+field+"_"+name).removeClass('C');
		
	}
}