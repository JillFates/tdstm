<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<g:set var="isActive" value="${session.getAttribute('InActive')}" />
		<title>User List - ${isActive =='N' ? 'Inactive' : 'Active'} Users</title>
		<g:render template="../layouts/angularResources" />
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'adminController.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'adminService.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/admin',file:'unlockAccountDirective.js')}"></script>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqgrid:resources />
		<g:javascript src="admin.js" />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="person.js" />
		<g:javascript src="jqgrid-support.js" />
		<g:javascript src="asset.comment.js" />
		<script type="text/javascript">
		function onInvokeAction(id) {
			setExportToLimit(id, '');
			createHiddenInputFieldsForLimitAndSubmit(id);
		}
		function redirectToListStaff() {
			if (confirm("Please click on the CREATE button of existing staff in the Admin > List Staff to create users. Click OK to go there now or Cancel to do nothing.")) {
				window.location.href = tdsCommon.createAppURL("/person/index");
			}
		}
		</script>
		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption = "Users: \
				<tds:hasPermission permission='CreateUserLogin'>\
					<span class='capBtn'><input type='button' value='Create User Login' onClick=\"redirectToListStaff()\"/></span> \
				</tds:hasPermission>\
				<span class='capBtn'><input type='button' value=' Show ${isActive == 'N' ? 'Active' : 'Inactive'} Users' onClick=\"$(\'#showActiveId\').val(${(session.getAttribute('InActive') == 'N')?"\'Y\'":"\'N\'"});submitForm();\"/></span>"
				$("#personGeneralViewId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })
				
				$("#filterSelect").change(function(ev) {
					ev.preventDefault();
					submitForm();
				});
				<jqgrid:grid id="userLoginId" url="'${''+listJsonUrl?:'no'}'"
					colNames="'Actions', 'Username', 'Person', 'Roles', 'Company', 'Local User', 'Last Login', 'Date Created', 'Expiry Date'"
					colModel="
						{name:'act', index: 'act' , sortable: false, ${true ? 'formatter:actionFormatter,' :''} search:false, width:'50', fixed:true},
						{name:'username', index: 'username', width:'80'},
						{name:'fullname', width:'100'},
						{name:'roles',width:'100'},
						{name:'company', width:'100'},
						{name:'isLocal', width:'80', fixed:true},
						{name:'lastLogin',width:'50', formatter:tdsCommon.jqgridDateCellFormatter},
						{name:'dateCreated',width:'50', formatter:tdsCommon.jqgridDateCellFormatter},
						{name:'expiryDate',width:'50', formatter:tdsCommon.jqgridDateCellFormatter}"
					sortname="'username'"
					caption="listCaption"
					gridComplete="function(){bindResize('userLoginId');recompileDOM('userLoginIdWrapper', angular.element(\$('div.body')[0]).scope())}"
					showPager="true">
					<jqgrid:filterToolbar id="userLoginId" searchOnEnter="false" />
					<jqgrid:navigation id="userLoginId" add="false" edit="false" del="false" search="false" refresh="true" />
				</jqgrid:grid>
				$.jgrid.formatter.integer.thousandsSeparator='';
				
				function actionFormatter (cellVal, options, rowObject) {
					var unlockButton = '';
					<g:if test="${isActive != 'N'}">
						<tds:hasPermission permission="UnlockUserLogin">
							if (cellVal.lockedOutUntil && cellVal.lockedOutTime.charAt(0) != '-')
								unlockButton += "<img tm-unlock-account src='${resource(dir:'icons',file:'lock_delete.png')}' border='0px' title='Click to unlock user account' cellValue='" + JSON.stringify(cellVal) + "' />";
						</tds:hasPermission>
					</g:if>
					return unlockButton;
				}
			})
			
			function submitForm () {
				$("#formId").submit();
			}
		</script>
			
	</head>
	<body>
		<div class="body fluid" ng-app="tdsAdmin" ng-controller="tds.admin.controller.MainController as admin">
			<h1 ng-teast>UserLogin List - ${isActive == 'N' ? 'Inactive' : 'Active'} Users</h1>
			<br/>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<g:form id="formId" url="[action:'list', controller:'userLogin', params:'[companyId:${companyId}, activeUsers:${activeUsers}]']">
					<g:select id="filterSelect" name="companyId" from="${partyGroupList}" value="${companyId}" optionKey="id" optionValue="name" noSelection="['All':'All']" />
					<input id="showActiveId" name="activeUsers" hidden="hidden" value="${(session.getAttribute('InActive'))}" />
				</g:form>
			</div>
			<jqgrid:wrapper id="userLoginId" />
			<div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
		</div>
	</body>
</html>