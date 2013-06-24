<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="projectHeader" />
    <title>Staff List</title>

    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
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

  </head>
  <body>
   
    <div class="body">
      <h1>Staff List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
	 <span id="spinnerId" style="display: none">Merging ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
      <div>
      <form name="personForm" id="formId">
          <div class="buttonR"> 
          	<g:select id="filterSelect" name="companyName" from="${totalCompanies}" value="${company}"  noSelection="['All':'All']" />
          		<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='compareOrMerge()' disabled='disabled'/></span>
          </div>
	      <jmesa:tableFacade id="tag" items="${personsList}" maxRows="25" stateAttr="restore" var="personBean" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
	          <jmesa:htmlTable style=" border-collapse: separate">
	              <jmesa:htmlRow highlighter="true">
	                 <jmesa:htmlColumn sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor"  nowrap>
	                     <g:checkBox name="checkChange" class="cbox" id="checkId_${personBean.id}" ></g:checkBox>
					 </jmesa:htmlColumn>
	              	 <jmesa:htmlColumn property="firstName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor"  nowrap>
	                     <span id="${personBean.id}" style="cursor: pointer;" onClick="loadPersonDiv(this.id,'generalInfoShow')"><b>${personBean.firstName}</b></span>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="lastName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<span id="${personBean.id}" style="cursor: pointer;" onClick="loadPersonDiv(this.id,'generalInfoShow')"><b>${personBean.lastName}</b></span>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="userLogin" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<g:if test="${personBean.userLoginId}">
							 <g:link controller="userLogin" action="edit" id="${personBean.userLoginId}" params="[companyId:companyId]"><b>${personBean.userLogin}</b></g:link>
						</g:if>
						<g:else>
							 <g:link controller="userLogin" action="create" id="${personBean.id}" params="[companyId:companyId]">CREATE</g:link>
						</g:else>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="userCompany" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${personBean.userCompany}</jmesa:htmlColumn>
	                 <jmesa:htmlColumn property="dateCreated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.BasicCellEditor"><tds:convertDateTime date="${personBean.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
	                 <jmesa:htmlColumn property="lastUpdated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.BasicCellEditor"><tds:convertDateTime date="${personBean.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
	               <jmesa:htmlColumn property="modelScore" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
	                     <g:remoteLink controller="person" action="editShow" id="${personBean.id}" params="[companyId:companyId]" onComplete ="showPersonDialog( e );">${personBean.modelScore}</g:remoteLink>
					 </jmesa:htmlColumn>
	              </jmesa:htmlRow>
	          </jmesa:htmlTable>
	      </jmesa:tableFacade>
	  </form>
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
               
