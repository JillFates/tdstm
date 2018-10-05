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
//moved code from room.rack.combined.js to update the json which is useful to update a particular row.
    $scope.submitAction = function(cableId){
    	var actionId = $("#actionTypeId").val()
    	var isValid = true
    	var statusVal = $scope.cables[cableId]['status']
    	if(actionId == "assignId"){

    		if($("#connectType_"+cableId).val() != 'Power'){

    			var assetFrom = $("#assetFromId_"+cableId).val();
    			var modelConnectorId = $scope.cables[cableId]['connectorId']
    			if(statusVal == 'Cabled' || statusVal == 'Empty'){
    				if( assetFrom!='null' && modelConnectorId=='null' ){
    					isValid = false
    					alert("Please enter the target connector details")
    				}
    			}
    		}
    	}
    	var actionType=''
    	switch(statusVal){
    		case "Cabled" : actionType = 'assignId' ; break;
    		case "Empty" : actionType = 'assignId' ; break;
    	}
    	if(isValid){
    		$http({
    			url:contextPath+'/rackLayouts/updateCablingDetails',
    			data: {'assetCable':cableId ,'assetId':$("#assetEntityId").val(), 'status':$scope.cables[cableId]['status'],'actionType':actionType,
    				          'color':$scope.cableColor[cableId], 'connectorType':$("#connectType_"+cableId).val(),
    				          'assetFromId':$("#assetFromId_"+cableId).val(),'modelConnectorId':$scope.cables[cableId]['connectorId'],
    				          'staticConnector':$("input:radio[name=staticConnector]:checked").val(),'cableComment':$scope.cables[cableId]['comment'],
    				          'cableLength':$scope.cables[cableId]['length'],'roomType':$("#roomType").val()},
    			method: "POST"
    		}).success (function(resp) {
        		$scope.cancelRow(cableId)
        		$scope.cables[cableId]['status']= resp.status
        		$scope.cables[cableId]['length']= resp.length
        		$scope.cables[cableId]['color']= resp.color
        		$scope.cables[cableId]['comment']= resp.comment
        		$scope.cables[cableId]['fromAssetId']= resp.fromAssetId
        		$scope.cables[cableId]['fromAsset']= resp.fromAsset
        		$scope.cables[cableId]['rackUposition']= resp.rackUposition
        		$scope.cables[cableId]['connectorId']= resp.connectorId
        		$scope.cables[cableId]['asset']= resp.asset
        		$scope.cables[cableId]['locRoom'] = resp.locRoom
        		$scope.cables[cableId]['powerA'] = resp.powerA
        		$scope.cables[cableId]['powerB'] = resp.powerB
        		$scope.cables[cableId]['cableComment'] = resp.cableComment
        		if(resp.toCableId && resp.toCableId!=cableId){
        			$scope.cables[resp.toCableId]['fromAssetId']= ''
            		$scope.cables[resp.toCableId]['fromAsset']= ''
            		$scope.cables[resp.toCableId]['connectorId']= ''
            		$scope.cables[resp.toCableId]['asset']= ''
        		}
        		 $scope.backUpCables[cableId] =  $scope.cables[cableId];
    		});
    	}
    }
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
   <table id="cableTable" class="table table-bordered table-hover table-condensed" style="width:auto;display:none;">
    <tr style="font-weight: bold;">
      <th>Type</th>
      <th>Connector</th>
      <th>Status</th>
      <th>Color</th>
      <th>Length</th>
      <th>Comment</th>
      <th>Assigned To</th>
    </tr>
    <tr ng-repeat="cable in cables" >
      <td ng-click="showEditRow(cable.cableId)"><span>{{ cable.type }}</span></td>
      <td ng-click="showEditRow(cable.cableId)"><span>{{ cable.label }}</span></td>
      <td ng-click="showEditRow(cable.cableId)">
      	 <span ng-hide="showRow(cable.cableId)">{{cable.status}}</span>
      	 <span ng-show="showRow(cable.cableId)">
	     <select style="width:75px;" ng-model="cables[cable.cableId]['status']" ng-options="s for s in statues" ng-change="changeCableDetails(cable.cableId)"></select><br>
      	</span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" class='{{cable.color}}' ng-hide="showRow(cable.cableId)" >
      <span></span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" ng-show="showRow(cable.cableId)">
      	 <span>
			<select ng-model="cableColor[cable.cableId]" ng-options="c for c in colors" ng-change="changeCableStatus(cable.cableId)"></select><br>
      	 </span>
      </td>
      <td ng-click="showEditRow(cable.cableId)">
      	 <span ng-hide="showRow(cable.cableId)">{{ cable.length }}</span>
      	 <span ng-show="showRow(cable.cableId)">
      	 	<input type="text" ng-model="cables[cable.cableId]['length']"
      	 		value="{{ cable.length }}" size="2" ng-keypress="changeCableStatus(cable.cableId)"/>
      	 </span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" >
      	 <div ng-hide="showRow(cable.cableId)" class='commentEllip'>{{ cable.comment }}</div>
      	 <span ng-show="showRow(cable.cableId)">
      	 	<input type="text" ng-model="cables[cable.cableId]['comment']"
      	 		value="{{ cable.comment }}" size="8" ng-keypress="changeCableStatus(cable.cableId)"/>
      	 </span>
      </td>
      <td>
      	<span ng-hide="showRow(cable.cableId)" class='{{cable.powerA}}' ng-click="openCablingDiv(cable.fromAssetId,'${roomType}')"
      		style="text-decoration: underline;color:blue;cursor: pointer;">
      		{{ cable.fromAsset }}
      	</span>
      	<span ng-hide="showRow(cable.cableId)" class='{{cable.powerB}}'>
      		{{ cable.rackUposition }}
      	</span>
	      	<span ng-show="showRow(cable.cableId)">
		      	<span class="powerDiv" style="display:none;">
					<input type="radio" name="staticConnector" id="staticConnector_A_{{cable.cableId}}" value="A">A</input>&nbsp;
					<input type="radio" name="staticConnector" id="staticConnector_B_{{cable.cableId}}" value="B">B</input>&nbsp;
					<input type="radio" name="staticConnector" id="staticConnector_C_{{cable.cableId}}" value="C">C</input>
					<input type="hidden" name="power_{{cable.cableId}}" id="power_{{cable.cableId}}" value='{{cable.rackUposition}}'/>
				</span>

				<span class="nonPowerDiv" style="display:none;">
				     <select ui-select2  id="assetFromId_{{cable.cableId}}" ng-model="cables[cable.cableId]['fromAssetId']"
				     	ng-change="demoChange(cable.cableId,cable.type);changeCableStatus(cable.cableId);" style="width:100px;">
			        	<option value="{{cable.fromAssetId}}">{{ cable.asset }}</option>
				     </select>
				     <select id="assetHiddenId" style="display:none;width:75px;">
				        <option value="null">Please Select</option>
				        <option ng-repeat="v in assets" value="{{v.id}}" title="{{v.assetName}}" >{{v.assetName}}</option>
				     </select>
				     <select ng-model="cables[cable.cableId]['connectorId']" style="width:75px;" ng-change="changeCableStatus(cable.cableId)">
				        <option value="null">Please Select</option>
				        <option ng-repeat="c in modelConnectors" value='{{c.value}}' title='{{c.text}}' ng-selected='c.value == cable.connectorId'>{{c.text}}</option>
				     </select>
			    </span>
			    <input type="hidden" name="fromAsset_{{cable.cableId}}" id="fromAsset_{{cable.cableId}}" value="{{cable.fromAssetId}}"/>
			    <input type="hidden" name="connectType_{{cable.cableId}}" id="connectType_{{cable.cableId}}" value="{{cable.type}}"/>
		     </span>
      </td>
      <td ng-show="showRow(cable.cableId)">
      	<tds:hasPermission permission="${Permission.ModelEdit}">
	     	<asset:image src="icons/cancel.png" id="cancelButton_${cable.cableId}" class="pointer btn" ng-click="cancelRow(cable.cableId)" style="width:18px;" title="Cancel Changes"/>
			<asset:image src="icons/accept.png" class="pointer btn" ng-click="submitAction(cable.cableId)" style="width:18px;" title="Save Changes"/>
		</tds:hasPermission>
      </td>
    </tr>
  </table>
  <input type="hidden" name="assetEntityId" id="assetEntityId"/>
  <input type="hidden" name="actionType" id="actionTypeId" value='assignId'/>
