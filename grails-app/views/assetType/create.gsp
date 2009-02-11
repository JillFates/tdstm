

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create AssetType</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
            <span class="menuButton"><g:link class="list" action="list">AssetType List</g:link></span>
        </div>
        <div class="body">
            <h1>Create AssetType</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetType">Asset Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:assetTypeInstance,field:'assetType','errors')}">
                                    <input type="text" id="assetType" name="assetType" value="${fieldValue(bean:assetTypeInstance,field:'assetType')}"/>
                                <g:hasErrors bean="${assetTypeInstance}" field="assetType">
					            <div class="errors">
					                <g:renderErrors bean="${assetTypeInstance}" as="list" field="assetType"/>
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
