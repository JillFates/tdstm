  //-----------------------------Angular.js Implementation .--------------------//
  /*
   * this function is used for assetFields angularjs
   */

function assetFieldImportanceCtrl($scope,$http) {
	$scope.newObject={}; // initializing jsonObject
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
					var data
					if(resp.assetImp){
						data = resp.assetImp
						$scope.newObject=data
					}
						
					Object.keys(data).forEach(function(key) {
				        var value = data [key]
				        $("#"+key).html(importSign[value])
				        $("#td_"+key).addClass(value);
				    });
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
			data:{'jsonString':$("#jsonId").val(), 'entityType':type}
			}).success (function(resp) {
				if(!resp.errorMsg){
					var importSign = {"C":'!!!', 'V':'!!', 'I':'!'} 
					$(".radioShow").hide()
					var data = resp.assetImp
					if(data){
						$scope.newObject=data
					}
					Object.keys(data).forEach(function(key) {
				        var value = data [key]
				        $("#"+key).html(importSign[value])
				        $("#td_"+key).addClass(value);
					});
					$(".radioEdit").show()
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