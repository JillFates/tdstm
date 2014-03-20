<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Bundle List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		
		<script type="text/javascript">
		$(document).ready(function() {
			var listCaption ="Bundles: <tds:hasPermission permission='MoveBundleEditView'><span class='capBtn'>"+
				"<input type='button' value='Create Bundle' onClick=\"window.location.href=\'"+contextPath+"/moveBundle/create\'\"/></span></tds:hasPermission>"			
			<jqgrid:grid id="bundleGridId" url="'${createLink(action: 'listJson')}'"
				colNames="'Name', 'Description','Planning', 'Asset Qty', 'Start', 'Completion'"
				colModel="{name:'name', index: 'name', width:'150',formatter: myLinkFormatter},
					{name:'description', width:'150'},
					{name:'useForPlanning',width:'150'},
					{name:'assetQty',width:'100', search:false},
					{name:'startTime', width:'150'},
					{name:'completionTime',width:'100'}"
				sortname="'name'"
				caption="listCaption"
				width="'500px'"
				gridComplete="function(){bindResize('bundleGridId')}"
				showPager="true">
				<jqgrid:filterToolbar id="bundleGridId" searchOnEnter="false" />
				<jqgrid:navigation id="bundleGridId" add="false" edit="false" del="false" search="false"/>
				<jqgrid:refreshButton id="bundleGridId" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
					return '<a href="'+contextPath+'/moveBundle/show/'+options.rowId+'">'+value+'</a>'
			}
		});
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Bundle List</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message" >${flash.message}</div>
			</g:if>
			<div >
				<div id="messageId" class="message" style="display:none">
				</div>
			</div>
			<table id="gridTableId" style="width:58%!important;">
				<tr>
					<td><jqgrid:wrapper id="bundleGridId" /></td>
				</tr>
			</table>
		</div>
		<script>
			currentMenuId = "#eventMenu";
			$("#eventMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>