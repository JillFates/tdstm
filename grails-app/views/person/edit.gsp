

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Person</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Person List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Person</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Person</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${personInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${personInstance?.dateCreated}" ></g:datePicker>
                                <g:hasErrors bean="${personInstance}" field="dateCreated">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="dateCreated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${personInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${personInstance}" field="lastUpdated">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="lastUpdated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${personInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                <g:hasErrors bean="${personInstance}" field="partyType">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="partyType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName">First Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                                    <input type="text" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}"/>
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
                                    <input type="text" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}"/>
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
                                    <input type="text" id="nickName" name="nickName" value="${fieldValue(bean:personInstance,field:'nickName')}"/>
                                <g:hasErrors bean="${personInstance}" field="nickName">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="nickName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'active','errors')}">
                                    <g:select id="active" name="active" from="${personInstance.constraints.active.inList}" value="${personInstance.active}" ></g:select>
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
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
