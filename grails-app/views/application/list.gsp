<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Application list</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>	
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
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
				$("#cablingDialogId").dialog({ autoOpen:false })
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
				var latencys = '${latencys}'
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
				var toValidate = '${toValidate}'
				var runbook = '${runbook}'
				
				var sizePref = '${sizePref}'
				var listCaption ='Applications: \
					<tds:hasPermission permission="AssetEdit">\
						<span class="capBtn"><input type="button" value="Create App"  onclick="createAssetDetails(\'Application\')"/></span>\
					</tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Application\')" disabled="disabled"/></span>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					</tds:hasPermission>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="application" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'applicationId\')"/></g:else></span>'
				<jqgrid:grid id="applicationId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','id', 'commentType', 'Event'"
					colModel="{name:'act', index: 'act' , sortable: false, ${hasPerm? 'formatter:myCustomFormatter,' :''} search:false, width:'50', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${appPref['1']}',width:'120'},
						{name:'${appPref['2']}', width:'120'},
						{name:'${appPref['3']}', width:'120'}, 
						{name:'${appPref['4']}', width:'120'},
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
					postData="{filter: filter, event:event, latencys:latencys, plannedStatus:plannedStatus, validationFilter:validation, moveBundleId:moveBundleId,
						assetName:appName, planStatus:planStatus, moveBundle:moveBundle, validation:validationFilter, sme:appSme, toValidate:toValidate,runbook:runbook}">
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
					$("#applicationIdGrid_"+appPref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+appPref+'\',\'application\',\''+${key}+'\')">');
				</g:each>
				$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? cellvalue : ''
				return '<a href="javascript:getEntityDetails(\'application\',\'Application\','+options.rowId+')">'+value+'</a>'
			}

			function myCustomFormatter (cellVal,options,rowObject) {
				var editButton = '<a href="javascript:editEntity(\'application\',\'Application\','+options.rowId+')">'+
						"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
				if (rowObject[6]=='issue') {
					var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
						+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
					editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
						+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
				} else if (rowObject[6]=='comment') {
					var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
						+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
					editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
						+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_bold.png')}' border='0px'/>"+"</a></span>"
				} else {
					editButton+='<span id="icon_'+options.rowId+'"><a href="javascript:createNewAssetComment('+options.rowId+',\''+rowObject[1]+'\',\''+rowObject[7]+'\')">'
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
					<div class="columnDiv_${key} customScroll customizeDiv" style="width:13.7% !important;">
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
			<div id="cablingDialogId" style="display: none;"></div>
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
