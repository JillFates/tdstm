

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Create Application</title>         
    </head>
    <body>
      
        <div class="body">
            <h1>Create Application</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${applicationInstance}">
            <div class="errors">
                <g:renderErrors bean="${applicationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appCode">Company:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appCode','errors')}">
                                    ${Party.findById(partyId)}
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appCode">App Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appCode','errors')}">
                                   
                                    <input type="hidden"   name="owner.id" value="${partyId}" />
                                    <input type="text" id="appCode" name="appCode" value="${fieldValue(bean:applicationInstance,field:'appCode')}"/>
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
                                    <label for="environment">Environment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'environment','errors')}">
                                    <g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}" ></g:select>
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="environment">Application Owner:</label>
                                </td>
                                <td valign="top" >
                                    <tds:staffSelect optionKey="id" id="applicationOwner" name="applicationOwner" from="${Person.list()}" companyId="${partyId}" default="${personId}"   isNew="true" />
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="environment">Subject Matter Expert:</label>
                                </td>
                                <td valign="top" >
                                    <tds:staffSelect optionKey="id" id="subjectMatterExpert" name="subjectMatterExpert" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" />
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="environment">Primary Contact:</label>
                                </td>
                                <td valign="top" >
                                    <tds:staffSelect optionKey="id" id="primaryContact" name="primaryContact" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" />
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="environment">Second Contact:</label>
                                </td>
                                <td valign="top" >
                                    <tds:staffSelect optionKey="id" id="secondContact" name="secondContact" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" />
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
