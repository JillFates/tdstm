<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Staff List</title>
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqgrid:resources />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="person.js" />
		<g:javascript src="jqgrid-support.js" />
		<script type="text/javascript">
		function onInvokeAction(id) {
			setExportToLimit(id, '');
			createHiddenInputFieldsForLimitAndSubmit(id);
		}
		 
		</script>
		<script type="text/javascript">
			 $(document).ready(function() {
			  $("#filterSelect").change(function(ev) {
					ev.preventDefault();
					$("#formId").submit();
				  });
			 })
			
		</script>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#personGeneralViewId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })
				$("#showOrMergeId").dialog({ autoOpen: false })
				$('.cbox').change(function() {
					var checkedLen = $('.cbox:checkbox:checked').length
					if(checkedLen > 1 && checkedLen < 3) {
						$("#compareMergeId").removeAttr("disabled")
					} else {
						$("#compareMergeId").attr("disabled","disabled")
					}
				})
			})
		</script>
		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption ="Staff: \
				<tds:hasPermission permission='PersonCreateView'>\
					<span class=\"button\"><input type=\"button\" value=\"Create Staff\" class=\"create\" onClick=\"createDialog()\"/></span> \
				</tds:hasPermission>\
				<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='compareOrMerge()' disabled='disabled'/></span>"
				$("#personGeneralViewId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })
				
				$("#filterSelect").change(function(ev) {
					ev.preventDefault();
					$("#formId").submit();
				});
				<jqgrid:grid id="personId" url="'${''+listJsonUrl?:'no'}'"
					colNames="'First Name', 'Middle Name', 'Last Name', 'User Login', 'User Company', 'Date Created', 'Last Updated', 'Model Score'"
					colModel="{name:'firstname', width:'80'},
						{name:'middlename', width:'80'},
						{name:'lastname', index: 'lastname', width:'80'},
						{name:'userLogin', width:'80'},
						{name:'company',width:'100'},
						{name:'dateCreated',width:'50', formatter:formatDate},
						{name:'lastUpdated',width:'50', formatter:formatDate},
						{name:'modelScore',width:'50'}"
					sortname="'lastname'"
					caption="listCaption"
					multiselect="true"
					gridComplete="function(){bindResize('personId')}"
					showPager="true"
					loadComplete="initCheck"
					onSelectRow="validateMergeCount">
					<jqgrid:filterToolbar id="personId" searchOnEnter="false" />
					<jqgrid:navigation id="personId" add="false" edit="false" del="false" search="false" refresh="true" />
				</jqgrid:grid>
				$.jgrid.formatter.integer.thousandsSeparator='';
				
				function formatDate (cellvalue, options, rowObject) {
					if(cellvalue)
						return cellvalue.substring(0,10) // Cut off the timestamp portion of the date
					return 'Never'
				}
			})
		</script>
	</head>
	<body>
	<div class="body fluid">
		<h1>Staff List</h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<div id="messageId" class="message nodisplay"></div>
		<span id="spinnerId" class="nodisplay">Merging ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
		<div>
			<g:form name="personForm" id="formId" url="[action:'list', controller:'person', params:'[companyId:${companyId}]']">
				<g:select id="filterSelect" name="companyId" from="${partyGroupList}" value="${companyId}"  optionKey="id" optionValue="name" noSelection="['All':'All']" />
			</g:form>
			<jqgrid:wrapper id="personId" />
		</div>
	</div>
	
	<div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
	
	<div id="createStaffDialog" title="Create Staff" style="display:none;">
		<g:render template="createStaff" model="[forWhom:'person']"></g:render>
	</div>
	<div class="dialog">
		<div id="showOrMergeId" style="display: none;" title="Compare/Merge Persons"></div>
	</div>
	</body>
</html>