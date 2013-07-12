<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Create Staff</title>         
    </head>
    <body>

   
        <div class="body">
            <h1>Create Staff</h1>

	        <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        	<input type="hidden" name="companyId" value="${companyId}">
                                                       
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName">First Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'firstName','errors')}">
                                    <input type="text" maxlength="34" size="34" id="firstName" name="firstName" value="${fieldValue(bean:personInstance,field:'firstName')}"/>
                                <g:hasErrors bean="${personInstance}" field="firstName">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="firstName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="middleName">Middle Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'middleName','errors')}">
                                    <input type="text" maxlength="34" size="34" id="middleName" name="middleName" value="${fieldValue(bean:personInstance,field:'middleName')}"/>
                                <g:hasErrors bean="${personInstance}" field="middleName">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="middleName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastName">Last Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'lastName','errors')}">
                                    <input type="text" maxlength="34" size="34" id="lastName" name="lastName" value="${fieldValue(bean:personInstance,field:'lastName')}"/>
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
                                    <input type="text" maxlength="34" size="34" id="nickName" name="nickName" value="${fieldValue(bean:personInstance,field:'nickName')}"/>
                                <g:hasErrors bean="${personInstance}" field="nickName">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="nickName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="title">Title:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'title','errors')}">
                                    <input type="text" maxlength="34" size="34" id="title" name="title" value="${fieldValue(bean:personInstance,field:'title')}"/>
                                <g:hasErrors bean="${personInstance}" field="title">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="title"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'active','errors')}">
                                    <g:select id="active" name="active" from="${personInstance.constraints.active.inList}" value="${personInstance.active}" ></g:select>
                                <g:hasErrors bean="${personInstance}" field="active">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="active"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Save" /></span>
                </div>
            </g:form>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
