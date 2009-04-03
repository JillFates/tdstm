

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show AssetEntity</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">AssetEntity List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New AssetEntity</g:link></span>
        </div>
        <div class="body">
            <h1>Show AssetEntity</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Server Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'serverName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Model:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'model')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Source Location:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'sourceLocation')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Target Location:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'targetLocation')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Source Rack:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'sourceRack')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Target Rack:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'targetRack')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Position:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'position')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Unit Size:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'unitSize')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Project:</td>
                            
                            <td valign="top" class="value"><g:link controller="project" action="show" id="${assetEntityInstance?.project?.id}">${assetEntityInstance?.project?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Type:</td>
                            
                            <td valign="top" class="value"><g:link controller="assetType" action="show" id="${assetEntityInstance?.assetType?.id}">${assetEntityInstance?.assetType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'assetName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Tag:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'assetTag')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Serial Number:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'serialNumber')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Device Function:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'deviceFunction')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset Entity Varchars:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="a" in="${assetEntityInstance.assetEntityVarchars}">
                                    <li><g:link controller="assetEntityVarchar" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Attribute Set:</td>
                            
                            <td valign="top" class="value"><g:link controller="eavAttributeSet" action="show" id="${assetEntityInstance?.attributeSet?.id}">${assetEntityInstance?.attributeSet?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'dateCreated')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Entity Attribute:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="e" in="${assetEntityInstance.entityAttribute}">
                                    <li><g:link controller="eavEntityAttribute" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Updated:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:assetEntityInstance, field:'lastUpdated')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${assetEntityInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
