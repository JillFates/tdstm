

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create PartyRelationship</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyRelationship List</g:link></span>
        </div>
        <div class="body">
            <h1>Create PartyRelationship</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyRelationshipType">Party Relationship Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'partyRelationshipType','errors')}">
                                    <g:select optionKey="id" from="${PartyRelationshipType.list()}" name="partyRelationshipType.id" value="${partyRelationshipInstance?.partyRelationshipType?.id}" ></g:select>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="partyRelationshipType">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="partyRelationshipType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyIdFrom">Party Id From:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'partyIdFrom','errors')}">
                                    <g:select optionKey="id" from="${Party.list()}" name="partyIdFrom.id" value="${partyRelationshipInstance?.partyIdFrom?.id}" ></g:select>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="partyIdFrom">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="partyIdFrom"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyIdTo">Party Id To:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'partyIdTo','errors')}">
                                    <g:select optionKey="id" from="${Party.list()}" name="partyIdTo.id" value="${partyRelationshipInstance?.partyIdTo?.id}" ></g:select>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="partyIdTo">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="partyIdTo"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="roleTypeCodeFrom">Role Type Code From:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'roleTypeCodeFrom','errors')}">
                                    <g:select optionKey="id" from="${RoleType.list()}" name="roleTypeCodeFrom.id" value="${partyRelationshipInstance?.roleTypeCodeFrom?.id}" ></g:select>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="roleTypeCodeFrom">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="roleTypeCodeFrom"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="roleTypeCodeTo">Role Type Code To:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'roleTypeCodeTo','errors')}">
                                    <g:select optionKey="id" from="${RoleType.list()}" name="roleTypeCodeTo.id" value="${partyRelationshipInstance?.roleTypeCodeTo?.id}" ></g:select>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="roleTypeCodeTo">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="roleTypeCodeTo"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="statusCode">Status Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'statusCode','errors')}">
                                    <input type="text" id="statusCode" name="statusCode" value="${fieldValue(bean:partyRelationshipInstance,field:'statusCode')}"/>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="statusCode">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="statusCode"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comments">Comments:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRelationshipInstance,field:'comments','errors')}">
                                    <input type="text" id="comments" name="comments" value="${fieldValue(bean:partyRelationshipInstance,field:'comments')}"/>
                                <g:hasErrors bean="${partyRelationshipInstance}" field="comments">
					            <div class="errors">
					                <g:renderErrors bean="${partyRelationshipInstance}" as="list" field="comments"/>
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
    </body>
</html>
