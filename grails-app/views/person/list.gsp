<%@page import="net.transitionmanager.domain.Person" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Staff List</title>

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />

		<jqgrid:resources />
		<g:javascript src="projectStaff.js" />
		<g:javascript src="jqgrid-support.js" />

		<style>

			div.main_bottom table {
				border-collapse: initial;
				border-spacing: initial;
			}

			#filterSelect {
				margin-bottom: 6px;
			}
		</style>

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
				<tds:hasPermission permission="${Permission.PersonCreate}">\
					<span class=\"button\"><input type=\"button\" value=\"Create Staff\" class=\"create\" onClick=\"createDialog()\"/></span> \
				</tds:hasPermission>\
				<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='Person.compareOrMerge()' disabled='disabled'/></span>\
				<tds:hasPermission permission="${Permission.PersonBulkDelete}">\
					<span class='capBtn'><input type='button' id='bulkDelete' value='Bulk Delete' onclick='Person.showBulkDeleteModal()' /></span>\
				</tds:hasPermission>"
				$("#personGeneralViewId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })

				$("#filterSelect").change(function(ev) {
					ev.preventDefault();
					$("#formId").submit();
				});
				<jqgrid:grid id="personId" url="'${''+listJsonUrl?:'no'}'"
					colNames="'First Name', 'Middle Name', 'Last Name', 'Username', 'Email', 'User Company', 'Date Created', 'Last Updated'"
					colModel="{name:'firstname', width:'80', formatter: myLinkFormatter},
						{name:'middlename', width:'80', formatter: myLinkFormatter},
						{name:'lastname', index: 'lastname', width:'80', formatter: myLinkFormatter},
						{name:'userLogin', width:'130' },
						{name:'email',width:'220', formatter: tdsCommon.jqgridTextCellFormatter},
						{name:'company',width:'130', formatter: tdsCommon.jqgridTextCellFormatter},
						{name:'dateCreated',width:'100', formatter:tdsCommon.jqgridDateCellFormatter},
						{name:'lastUpdated',width:'100', formatter:tdsCommon.jqgridDateCellFormatter}"
					sortname="'lastname'"
					caption="listCaption"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					multiselect="true"
					gridComplete="function(){bindResize('personId')}"
					showPager="true"
					loadComplete="initCheck"
					onSelectRow="validateMergeCount">
					<jqgrid:navigation id="personId" add="false" edit="false" del="false" search="false" refresh="true" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('personId');

				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? _.escape(cellvalue) : ''
					return '<a href="javascript:Person.showPersonDialog(' + +options.rowId + ',\'generalInfoShow\')">' + value + '</a>'

				}

			})
		</script>
	</head>
	<body>
	<tds:subHeader title="Staff List" crumbs="['Admin','Client', 'Staff']"/>

	<!-- BULK DELETE model -->
	<div class="modal fade" id="bulkDeleteModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
		     <button id="bulkDeleteCloseAndReloadButton" aria-label="Close"
					 class="close" type="button" onclick='Person.closePopup()' title="Close">
				 <span aria-hidden="true">×</span>
			 </button>
			  <button id="bulkDeleteClose" aria-label="Close"
					  class="close" type="button" data-dismiss="modal" title="Close">
				  <span aria-hidden="true">×</span>
			  </button>
			  <h4 class="modal-title" id="myModalLabel">Bulk Delete</h4>
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
		<g:if test="${flash.message}">
			<div class="message">${raw(flash.message)}</div>
		</g:if>
		<div id="messageId" class="message nodisplay"></div>
		<span id="spinnerId" class="nodisplay">Merging ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
		<div>
			<g:form name="personForm" id="formId" url="[action:'list', controller:'person', params:'[companyId:${companyId}]']" autocomplete="off">
				<g:select id="filterSelect" name="companyId" from="${partyGroupList}" value="${companyId}"  optionKey="id" optionValue="name" noSelection="['All':'All']" />
			<jqgrid:wrapper id="personId" />
			</g:form>
		</div>
	</div>

	<div id="createStaffDialog" title="Create Staff" style="display:none;" class="static-dialog">
		<g:render template="createStaff" model="[forWhom:'person']"></g:render>
	</div>
	<div class="dialog">
		<div id="showOrMergeId" style="display: none;" title="Compare/Merge Persons"></div>
	</div>
	<script>
		$('.menu-list-staff').addClass('active');
		$('.menu-parent-admin').addClass('active');
	</script>

	</body>
</html>
