<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit PartyType</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyType List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">Create PartyType</g:link></span>
        </div>
        <div class="body">
            <h1>Edit PartyType</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="partyTypeId" value="${partyTypeInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="id">Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyTypeInstance,field:'id','errors')}">
                                    <input type="text" id="id" name="id" value="${fieldValue(bean:partyTypeInstance,field:'id')}"/>
                                <g:hasErrors bean="${partyTypeInstance}" field="id">
					            <div class="errors">
					                <g:renderErrors bean="${partyTypeInstance}" as="list" field="id"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyTypeInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:partyTypeInstance,field:'description')}"/>
                                <g:hasErrors bean="${partyTypeInstance}" field="description">
					            <div class="errors">
					                <g:renderErrors bean="${partyTypeInstance}" as="list" field="description"/>
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