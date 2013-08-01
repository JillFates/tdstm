

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<title>UserLogin List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqui:resources />
		<jqgrid:resources />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="person.js" />
		<script type="text/javascript">
		function onInvokeAction(id) {
			setExportToLimit(id, '');
			createHiddenInputFieldsForLimitAndSubmit(id);
		}
		</script>
		<script type="text/javascript">
		$(document).ready(function() {
			var listCaption ="Users: \
			<tds:hasPermission permission='CreateUserLogin'>\
				<span class='capBtn'><input type='button' value='Create User Login' onClick=\"window.location.href=\'"+contextPath+"/userLogin/create\'\"/></span> \
			</tds:hasPermission>\
			<span class='capBtn'><input type='button' value=' Show ${(session.getAttribute('InActive') == 'N')?'Active':'Inactive'} Users' onClick=\"window.location.href=\'"+contextPath+"/userLogin/list/?activeUsers=${(session.getAttribute('InActive') == 'N')?'Y':'N'}\'\"/></span>"
			$("#personGeneralViewId").dialog({ autoOpen: false })
			$("#createStaffDialog").dialog({ autoOpen: false })
			
			$("#filterSelect").change(function(ev) {
				ev.preventDefault();
				$("#formId").submit();
			});
			<jqgrid:grid id="userLoginId" url="'${''+listJsonUrl?:'no'}'"
				colNames="'Username', 'Person', 'Roles', 'Company','Last Login', 'Date Created', 'Expiry Date'"
				colModel="{name:'username', index: 'username', width:'80'},
					{name:'fullname', editable: true, width:'100'},
					{name:'roles', editable: true,width:'100'},
					{name:'company', editable: true, width:'100'},
					{name:'lastLogin', editable: true,width:'50', formatter:formatDate},
					{name:'dateCreated', editable: true,width:'50', formatter:formatDate},
					{name:'expiryDate', editable: true,width:'50', formatter:formatDate}"
				sortname="'username'"
				sortable = "true"
				caption="listCaption"
				height="'100%'"
				rowNum="'25'"
				rowList= "'25','100','500','1000'"
				viewrecords="true"
				showPager="true"
				datatype="'json'">
				<jqgrid:filterToolbar id="userLoginId" searchOnEnter="false" />
				<jqgrid:navigation id="userLoginId" add="false" edit="false" del="false" search="false" refresh="true" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			
			function formatDate (cellvalue, options, rowObject) {
				if(cellvalue)
					return cellvalue.substring(0,10) // Cut off the timestamp portion of the date
				return 'Never'
			}
			
			$('#userLoginIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
			$('#userLoginIdGrid').fluidGrid({ base:'#userLoginIdWrapper', offset: 0 });
		})
		$(window).resize(resizeGrid);

		// Called when the window is resized to resize the grid wrapper 
		function resizeGrid(){
			$('#userLoginIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
			$('#userLoginIdGrid').fluidGrid({ base:'#userLoginIdWrapper', offset: 0 });
		}
		
		</script>
			
	</head>
	<body>
		<div class="body fluid" >
			<h1>UserLogin List</h1>
			<br/>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<g:form id="formId" url="[action:'list', controller:'userLogin', params:'[companyId:${companyId}]']">
					<g:select id="filterSelect" name="companyId" from="${partyGroupList}" value="${companyId}"  optionKey="id" optionValue="name" noSelection="['All':'All']" />
				</g:form>
			</div>
			<jqgrid:wrapper id="userLoginId" />
			<div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
			</div>
		</div>
	</body>
</html>
