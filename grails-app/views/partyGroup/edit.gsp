

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit PartyGroup</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyGroup List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PartyGroup</g:link></span>
        </div>
        <div class="body">
            <h1>Edit PartyGroup</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${partyGroupInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${partyGroupInstance?.dateCreated}" ></g:datePicker>
                                <g:hasErrors bean="${partyGroupInstance}" field="dateCreated">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="dateCreated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${partyGroupInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${partyGroupInstance}" field="lastUpdated">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="lastUpdated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${partyGroupInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                <g:hasErrors bean="${partyGroupInstance}" field="partyType">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="partyType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:partyGroupInstance,field:'name')}"/>
                                <g:hasErrors bean="${partyGroupInstance}" field="name">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="name"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'comment','errors')}">
                                    <input type="text" id="comment" name="comment" value="${fieldValue(bean:partyGroupInstance,field:'comment')}"/>
                                <g:hasErrors bean="${partyGroupInstance}" field="comment">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="comment"/>
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
