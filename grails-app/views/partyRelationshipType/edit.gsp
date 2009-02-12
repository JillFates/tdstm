

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit PartyRelationshipType</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyRelationshipType List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PartyRelationshipType</g:link></span>
        </div>
        <div class="body">
            <h1>Edit PartyRelationshipType</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${partyRelationshipTypeInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyRelationshipTypeCode">Party Relationship Type Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipTypeInstance,field:'partyRelationshipTypeCode','errors')}">
                                    <input type="text" id="partyRelationshipTypeCode" name="partyRelationshipTypeCode" value="${fieldValue(bean:partyRelationshipTypeInstance,field:'partyRelationshipTypeCode')}"/>
                                <g:hasErrors bean="${partyRelationshipTypeInstance}" field="partyRelationshipTypeCode">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipTypeInstance}" as="list" field="partyRelationshipTypeCode"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipTypeInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:partyRelationshipTypeInstance,field:'description')}"/>
                                <g:hasErrors bean="${partyRelationshipTypeInstance}" field="description">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipTypeInstance}" as="list" field="description"/>
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
