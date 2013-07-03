<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="projectHeader" />
    <title>Staff List</title>

    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
	<jqui:resources />
	<jqgrid:resources />
	<link rel="stylesheet" type="text/css" href="${resource(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
	<script language="javascript" src="${resource(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
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
			 }else{
				$("#compareMergeId").attr("disabled","disabled")
			 }
		})
      })
	</script>
		<script type="text/javascript">
		$(document).ready(function() {
			var listCaption ="Users: \
			<tds:hasPermission permission='PersonCreateView'>\
				<span class='capBtn'><input type='button' value='Create Person' onClick=\"window.location.href=\'"+contextPath+"/person/create\'\"/></span> \
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
					{name:'company', editable: true,width:'100'},
					{name:'dateCreated', editable: true,width:'50', formatter:formatDate},
					{name:'lastUpdated', editable: true,width:'50', formatter:formatDate},
					{name:'modelScore', editable: true,width:'50'}"
				sortname="'lastname'"
				caption="listCaption"
				height="'100%'"
				rowNum="'25'"
				rowList= "'25','100','500','1000'"
				multiselect="true"
				viewrecords="true"
				showPager="true"
				loadComplete="initCheck"
				datatype="'json'">
				<jqgrid:filterToolbar id="personId" searchOnEnter="false" />
				<jqgrid:navigation id="personId" add="false" edit="false" del="false" search="false" refresh="true" />
			</jqgrid:grid>
			$.jgrid.formatter.integer.thousandsSeparator='';
			
			function formatDate (cellvalue, options, rowObject) {
				if(cellvalue)
					return cellvalue.substring(0,10) // Cut off the timestamp portion of the date
				return 'Never'
			}
			
			function initCheck() {
				 $('.cbox').change(function() {
					 var checkedLen = $('.cbox:checkbox:checked').length
					 if(checkedLen > 1 && checkedLen < 5) {
						$("#compareMergeId").removeAttr("disabled")
					 }else{
						$("#compareMergeId").attr("disabled","disabled")
					 }
				})
			}
			
			$('#personIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
			$('#personIdGrid').fluidGrid({ base:'#personIdWrapper', offset: 0 });
		})
		$(window).resize(resizeGrid);

		// Called when the window is resized to resize the grid wrapper 
		function resizeGrid(){
			$('#personIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
			$('#personIdGrid').fluidGrid({ base:'#personIdWrapper', offset: 0 });
		}
		
	</script>
  </head>
  <body>
   
    <div class="body fluid">
      <h1>Staff List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
	 <span id="spinnerId" style="display: none">Merging ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
	<div>
		<g:form name="personForm" id="formId" url="[action:'list', controller:'person', params:'[companyId:${companyId}]']">
			<g:select id="filterSelect" name="companyId" from="${partyGroupList}" value="${companyId}"  optionKey="id" optionValue="name" noSelection="['All':'All']" />
		</g:form>
		<jqgrid:wrapper id="personId" />
	</div>
    <tds:hasPermission permission='PersonCreateView'>
        <div class="buttons"><g:form>
            <input type="hidden" value="${companyId}" name="companyId" />
            <span class="button"><input type="button" value="New" class="create" onClick="createDialog()"/></span>
        </g:form></div>
    </tds:hasPermission></div>
    
     <div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
     
     <div id="createStaffDialog" title="Create Staff" style="display:none;">
      <div class="dialog">
	      <div id="showOrMergeId" style="display: none;" title="Compare/Merge Persons"></div>
	      <g:render template="createStaff" model="[forWhom:'person']"></g:render>
      </div>
    </div>
  </body>
</html>
               
