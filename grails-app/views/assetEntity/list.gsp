<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>${listType=='server'? 'Server' : 'Physical'} List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
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
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet" type="text/css" />
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
				$("#editManufacturerView").dialog({ autoOpen: false})
				$("#cablingDialogId").dialog({ autoOpen:false })
				$("#filterPane").draggable()
				var filter = '${filter}'
				var type = '${type}'
				var event = '${event}'
				var plannedStatus = '${plannedStatus}' 
				var listType = '${listType}'

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
				var windowWidth = $(window).width() - $(window).width()*5/100 ;
				var sizePref = '${sizePref}'
				var listCaption ='Assets: <tds:hasPermission permission="AssetEdit"><span class=\'button\'><input type=\'button\' value=\'Create Asset\' class=\'create\' onclick="createAssetDetails(\'assetEntity\')"/></span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'AssetEntity\')" disabled="disabled"/></span>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					</tds:hasPermission>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="assetEntity" params="[listType: listType?:'server']" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'assetListId\')"/></g:else></span>'
				<jqgrid:grid id="assetListId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Asset Name', 'Asset Type','Model', 'Location','Rack','${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','Plan Status','Bundle',
						 'id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, ${hasPerm? 'formatter:myCustomFormatter,' :''} search:false,width:'40', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'250'},
						{name:'assetType'},
						{name:'model'}, 
						{name:'sourceLocation'},
						{name:'sourceRack'},
						{name:'${assetPref['1']}',width:'130'},
						{name:'${assetPref['2']}', width:'130'},
						{name:'${assetPref['3']}', width:'130'}, 
						{name:'${assetPref['4']}', width:'130'},
						{name:'planStatus'},
						{name:'moveBundle'},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					width="windowWidth"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('assetListId')}"
					onSelectRow="validateMergeCount"
					showPager="true"
					postData="{filter: filter, event:event, type:type, plannedStatus:plannedStatus, assetName:assetName, planStatus:planStatus, moveBundle:moveBundle,
						moveBundle : moveBundle, assetType:assetType , model :model , sourceLocation: sourceLocation , sourceRack:sourceRack,
						targetLocation:targetLocation, targetRack :targetRack,assetTag :assetTag,serialNumber:serialNumber, moveBundleId:moveBundleId,listType:listType}">
					<jqgrid:filterToolbar id="assetListId" searchOnEnter="false" />
					<jqgrid:navigation id="assetListId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="assetListId" />
				</jqgrid:grid>
				populateFilter();
				$("#del_assetListIdGrid").click(function() {
					$("#assetListId").jqGrid("editGridRow","new",
						{afterSubmit:deleteMessage});
				});

				<g:each var="key" in="['1','2','3','4']">
					var assetPref= '${assetPref[key]}';
					$("#assetListIdGrid_"+assetPref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+assetPref+'\',\'assetList\',\''+${key}+'\')">');
				</g:each>
			
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:getEntityDetails(\'assetEntity\',\''+rowObjcet[2]+'\','+options.rowId+')">'+value+'</a>'
				}

				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = '<a href="javascript:editEntity(\'assetEntity\',\''+rowObject[1]+'\','+options.rowId+')">'+
							"<img src='${resource(dir:'images/skin',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					if(rowObject[12]=='issue'){
						var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
							+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
						editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
							+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
					} else if (rowObject[12]=='comment') {
						var ajaxString = "new Ajax.Request('/tdstm/assetEntity/listComments/"
							+options.rowId+"',{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog( e ,'never' )}})"
						editButton+='<span id="icon_'+options.rowId+'"><a href="#" onclick="setAssetId('+options.rowId+');'
							+ajaxString+'">'+"<img src='${resource(dir:'i',file:'db_table_bold.png')}' border='0px'/>"+"</a></span>"
					} else {
						editButton+='<span id="icon_'+options.rowId+'"><a href="javascript:createNewAssetComment('+options.rowId+',\''+rowObject[1]+'\',\''+rowObject[13]+'\')">'
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
					if(planStatus) {
						$("#gs_planStatus").val(planStatus)
					} else if (plannedStatus){
						$("#gs_planStatus").val(plannedStatus)
					}
					
					$("#gs_moveBundle").val('${moveBundle}')
					$("#gs_assetTag").val('${assetTag}')
				}
				
			})
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>${listType=='server'? 'Server' : 'Physical'  } List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<g:each var="key" in="['1','2','3','4']">
				<div id="columnCustomDiv_${assetPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv">
						<input type="hidden" id="previousValue_${key}" value="${assetPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${assetPref[key]}" id="coloumnSelector_${assetPref[key]}" value="${attribute.attributeCode}" 
								${assetPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:2px;" 
								onchange="setColumnAssetPref(this.value,'${key}','${prefType}')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div>
				  <jqgrid:wrapper id="assetListId" /> 
			</div>
		</div> <%-- End of Body --%>
		<g:render template="commentCrud"/>
		<g:render template="modelDialog"/>
		<div id="createEntityView" style="display: none;"></div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
		<div id="editManufacturerView" style="display: none;"></div>
		<div id="cablingDialogId" style="display: none;"></div>
		<g:render template="newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
