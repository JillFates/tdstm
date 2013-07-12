<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show PartyRole</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyRole List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">Create PartyRole</g:link></span>
        </div>
        <div class="body">
            <h1>Show PartyRole</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Party:</td>
                            
                            <td valign="top" class="value"><g:link controller="party" action="show" id="${partyRoleInstance?.party?.id}">${partyRoleInstance?.party?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Role Type:</td>
                            
                            <td valign="top" class="value"><g:link controller="roleType" action="show" id="${partyRoleInstance?.roleType?.id}">${partyRoleInstance?.roleType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="partyId" value="${partyRoleInstance?.party.id}" />
                    <input type="hidden" name="roleTypeId" value="${partyRoleInstance?.roleType.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>