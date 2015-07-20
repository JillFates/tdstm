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
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<g:javascript src="bootstrap.js" />

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
					if(checkedLen > 1 && checkedLen < 25) {
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
				<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='compareOrMerge()' disabled='disabled'/></span>\
				<tds:hasPermission permission='BulkDeletePerson'>\
					<span class='capBtn'><input type='button' id='bulkDelete' value='Bulk Delete' onclick='Person.showBulkDeleteModal()' /></span>\
				</tds:hasPermission>"
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
						{name:'dateCreated',width:'50', formatter:tdsCommon.jqgridFormatDateCell},
						{name:'lastUpdated',width:'50', formatter:tdsCommon.jqgridFormatDateCell},
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
				
			})
		</script>
	</head>
	<body>
	
	<!-- BULK DELETE model -->
	<div class="modal fade" id="bulkDeleteModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h1 class="modal-title" id="myModalLabel">Bulk Delete</h1>
	      </div>
	      <div class="modal-body">
	      	<p>
	      	This process will delete the persons selected as long as they are not associated to a UserLogin or Tasks. By default it will 
	      	also skip persons associated as the App Owner or SME for one or more applications.
	      	</p>
	      	<p> 
	        <input type="checkbox" id="deleteIfAssocWithAssets">&nbsp;<label for="deleteIfAssocWithAssets">Delete persons associated as App Owner or SMEs?</label>
	        </p>
	        <p id="bulkDeleteMessages"></p>
	      </div>
	      <div class="modal-footer">
	        <button id="bulkModalDeleteBtn" type="button" class="btn btn-danger" onclick='Person.bulkDelete()'>Delete</button>
	        <button id="bulkModalCancelBtn" type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        <button id="bulkModalCloseBtn" type="button" class="btn btn-default" onclick='Person.closePopup()'>Close</button>	        
	      </div>
	    </div>
	  </div>
	</div>
	
	
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
	
	<div id="createStaffDialog" title="Create Staff" style="display:none;" class="static-dialog">
		<g:render template="createStaff" model="[forWhom:'person']"></g:render>
	</div>
	<div class="dialog">
		<div id="showOrMergeId" style="display: none;" title="Compare/Merge Persons"></div>
	</div>
	</body>
</html>