<%@page import="com.tds.asset.AssetCableMap"%>
<script type="text/javascript">
var app = angular.module("app", ['ui']);

app.controller('Ctrl', function($scope, $filter, $http) {
	 $scope.statues = [
	                   {value: 1, text:'Unknown'},
	   				   {value: 2, text:'Cabled'},
	   				   {value: 3, text:'Empty'}
	   				  ];

	 $scope.colors = [
	                  {value: 1, text:'White'},
	   				  {value: 2, text:'Grey'},
	   				  {value: 3, text:'Green'},
	   				  {value: 4, text:'Yellow'},
	   				  {value: 5, text:'Orange'},
	   				  {value: 6, text:'Red'},
	   				  {value: 7, text:'Blue'},
	   				  {value: 8, text:'Purple'},
	   				  {value: 9, text:'Black'}
   				  	];

	 $scope.cables = ${assetCablingMap};
	 $scope.assets = ${currRoomRackAssets};  
	 $scope.connectors = ${modelConnectorJson};
	 $scope.row = ${assetRows};
  	 $scope.power = ${cableTypes};
  	 $scope.connectors['null']={};
  	$scope.params = {};
  	$scope.modelConnectors = {};
  	$scope.demoChange = function(value,type){
  	  	if(value)
        	$scope.modelConnectors = $scope.connectors[value][type];
    }
	
	$scope.showRow = function(id) {
		return $scope.row[id] == 's';
	}
	var tempId=''
    $scope.showEditRow = function(id){
		var asset = $("#fromAsset_"+id).val();
		var type = $("#connectType_"+id).val();
	    if(tempId!='' && id!=tempId){
    		$scope.cancelRow(tempId);
		    $scope.row[id] = $scope.row[id] == 'h' ? 's' : 'h';
	    } else {
	    	if($('.btn:visible').length== 0){
	    		$scope.row[id] = $scope.row[id] == 'h' ? 's' : 'h';
	    		if(asset)
	    			$scope.modelConnectors = $scope.connectors[asset][type];
	         }
	    }
	    if(asset){
		    $("#assetFromId_"+id).val(asset);
		    if(!isIE7OrLesser)
		    	$("#assetFromId_"+id).select2();
	    }
	  //TODO: Used jquery syntax for now,should be replaced with angular.
		if(type=='Power'){
			$(".powerDiv").show();
			$(".nonPowerDiv").hide();
			$("#staticConnector_"+$('#power_'+id).val()+"_"+id).attr('checked', true);
    	}else{
    		$(".powerDiv").hide();
			$(".nonPowerDiv").show();
        }
	    tempId=id
    };

    $scope.cancelRow = function(id){
    	return $scope.row[id] = 'h';
    };
});

</script>
<script type="text/javascript">
	if(!${assetCablingDetails[0]?.hasImageExist}){
		$("#cablingPanel").css("height",${assetCablingDetails[0].usize? assetCablingDetails[0].usize*30+2 : 0}+'px')
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
<div id="cablingPanel" style="height: auto; ">
	<g:if test="${assetCablingDetails}">
		<g:each in="${assetCablingDetails}" var="assetCabling">
			<div id='connector${assetCabling.id}' style='top: "${(assetCabling.connectorPosY / 2)}+"px; left: "${assetCabling.connectorPosX}+"px;'>
				<a href='#'><div><img id='${assetCabling.status}' src='../i/cabling/${assetCabling.status.toLowerCase()}.png' onclick="openActionButtonsDiv( '${assetCabling.id}', this.id, '${assetCabling.type}')"></div></a>
				<div class='connector_${assetCabling.labelPosition}'><span>${assetCabling.label}</span></div>
			</div>
		</g:each>
	</g:if>
	<g:if test="${currentBundle}">
		<g:each in="${models}" var="model">
			<g:if test="${model?.rearImage && model?.useImage == 1}">
				<img id="rearImage_${model.id}" src="${createLink(controller:'model', action:'getRearImage', id:model.id)}" style="display: none;"/>
			</g:if>
		</g:each>
	</g:if>
	</div>
<div ng-app="app" ng-controller="Ctrl">
   <table id="cableTable" class="table table-bordered table-hover table-condensed" style="width:auto;display:none;">
    <tr style="font-weight: bold">
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
      	 <select id="status_{{cable.cableId}}" name="status_{{cable.cableId}}" style="width:75px;">
	        <option value="">Please Select</option>
	        <option ng-repeat="s in statues" value="{{s.text}}" title="{{s.text}}" ng-selected="s.text == cable.status">{{s.text}}</option>
	     </select>
      	 </span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" class='{{cable.color}}' ng-hide="showRow(cable.cableId)" >
      	 <span></span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" ng-show="showRow(cable.cableId)">
      	 <span>
	      	 <select id="color_{{cable.cableId}}" name="color_{{cable.cableId}}" style="width:75px;">
			        <option value="">Please Select</option>
			        <option ng-repeat="c in colors" value="{{c.text}}" title="{{c.text}}" ng-selected="c.text == cable.color">{{c.text}}</option>
		     </select>
      	 </span>
      </td>
      <td ng-click="showEditRow(cable.cableId)">
      	 <span ng-hide="showRow(cable.cableId)">{{ cable.length }}</span>
      	 <span ng-show="showRow(cable.cableId)">
      	 	<input type="text" id="cableLength_{{cable.cableId}}" name="cableLength_{{cable.cableId}}" value="{{ cable.length }}" size="2"/>
      	 </span>
      </td>
      <td ng-click="showEditRow(cable.cableId)" >
      	 <div ng-hide="showRow(cable.cableId)" class='commentEllip'>{{ cable.comment }}</div>
      	 <span ng-show="showRow(cable.cableId)">
      	 	<input type="text" name="cableComment_{{cable.cableId}}" id="cableComment_{{cable.cableId}}" value="{{ cable.comment }}" size="8"/>
      	 </span>
      </td>
      <td>
      	<span ng-hide="showRow(cable.cableId);" class="power_{{power[cable.cableId]}}" style="display:none;" onclick="javascript:openCablingDiv({{cable.fromAssetId}})">
      		{{ cable.fromAsset }}
      	</span>
      	<span ng-hide="showRow(cable.cableId);" style="display:none;" class="type_{{power[cable.cableId]}}">
      		{{ cable.rackUposition }}
      	</span>
	      	<span ng-show="showRow(cable.cableId)">
			      	<span class="powerDiv" style="display:none;">
						<input type="radio" name="staticConnector" id="staticConnector_A_{{cable.cableId}}" value="A">A</input>&nbsp;
						<input type="radio" name="staticConnector" id="staticConnector_B_{{cable.cableId}}" value="B">B</input>&nbsp;
						<input type="radio" name="staticConnector" id="staticConnector_C_{{cable.cableId}}" value="C">C</input>
						<input type="hidden" name="power_{{cable.cableId}}" id="power_{{cable.cableId}}" value="{{cable.rackUposition}}"/>
					</span>
					<span class="nonPowerDiv" style="display:none;">
					     <select ui-select2 ng-model="params.value" ng-change="demoChange(params.value, cable.type)" id="assetFromId_{{cable.cableId}}" style="width:100px;">
				        	<option value="null">Please Select</option>
					        <option ng-repeat="v in assets" value="{{v.id}}" title="{{v.assetName}}">{{v.assetName}}</option>
					     </select>
					     <select id="modelConnectorId_{{cable.cableId}}" style="width:75px;">
					        <option value="null">Please Select</option>
					        <option ng-repeat="c in modelConnectors" value="{{c.value}}" title="{{c.text}}" ng-selected="c.value == cable.connectorId">{{c.text}}</option>
					     </select>
				    </span>
				    <input type="hidden" name="fromAsset_{{cable.cableId}}" id="fromAsset_{{cable.cableId}}" value="{{cable.fromAssetId}}"/>
				    <input type="hidden" name="connectType_{{cable.cableId}}" id="connectType_{{cable.cableId}}" value="{{cable.type}}"/>
		     </span>
      </td>
      <td ng-show="showRow(cable.cableId)">
			<img src="${resource(dir:'images',file:'check12.png')}" class="pointer btn" onclick="submitAction($('form[name=cablingDetailsForm]'),{{cable.cableId}})" style="width:18px;"/>
			<img src="${resource(dir:'images',file:'delete.png')}" class="pointer btn" ng-click="cancelRow(cable.cableId)" style="width:18px;"/>
      </td>
    </tr>
  </table>
  <input type="hidden" name="assetEntityId" id="assetEntityId"/>
  <input type="hidden" name="actionType" id="actionTypeId" value='assignId'/>
</div>
