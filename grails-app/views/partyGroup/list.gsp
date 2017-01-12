<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
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
				colNames="'Name', 'Partner', 'Date Created', 'Last Updated'"
				colModel="{name:'companyName', index: 'companyName', width:'150'},
					{name:'partner', width:'30'},
					{name:'dateCreated', width:'100', formatter:tdsCommon.jqgridDateCellFormatter},
					{name:'lastUpdated', width:'100', formatter:tdsCommon.jqgridDateCellFormatter}"
				sortname="'companyName'"
				caption="listCaption"
				gridComplete="function(){bindResize('companyId')}"
				showPager="true">
				<jqgrid:navigation id="companyId" add="false" edit="false" del="false" search="false"/>
				<jqgrid:refreshButton id="companyId" />
			</jqgrid:grid>
			TDS.jqGridFilterToolbar('companyId');

			$.jgrid.formatter.integer.thousandsSeparator='';			
		});
		</script>
	</head>
	<body>
		<tds:subHeader title="Company List" crumbs="['Admin','Client', 'List']"/> <br />
		<div class="body fluid" style="width:50% !important;">
			<g:if test="${flash.message}">
				<div class="message">${raw(flash.message)}</div>
			</g:if>
			<div>
				<jqgrid:wrapper id="companyId" />
			</div>
		</div>
		<script>
			currentMenuId = "#adminMenu";

			$('.menu-list-companies').addClass('active');
			$('.menu-parent-admin').addClass('active');

		</script>
	</body>
</html>
