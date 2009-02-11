

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Person</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Person List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Person</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName">First Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                                    <input type="text" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}" maxlength="64"/>
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
                                    <input type="text" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}" maxlength="64"/>
                                       <g:hasErrors bean="${personInstance}" field="lastName">
						            <div class="errors">
						                <g:renderErrors bean="${personInstance}" as="list" field="lastName"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="party">Party:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'party','errors')}">
                                    <g:select optionKey="id" from="${Party.list()}" name="party.id" value="${personInstance?.party?.id}" ></g:select>
                                <g:hasErrors bean="${personInstance}" field="party">
						            <div class="errors">
						                <g:renderErrors bean="${personInstance}" as="list" field="party"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'active','errors')}">
                                    <g:select from="${['Y','N']}" name="active" value="${personInstance?.active}" valueMessagePrefix="app.active"></g:select>
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
