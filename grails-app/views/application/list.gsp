<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="com.tds.asset.Database" %>
<%@page import="com.tds.asset.Files" %>
<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tds.asset.Application" %>
<%@page import="net.transitionmanager.security.Permission"%>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Application list</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="projectStaff.js" />

		<g:render template="/layouts/responsiveAngularResources" />

		<g:javascript src="asset.comment.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<asset:stylesheet href="css/jquery.autocomplete.css" />
		<asset:stylesheet href="css/ui.accordion.css" />
		<asset:stylesheet href="css/ui.resizable.css" />
		<asset:stylesheet href="css/ui.slider.css" />
		<asset:stylesheet href="css/ui.tabs.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<script type="text/javascript">

            $(document).on('entityAssetUpdated',function (e,obj) {
                $("#messageId").html(obj.asset.assetName + ' Updated').show();
                $('#applicationIdGrid').trigger("reloadGrid");
            });
            $(document).on('entityAssetCreated',function (e,obj) {
                if(obj != null){
                	$("#messageId").html(obj.asset.assetName + ' Created').show();
                }
                $('#applicationIdGrid').trigger("reloadGrid");
            });
			$(document).ready(function() {
				$("#createEntityView").dialog({ autoOpen: false });
                $("#cloneEntityView").dialog({ autoOpen: false });
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false});
				$("#cablingDialogId").dialog({ autoOpen:false })
				$("#manufacturerShowDialog").dialog({ autoOpen: false })
				$("#modelShowDialog").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })
				$("#filterPane").draggable()

				// JqGrid implementations
				var filter = '${filter}'
				var latencys = '${latencys}'
			  	var planMethodology = '${planMethodology}'
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
				var unassigned = '${unassigned}'
				var ufp = '${ufp}'

				var sizePref = '${sizePref}'
				var listCaption = 'Application: \
					<tds:hasPermission permission="${Permission.AssetEdit}">\
						<span class="capBtn"><input type="button" value="Create App" onclick="EntityCrud.showAssetCreateView(\'${assetClass}\');"/></span>\
					</tds:hasPermission>\
					<tds:hasPermission permission="${Permission.AssetDelete}">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Application\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="application" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'applicationId\')"/></g:else></span>'
				<jqgrid:grid id="applicationId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','id', 'commentType', 'Event'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false, width:'90', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${appPref['1']}',width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${appPref['2']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${appPref['3']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${appPref['4']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${appPref['5']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true},
						{name:'event', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('applicationId');recompileDOM('applicationIdWrapper');}"
					onSelectRow="validateMergeCount"
					showPager="true"
					loadComplete="function(){resizeGrid('applicationId')}"
					postData="{filter: filter, event:event, latencys:latencys, planMethodology:planMethodology, plannedStatus:plannedStatus, validationFilter:validation, moveBundleId:moveBundleId,
						assetName:appName, planStatus:planStatus, moveBundle:moveBundle, validation:validationFilter, sme:appSme,
						toValidate:toValidate,runbook:runbook, unassigned:unassigned, ufp:ufp}">
					<jqgrid:navigation id="applicationId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="applicationId" />
				</jqgrid:grid>

				TDS.jqGridFilterToolbar('applicationId');

				populateFilter();

				$("#del_applicationIdGrid").click(function(){
					$("#applicationId").jqGrid("editGridRow","new",
						{afterSubmit:deleteMessage}
					);
				});

				<%-- Add the Column Selector arrow to the customizable columns --%>
				<g:each var="key" in="${appPref.keySet().toList()}">
					$("#applicationIdGrid_${appPref[key]}").append('<asset:image src="images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_${key}" onclick="showSelect(\\\'${appPref[key]}\\\',\\\'application\\\',\\\'${key}\\\')" />');
				</g:each>

				$.jgrid.formatter.integer.thousandsSeparator='';

			function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? _.escape(cellvalue) : ''
				return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\','+options.rowId+')">'+value+'</a>'
			}

			function myCustomFormatter (cellVal, options, rowObject) {
				var actionButton = '';
				if (${hasPerm}) { // Valid what? what does it means hasPerm, perm on what?
                    actionButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\','+options.rowId+');" title=\'Edit Asset\'>' +
						'<asset:image src="icons/database_edit.png" border="0px" /></a>&nbsp;&nbsp;';
				}
                actionButton += "<grid-buttons asset-id='" + options.rowId +
					"' asset-type='" + rowObject[8] +
					"' tasks='" + rowObject[7] + "' comments='" + rowObject[10] +
                    "' can-view-tasks='" + ${canViewTasks} + "' can-view-comments='" + ${canViewComments} +
                    "' can-create-tasks='" + ${canCreateTasks} + "' can-create-comments='" + ${canCreateComments} + "'>" +
					"</grid-buttons>";

				<tds:hasPermission permission="${Permission.AssetCreate}">
					var value = rowObject[1] ? _.escape(rowObject[1]) : '';
					actionButton += '&nbsp;&nbsp;<a href="javascript:EntityCrud.cloneAssetView(\'${assetClass}\', \'' +
						value + '\', '+options.rowId+');" title=\'Clone Asset\'>' +
						'<asset:image src="icons/database_copy.png" border="0px" /></a>';
                </tds:hasPermission>

				return actionButton;
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
		<tds:subHeader title="Application List${(event)?(' for Move Event '+moveEvent.name):('')}" crumbs="['Assets', 'Applications']"/>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<g:render template="/assetEntity/listTitleAlerts" ></g:render>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${appPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width:13.7% !important;">
						<input type="hidden" id="previousValue_${key}" value="${appPref[key]}" />
						%{--<g:each var="attribute" in="${attributesList}">--}%
						<g:each var="attribute" in="${fieldSpecs}">
							<label>
								<input type="radio" name="coloumnSelector_${appPref[key]}" id="coloumnSelector_${appPref[key]}" value="${attribute.attributeCode}"
								${appPref[key]==attribute.attributeCode ? 'checked' : '' } style="margin-left:11px;"
								onchange="setColumnAssetPref(this.value,'${key}','${com.tdsops.tm.enums.domain.UserPreferenceEnum.App_Columns}')"
								/> ${attribute.frontendLabel}
							</label>
							<br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div id="commentScopeId" ng-controller="tds.comments.controller.MainController as comments">
				<jqgrid:wrapper id="applicationId" />
			</div>
			<g:render template="/assetEntity/modelDialog"/>
			<g:render template="/assetEntity/entityCrudDivs" />
			<g:render template="/assetEntity/dependentAdd" />
			<div id="createStaffDialog" style="display:none;" class="static-dialog">
				<g:render template="/person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
		</div>
            <g:render template="/assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$($(".menu-parent-assets-application-list")[1]).addClass('active');
			$(".menu-parent-assets").addClass('active');
		</script>
	</body>
</html>
