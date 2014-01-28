<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Storage List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
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
				var size = '${size}'
				var sizePref = '${sizePref}'
				var listCaption ='Storages: <tds:hasPermission permission="EditAndDelete"><span class="capBtn"><input type="button" value="Create Storage" onclick="createAssetDetails(\'Files\')"/></span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Files\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
					<g:if test="${moveEvent != null}"><g:link class="mmlink" controller="files" action="list"><span class="capBtn"><input type="button" value="Clear Filters" /></span></g:link></g:if>'
				// JqGrid implementations 
				<jqgrid:grid id="storageId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','Dep # ','Dep to resolve','Dep Conflicts','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:'80'},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${filesPref['1']}',width:'120'},
						{name:'${filesPref['2']}', width:'120'},
						{name:'${filesPref['3']}', width:'120'}, 
						{name:'${filesPref['4']}', width:'120'},
						{name:'depNumber',sortable:false,search:false},
						{name:'depResolve',sortable:false,search:false },
						{name:'depConflicts',sortable:false,search:false},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('storageId')}"
					onSelectRow="validateMergeCount"
					postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId, assetName:fileName, 
						planStatus:planStatus, moveBundle:moveBundle, fileFormat:fileFormat, size:size}"
					showPager="true">
					<jqgrid:filterToolbar id="storageId" searchOnEnter="false" />
					<jqgrid:navigation id="storageId" add="false" edit="false" del="false" search="false" refresh="false"/>
					<jqgrid:refreshButton id="storageId" />
				</jqgrid:grid>
				populateFilter();

				<g:each var="key" in="['1','2','3','4']">
					var filePref= '${filesPref[key]}';
					$("#storageIdGrid_"+filePref).append('<img src="../images/select2Arrow.png" class="selectImage editSelectimage_'+${key}+'" style="position:absolute;margin-left: 42px;margin-top: -15px;" onclick="showSelect(\''+filePref+'\',\'storage\',\''+${key}+'\')">');
				</g:each>
				
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
					$("#gs_size").val('${size}')
					$("#gs_planStatus").val('${planStatus}')
					$("#gs_moveBundle").val('${moveBundle}')
				}
			})
		</script>
		
	</head>
	<body>
		<div class="body fluid">
			<h1>Storage List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<jqgrid:wrapper id="storageId" />
			<g:each var="key" in="['1','2','3','4']">
				<div id="columnCustomDiv_${filesPref[key]}" style="display:none;">
					<div class="columnDiv_${key}" style="background-color: #F8F8F8 ;height: 300px;position: fixed; top: 148px;width: 116px;z-index: 2147483647; overflow-y: scroll;text-align: left;">
						<input type="hidden" id="previousValue_${key}" value="${filesPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${filesPref[key]}" id="coloumnSelector_${filesPref[key]}" value="${attribute.attributeCode}" 
								${filesPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','Storage_Columns')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div id="createEntityView" style="display: none;"></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>	
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Storage', entities:files]"></g:render>
		</div>
		<g:render template="../assetEntity/commentCrud"/>
		<g:render template="../assetEntity/modelDialog"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
