<%@page import="com.tds.asset.AssetCableMap"%>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'xeditable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrapXedit.css')}" />
<script type="text/javascript">
var app = angular.module("app", ["xeditable"]);

app.run(function(editableOptions) {
  editableOptions.theme = 'bs3';
});

app.controller('Ctrl', function($scope, $filter, $http) {
 $scope.users = ${assetCablingMap}; 

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

 $scope.statues = [
                   {value: 1, text:'Unknown'},
   				  {value: 2, text:'Cabled'},
   				  {value: 3, text:'Assigned'},
   				  {value: 4, text:'Empty'}
   				  ];

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
	if(!isIE7OrLesser)
		$("select.assetConnectSelect").select2();

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
   <table id="cableTable" class="table table-bordered table-hover table-condensed" style="width:auto;display:none:">
    <tr style="font-weight: bold">
      <th>Type</th>
      <th>Label</th>
      <th>Status</th>
      <th>Color</th>
      <th>Feet</th>
      <th>Comment</th>
      <th>Assigned To</th>
    </tr>
    <tr ng-repeat="user in users">
      <td ng-click="rowform.$show()">
        <span editable-text="user.type" e-name="type" e-form="rowform" e-required>
          {{ user.type }}
        </span>
      </td>
      <td ng-click="rowform.$show()">
        <span editable-text="user.label" e-name="status" e-form="rowform">
          {{ user.label }}
        </span>
      </td>
      <td ng-click="rowform.$show()">
        <span editable-select="user.status" e-name="status" e-form="rowform" e-ng-options="s.value as s.text for s in statues">
          {{ user.status }}
        </span>
      </td>
      <td ng-click="rowform.$show()" class="{{user.color}}">
        <span editable-select="user.color" e-name="color" e-form="rowform" e-ng-options="c.value as c.text for c in colors" >
        </span>
      </td>
      <td ng-click="rowform.$show()">
        <span editable-text="user.length" e-name="length" e-form="rowform">
          {{ user.length }}
        </span>
      </td>
      <td ng-click="rowform.$show()">
        <span editable-text="user.comment" e-name="comment" e-form="rowform">
          {{ user.comment }}
        </span>
      </td>
      <td>
        <span e-name="fromAssetId" e-form="rowform">
          {{ user.fromAssetId }}
        </span>
      </td>
      <td style="white-space: nowrap" ng-show="rowform.$visible">
        <!-- form -->
        <form editable-form name="rowform" onbeforesave="saveUser($data, user.id)" ng-show="rowform.$visible" class="form-buttons form-inline" shown="inserted == user">
          <button type="submit" ng-disabled="rowform.$waiting" class="btn btn-primary">
            save
          </button>
          <button type="button" ng-disabled="rowform.$waiting" ng-click="rowform.$cancel()" class="btn btn-default">
            cancel
          </button>
        </form>  
      </td>
      
    </tr>
  </table>

</div>
