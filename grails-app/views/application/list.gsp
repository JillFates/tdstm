<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Application list</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="projectStaff.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />

		<script type="text/javascript">
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
				$("#createStaffDialog").dialog({ autoOpen: false })
				$("#filterPane").draggable()

				// JqGrid implementations 
				var filter = '${filter}'
				var latency = '${latency}'
				var event = '${event}'
				var moveEvent = '${moveEvent}'
				var plannedStatus = '${plannedStatus}' 
				var validation = '${validation}'
				var moveBundleId = '${moveBundleId}'
				var appName = '${appName}'
				var planStatus = '${planStatus}'
				var moveBundle = '${moveBundle}'
				var validationFilter = '${validationFilter}'
				var appSme = '${appSme}'
				
				var sizePref = '${sizePref}'
				var listCaption ='Applications: \
					<tds:hasPermission permission="EditAndDelete">\
						<span class="capBtn"><input type="button" value="Create App"  onclick="createAssetDetails(\'Application\')"/></span>\
					</tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Application\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
					<g:if test="${moveEvent != null}"><g:link class="mmlink" controller="application" action="list"><span class="capBtn"><input type="button" value="Clear Filters" /></span></g:link></g:if>'
				<jqgrid:grid id="applicationId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','Dep # ','Dep to resolve','Dep Conflicts','id', 'commentType', 'Event'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:'50', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${appPref['1']}',width:'120'},
						{name:'${appPref['2']}', width:'120'},
						{name:'${appPref['3']}', width:'120'}, 
						{name:'${appPref['4']}', width:'120'},
						{name:'depNumber'},
						{name:'depResolve'},
						{name:'depConflicts'},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true},
						{name:'event', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('applicationId')}"
					onSelectRow="validateMergeCount"
					showPager="true"
					loadComplete=function(){
						resizeGrid()
					}
					postData="{filter: filter, event:event, latency:latency, plannedStatus:plannedStatus, validationFilter:validation, moveBundleId:moveBundleId,
						assetName:appName, planStatus:planStatus, moveBundle:moveBundle, validation:validationFilter, sme:appSme}">
					<jqgrid:filterToolbar id="applicationId" searchOnEnter="false" />
					<jqgrid:navigation id="applicationId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="applicationId" />
				</jqgrid:grid>
				populateFilter();
				
				$("#del_applicationIdGrid").click(function(){
					$("#applicationId").jqGrid("editGridRow","new",
						{afterSubmit:deleteMessage}
					);
				});
				<g:each var="key" in="['1','2','3','4']">
					var appPref= '${appPref[key]}';
					$("#applicationIdGrid_"+appPref).append('<img src="../images/select2Arrow.png" class="selectImage editSelectimage_'+${key}+'" style="position:absolute;margin-left: 42px;margin-top: -15px;" onclick="showSelect(\''+appPref+'\',\'application\',\''+${key}+'\')">');
				</g:each>
				$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? cellvalue : ''
				return '<a href="javascript:getEntityDetails(\'application\',\'Application\','+options.rowId+')">'+value+'</a>'
			}

			function myCustomFormatter (cellVal,options,rowObject) {
				var editButton = '<a href="javascript:editEntity(\'application\',\'Application\','+options.rowId+')">'+
						"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
				if (rowObject[9]=='issue') {
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
			function deleteMessage(response, postdata) {
				 $("#messageId").show()
				 $("#messageDivId").hide()
				 $("#messageId").html(response.responseText)
				 $("#delmodapplicationIdGrid").hide()
				 return true
			}

			function populateFilter() {
				$("#gs_assetName").val('${appName}')
				$("#gs_sme").val('${appSme}')
				
				if (validationFilter)
					$("#gs_validation").val('${validationFilter}')
				else if ( validation )
					$("#gs_validation").val('${validation}')
					
				if (planStatus)
					$("#gs_planStatus").val('${planStatus}')
				else if( plannedStatus )
					$("#gs_planStatus").val( plannedStatus )
					
				if (event)
					$("#gs_event").val('${event}')
				else if( event )
					$("#gs_event").val( event )
				
				$("#gs_assetName").trigger( 'keydown' );
			}
			})
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Application List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<g:each var="key" in="['1','2','3','4']">
				<div id="columnCustomDiv_${appPref[key]}" style="display:none;">
					<div class="columnDiv_${key}" style="background-color: #F8F8F8 ;height: 300px;position: fixed; top: 148px;width: 120px;z-index: 2147483647; overflow-y: scroll;text-align: left;">
						<input type="hidden" id="previousValue_${key}" value="${appPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${appPref[key]}" id="coloumnSelector_${appPref[key]}" value="${attribute.attributeCode}" 
								${appPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','App_Columns')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<jqgrid:wrapper id="applicationId" />
			<g:render template="../assetEntity/commentCrud"/>
			<g:render template="../assetEntity/modelDialog"/>
			<div id="createEntityView" style="display: none;" ></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="createStaffDialog" style="display:none;">
				<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Application', entities:applications]"></g:render>
			</div>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
