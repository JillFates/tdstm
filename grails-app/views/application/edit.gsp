

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Application</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Application List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Application</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Application</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${applicationInstance}">
            <div class="errors">
                <g:renderErrors bean="${applicationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${applicationInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${applicationInstance?.dateCreated}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${applicationInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${applicationInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:applicationInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'comment','errors')}">
                                    <input type="text" id="comment" name="comment" value="${fieldValue(bean:applicationInstance,field:'comment')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="owner">Owner:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'owner','errors')}">
                                    <g:select optionKey="id" from="${PartyGroup.list()}" name="owner.id" value="${applicationInstance?.owner?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appCode">App Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appCode','errors')}">
                                    <input type="text" id="appCode" name="appCode" value="${fieldValue(bean:applicationInstance,field:'appCode')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="environment">Environment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'environment','errors')}">
                                    <g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}" ></g:select>
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
