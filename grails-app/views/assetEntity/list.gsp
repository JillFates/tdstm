<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset List</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<jqgrid:resources />
<jqui:resources /> 
<jqgrid:resources />

<script type="text/javascript">
// TODO : move this code to JS once verified in tmdev

$(document).ready(function() {
			$("#createEntityView").dialog({ autoOpen: false })
			$("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
	        $("#commentsListDialog").dialog({ autoOpen: false })
	        $("#createCommentDialog").dialog({ autoOpen: false })
	        $("#showCommentDialog").dialog({ autoOpen: false })
	        $("#editCommentDialog").dialog({ autoOpen: false })
	        $("#manufacturerShowDialog").dialog({ autoOpen: false })
	        $("#modelShowDialog").dialog({ autoOpen: false })
	        $("#filterPane").draggable()
var filter = '${filter}'
var type = '${type}'
var event = '${event}'
var plannedStatus = '${plannedStatus}' 

var assetName = '${assetName}'
var planStatus = '${planStatus}'
var moveBundle = '${moveBundle}'
var assetType = '${assetType}'
var model = '${model}'
var sourceLocation = '${sourceLocation}'
var sourceRack = '${sourceRack}'
var targetLocation = '${targetLocation}'
var targetRack = '${targetRack}'
var assetTag = '${assetTag}'
var serialNumber = '${serialNumber}'
var sortIndex = '${sortIndex}'
var sortOrder = '${sortOrder}'
var moveBundleId = '${moveBundleId}'
var windowWidth = $(window).width()- 50;
<jqgrid:grid id="assetListId" url="'${createLink(action: 'listJson')}'"
    editurl="'${createLink(action: 'deleteBulkAsset')}'"
    colNames="'Actions','Asset Name', 'Asset Type','Model', 'Location','Rack','Target Location','Target Rack','Tag','Serial#','Plan Status','Bundle',
        'Dep#','Dep Up', 'Dep Down', 'id', 'commentType'"
    colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false,width:'80'},
      			  {name:'assetName',index: 'assetName', editable: true, formatter: myLinkFormatter, width:'300'},
                  {name:'assetType', editable: true},
                  {name:'model', editable: true}, 
                  {name:'sourceLocation', editable: true},
                  {name:'sourceRack', editable: true},
                  {name:'targetLocation', editable: true},
                  {name:'targetRack', editable: true},
                  {name:'assetTag', editable: true},
                  {name:'serialNumber', editable: true},
                  {name:'planStatus', editable: true},
                  {name:'moveBundle', editable: true},
                  {name:'depNumber', editable: false,sortable:false,search:false,width:'50'},
                  {name:'depUp', editable: false,sortable:false,search:false ,width:'50'},
                  {name:'depDown', editable: false,sortable:false,search:false,width:'50'},
                  {name:'id', hidden: true},
                  {name:'commentType', hidden: true} "
    sortname="'assetName'"
    caption="'Asset List'"
   	height="'auto'"
    width="windowWidth"
    rowNum="25"
    rowList= "'25','50','100'"
    multiselect="true"
    viewrecords="true"
    showPager="true"
    postData="{filter: filter, event:event, type:type, plannedStatus:plannedStatus, assetName:assetName, planStatus:planStatus, moveBundle:moveBundle,
   			 planStatus : planStatus,moveBundle : moveBundle, assetType:assetType , model :model , sourceLocation: sourceLocation , sourceRack:sourceRack,
    		 targetLocation:targetLocation, targetRack :targetRack,assetTag :assetTag,serialNumber:serialNumber, moveBundleId:moveBundleId}"
    		 
    datatype="'json'">
    <jqgrid:filterToolbar id="assetListId" searchOnEnter="false" />
    <jqgrid:navigation id="assetListId" add="false" edit="false" del="true" search="false" refresh="true" />
    <jqgrid:resize id="assetListId" resizeOffset="-2" />
</jqgrid:grid>
	populateFilter();

	$("#del_assetListIdGrid").click(function(){
    $("#assetListId").jqGrid("editGridRow","new",
            {afterSubmit:deleteMessage});
     });

function myLinkFormatter (cellvalue, options, rowObjcet) {
	var value = cellvalue ? cellvalue : ''
	return '<a href="javascript:getEntityDetails(\'assetEntity\',\''+rowObjcet[2]+'\','+options.rowId+')">'+value+'</a>'
}

function myCustomFormatter (cellVal,options,rowObject) {
	var editButton = '<a href="javascript:editEntity(\'assetEntity\',\''+rowObject[1]+'\','+options.rowId+')">'+
			"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
	if(rowObject[14]=='issue'){
		var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
			+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
		editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
			+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
	} else if (rowObject[14]=='comment') {
		var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
			+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
		editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
			+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_bold.png')}' border='0px'/>"+"</a></span>"
	} else {
		editButton+='<span id="icon_'+options.rowId+'"><a href="javascript:createNewAssetComment('+options.rowId+',\''+rowObject[1]+'\')">'
			+"<img src='${resource(dir:'i',file:'db_table_light.png')}' border='0px'/>"+"</a></span>"
	}
    return editButton
}

function deleteMessage(response, postdata){
	 $("#messageId").show()
	 $("#messageDivId").hide()
	 $("#messageId").html(response.responseText)
	 $("#delmodassetListIdGrid").remove()
	 $(".jqmOverlay").remove()
      return true
}

function populateFilter(){
	$("#gs_assetName").val('${assetName}')
	$("#gs_assetType").val('${assetType}')
	$("#gs_model").val('${model}')
	$("#gs_sourceLocation").val('${sourceLocation}')
	$("#gs_sourceRack").val('${sourceRack}')
	$("#gs_targetLocation").val('${targetLocation}')
	$("#gs_targetRack").val('${targetRack}')
	$("#gs_serialNumber").val('${serialNumber}')
	$("#gs_planStatus").val('${planStatus}')
	$("#gs_moveBundle").val('${moveBundle}')
	$("#gs_assetTag").val('${assetTag}')
}

})
</script>
</head>
<body>
<div class="body">
<h1>Asset List</h1>
<g:if test="${flash.message}">
	<div id="messageDivId" class="message">${flash.message}</div>
</g:if>
<div >
	<div id="messageId" class="message" style="display:none"></div>
</div>
<div>
	  <jqgrid:wrapper id="assetListId" /> 
</div>
<div class="buttons"><g:form>
<tds:hasPermission permission='EditAndDelete'>
	<span class="button"><input type="button" value="New Asset" class="create" 
	onclick="${remoteFunction(action:'create', onComplete:'createEntityView(e, \'Server\')')}"/></span>
</tds:hasPermission>
</g:form></div>
</div> <%-- End of Body --%>
<g:render template="commentCrud"/>
<g:render template="modelDialog"/>
<div id="createEntityView" style="display: none;"></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
<div style="display: none;">
	<table id="assetDependencyRow">
	<tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)'>
			</g:select></td>
		<td><span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span></td>
		<td><g:select name="dtype" from="${dependencyType.value}"  optionValue="value"></g:select></td>
		<td><g:select name="status" from="${dependencyStatus.value}" optionValue="value"></g:select></td>
	</tr>
	</table>
</div>
<%-- This DIV is used by the Asset Dependency DIVs to populate the form --%>
<div style="display: none;">
<%-- The "Server" SELECT was duplicated above so we are leveraging it above by adding the SPAN tag there. --%>
	<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
	<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
	<span id="Storage"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
</div>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
</body>
</html>
