<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Company</title>
    </head>
    <body>
        <tds:subHeader title="Admin Company" crumbs="['Admin','Company']"/>

    <div class="body">
			<!-- <h1>Company</h1>  -->
        	<br/>
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
                            <td valign="top" class="name">
                                <label for="comment">Partner:</label>
                            </td>
                            <td valign="top">
                                <g:if test="${partner}">
                                    <input type="checkbox" name="partner" value="" checked="true" disabled="true"> 
                                </g:if>
                                <g:else>
                                    <input type="checkbox" name="partner" value="Y" disabled="true">
                                </g:else>
                                <span style="padding-left: 5px"><b>Note:</b><i>Partners can participate on projects of other companies</i></span>
                            </td>
                        </tr> 

                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>

                            <td valign="top" class="value"><tds:convertDateTime date="${partyGroupInstance?.dateCreated}" /> </td>

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
<script>
	currentMenuId = "#adminMenu";
    $('.menu-list-companies').addClass('active');
    $('.menu-parent-admin').addClass('active');
</script>
    </body>
</html>
