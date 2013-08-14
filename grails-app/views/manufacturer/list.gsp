<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Manufacturer List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<g:javascript src="model.manufacturer.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<script type="text/javascript">
		$(document).ready(function() {
			$("#createManufacturerView").dialog({ autoOpen: false })
			$("#showManufacturerView").dialog({ autoOpen: false })
			var listCaption = "Manufacturers: <span class='capBtn'><input type='button' value='Create Manufacturer' onClick=\"createModelManuDetails('manufacturer','Manufacturer')\"/></span> "
			<jqgrid:grid id="manufacturerId" url="'${createLink(action: 'listJson')}'"
				colNames="'Name','AKA', 'Description','Models', 'AssetCount'"
				colModel="{name:'name', index: 'name', width:'100',formatter: myLinkFormatter},
					{name:'aka', width:'200',search:false,sortable:false},
					{name:'description',width:'100'},
					{name:'models', width:'40',search:false,sortable:false}, 
					{name:'assetCount',width:'50',search:false,sortable:false}"
				sortname="'name'"
				caption="listCaption"
				gridComplete="function(){bindResize('manufacturerId')}"
				showPager="true">
				<jqgrid:filterToolbar id="manufacturerId" searchOnEnter="false" />
				<jqgrid:navigation id="manufacturerId" add="false" edit="false" del="false" search="false" refresh="true" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:showOrEditModelManuDetails(\'manufacturer\','+options.rowId+',\'Manufacturer\',\'show\',\'Show\')">'+value+'</a>'
			}
		})
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Manufacturer List</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div >
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<div>
				<jqgrid:wrapper id="manufacturerId" />
			</div>
			<div id="createManufacturerView" style="display: none;" ></div>
			<div id="showManufacturerView" style="display: none;" ></div>
		</div>
	</body>
</html>