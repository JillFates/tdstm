

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
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
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:personInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${personInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                <g:hasErrors bean="${personInstance}" field="partyType">
					            <div class="errors">
					                <g:renderErrors bean="${personInstance}" as="list" field="partyType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="company">Company :</label>
                                </td>
                              	<td valign="top" class="value">
	                              	<select id="company" name="company">
					                    <g:each status="i" in="${companies}" var="company">
					                      <option value="${company.id}">${company}</option>
					                    </g:each>
	                				</select>
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
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
