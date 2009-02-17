

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create UserLogin</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">UserLogin List</g:link></span>
        </div>
        <div class="body">
            <h1>Create UserLogin</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="person">Person:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'person','errors')}">
                                    <g:select optionKey="id" from="${Person.list()}" name="person.id" value="${userLoginInstance?.person?.id}" ></g:select>
                                <g:hasErrors bean="${userLoginInstance}" field="person">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="person"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="username">Username:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
                                    <input type="text" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
                                <g:hasErrors bean="${userLoginInstance}" field="username">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="username"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="password">Password:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
                                    <input type="password" id="password" name="password" value="${fieldValue(bean:userLoginInstance,field:'password')}"/>
                                <g:hasErrors bean="${userLoginInstance}" field="password">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${userLoginInstance?.dateCreated}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${userLoginInstance}" field="dateCreated">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="dateCreated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastLogin">Last Login:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'lastLogin','errors')}">
                                    <g:datePicker name="lastLogin" value="${userLoginInstance?.lastLogin}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${userLoginInstance}" field="lastLogin">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="lastLogin"/>
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
