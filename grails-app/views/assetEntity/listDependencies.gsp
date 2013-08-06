<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Dependencies List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<g:javascript src="entity.crud.js" />
		<jqgrid:resources />
		
		<script type="text/javascript">
			$(document).ready(function() {
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				var listCaption ="Dependencies: \
					<tds:hasPermission permission='AssetDelete'>\
					<span class='capBtn'><input type='button' id='deleteAssetId' value='Bulk Delete' onclick='deleteAssets(\"dependencies\")' disabled='disabled'/></span>\
					</tds:hasPermission>"
				<jqgrid:grid id="dependencyGridId" url="'${createLink(action: 'listDepJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Asset','AssetClass', 'Bundle','Type', 'Dependency', 'Dep Class', 'Dep Bundle', 'Frequency', 'Status'"
					colModel="{name:'assetName', index: 'assetId', width:'200',formatter: myLinkFormatter},
								  {name:'assetType', editable: true},
								  {name:'assetbundle', editable: true},
								  {name:'type', editable: true}, 
								  {name:'dependentName', editable: true,formatter: dependentFormatter,width:'200'},
								  {name:'depType', editable: true},
								  {name:'depBundle', editable: true},
								  {name:'frequency', editable: true,width:'90'},
								  {name:'status', editable: true, width:'80'}"
					sortname="'assetName'"
					sortable = "true"
					caption="listCaption"
					height="'100%'"
					rowNum="'25'"
					rowList= "'25','100','500','1000'"
					viewrecords="true"
					multiselect="true"
					loadComplete="initCheck"
					showPager="true"
					datatype="'json'">
					<jqgrid:filterToolbar id="dependencyGridId" searchOnEnter="false" />
					<jqgrid:navigation id="dependencyGridId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="dependencyGridId" />
				</jqgrid:grid>
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
						return '<a href="javascript:getEntityDetails(\'dependencies\',\''+rowObjcet[1]+'\',\''+rowObjcet[9]+'\')">'+value+'</a>'
				}
				function dependentFormatter(cellvalue, options, rowObjcet){
					var value = cellvalue ? cellvalue : ''
						return '<a href="javascript:getEntityDetails(\'dependencies\',\''+rowObjcet[5]+'\',\''+rowObjcet[10]+'\')">'+value+'</a>'
				}
				
				$('#dependencyGridIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
				$('#dependencyGridIdGrid').fluidGrid({ base:'#dependencyGridIdWrapper', offset: 0 });
				
			})
			$(window).resize(resizeGrid);
			
			// Called when the window is resized to resize the grid wrapper 
			function resizeGrid(){
				$('#dependencyGridIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
				$('#dependencyGridIdGrid').fluidGrid({ base:'#dependencyGridIdWrapper', offset: 0 });
			}
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Dependencies List</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<jqgrid:wrapper id="dependencyGridId" />
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
		</div>
	</body>
</html>