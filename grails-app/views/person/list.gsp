<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="companyHeader" />
    <title>Staff List</title>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
	<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
	<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
	<script type="text/javascript">
	function onInvokeAction(id) {
	    setExportToLimit(id, '');
	    createHiddenInputFieldsForLimitAndSubmit(id);
	}
	</script>
    <script type="text/javascript">

      $(document).ready(function() {

        $("#dialog").dialog({ autoOpen: false })
        $("#dialog1").dialog({ autoOpen: false })
        $("#dialog2").dialog({ autoOpen: false })

      })

      function showPersonDialog( e ) {

      var person = eval('(' + e.responseText + ')')
      document.editForm.id.value = person.id
      document.showForm.id.value = person.id
      document.showForm.firstName.value = person.firstName
      document.editForm.firstName.value = person.firstName
      document.showForm.lastName.value = person.lastName
      document.editForm.lastName.value = person.lastName
      if(person.nickName == null) {
      document.showForm.nickName.value = ""
      document.editForm.nickName.value = ""
      } else {
      document.showForm.nickName.value = person.nickName
      document.editForm.nickName.value = person.nickName
      }
      if( !person.email ) {
          document.showForm.email.value = ""
          document.editForm.email.value = ""
      } else {
          document.showForm.email.value = person.email
          document.editForm.email.value = person.email
      }
      document.showForm.title.value = person.title
      document.editForm.title.value = person.title
      document.showForm.active.value = person.active
      if( !person.department ) {
          document.showForm.department.value = ""
          document.editForm.department.value = ""
      } else {
          document.showForm.department.value = person.department
          document.editForm.department.value = person.department
      }
      if( !person.location ) {
          document.showForm.location.value = ""
          document.editForm.location.value = ""
      } else {
          document.showForm.location.value = person.location
          document.editForm.location.value = person.location
      }
      if( !person.workPhone ) {
          document.showForm.workPhone.value = ""
          document.editForm.workPhone.value = ""
      } else {
          document.showForm.workPhone.value = person.workPhone
          document.editForm.workPhone.value = person.workPhone
      }
      if( !person.mobilePhone ) {
          document.showForm.mobilePhone.value = ""
          document.editForm.mobilePhone.value = ""
      } else {
          document.showForm.mobilePhone.value = person.mobilePhone
          document.editForm.mobilePhone.value = person.mobilePhone
      }
      document.editForm.companyId.value = person.companyId
      document.showForm.companyId.value = person.companyId
      document.showForm.dateCreated.value = person.dateCreated
      document.showForm.lastUpdated.value = person.lastUpdated
      document.showForm.company.value = person.companyParty.name
      document.editForm.company.value = person.companyParty.id

      $("#dialog").dialog('option', 'width', 400)
      $("#dialog").dialog("open")

      }

      function editPersonDialog() {
      document.editForm.active.value = document.showForm.active.value;
      $("#dialog").dialog("close")
      $("#dialog1").dialog('option', 'width', 500)
      $("#dialog1").dialog("open")

      }

      function createDialog(){
      
document.createDialogForm.company.value = ${companyId}
      $("#dialog2").dialog('option', 'width', 500)
      $("#dialog2").dialog("open")

      }


   </script>

  </head>
  <body>
   
    <div class="body">
      <h1>Staff List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

      <div>
      <form name="personForm">
	      <jmesa:tableFacade id="tag" items="${personsList}" maxRows="25" stateAttr="restore" var="personBean" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
	          <jmesa:htmlTable style=" border-collapse: separate">
	              <jmesa:htmlRow highlighter="true">
	              	 <jmesa:htmlColumn property="firstName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
	                     <g:remoteLink controller="person" action="editShow" id="${personBean.id}" params="[companyId:companyId]" onComplete ="showPersonDialog( e );">${personBean.firstName}</g:remoteLink>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="lastName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<g:remoteLink controller="person" action="editShow" id="${personBean.id}" params="[companyId:companyId]" onComplete ="showPersonDialog( e );">${personBean.lastName}</g:remoteLink>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="userLogin" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<g:if test="${personBean.userLoginId}">
							<g:link controller="userLogin" action="edit" id="${personBean.userLoginId}" params="[companyId:companyId]">${personBean.userLogin}</g:link>
						</g:if>
						<g:else>
							 <g:link controller="userLogin" action="create" id="${personBean.id}" params="[companyId:companyId]">CREATE</g:link>
						</g:else>
					 </jmesa:htmlColumn>
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
      <jsec:hasRole name="ADMIN">
        <div class="buttons"><g:form>
            <input type="hidden" value="${companyId}" name="companyId" />
            <span class="button"><input type="button" value="New" class="create" onClick="createDialog()"/></span>
        </g:form></div>
    </jsec:hasRole></div>

    <div id="dialog" title="Show Staff" style="display:none;">

      <div class="dialog">
        <g:form name="showForm">
          <div>
            <table>
              <tbody>
	              
              <tr class="prop">
                  <td valign="top" class="name">Company:</td>

                  <td valign="top" class="value"><input type="text" id="company" name="company" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">First Name:</td>

                  <td valign="top" class="value"><input type="text" id="firstName" name="firstName" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Last Name:</td>

                  <td valign="top" class="value"><input type="text" id="lastName" name="lastName" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Nick Name:</td>

                  <td valign="top" class="value"><input type="text" id="nickName" name="nickName" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Title:</td>

                  <td valign="top" class="value"><input type="text" id="title" name="title" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
				<tr class="prop">
		        	<td valign="top" class="name"><g:message code="person.email.label" default="Email" />:</td>
		                            
		            <td valign="top" class="value"><input type="text" id="email" name="email" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>
		                            
		        </tr>
                <tr class="prop">
                  <td valign="top" class="name">Active:</td>

                  <td valign="top" class="value"><input type="text" id="active" name="active" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Department:</td>

                  <td valign="top" class="value"><input type="text" id="department" name="department" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Location:</td>

                  <td valign="top" class="value"><input type="text" id="location" name="location" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Work Phone:</td>

                  <td valign="top" class="value"><input type="text" id="workPhone" name="workPhone" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Mobile Phone:</td>

                  <td valign="top" class="value"><input type="text" id="mobilePhone" name="mobilePhone" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Date Created:</td>

                  <td valign="top" class="value"><input type="text" id="dateCreated" name="dateCreated" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Last Updated:</td>

                  <td valign="top" class="value"><input type="text" id="lastUpdated" name="lastUpdated" size="34" value=""  style="border: 0px;background: none;" readonly="readonly"/></td>

                </tr>

              </tbody>
            </table>
          </div>
          <jsec:hasRole name="ADMIN">
          <div class="buttons">
            <input type="hidden" id="id" name="id" value="${personInstance?.id}" />
            <input type="hidden" id="companyId" name="companyId" value="${companyId}" />
            <span class="button"><input type="button" class="edit" value="Edit" onClick="return editPersonDialog()"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
          </div>
          </jsec:hasRole>
        </g:form>
      </div>
    </div>

    <div id="dialog1" title="Edit Person" style="display:none;">

      <div class="dialog">

        <g:form method="post" name="editForm">

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
                               
								<select name="company" id="companyId">
	                                <g:each in="${totalCompanies}" status="i" var="company">
	                                	<option value="${company?.id}">${company?.name}</option>
	                                </g:each>
                                </select>
                                </td>
                            </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                    <input type="text" maxlength="64" size="34" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}"/>
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
                    <input type="text" maxlength="64" size="34" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}"/>
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
                    <input type="text" maxlength="64" size="34" id="nickName" name="nickName" value="${fieldValue(bean:personInstance,field:'nickName')}"/>
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
                	<td valign="top" class="name">
                    	<label for="email"><g:message code="person.email.label" default="Email" />:</label>
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
              </tbody>
            </table>
          </div>
          <jsec:hasRole name="ADMIN">
          <div class="buttons">
            <input type="hidden" id="id" name="id" value="${personInstance?.id}" />
            <input type="hidden" id="companyId" name="companyId" value="${companyId}" />
            <span class="button"><g:actionSubmit class="edit" value="Update"  onclick="return validatePersonForm('editForm');" /></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
          </div>
          </jsec:hasRole>
        </g:form>
      </div>


    </div>

    <div id="dialog2" title="Create Staff" style="display:none;">
      <div class="dialog">

        <g:form action="save" method="post" name="createDialogForm" onsubmit="return validatePersonForm('createDialogForm')">
          <div class="dialog">
            <table>
              <tbody>
              <tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
				</tr>
               <%-- <input type="hidden" name="companyId" value="${companyId}"> --%>
                
 					<tr class="prop">
                                <td valign="top" class="name">
                                    <label>Company:</label>
                                </td>
                                <td valign="top" class="value ">
                               
								<select name="company" id="companyId">
	                                <g:each in="${totalCompanies}" status="i" var="company">
	                                	<option value="${company?.id}">${company?.name}</option>
	                                </g:each>
                                </select>
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
              </tbody>
            </table>
          </div>
          <div class="buttons">
            <span class="button"><input class="save" type="submit" value="Create" /></span>
          </div>
        </g:form>
      </div>

    </div>
    <script type="text/javascript">
    /*
    	Validate person form
    */
    var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
    function validatePersonForm( form ){
        var returnVal = true 
    	var firstName = $("form[name = 'createDialogForm'] input[name = 'firstName']").val()
        var email = $("form[name = 'createDialogForm'] input[name = 'email']").val()
        var workPhone = $("form[name = 'createDialogForm'] input[name = 'workPhone']").val().replace(/[\(\)\.\-\ ]/g, '')
        var mobilePhone = $("form[name = 'createDialogForm'] input[name = 'mobilePhone']").val().replace(/[\(\)\.\-\ ]/g, '')
        if(form == 'editForm'){
	        firstName = $("form[name = 'editForm'] input[name = 'firstName']").val()
	        email = $("form[name = 'editForm'] input[name = 'email']").val()
	        workPhone = $("form[name = 'editForm'] input[name = 'workPhone']").val().replace(/[\(\)\.\-\ ]/g, '')
	        mobilePhone = $("form[name = 'editForm'] input[name = 'mobilePhone']").val().replace(/[\(\)\.\-\ ]/g, '')
        }
        if(!firstName) {
            alert("First Name should not be blank ")
            returnVal = false
        } 
        if( email && !emailExp.test(email)){
        	 alert(email +" is not a valid e-mail address ")
        	 returnVal = false
        } 
        if(workPhone){
            if (isNaN(workPhone)) { alert("The Work phone number contains illegal characters.");returnVal = false }
	        if (!(workPhone.length == 10)) { alert("The Work phone number is the wrong length. Make sure you included an area code.");returnVal = false }
        }
        if(mobilePhone){
            if (isNaN(mobilePhone)) { alert("The Mobile phone number contains illegal characters.");returnVal = false }
	        if (!(mobilePhone.length == 10)) { alert("The Mobile phone number is the wrong length. Make sure you included an area code.");returnVal = false }
        }
        return returnVal
    }
    </script>
  </body>
</html>
               