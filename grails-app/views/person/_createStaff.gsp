<%@ page import="net.transitionmanager.domain.Person" %>
<div class="dialog">

<g:form action="save" method="post" name="createDialogForm" onsubmit="return validatePersonForm('createDialogForm')">
          <input type="hidden"  id="createstaff" name="createstaff" value="${forWhom}" />
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
                   <g:select name="company" id="companyId" optionKey="id" optionValue="name" from="${partyGroupList}" value="${company?.id}"/>
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
                    <label for="middleName">Middle Name:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'middleName','errors')}">
                    <input type="text" maxlength="34" size="34" id="middleName" name="middleName" value="${fieldValue(bean:personInstance,field:'middleName')}"/>
                    <g:hasErrors bean="${personInstance}" field="middleName">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="middleName"/>
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
					<td valign="top" class="name"><label>Team :</label></td>
					<td valign="top" class="value" colspan="2">
						<table style="border: 0px">
							<tbody id="funcsCreateTbodyId">
							</tbody>
						</table> <span style="cursor: pointer;" onclick="addFunctionsCreate()"><b>Add
								Team </b></span>
					</td>
				</tr>
				</tbody>
            </table>
          </div>
          <div id="availableFuncsCreateId" style="display: none">
				<g:select from="${availabaleRoles}" id="functionId" name="funcToAdd"
					optionValue="${{it.description}}"
					value="" optionKey="id" />
			</div>
			<input type="hidden" id="maxSize" value="0">
          <div class="buttons">
            <span class="button">
            <g:if test="${forWhom =='person'}">
            	<input type="hidden" name="forWhom" value="${forWhom}"  />
            	<input class="save" type="submit" value="Save" />
            </g:if>
            <g:else>
                <input type="hidden" id="fieldName" name="fieldName" />
                <input class="save" type="button" value="Save" onClick="createPersonDetails()"/>
           	</g:else>
            	<input class="delete" type="button" id="cancelBId" value="Cancel" onClick="Person.closePersonDiv('createStaffDialog')"/>
			</span>
          </div>
</g:form>
</div>
      <script type="text/javascript"> 
       function addFunctionsCreate(){
    	  	var selectHtml = $("#availableFuncsCreateId").html().replace("funcToAdd","function")
    		var id=$("#maxSize").val()
    		$("#funcsCreateTbodyId").append("<tr id='roleTrId_"+id+"'><td> "+ selectHtml +"<a href=\"javascript:deleteFuncsRow(\'roleTrId_"+id+"')\">&nbsp;&nbsp;"+"<span class=\'clear_filter\'>X</span></a> </td></tr><br/>")
    		$("#maxSize").val(parseInt(id)+1)
       }
      
      </script>