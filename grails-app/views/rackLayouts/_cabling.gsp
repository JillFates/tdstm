<%@page import="com.tds.asset.AssetCableMap"%>
<%@page import="net.transitionmanager.security.Permission"%>
<script type="text/javascript">
var app = angular.module("cablingApp${assetId}", ['ui']);

app.controller('Ctrl', function($scope, $filter, $http) {
	 $scope.statues = ['Cabled','Unknown','Empty'];

	 $scope.colors = ['White','Grey','Green','Yellow','Orange','Red','Blue','Purple','Black'];
	
	 $scope.cables = ${assetCablingMap};
	 // which is used to reassign the cable value if user cancel without updating
	 $scope.backUpCables = ${assetCablingMap};
	 $scope.assets = {}; 
	 $scope.cableColor = {};
	 $scope.connectors = {};
	 $scope.row = ${assetRows};
   	 $scope.modelConnectors = {};
  	 $scope.demoChange = function(id,type){
  	  	var value= $scope.cables[id]['fromAssetId']
  	  	if(value){
        	$scope.modelConnectors = $scope.connectors[value];
  	  	}
  	 	$scope.cables[id]['connectorId'] = 'null';
    }
	
	$scope.showRow = function(id) {
		return $scope.row[id] == 's';
	}
	var tempId=''
	$scope.showEditRow = function(id) {
		<tds:hasPermission permission="${Permission.ModelEdit}">
			var asset = $("#fromAsset_"+id).val();
			var type = $("#connectType_"+id).val();
			var roomType = $("#roomType").val();
			if (tempId!='' && id!=tempId) {
				if($scope.cables[id]['color'])
					$scope.cableColor[id] = $scope.cables[id]['color']
				else
					$scope.cableColor[id] = "White"
				$scope.cancelRow(tempId);
				$scope.row[id] = $scope.row[id] == 'h' ? 's' : 'h';
			} else {
				if ($('.btn:visible').length== 0) {
					if ($scope.cables[id]['color'])
						$scope.cableColor[id] = $scope.cables[id]['color']
					else
						$scope.cableColor[id] = "White"
					$scope.row[id] = $scope.row[id] == 'h' ? 's' : 'h';
				}
			}
			//TODO: Used jquery syntax for now,should be replaced with angular.
			if (type=='Power') {
				$(".powerDiv").show();
				$(".nonPowerDiv").hide();
				$("#staticConnector_"+$('#power_'+id).val()+"_"+id).attr('checked', true);
			} else {
				$(".nonPowerDiv").show();
				$(".powerDiv").hide();
				$scope.getAsset(id, asset, type, roomType)
			}
			tempId=id
		</tds:hasPermission>
	};
	var assetTemp = ''
	var roomTemp = ''
    $scope.getAsset = function(id, asset, type, room){
	    if(id != assetTemp || room!=roomTemp){
	    	$http({
				url : "../rackLayouts/retrieveAssetModelConnectors",
				method: "POST",
				async: false,
				data:{'asset': $("#assetEntityId").val(), 'type':type,'roomType':room}
			}).success (function(resp) {
				$scope.assets = resp['assets'];
				$scope.connectors = resp['connectors'];
				$scope.showAsset(id, asset, type);
				if(asset){
					$scope.modelConnectors = $scope.connectors[asset];
				}else{
					$scope.modelConnectors = {};
					$scope.cables[id]['connectorId'] = 'null';
				}
			}).error(function(resp, status, headers, config) {
				alert("An Unexpected error while showing the asset fields.")
			});
	    }
	    assetTemp=id
	    roomTemp=room
     }
	$scope.showAsset = function(id, asset, type){
		setTimeout(function(){
			$("#assetFromId_"+id).html($("#assetHiddenId").html());
		    $("#assetFromId_"+id).val(asset);
		    if(!isIE7OrLesser)
		    	$("#assetFromId_"+id).select2();
		},600);
	}
    $scope.cancelRow = function(id){
    	$scope.cables[id] = $.extend(true, {}, $scope.backUpCables[id]);
    	return $scope.row[id] = 'h';
    };

    $scope.changeCableDetails = function(cableId){
        var value = $scope.cables[cableId]['status']
    	if(value=='Empty'){
    		$scope.modelConnectors={}
    		console.log("--"+$("#assetFromId_"+cableId).val())
    		if($("#assetFromId_"+cableId).val()){
    			$("#assetFromId_"+cableId).val('');
    		}
    		$scope.cables[cableId]['connectorId']= 'null';
    	}else if(value=='Unknown') {
    		$scope.modelConnectors={}
    		if($("#assetFromId_"+cableId).val()){
    			$("#assetFromId_"+cableId).val('');
    		}
    		$scope.cableColor[cableId] = "White";
    		$scope.cables[cableId]['comment']= '';
    		$scope.cables[cableId]['length']= '';
    		$scope.cables[cableId]['connectorId']= 'null';
    	}else{
    		$scope.cables[cableId]=$scope.backUpCables[cableId]
    		$scope.cableColor[cableId] = $scope.backUpCables[cableId]['color']
    		$("#assetFromId_"+cableId).val($scope.backUpCables[cableId]['fromAssetId']);
    		$scope.modelConnectors = $scope.connectors[$scope.backUpCables[cableId]['fromAssetId']]
    	}
    	if(!isIE7OrLesser)
        	$("#assetFromId_"+cableId).select2();
    }
    $scope.changeCableStatus = function(cableId){
        if($scope.cables[cableId]['status']!='Cabled'){
        	$scope.cables[cableId]['status']= 'Cabled';
        }
    }
    $scope.openCablingDiv= function( assetId , type){
    	var defRoomType = $("#roomTypeForCabling").val();
    	if(!type && defRoomType=='0'){
    		type = 'T'
    	}
    	
    	if(!type){
    		type='S'
    	}
    	new Ajax.Request(contextPath+'/rackLayouts/retrieveCablingDetails?assetId='+assetId+'&roomType='+type,{asynchronous:true,evalScripts:true,onComplete:function(e){showCablingDetails(e,assetId);}})
    }
});

angular.bootstrap($("#cablingDialogId").children()[0], ["cablingApp${assetId}"]);

</script>
<script type="text/javascript">
	if(!${assetCablingDetails[0]?.hasImageExist}){
		$("#cablingPanel").css("height",${assetCablingDetails[0].usize? assetCablingDetails[0].usize*30+2 : 0}+'px')
		$("#roomTypeDiv").css("margin-top",${assetCablingDetails[0].usize? assetCablingDetails[0].usize*10 : 0}+'px')
		$("#cablingPanel").css("background-color","LightGreen")
	} else {
		$("#rearImage_${assetCablingDetails[0]?.model}").show()
		$("#cablingPanel").css("background-color","#FFF")
	}
	$("#cablingDialogId").dialog( "option", "title", "${assetCablingDetails[0]?.title}");
	$('div.connector_Left').each(function(index) {
		$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
	}); 

</script>
<div id="roomTypeDiv" style="float:right;">
      	<label><input type="radio" class="cableRoomType" name="cableRoomType" id="cableRoomType_S" value="S" ${roomType=='S'? 'checked="checked"' :'' } onclick="openCablingDiv('${assetId}',this.value)"/>Current</label>
		<label><input type="radio" class="cableRoomType" name="cableRoomType" id="cableRoomType_T" value="T" ${isTargetRoom? '' :'disabled="disabled"'} ${roomType=='T'? 'checked="checked"' :'' } onclick="openCablingDiv('${assetId}',this.value)" style="margin-left:10px;"/>Target</label>
		<input type="hidden" id="roomType" name="roomType"  value="${roomType}"/>
</div>
<div id="cablingPanel" style="min-width: 400px; font-size: 14px; clear: both;">
	<g:if test="${assetCablingDetails}">
		<g:each in="${assetCablingDetails}" var="assetCabling" >
			<div id='connector${assetCabling.id}' style='position:absolute;top: ${(assetCabling.connectorPosY / 2)}px; left: ${assetCabling.connectorPosX}px;'>
				<a href='#' style="float: left"><div><img id='${assetCabling.status}' src="${resource(dir:'i/cabling',file:assetCabling.status.toLowerCase() + '.png')}"></div></a>
				<div style="float: left; margin-left:5px"><span>${assetCabling.label}</span></div>
			</div>
		</g:each>
	</g:if>
	<g:if test="${currentBundle}">
		<g:each in="${models}" var="model">
			<g:if test="${model?.rearImage && model?.useImage == 1}">
				<img id="rearImage_${model.id}" src="${createLink(controller:'model', action:'retrieveRearImage', id:model.id)}" style="display: none;"/>
			</g:if>
		</g:each>
	</g:if>
</div>
<div id="app" ng-app="app" ng-controller="Ctrl">
  <input type="hidden" name="assetEntityId" id="assetEntityId"/>
  <input type="hidden" name="actionType" id="actionTypeId" value='assignId'/>
</div>