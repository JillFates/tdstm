<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
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
				var listCaption ='DataBases: <tds:hasPermission permission="EditAndDelete"><span class="capBtn"><input type="button" value="Create DB" onclick="createAssetDetails(\'Database\')"/></span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Database\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
					<g:if test="${moveEvent != null}"><g:link class="mmlink" controller="database" action="list"><span class="capBtn"><input type="button" value="Clear Filters" /></span></g:link></g:if>'		
				<jqgrid:grid id="databaseId" url="'${createLink(action: 'listJson')}'"
				editurl="'${createLink(action: 'deleteBulkAsset')}'"
				colNames="'Actions','Name', 'DB Format','Plan Status','Bundle','Dep # ','Dep to resolve','Dep Conflicts','id', 'commentType'"
				colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:'50', fixed:false},
							  {name:'assetName',index: 'assetName', editable: true, formatter: myLinkFormatter, width:'300'},
							  {name:'dbFormat', editable: true},
							  {name:'planStatus', editable: true}, 
							  {name:'moveBundle', editable: true},
							  {name:'depNumber', editable: false,sortable:false,search:false},
							  {name:'depResolve', editable: false,sortable:false,search:false },
							  {name:'depConflicts', editable: false,sortable:false,search:false},
							  {name:'id', hidden: true},
							  {name:'commentType', hidden: true} "
				sortname="'assetName'"
				sortable = "true"
				caption="listCaption"
				height="'100%'"
				rowNum="sizePref"
				rowList= "'25','100','500','1000'"
				multiselect="true"
				loadComplete="initCheck"
				viewrecords="true"
				postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId,
					assetName:dbName, planStatus:planStatus, moveBundle:moveBundle, dbFormat:dbFormat}"
				showPager="true"
				datatype="'json'">
				<jqgrid:filterToolbar id="databaseId" searchOnEnter="false" />
				<jqgrid:navigation id="databaseId" add="false" edit="false" del="false" search="false" refresh="false" afterSubmit="deleteMessage"/>
				<jqgrid:resize id="databaseId" resizeOffset="-2" />
				<jqgrid:refreshButton id="databaseId" />
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
			$('#databaseIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
			$('#databaseIdGrid').fluidGrid({ base:'#databaseIdWrapper', offset: 0 });
			})
			$(window).resize(resizeGrid);

			// Called when the window is resized to resize the grid wrapper 
			function resizeGrid(){
				$('#databaseIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
				$('#databaseIdGrid').fluidGrid({ base:'#databaseIdWrapper', offset: 0 });
			}
		</script>

		<title>DB list</title>
	</head>
	<body>

		
		<div class="body fluid">
			<h1>DB List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div >
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<jqgrid:wrapper id="databaseId" /> 
			<div id="createEntityView" style="display: none;" ></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Database', entities:dbs]"></g:render>
		</div>
		<g:render template="../assetEntity/commentCrud"/>
		<g:render template="../assetEntity/modelDialog"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
