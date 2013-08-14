<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<title>Company List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		
		<script type="text/javascript">
		function onInvokeAction(id) {
			setExportToLimit(id, '');
			createHiddenInputFieldsForLimitAndSubmit(id);
		}
		$(document).ready(function() {
			var listCaption ="Companies: <span class='capBtn'><input type='button' value='Create Company' onClick=\"window.location.href=\'"+contextPath+"/partyGroup/create\'\"/></span>"
			<jqgrid:grid id="companyId" url="'${createLink(action: 'listJson')}'"
				colNames="'Name','Date Created', 'Last Updated'"
				colModel="{name:'companyName', index: 'companyName', width:'150'},
					{name:'dateCreated', width:'100', formatter:formatDate},
					{name:'lastUpdated', width:'100', formatter:formatDate}"
				sortname="'companyName'"
				caption="listCaption"
				gridComplete="function(){bindResize('companyId')}"
				showPager="true">
				<jqgrid:filterToolbar id="companyId" searchOnEnter="false" />
				<jqgrid:navigation id="companyId" add="false" edit="false" del="false" search="false"/>
				<jqgrid:refreshButton id="companyId" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			
			function formatDate (cellvalue, options, rowObject) {
				if(cellvalue)
					return cellvalue.substring(0,10) // Cut off the timestamp portion of the date
				return 'Never'
			}
		});
		</script>
	</head>
	<body>
		<div class="body fluid" style="width:50% !important;">
			<h1>Company List</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<jqgrid:wrapper id="companyId" />
			</div>
		</div>
		<script>
			currentMenuId = "#adminMenu";
			$("#adminMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
