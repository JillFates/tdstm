

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create AssetEntity</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">AssetEntity List</g:link></span>
        </div>
        <div class="body">
            <h1>Create AssetEntity</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${assetEntityInstance}">
            <div class="errors">
                <g:renderErrors bean="${assetEntityInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="serverName">Server Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'serverName','errors')}">
                                    <input type="text" id="serverName" name="serverName" value="${fieldValue(bean:assetEntityInstance,field:'serverName')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="model">Model:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'model','errors')}">
                                    <input type="text" id="model" name="model" value="${fieldValue(bean:assetEntityInstance,field:'model')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sourceLocation">Source Location:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'sourceLocation','errors')}">
                                    <input type="text" id="sourceLocation" name="sourceLocation" value="${fieldValue(bean:assetEntityInstance,field:'sourceLocation')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="targetLocation">Target Location:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'targetLocation','errors')}">
                                    <input type="text" id="targetLocation" name="targetLocation" value="${fieldValue(bean:assetEntityInstance,field:'targetLocation')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sourceRack">Source Rack:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'sourceRack','errors')}">
                                    <input type="text" id="sourceRack" name="sourceRack" value="${fieldValue(bean:assetEntityInstance,field:'sourceRack')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="targetRack">Target Rack:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'targetRack','errors')}">
                                    <input type="text" id="targetRack" name="targetRack" value="${fieldValue(bean:assetEntityInstance,field:'targetRack')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="position">Position:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'position','errors')}">
                                    <input type="text" id="position" name="position" value="${fieldValue(bean:assetEntityInstance,field:'position')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="unitSize">Unit Size:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'unitSize','errors')}">
                                    <input type="text" id="unitSize" name="unitSize" value="${fieldValue(bean:assetEntityInstance,field:'unitSize')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="project">Project:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'project','errors')}">
                                    <g:select optionKey="id" from="${Project.list()}" name="project.id" value="${assetEntityInstance?.project?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetType">Asset Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'assetType','errors')}">
                                    
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetName">Asset Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'assetName','errors')}">
                                    <input type="text" id="assetName" name="assetName" value="${fieldValue(bean:assetEntityInstance,field:'assetName')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetTag">Asset Tag:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'assetTag','errors')}">
                                    <input type="text" id="assetTag" name="assetTag" value="${fieldValue(bean:assetEntityInstance,field:'assetTag')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="serialNumber">Serial Number:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'serialNumber','errors')}">
                                    <input type="text" id="serialNumber" name="serialNumber" value="${fieldValue(bean:assetEntityInstance,field:'serialNumber')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="deviceFunction">Device Function:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'deviceFunction','errors')}">
                                    <input type="text" id="deviceFunction" name="deviceFunction" value="${fieldValue(bean:assetEntityInstance,field:'deviceFunction')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="attributeSet">Attribute Set:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'attributeSet','errors')}">
                                    <g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${assetEntityInstance?.dateCreated}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${assetEntityInstance?.lastUpdated}" ></g:datePicker>
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
