

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Assety</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">AssetEntity List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New AssetEntity</g:link></span>
        </div>
        <div class="body">
            <h1>Edit AssetEntity</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${assetEntityInstance}">
            <div class="errors">
                <g:renderErrors bean="${assetEntityInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${assetEntityInstance?.id}" />
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
                                    <label for="assetEntityVarchars">Asset Entity Varchars:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'assetEntityVarchars','errors')}">
                                    
<ul>
<g:each var="a" in="${assetEntityInstance?.assetEntityVarchars?}">
    <li><g:link controller="assetEntityVarchar" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="assetEntityVarchar" params="['assetEntity.id':assetEntityInstance?.id]" action="create">Add AssetEntityVarchar</g:link>

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
                                    <label for="entityAttribute">Entity Attribute:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'entityAttribute','errors')}">
                                    
<ul>
<g:each var="e" in="${assetEntityInstance?.entityAttribute?}">
    <li><g:link controller="eavEntityAttribute" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="eavEntityAttribute" params="['assetEntity.id':assetEntityInstance?.id]" action="create">Add EavEntityAttribute</g:link>

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
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
