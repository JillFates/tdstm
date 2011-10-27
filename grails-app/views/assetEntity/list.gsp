<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset List</title>
<g:javascript src="asset.tranman.js" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />

<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>

<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
function onInvokeExportAction(id) {
    var parameterString = createParameterStringForLimit(id);
    location.href = 'list?' + parameterString;
}
$(document).ready(function() {
	        $("#showDialog").dialog({ autoOpen: false })
	        $("#editDialog").dialog({ autoOpen: false })
	        $("#createDialog").dialog({ autoOpen: false })
	        $("#commentsListDialog").dialog({ autoOpen: false })
	        $("#createCommentDialog").dialog({ autoOpen: false })
	        $("#showCommentDialog").dialog({ autoOpen: false })
	        $("#editCommentDialog").dialog({ autoOpen: false })
	        $("#manufacturerShowDialog").dialog({ autoOpen: false })
	        $("#modelShowDialog").dialog({ autoOpen: false })
	        $("#filterPane").draggable()
	        $("#showAssetList").dialog({autoOpen: false})
	        $("#createAsset").dialog({autoOpen: false})
	        $("#editAsset").dialog({autoOpen: false})
})
</script>
<script type="text/javascript">	
	   		
	function createDialog(){
		      $("#createDialog").dialog('option', 'width', 950)
		      $("#createDialog").dialog('option', 'position', ['center','top']);
		      if($('#createFormTbodyId')){
			      $('#createFormTbodyId').css('display','none');
			      $('#attributeSetId').val('');
		      }
		      $("#createDialog").dialog("open")
		      $("#editDialog").dialog("close")
		      $("#showDialog").dialog("close")
		      $('#createCommentDialog').dialog('close');
		      $('#commentsListDialog').dialog('close');
		      $('#editCommentDialog').dialog('close');
		      $('#showCommentDialog').dialog('close');
		      $("#attributeSetId").val(1)
		      ${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + $("#attributeSetId").val() ', onComplete:'generateCreateForm(e)')}
		    }
		    
	function editAssetDialog() {
		      $("#showDialog").dialog("close")
		      $("#editDialog").dialog('option', 'width', 'auto')
		      $("#editDialog").dialog('option', 'position', ['center','top']);
		      $("#editDialog").dialog("open")
		    }
		    
	function showEditAsset(e) {
		      var assetEntityAttributes = eval('(' + e.responseText + ')')
			if (assetEntityAttributes != "") {
				var trObj = $("#assetRow_"+assetEntityAttributes[0].id)
				trObj.css('background','#65a342');
		    		var length = assetEntityAttributes.length
				for (var i=0; i < length; i ++) {
					var attribute = assetEntityAttributes[i]
					var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
					if(tdId != null ){
						tdId.html( attribute.value )
				      	}
				}
				$("#editDialog").dialog("close")
			} else {
				alert("Asset is not updated, Please check the required fields")
				}
      		}
		    
	function validateAssetEntity(formname) {
      			var attributeSet = $("#attributeSetId").val();
      			if(attributeSet || formname == 'editForm'){
      				var assetName = document.forms[formname].assetName.value.replace(/^\s*/, "").replace(/\s*$/, "");
      				
	      			if( !assetName ){
	      				alert(" Please Enter Asset Name. ");
	      				return false;
	      			} else {
	      				return true;
	      			}
      			} else {
      				alert(" Please select Attribute Set. ");
	      			return false;
      			}
      		}
	function showAssetDetails( assetId ){
		${remoteFunction(action:'show', params:'\'id=\'+assetId', before:'document.showForm.id.value = assetId;document.editForm.id.value = assetId;', onComplete:"showAssetDialog(e)")}
      		}
</script>
<filterpane:includes />
</head>
<body>

<div class="body">
<h1>AssetList</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<input type="hidden" id="role" value="role"/>
<div>
	<form name="assetEntityForm" action="list">
		<jmesa:tableFacade id="tag" items="${assetEntityList}" maxRows="25" exportTypes="csv,excel" stateAttr="restore" var="assetEntityInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
		    <jmesa:htmlTable style=" border-collapse: separate">
		        <jmesa:htmlRow highlighter="true">
		        	<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" >
		        		<a href="javascript:createEditPage(${assetEntityInstance.id})"><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
						<span id="icon_${assetEntityInstance.id}">
							<g:if test="${assetEntityInstance.commentType == 'issue'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
									<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="0px"/>
								</g:remoteLink>
							</g:if>
							<g:elseif test="${assetEntityInstance.commentType == 'comment'}">
							<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
								<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px"/>
							</g:remoteLink>
							</g:elseif>
							<g:else>
							<a href="javascript:createNewAssetComment(${appEntityInstance.id});">
								<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px"/>
							</a>
							</g:else>
						</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="application" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span  onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.application}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<a href="#" id="assetName_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.assetName}</a>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="modelName" title="Model" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="model_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.model}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="sourceLocation" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="sourceLocation_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.sourceLocation}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="sourceRack" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="sourceRack_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.sourceRack}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="targetLocation" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="targetLocation_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.targetLocation}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="targetRack" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="targetRack_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.targetRack}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetType" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="assetType_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.assetType}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetTag" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="assetTag_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.assetTag}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="serialNumber" title="Serial #" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="serialNumber_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.serialNumber}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="moveBundleName" title="Move Bundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="moveBundle_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${assetEntityInstance.moveBundle}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depUp" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getAppDetails('${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.depUp}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depDown" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getAppDetails('${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.depDown}</span>
		        	</jmesa:htmlColumn>
		        </jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
</div>
<div class="buttons"><g:form>
	<span class="button"><input type="button" value="New Asset" class="create" onClick="createAssetPage()" /></span>
</g:form></div>
</div> <%-- End of Body --%>
<div id="createDialog" title="Create Asset" style="display: none;">
<g:form action="save" method="post" name="createForm" >

	<div class="dialog" id="createDiv" >

		<table style="border: 0px;">
			<tr class="prop">
				<td valign="top" class="name" ><label for="attributeSet">Attribute Set:</label><span style="padding-left: 46px;"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" id="attributeSetId" name="attributeSet.id" noSelection="['':'select']" 
				 onchange="${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + this.value ', onComplete:'generateCreateForm(e)')}"></g:select></span> </td>
			</tr>
		</table>
		<table id="createFormTbodyId"></table>

	</div>
	<div class="buttons">
		<input type="hidden" name="projectId" value="${projectId }" />
		<span class="button"><input class="save" type="submit" value="Create" onclick="return validateAssetEntity('createForm');" />
		</span>
	</div>
</g:form></div>
<div id="showDialog" title="Show Asset" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
		<input type="hidden" name="id" value="" />
		<input type="hidden" name="projectId" value="${projectId}" />
		<span class="button"><input type="button" class="edit" value="Edit" onClick="return editAssetDialog()" />
		</span>
		<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Delete Asset, are you sure?');" value="Delete" />
		</span>
	</div>
</g:form></div>

<div id="editDialog" title="Edit Asset" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons">
		<span class="button"><input type="button" class="save" value="Update Asset" onClick="if(validateAssetEntity('editForm')) ${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
		</span>
		<span class="button"><input type="button" class="delete" onclick="return editDialogDeleteRemove('delete')" value="Delete" />
		</span>
	</div>
</g:form></div>
<g:render template="commentCrud"/>
<div id="manufacturerShowDialog" title="Show Manufacturer">
	<div class="dialog">
		<table>
	    	<tbody>
		<tr class="prop">
			<td valign="top" class="name">Name:</td>
			<td valign="top" class="value" id="showManuName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value"  id="showManuAka"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="value" id="showManuDescription"></td>
		</tr>
		</tbody>
		</table>
	</div>
	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
	<div class="buttons">
	    <g:form controller="manufacturer" action="edit" target="new">
	        <input type="hidden" name="id" id="show_manufacturerId" />
	        <span class="button"><input type="submit" class="edit" value="Edit" onclick="$('#manufacturerShowDialog').dialog('close')"/></span>
	    </g:form>
	</div>
	</jsec:hasAnyRole>
</div>
<div id="modelShowDialog"  title="Show Model">
<div class="dialog">
	<table>
		<tbody>
		<tr>
			<td valign="top" class="name">Manufacturer:</td>
			<td valign="top" class="value" id="showManufacturer"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Model Name:</td>
			<td valign="top" class="value" id="showModelName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value" id="showModelAka"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Asset Type:</td>
			<td valign="top" class="value" id="showModelAssetType"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td valign="top" class="value" id="showModelUsize"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Power (typical):</td>
			<td valign="top" class="value" id="showModelPower"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Front image:</label></td>
			<td valign="top" class="value" id="showModelFrontImage"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Rear image:</td>
			<td valign="top" class="value" id="showModelRearImage"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Use Image:</td>
			<td valign="top" class="value" id="showModelUseImage"></td>
	        </tr>
		<tr id="showModelBladeRowsTr">
			<td valign="top" class="name">Blade Rows:</td>
			<td valign="top" class="value" id="showModelBladeRows"></td>
		</tr>
		<tr id="showModelBladeCountTr">
			<td valign="top" class="name">Blade Count:</td>
			<td valign="top" class="value" id="showModelBladeCount"></td>
		</tr>
		<tr id="showModelBladLabelCountTr">
			<td valign="top" class="name">Blade Label Count:</td>
			<td valign="top" class="value" id="showModelBladLabelCount"></td>
		</tr>
		<tr id="showModelBladeHeightTr">
			<td valign="top" class="name">Blade Height:</td>
			<td valign="top" class="value" id="showModelBladeHeight"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Source TDS:</td>
			<td valign="top" class="value" id="showModelSourceTds"></td>
	        </tr>
		<tr>
			<td valign="top" class="name">Notes:</td>
			<td valign="top" class="value" id="showModelNotes"></td>
		</tr>
		</tbody>
	</table>
</div>
<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
<div id ="createAsset" style="display: none" title="Create Asset"></div>
<div id ="showAssetList" style="display: none" title="Show Asset"></div>
<div id ="editAsset" style="display: none" title="Edit Asset"></div>

<div style="display: none;">
<table id="assetDependencyRow">
	<tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
		<td><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></td>
		<td><g:select name="dtype" from="${assetDependency.constraints.type.inList}"></g:select></td>
		<td><g:select name="status" from="${assetDependency.constraints.status.inList}"></g:select></td>
	</tr>
	</table>
</div>
<div style="display: none;">
<span id="Server"><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
<span id="Files"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
</div>
<div class="buttons"> 
	<g:form action="edit" controller="model" target="new">
	<input type="hidden" name="assetId" value="${assetEntityInstance?.id}" />
		<input name="id" type="hidden" id="show_modelId"/>
		<span class="button">
			<input type="submit" class="edit" value="Edit"></input>
		</span>
	</g:form>
</div>
</jsec:hasAnyRole>
</div>
<script type="text/javascript">
$('#assetMenu').show();

function showAssetDialog(e){
    var resp = e.responseText;
	$("#showAssetList").html(resp);
	$("#showAssetList").dialog('option', 'width', 'auto');
	$("#showAssetList").dialog('option', 'position', ['center','top']);
	$("#showAssetList").dialog('open');
	$("#createAsset").dialog('close');
	$("#editAsset").dialog('close');
}
function createAssetPage(){
	${remoteFunction(action:'create', onComplete:'showCreateView(e)')}
	
}
function showCreateView(e){
	var resp = e.responseText;
	$("#createAsset").html(resp)	
	$("#createAsset").dialog('option', 'width', 'auto');
	$("#createAsset").dialog('option', 'position', ['center','top']);
	$("#createAsset").dialog('open');
	$("#showAssetList").dialog('close');
	$("#editAsset").dialog('close');
}
function selectManufacturer(value){
	var val = value;
	${remoteFunction(action:'getManufacturersList', params:'\'assetType=\' + val ', onComplete:'showManufacView(e)' )}
	}
function showManufacView(e){
	alert("WARNING : Change of Asset Type may impact on Manufacturer and Model, Do you want to continue ?");
    var resp = e.responseText;
    $("#manufacturerId").html(resp);
    $("#manufacturers").removeAttr("multiple")
}
function selectModel(value){
	var val = value;
	var assetType = $("#assetTypeId").val() ;
	${remoteFunction(action:'getModelsList', params:'\'assetType=\' +assetType +\'&manufacturer=\'+ val', onComplete:'showModelView(e)' )}
	}
function showModelView(e){
	alert("WARNING : Change of Manufacturer may impact on Model data, Do you want to continue ?")
    var resp = e.responseText;
    $("#modelId").html(resp);
    $("#models").removeAttr("multiple")
}
function createEditPage(value){
	var val = value
	${remoteFunction(action:'edit',params:'\'id=\' + val ', onComplete:'showEditView(e)')}
	
}
function showEditView(e){
	var resp = e.responseText;
	$("#editAsset").html(resp)	
	$("#editAsset").dialog('option', 'width', 'auto');
	$("#editAsset").dialog('option', 'position', ['center','top']);
	$("#editAsset").dialog('open');
	$("#createAsset").dialog('close');
	$("#showAssetList").dialog('close');
}
function addAssetDependency( type ){
	var rowNo = $("#"+type+"Count").val()
	var rowData = $("#assetDependencyRow tr").html().replace("dataFlowFreq","dataFlowFreq_"+type+"_"+rowNo).replace("asset","asset_"+type+"_"+rowNo).replace("dtype","dtype_"+type+"_"+rowNo).replace("status","status_"+type+"_"+rowNo).replace("entity","entity_"+type+"_"+rowNo)
	if(type!="support"){
		$("#createDependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	} else {
		$("#createSupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	}
	$("#"+type+"Count").val(parseInt(rowNo)+1)
}
function deleteRow( rowId ){
	$("#"+rowId).remove()
}
function updateAssetsList( name, value ){
	var idValues = name.split("_")
	$("select[name='asset_"+idValues[1]+"_"+idValues[2]+"']").html($("#"+value+" select").html())
}
function getAppDetails(type, value){
	if(type == "Server"){
	   var val = value
	   ${remoteFunction(action:'show', params:'\'id=\' + value ', onComplete:'showAssetDialog(e)')}
	}
}
</script>
</body>
</html>
