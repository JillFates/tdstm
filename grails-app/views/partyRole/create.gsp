

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create PartyRole</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">PartyRole List</g:link></span>
        </div>
        <div class="body">
            <h1>Create PartyRole</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="role">Role:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRoleInstance,field:'role','errors')}">
                                    <g:select optionKey="id" from="${Role.list()}" name="role.id" value="${partyRoleInstance?.role?.id}" ></g:select>
                                <g:hasErrors bean="${partyRoleInstance}" field="${role}">
					            <div class="errors">
					                <g:renderErrors bean="${partyRoleInstance}" as="list" field="${role}"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="party">Party:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyRoleInstance,field:'party','errors')}">
                                    <g:select optionKey="id" from="${Party.list()}" name="party.id" value="${partyRoleInstance?.party?.id}" ></g:select>
                                <g:hasErrors bean="${partyRoleInstance}" field="${party}">
					            <div class="errors">
					                <g:renderErrors bean="${partyRoleInstance}" as="list" field="${party}"/>
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
