<%@page import="net.transitionmanager.security.Permission"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="topNav" />
        <title>Dependencies List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="asset.tranman.js" />
		
		<g:render template="../layouts/responsiveAngularResources" />
		
		<g:javascript src="asset.comment.js" />

		<jqgrid:resources />

		<g:javascript src="jqgrid-support.js" />

		<script type="text/javascript">
		
			$(document).ready(function() {
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				var listCaption ="Dependencies: \
					<tds:hasPermission permission="${Permission.AssetDelete}">\
					<span class='capBtn'><input type='button' id='deleteAssetId' value='Bulk Delete' onclick='bulkDeleteDependencies()' disabled='disabled'/></span>\
					</tds:hasPermission>"
				<jqgrid:grid id="dependencyGridId" url="'${createLink(action: 'listDepJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Asset','Asset Type', 'Bundle','Type', 'Depends On', 'Dep Asset Type', 'Dep Asset Bundle', '${columnLabelpref['1']}', '${columnLabelpref['2']}', 'Status'"
					colModel="{name:'assetName', index: 'assetName', width:'200',formatter: myLinkFormatter},
						{name:'assetType', editable: true},
						{name:'assetBundle', editable: true},
						{name:'type', editable: true, formatter: dependencyViewFormatter},
						{name:'dependentName', editable: true, formatter: dependentFormatter,width:'200'},
						{name:'dependentType', editable: true},
						{name:'dependentBundle', editable: true},
						{name:'${depPref['1']}', editable: true,width:'100'},
						{name:'${depPref['2']}',editable:true, width:'100'},
  						{name:'status', editable: true, width:'80'}"
					sortname="'assetName'"
					caption="listCaption"
					multiselect="true"
					loadComplete="initCheck"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					gridComplete="function(){bindResize('dependencyGridId')}"
					onSelectRow="validateMergeCount"
					showPager="true">
					<jqgrid:navigation id="dependencyGridId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="dependencyGridId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('dependencyGridId');

				<g:each var="key" in="['1','2']">
					var depPref= '${depPref[key]}';

				$("#dependencyGridIdGrid_"+depPref).append("<img src=\"${resource(dir:'images',file:'select2Arrow.png')}\" class=\"selectImage editSelectimage_"+${key}+"\" style=\"position:relative;float:right;margin-top: -15px;\" onclick=\"showSelect('"+depPref+"','dependencyGrid','"+${key}+"')\">");

				</g:each>
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObject) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:EntityCrud.showAssetDetailView(\''+rowObject[12]+'\',\''+rowObject[10]+'\')">'+ _.escape(value) +'</a>'
				}
				function dependentFormatter(cellvalue, options, rowObject){
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:EntityCrud.showAssetDetailView(\''+rowObject[13]+'\',\''+rowObject[11]+'\')">'+ _.escape(value) +'</a>'
				}
				function dependencyViewFormatter(cellvalue, options, rowObject) {
					var value = cellvalue ? cellvalue : '';
					return '<a href="javascript:EntityCrud.showAssetDependencyEditView({ id:'+rowObject[10]+' }, { id: '+rowObject[11]+'})">'+ _.escape(value) +'</a>';
				}
			})
		</script>
	</head>
	<body>
		<tds:subHeader title="Dependencies List" crumbs="['Assets','Dependencies']"/>
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<jqgrid:wrapper id="dependencyGridId" />
			<g:each var="key" in="['1','2']">
				<div id="columnCustomDiv_${depPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll" style="background-color: #F8F8F8 ;height: 133px;position: fixed; top: 148px;width:8%;;z-index: 2147483647; overflow-y: scroll;text-align: left;">
						<input type="hidden" id="previousValue_${key}" value="${depPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${depPref[key]}" id="coloumnSelector_${depPref[key]}" value="${attribute}" 
								${depPref[key]==attribute?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','${com.tdsops.tm.enums.domain.UserPreferenceEnum.Dep_Columns}')"
								/> 
								${attribute}	
							</label>
							<br>
						</g:each>
					</div>
				</div>
			</g:each>
			<g:render template="../assetEntity/entityCrudDivs" />
			<g:render template="../assetEntity/dependentAdd" />
			<g:render template="initAssetEntityData"/>
		</div>
		<script>
			$(".menu-parent-assets-dependencies-list").addClass('active');
			$(".menu-parent-assets").addClass('active');

			$(document).on('entityAssetUpdated',function () {
				$('#dependencyGridId').trigger('click');
			});
		</script>
		<div class="tdsAssetsApp" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets"></div>
	</body>
</html>