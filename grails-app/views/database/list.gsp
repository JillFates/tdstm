<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<g:javascript src="select2.js" />
<g:javascript src="select2.min.js" />
<link rel="stylesheet" href="${resource(dir:'css',file:'select2.css')}" type="text/css"/>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<jqgrid:resources />
<jqui:resources /> 
<jqgrid:resources />
<script type="text/javascript">

$(document).ready(function() {
	//$('#assetMenu').show();
	$("#createEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
    $("#commentsListDialog").dialog({ autoOpen: false })
    $("#createCommentDialog").dialog({ autoOpen: false })
    $("#showCommentDialog").dialog({ autoOpen: false })
    $("#editCommentDialog").dialog({ autoOpen: false })
	$("#manufacturerShowDialog").dialog({ autoOpen: false })
	$("#modelShowDialog").dialog({ autoOpen: false })


	// JqGrid implementations 
	var filter = '${filter}'
	var event = '${event}'
	var plannedStatus = '${plannedStatus}' 
	var validation = '${validation}'
	var moveBundleId = '${moveBundleId}'
	var dbName = '${dbName}'
	var planStatus = '${planStatus}'
	var moveBundle = '${moveBundle}'
	var dbFormat = '${validationFilter}'
	var sizePref = '${sizePref}'
	var listCaption ="DataBases:<span class='capBtn'><input type='button' value='Create DB'  onclick='createAssetDetails(\"Database\")'/></span>"				
    <jqgrid:grid id="databaseId" url="'${createLink(action: 'listJson')}'"
    editurl="'${createLink(action: 'deleteBulkAsset')}'"
    colNames="'Actions','Name', 'DB Format','Plan Status','Bundle','Dep # ','Dep Up','Dep Down','id', 'commentType'"
    colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:'80'},
      			  {name:'assetName',index: 'assetName', editable: true, formatter: myLinkFormatter, width:'300'},
      			  {name:'dbFormat', editable: true},
                  {name:'planStatus', editable: true}, 
                  {name:'moveBundle', editable: true},
                  {name:'depNumber', editable: false,sortable:false,search:false},
                  {name:'depUp', editable: false,sortable:false,search:false },
                  {name:'depDown', editable: false,sortable:false,search:false},
                  {name:'id', hidden: true},
                  {name:'commentType', hidden: true} "
    sortname="'assetName'"
    caption="listCaption"
   	height="'100%'"
    width="1000"
   	rowNum="sizePref"
 	rowList= "'25','100','500','1000'"
    multiselect="true"
    viewrecords="true"
   	postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId,
   		assetName:dbName, planStatus:planStatus, moveBundle:moveBundle, dbFormat:dbFormat}"
    showPager="true"
    datatype="'json'">
    <jqgrid:filterToolbar id="databaseId" searchOnEnter="false" />
    <jqgrid:navigation id="databaseId" add="false" edit="false" del="true" search="false" refresh="true" afterSubmit="deleteMessage"/>
    <jqgrid:resize id="databaseId" resizeOffset="-2" />
</jqgrid:grid>
	populateFilter();
	$("#del_databaseIdGrid").click(function(){
    $("#databaseId").jqGrid("editGridRow","new",
            {afterSubmit:deleteMessage});
     });

	$.jgrid.formatter.integer.thousandsSeparator='';
function myLinkFormatter (cellvalue, options, rowObjcet) {
	var value = cellvalue ? cellvalue : ''
	return '<a href="javascript:getEntityDetails(\'database\',\''+rowObjcet[9]+'\','+options.rowId+')">'+value+'</a>'
}

function myCustomFormatter (cellVal,options,rowObject) {
	var editButton = '<a href="javascript:editEntity(\'database\',\''+rowObject[9]+'\','+options.rowId+')">'+
			"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
	if(rowObject[8]=='issue'){
		var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
			+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
		editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
			+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
	} else if (rowObject[8]=='comment') {
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
	 $("#delmoddatabaseIdGrid").hide()
       return true
}
function populateFilter(){
	$("#gs_assetName").val('${dbName}')
	$("#gs_dbFormat").val('${dbFormat}')
	$("#gs_planStatus").val('${planStatus}')
	$("#gs_moveBundle").val('${moveBundle}')
}

})
</script>

<title>DB list</title>
</head>
<body>

	
<div class="body">
<h1>DB List</h1>
<g:if test="${flash.message}">
	<div id="messageDivId" class="message">${flash.message}</div>
</g:if>
<div >
	<div id="messageId" class="message" style="display:none"></div>
</div>
<jqgrid:wrapper id="databaseId" /> 
<div class="buttons">
<tds:hasPermission permission='EditAndDelete'>
	<span class="button"><input type="button" class="save" value="Create DB"
		onclick='createAssetDetails("Database")' /></span>
</tds:hasPermission>
</div>
<div id="createEntityView" style="display: none;" ></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
	<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Server','Application','Database','Storage','Network']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
		<td><span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" style="width:150px;"></g:select></span></td>
		<td><g:select name="dtype" from="${dependencyType.value}"  optionValue="value"></g:select></td>
		<td><g:select name="status" from="${dependencyStatus.value}" optionValue="value"></g:select></td>
	</tr>
	</table>
    </div>
    <div style="display: none;">
		<span id="Application"><g:select name="asset" from="${applications}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
		<span id="Database"><g:select name="asset" from="${dbs}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
		<span id="Storage"><g:select name="asset" from="${files}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
		<span id="Network"><g:select name="asset" from="${networks}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span>
	</div>
</div>
<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
</div>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
</body>
</html>
