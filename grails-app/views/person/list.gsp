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

      })
   </script>

  </head>
  <body>
   
    <div class="body">
      <h1>Staff List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

      <div>
      <form name="personForm" id="formId">
          <div> 
          	<g:select id="filterSelect" name="companyName" from="${totalCompanies}" value="${company}"  noSelection="['All':'All']" />
          </div>
	      <jmesa:tableFacade id="tag" items="${personsList}" maxRows="25" stateAttr="restore" var="personBean" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
	          <jmesa:htmlTable style=" border-collapse: separate">
	              <jmesa:htmlRow highlighter="true">
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
					 <jmesa:htmlColumn property="userCompany" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.DateCellEditor">${personBean.userCompany}</jmesa:htmlColumn>
	                 <jmesa:htmlColumn property="dateCreated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${personBean.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
	                 <jmesa:htmlColumn property="lastUpdated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${personBean.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
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

        <g:form action="save" method="post" name="createDialogForm" onsubmit="return validatePersonForm('createDialogForm')">
          <div class="dialog">
            <table>
              <tbody>
              <tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
				</tr>
 				<tr class="prop">
                   <td valign="top" class="name">
                       <label>Company:</label>
                   </td>
                   <td valign="top" class="value ">
                   <g:select name="company" id="companyId" optionKey="id" optionValue="name" from="${totalCompanies}" value="${company?.id}"/>
                   </td>
				</tr> 
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                    <input type="text" maxlength="34" size="34" id="firstName" name="firstName" 
                    value="${fieldValue(bean:personInstance,field:'firstName')}"/>
                    <g:hasErrors bean="${personInstance}" field="firstName">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="firstName"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="lastName">Last Name:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'lastName','errors')}">
                    <input type="text" maxlength="34" size="34" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}"/>
                    <g:hasErrors bean="${personInstance}" field="lastName">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="lastName"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="nickName">Nick Name:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'nickName','errors')}">
                    <input type="text" maxlength="34" size="34" id="nickName" name="nickName" value="${fieldValue(bean:personInstance,field:'nickName')}"/>
                    <g:hasErrors bean="${personInstance}" field="nickName">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="nickName"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="title">Title:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'title','errors')}">
                    <input type="text" maxlength="34" size="34" id="title" name="title" value="${fieldValue(bean:personInstance,field:'title')}"/>
                    <g:hasErrors bean="${personInstance}" field="title">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="title"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>
                <tr class="prop">
					<td valign="top" class="name"><label for="staffType ">StaffType:</label></td>
					<td valign="top" class="value" colspan="2">
					<g:select id="staffTypeId" name="staffType" from="${Person.constraints.staffType.inList}" value="Salary" />
					</td>
				</tr>
				<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="email"><g:message code="person.email.label" default="Email" /></label>
			        </td>
                    <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
						<g:textField name="email" value="${personInstance?.email}" size="34" />
					</td>
		       </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="active"><b>Active:&nbsp;<span style="color: red">*</span></b></label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'active','errors')}">
                    <select name="active" id="active" >
                      <g:each in="${Person.constraints.active.inList}" status="i" var="active">
                        <option value="${active}">${active}</option>
                      </g:each>
                    </select>
                    <g:hasErrors bean="${personInstance}" field="active">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="active"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>
				<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="department"><g:message code="person.department.label" default="Department" />:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'department', 'errors')}">
                    	<g:textField name="department" value="${personInstance?.department}" size="34"/>
					</td>
				</tr>
				<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="location"><g:message code="person.location.label" default="Location" />:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'location', 'errors')}">
                    	<g:textField name="location" value="${personInstance?.location}" size="34"/>
					</td>
				</tr>
				<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="workPhone"><g:message code="person.workPhone.label" default="Work Phone" />:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'workPhone', 'errors')}">
                    	<g:textField name="workPhone" value="${personInstance?.workPhone}" size="34"/>
					</td>
				</tr>
				<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="mobilePhone"><g:message code="person.mobilePhone.label" default="Mobile Phone" />:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'mobilePhone', 'errors')}">
                    	<g:textField name="mobilePhone" value="${personInstance?.mobilePhone}" size="34"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label for="roles">Roles :</label></td>
					<td valign="top" class="value" colspan="2">
						<g:select from="${availabaleRoles}" id="roleId" name="role"
							optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}"
							value="" optionKey="id" />
					</td>
				</tr>
              </tbody>
            </table>
          </div>
          <div class="buttons">
            <span class="button">
            	<input class="save" type="submit" value="Create" />
            	<input class="delete" type="button" id="cancelBId" value="Cancel" onClick="closePersonDiv('createStaffDialog')"/>
			</span>
          </div>
        </g:form>
      </div>
    </div>
  </body>
</html>
               
