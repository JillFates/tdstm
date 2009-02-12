

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show PartyRelationship</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyRelationship List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PartyRelationship</g:link></span>
        </div>
        <div class="body">
            <h1>Show PartyRelationship</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyRelationshipInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Relationship Type:</td>
                            
                            <td valign="top" class="value"><g:link controller="partyRelationshipType" action="show" id="${partyRelationshipInstance?.partyRelationshipType?.id}">${partyRelationshipInstance?.partyRelationshipType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Id From:</td>
                            
                            <td valign="top" class="value"><g:link controller="party" action="show" id="${partyRelationshipInstance?.partyIdFrom?.id}">${partyRelationshipInstance?.partyIdFrom?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Party Id To:</td>
                            
                            <td valign="top" class="value"><g:link controller="party" action="show" id="${partyRelationshipInstance?.partyIdTo?.id}">${partyRelationshipInstance?.partyIdTo?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Role Type Code From:</td>
                            
                            <td valign="top" class="value"><g:link controller="roleType" action="show" id="${partyRelationshipInstance?.roleTypeCodeFrom?.id}">${partyRelationshipInstance?.roleTypeCodeFrom?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Role Type Code To:</td>
                            
                            <td valign="top" class="value"><g:link controller="roleType" action="show" id="${partyRelationshipInstance?.roleTypeCodeTo?.id}">${partyRelationshipInstance?.roleTypeCodeTo?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Status Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyRelationshipInstance, field:'statusCode')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Comments:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyRelationshipInstance, field:'comments')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${partyRelationshipInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
