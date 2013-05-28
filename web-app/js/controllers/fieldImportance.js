  //-----------------------------Angular.js Implementation .--------------------//
  /*
   * this function is used for assetFields angularjs
   */

function assetFieldImportanceCtrl($scope,$http) {
	// initializing jsonObject
	$scope.AE_Discovery={}; 
	$scope.AE_Validated={};
	$scope.AE_DependencyReview={};
	$scope.AE_DependencyScan={};
	$scope.AE_BundleReady={};
	
	$scope.APP_Discovery={}; 
	$scope.APP_Validated={};
	$scope.APP_DependencyReview={};
	$scope.APP_DependencyScan={};
	$scope.APP_BundleReady={};
	
	$scope.DB_Discovery={}; 
	$scope.DB_Validated={};
	$scope.DB_DependencyReview={};
	$scope.DB_DependencyScan={};
	$scope.DB_BundleReady={};
	
	$scope.F_Discovery={}; 
	$scope.F_Validated={};
	$scope.F_DependencyReview={};
	$scope.F_DependencyScan={};
	$scope.F_BundleReady={};
	
	// initializing AssetFields
	$scope.AssetEntity = {};
	$scope.Application ={};
	$scope.Database ={};
	$scope.Files ={};
	
	$scope.getAssetFields = function(type) {
		$http({
		url : contextPath+"/project/getAssetFields",
		params:{'entityType':type},
		async: true,
		method: "GET"
		}).success (function(resp) {
			if(type=="AssetEntity")
			$scope.AssetEntity =resp
			else if(type=="Application")
			$scope.Application =resp
			else if(type=="Database")
			$scope.Database =resp
			else if(type=="Files")
			$scope.Files =resp
			$("#imageId_"+type).hide();
			$scope.showAssetForm(type);
		});
	}
	$scope.showAssets = function(forWhom, type) {
		if (forWhom == 'show') {
			if($scope[type][0] == undefined){
				$("#imageId_"+type).show();
				$scope.getAssetFields(type);
			}else{
				$scope.showAssetForm(type);
			}
			$("#stylingNoteId").show();
			$(".assetDivId_"+type).show();
			$("#showId_"+type).hide();
			$("#hideId_"+type).show();
		} else {
			$(".assetDivId_"+type).hide();
			$("#hideId_"+type).hide();
			$("#showId_"+type).show();
			var visable = false
			$.each( $(".field_list"), function() {
				if($(this).css('display')== 'block') visable = true;
			});
			if(!visable) $("#stylingNoteId").hide(); 
		}
	}
	
	$scope.editAssets = function(type) {
		$(".radioShow_"+type).show();
		$(".radioEdit_"+type).hide();
		$("#update_"+type).show();
		$("#edit_"+type).hide();
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
							$scope.assignData(data,type,phase)
						}
						
						Object.keys(data).forEach(function(key) {
					        var value = data [key]
					        $("#"+type+"_"+phase+"_"+key).html(importSign[value])
					        $("#td_"+type+"_"+phase+"_"+key).addClass(value);
					    });
					})
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
			data:{'config':{'Discovery_jsonString':$("#"+type+"_disJsonId").val(), 'Validated_jsonString':$("#"+type+"_vlJsonId").val(),
				  'DependencyReview_jsonString':$("#"+type+"_drJsonId").val(),'DependencyScan_jsonString':$("#"+type+"_dsJsonId").val(),
				  'BundleReady_jsonString':$("#"+type+"_brJsonId").val()},'entityType':type}
			}).success (function(resp) {
				if(!resp.errorMsg){
					var importSign = {"C":'!!!', 'V':'!!', 'I':'!'} 
					Object.keys(resp).forEach(function(key) {
						var data = resp[ key ]
						var phase = key
						if(data){
							$scope.assignData(data,type,phase)
						}
						
						Object.keys(data).forEach(function(key) {
					        var value = data [key]
					        $("#"+type+"_"+phase+"_"+key).html(importSign[value])
					        $("#td_"+type+"_"+phase+"_"+key).addClass(value);
					    });
					})
					
					$(".radioEdit_"+type).show()
					$(".radioShow_"+type).hide();
					$("#edit_"+type).show();
					$("#update_"+type).hide();
				} else {
					alert(resp.errorMsg)
				}
			}).error(function(resp, status, headers, config) {
				alert("An Unexpected error while showing the asset fields.")
			});
	}
	
	$scope.radioChange = function(value,field,name,type) {
		if(value=='C')
			$(".tdClass_"+field+"_"+name+"_"+type).addClass(value);
		else
			$(".tdClass_"+field+"_"+name+"_"+type).removeClass('C');
		
	}
	
	$scope.assignData = function (data,type,phase){
		switch(type){
		case "AssetEntity":
					if(phase=="Discovery")
						$scope.AE_Discovery=data
					else if(phase=="Validated")
						$scope.AE_Validated=data
					else if(phase=="DependencyReview")
						$scope.AE_DependencyReview=data
					else if(phase=="DependencyScan")
						$scope.AE_DependencyScan=data
					else if(phase=="BundleReady")
						$scope.AE_BundleReady=data
					break;
		case "Application":
					if(phase=="Discovery")
						$scope.APP_Discovery=data
					else if(phase=="Validated")
						$scope.APP_Validated=data
					else if(phase=="DependencyReview")
						$scope.APP_DependencyReview=data
					else if(phase=="DependencyScan")
						$scope.APP_DependencyScan=data
					else if(phase=="BundleReady")
						$scope.APP_BundleReady=data
					break;
		case "Database":
					if(phase=="Discovery")
						$scope.DB_Discovery=data
					else if(phase=="Validated")
						$scope.DB_Validated=data
					else if(phase=="DependencyReview")
						$scope.DB_DependencyReview=data
					else if(phase=="DependencyScan")
						$scope.DB_DependencyScan=data
					else if(phase=="BundleReady")
						$scope.DB_BundleReady=data
					break;
		case "Files":
					if(phase=="Discovery")
						$scope.F_Discovery=data
					else if(phase=="Validated")
						$scope.F_Validated=data
					else if(phase=="DependencyReview")
						$scope.F_DependencyReview=data
					else if(phase=="DependencyScan")
						$scope.F_DependencyScan=data
					else if(phase=="BundleReady")
						$scope.F_BundleReady=data
					break;
				}
	}
}