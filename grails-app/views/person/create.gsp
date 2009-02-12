

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
                                    <label for="partyName">Party Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'partyName','errors')}">
                                    <input type="text" id="partyName" name="partyName" value="${fieldValue(bean:personInstance,field:'partyName')}"/>
                                <g:hasErrors bean="${personInstance}" field="partyName">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="partyName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyCreatedDate">Party Created Date:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'partyCreatedDate','errors')}">
                                    <g:datePicker name="partyCreatedDate" value="${personInstance?.partyCreatedDate}" ></g:datePicker>
                                <g:hasErrors bean="${personInstance}" field="partyCreatedDate">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="partyCreatedDate"/>
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
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'active','errors')}">
                                    <input type="text" id="active" name="active" value="${fieldValue(bean:personInstance,field:'active')}"/>
                                <g:hasErrors bean="${personInstance}" field="active">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="active"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="personCreatedDate">Person Created Date:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'personCreatedDate','errors')}">
                                    <g:datePicker name="personCreatedDate" value="${personInstance?.personCreatedDate}" ></g:datePicker>
                                <g:hasErrors bean="${personInstance}" field="personCreatedDate">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="personCreatedDate"/>
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
                                    <label for="personLastUpdated">Person Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'personLastUpdated','errors')}">
                                    <g:datePicker name="personLastUpdated" value="${personInstance?.personLastUpdated}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${personInstance}" field="personLastUpdated">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="personLastUpdated"/>
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
