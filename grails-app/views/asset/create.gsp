

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Asset</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">Asset List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Asset</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="projectName">Project Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'projectName','errors')}">
                                    <g:select optionKey="id" from="${Project.list()}" name="projectName.id" value="${assetInstance?.projectName?.id}" ></g:select>
                                <g:hasErrors bean="${assetInstance}" field="projectName">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="projectName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetType">Asset Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetType','errors')}">
                                    <g:select optionKey="id" from="${AssetType.list()}" name="assetType.id" value="${assetInstance?.assetType?.id}" noSelection="['null':'']"></g:select>
                                <g:hasErrors bean="${assetInstance}" field="assetType">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="assetType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetName">Asset Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetName','errors')}">
                                    <input type="text" id="assetName" name="assetName" value="${fieldValue(bean:assetInstance,field:'assetName')}"/>
                                <g:hasErrors bean="${assetInstance}" field="assetName">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="assetName"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetTag">Asset Tag:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetTag','errors')}">
                                    <input type="text" id="assetTag" name="assetTag" value="${fieldValue(bean:assetInstance,field:'assetTag')}"/>
                                <g:hasErrors bean="${assetInstance}" field="assetTag">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="assetTag"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="serialNumber">Serial Number:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'serialNumber','errors')}">
                                    <input type="text" id="serialNumber" name="serialNumber" value="${fieldValue(bean:assetInstance,field:'serialNumber')}"/>
                                <g:hasErrors bean="${assetInstance}" field="serialNumber">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="serialNumber"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="deviceFunction">Device Function:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'deviceFunction','errors')}">
                                    <input type="text" id="deviceFunction" name="deviceFunction" value="${fieldValue(bean:assetInstance,field:'deviceFunction')}"/>
                                <g:hasErrors bean="${assetInstance}" field="deviceFunction">
					            <div class="errors">
					                <g:renderErrors bean="${assetInstance}" as="list" field="deviceFunction"/>
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
