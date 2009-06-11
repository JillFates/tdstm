<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="companyHeader" />
    <title>Staff List</title>
    <g:javascript library="prototype" />
    <g:javascript library="jquery"/>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />

    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.resizable"/>
    <jq:plugin name="ui.dialog"/>

    <script>

      $(document).ready(function() {

        $("#dialog").dialog({ autoOpen: false })
        $("#dialog1").dialog({ autoOpen: false })
        $("#dialog2").dialog({ autoOpen: false })

      })

    </script>

    <g:javascript>

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
      document.showForm.title.value = person.title
      document.editForm.title.value = person.title
      document.showForm.active.value = person.active
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

      $("#dialog").dialog("close")
      $("#dialog1").dialog('option', 'width', 500)
      $("#dialog1").dialog("open")

      }

      function createDialog(){
      
document.createDialogForm.company.value = ${companyId}
      $("#dialog2").dialog('option', 'width', 500)
      $("#dialog2").dialog("open")

      }


    </g:javascript>

  </head>
  <body>
   
    <div class="body">
      <h1>Staff List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

      <div class="list">
        <table>
          <thead>
            <tr>
              <g:sortableColumn property="firstName" title="First Name" />

              <g:sortableColumn property="lastName" title="Last Name" />

              <th>User Login</th>

              <g:sortableColumn property="dateCreated" title="Date Created" />

              <g:sortableColumn property="lastUpdated" title="Last Updated" />

            </tr>
          </thead>
          <tbody>
            <g:each in="${personInstanceList}" status="i" var="personInstance">
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">


                <td><g:remoteLink controller="person" action="editShow" id="${personInstance.id}" params="[companyId:companyId]" onComplete ="showPersonDialog( e );">${fieldValue(bean:personInstance, field:'firstName')}</g:remoteLink></td>

                <td><g:remoteLink controller="person" action="editShow" id="${personInstance.id}" params="[companyId:companyId]" onComplete ="showPersonDialog( e );">${fieldValue(bean:personInstance, field:'lastName')}</g:remoteLink></td>

                <td>
                  <%
def userLogin = UserLogin.findByPerson(personInstance);
%> <g:if test="${userLogin}">
                    <g:link controller="userLogin" action="edit" id="${userLogin.id}" params="[companyId:companyId]">${userLogin}</g:link>
                  </g:if> <g:else>
                    <g:link controller="userLogin" action="create"
                            id="${personInstance.id}" params="[companyId:companyId]">CREATE</g:link>
                </g:else></td>

                <td><tds:convertDateTime date="${personInstance?.dateCreated}" /></td>

                <td><tds:convertDateTime date="${personInstance?.lastUpdated}" /></td>

              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
      <jsec:hasRole name="ADMIN">
        <div class="buttons"><g:form>
            <input type="hidden" value="${companyId}" name="companyId" >
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

                  <td valign="top" class="value"><input type="text" id="company" name="company" value="" style="border: 0px"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">First Name:</td>

                  <td valign="top" class="value"><input type="text" id="firstName" name="firstName" value="" style="border: 0px"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Last Name:</td>

                  <td valign="top" class="value"><input type="text" id="lastName" name="lastName" value="" style="border: 0px"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Nick Name:</td>

                  <td valign="top" class="value"><input type="text" id="nickName" name="nickName" value="" style="border: 0px"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Title:</td>

                  <td valign="top" class="value"><input type="text" id="title" name="title" value="" style="border: 0px"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Active:</td>

                  <td valign="top" class="value"><input type="text" id="active" name="active" value="" style="border: 0px"/></td>

                </tr>
                <tr class="prop">
                  <td valign="top" class="name">Date Created:</td>

                  <td valign="top" class="value"><input type="text" id="dateCreated" name="dateCreated" value="" style="border: 0px"/></td>

                </tr>

                <tr class="prop">
                  <td valign="top" class="name">Last Updated:</td>

                  <td valign="top" class="value"><input type="text" id="lastUpdated" name="lastUpdated" value="" style="border: 0px"/></td>

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
                    <label for="firstName">First Name:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                    <input type="text" maxlength="64" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}"/>
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
                    <input type="text" maxlength="64" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}"/>
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
                    <input type="text" maxlength="64" id="nickName" name="nickName" value="${fieldValue(bean:personInstance,field:'nickName')}"/>
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
                    <input type="text" maxlength="34" id="title" name="title" value="${fieldValue(bean:personInstance,field:'title')}"/>
                    <g:hasErrors bean="${personInstance}" field="title">
                      <div class="errors">
                        <g:renderErrors bean="${personInstance}" as="list" field="title"/>
                      </div>
                    </g:hasErrors>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="active">Active:</label>
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


              </tbody>
            </table>
          </div>
          <jsec:hasRole name="ADMIN">
          <div class="buttons">
            <input type="hidden" id="id" name="id" value="${personInstance?.id}" />
            <input type="hidden" id="companyId" name="companyId" value="${companyId}" />
            <span class="button"><g:actionSubmit class="edit" value="Update" /></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
          </div>
          </jsec:hasRole>
        </g:form>
      </div>


    </div>

    <div id="dialog2" title="Create Staff" style="display:none;">
      <div class="dialog">

        <g:form action="save" method="post" name="createDialogForm">
          <div class="dialog">
            <table>
              <tbody>
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
                    <label for="firstName">First Name:</label>
                  </td>
                  <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                    <input type="text" maxlength="34" size="34" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}"/>
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
                    <label for="active">Active:</label>
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

              </tbody>
            </table>
          </div>
          <div class="buttons">
            <span class="button"><input class="save" type="submit" value="Create" /></span>
          </div>
        </g:form>
      </div>

    </div>
  </body>
</html>
               