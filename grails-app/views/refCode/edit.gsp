<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Edit RefCode</title>
    </head>
    <body>
        <div class="body">
	        <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list">RefCode List</g:link></span>
	            <span class="menuButton"><g:link class="create" action="create">Create RefCode</g:link></span>
	        </div>
            <h1>Edit RefCode</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${refCodeInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr>
						<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="domain"><b>Domain:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'domain','errors')}">
                                    <input type="text" maxlength="100" id="domain" name="domain" value="${fieldValue(bean:refCodeInstance,field:'domain')}"/>
                                    <g:hasErrors bean="${refCodeInstance}" field="domain">
						            <div class="errors">
						                <g:renderErrors bean="${refCodeInstance}" as="list" field="domain"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="value"><b>Value:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'value','errors')}">
                                    <input type="text" maxlength="240" id="value" name="value" value="${fieldValue(bean:refCodeInstance,field:'value')}"/>
                                    <g:hasErrors bean="${refCodeInstance}" field="value">
						            <div class="errors">
						                <g:renderErrors bean="${refCodeInstance}" as="list" field="value"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="abbreviation">Abbreviation:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'abbreviation','errors')}">
                                    <input type="text" maxlength="240" id="abbreviation" name="abbreviation" value="${fieldValue(bean:refCodeInstance,field:'abbreviation')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="meaning">Meaning:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'meaning','errors')}">
                                    <input type="text" maxlength="240" id="meaning" name="meaning" value="${fieldValue(bean:refCodeInstance,field:'meaning')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sortOrder">Sort Order:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'sortOrder','errors')}">
                                    <g:select from="${0..99}" id="sortOrder" name="sortOrder" value="${refCodeInstance?.sortOrder}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="abbreviationOrValue">Abbreviation Or Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'abbreviationOrValue','errors')}">
                                    
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="meaningOrValue">Meaning Or Value:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:refCodeInstance,field:'meaningOrValue','errors')}">
                                    
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