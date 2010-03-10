

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Model</title>         
    </head>
    <body>
        <div class="body">
	        <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list">Model List</g:link></span>
	        </div>
            <h1>Create Model</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${modelInstance}">
            <div class="errors">
                <g:renderErrors bean="${modelInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
	                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:modelInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:modelInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="deviceType">Device Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'deviceType','errors')}">
                                    <g:select optionKey="id" from="${RefCode.findAllByDomain('kvmDevice')}" name="deviceType.id" value="${modelInstance?.deviceType?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="manufacturer">Manufacturer:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:modelInstance,field:'manufacturer','errors')}">
                                    <g:select optionKey="id" from="${Manufacturer.list()}" name="manufacturer.id" value="${modelInstance?.manufacturer?.id}" ></g:select>
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
