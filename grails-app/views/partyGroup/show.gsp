

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Show Company</title>
    </head>
    <body>
       
        <div class="body">
			<h1>Show Company</h1>
			<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" >Company List</g:link></span>
        	</div>
        	<br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:partyGroupInstance, field:'name')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Comment:</td>
                            
                            <td valign="top" class="value">
                            <textarea name="comment" cols="80" rows="3"	readonly="readonly" >${fieldValue(bean:partyGroupInstance, field:'comment')}</textarea>
                            
                            </td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>

                            <td valign="top" class="value"><tds:convertDateTime date="${partyGroupInstance?.dateCreated}"/> </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">Last Updated:</td>

                            <td valign="top" class="value"><tds:convertDateTime date="${partyGroupInstance?.lastUpdated}" /> </td>

                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${partyGroupInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
