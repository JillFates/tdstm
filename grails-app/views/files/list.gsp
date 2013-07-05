<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Storage List</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />

<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
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
	
	var filter = '${filter}'
	var event = '${event}'
	var plannedStatus = '${plannedStatus}' 
	var validation = '${validation}'
	var	moveBundleId = '${moveBundleId}'
	var fileName = '${fileName}'
	var planStatus = '${planStatus}'
	var moveBundle = '${moveBundle}'
	var fileFormat = '${fileFormat}'
	var fileSize = '${fileSize}'
	var sizePref = '${sizePref}'
	var listCaption ="Storages:<span class='capBtn'><input type='button' value='Create Storage'  onclick='createAssetDetails(\"Files\")'/></span>"
	// JqGrid implementations 
    <jqgrid:grid id="storageId" url="'${createLink(action: 'listJson')}'"
    editurl="'${createLink(action: 'deleteBulkAsset')}'"
    colNames="'Actions','Name', 'Storage Format', 'Storage Size', 'Plan Status','Bundle','Dep # ','Dep to resolve','Dep Conflicts','id', 'commentType'"
    colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:'80'},
      			  {name:'assetName',index: 'assetName', editable: true, formatter: myLinkFormatter, width:'300'},
      			  {name:'fileFormat', editable: true},
      			  {name:'fileSize', editable: true},
                  {name:'planStatus', editable: true}, 
                  {name:'moveBundle', editable: true},
                  {name:'depNumber', editable: false,sortable:false,search:false},
                  {name:'depResolve', editable: false,sortable:false,search:false },
                  {name:'depConflicts', editable: false,sortable:false,search:false},
                  {name:'id', hidden: true},
                  {name:'commentType', hidden: true} "
    sortname="'assetName'"
    caption="listCaption"
   	height="'100%'"
   	rowNum="sizePref"
 	rowList= "'25','100','500','1000'"
    multiselect="true"
    viewrecords="true"
   	postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId, assetName:fileName, 
   	   	planStatus:planStatus, moveBundle:moveBundle, fileFormat:fileFormat, fileSize:fileSize}"
    showPager="true"
    datatype="'json'">
    <jqgrid:filterToolbar id="storageId" searchOnEnter="false" />
    <jqgrid:navigation id="storageId" add="false" edit="false" del="true" search="false" refresh="true"/>
</jqgrid:grid>
	populateFilter();

	$.jgrid.formatter.integer.thousandsSeparator='';
function myLinkFormatter (cellvalue, options, rowObjcet) {
	var value = cellvalue ? cellvalue : ''
	return '<a href="javascript:getEntityDetails(\'files\',\''+rowObjcet[10]+'\','+options.rowId+')">'+value+'</a>'
}

function myCustomFormatter (cellVal,options,rowObject) {
	var editButton = '<a href="javascript:editEntity(\'files\',\''+rowObject[10]+'\','+options.rowId+')">'+
			"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
	if(rowObject[9]=='issue'){
		var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
			+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
		editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
			+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
	} else if (rowObject[9]=='comment') {
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

function populateFilter(){
	$("#gs_assetName").val('${fileName}')
	$("#gs_fileFormat").val('${fileFormat}')
	$("#gs_fileSize").val('${fileSize}')
	$("#gs_planStatus").val('${planStatus}')
	$("#gs_moveBundle").val('${moveBundle}')
}
$('#storageIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
$('#storageIdGrid').fluidGrid({ base:'#storageIdWrapper', offset: 0 });
})
$(window).resize(resizeGrid);

// Called when the window is resized to resize the grid wrapper 
function resizeGrid(){
	$('#storageIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
	$('#storageIdGrid').fluidGrid({ base:'#storageIdWrapper', offset: 0 });
}

</script>

</head>
<body>
<div class="body fluid">
<h1>Storage List</h1>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>
<jqgrid:wrapper id="storageId" /> 
	<div class="buttons">
	<tds:hasPermission permission='EditAndDelete'>
		<span class="button"><input type="button" class="save"
			value="Create Storage"
			onclick='createAssetDetails("Files")' />
		</span>
	</tds:hasPermission>
	</div>
<div id="createEntityView" style="display: none;" ></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>	
<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Application','Server','Database','Storage','Network']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
		<td><span id="Server"><g:select name="asset" from="${servers}" optionKey="${-2}" optionValue="${1}" style="width:90px;"></g:select></span></td>
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
</div>
<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
</body>
</html>
