<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Manufacturer List</title>
		<g:render template="../layouts/responsiveAngularResources" />
		<script type="text/javascript" src="${resource(dir:'components/manufacturer',file:'manufacturer.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<asset:stylesheet href="css/jquery.autocomplete.css" />
		<asset:stylesheet href="css/ui.accordion.css" />
		<asset:stylesheet href="css/ui.resizable.css" />
		<asset:stylesheet href="css/ui.slider.css" />
		<asset:stylesheet href="css/ui.tabs.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<g:javascript src="tds-common.js" />
		<g:javascript src="model.manufacturer.js" />
		<script type="text/javascript">
		$(document).ready(function() {
			$("#createManufacturerView").dialog({ autoOpen: false })
			$("#showManufacturerView").dialog({ autoOpen: false })
			var listCaption = "Manufacturers: <span class='capBtn'><input type='button' value='Create Manufacturer' onClick=\"createModelManuDetails('manufacturer','Manufacturer')\"/></span> "
			<jqgrid:grid id="manufacturerId" url="'${createLink(action: 'listJson')}'"
				colNames="'Name','AKA', 'Description', 'Corporate Name', 'Corporate Location', 'Website', 'Models', 'AssetCount'"
				colModel="{name:'name', index: 'name', width:'100',formatter: myLinkFormatter},
					{name:'aka', width:'200',search:false,sortable:false},
					{name:'description',width:'100'},
					{name:'corporateName',width:'100'},
					{name:'corporateLocation',width:'100'},
					{name:'website',width:'100'},
					{name:'models', width:'40',search:false,sortable:false},
					{name:'assetCount',width:'50',search:false,sortable:false}"
				sortname="'name'"
				caption="listCaption"
				rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
				gridComplete="function(){bindResize('manufacturerId')}"
				showPager="true">
				<jqgrid:navigation id="manufacturerId" add="false" edit="false" del="false" search="false" refresh="true" />
			</jqgrid:grid>
			TDS.jqGridFilterToolbar('manufacturerId');

			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:showOrEditModelManuDetails(\'manufacturer\','+options.rowId+',\'Manufacturer\',\'show\',\'Show\')">'+value+'</a>'
			}
		})
		</script>
		<style>
			/*TODO: REMOVE ON COMPLETE MIGRATION */
			div.content-wrapper {
				background-color: #ecf0f5 !important;
			}
		</style>
	</head>
	<body>
	<tds:subHeader title="Manufacturer List" crumbs="['Admin','Manufacturer']"/>
		<div class="body fluid" ng-app="tdsManufacturers" ng-controller="tds.manufacturers.controller.MainController as manufacturers">
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<div >
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<div>
				<jqgrid:wrapper id="manufacturerId" />
			</div>
			<div id="createManufacturerView" style="display: none;" ></div>
			<div id="showManufacturerView" style="display: none;" ></div>
		</div>
		<script>
			$('.menu-list-manufacturers').addClass('active');
			$('.menu-parent-admin').addClass('active');
		</script>
	</body>
</html>