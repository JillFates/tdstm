

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show MoveBundleAsset</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">MoveBundleAsset List</g:link></span>            
        </div>
        <div class="body">
            <h1>Show MoveBundleAsset</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>                    
                       
                    
                        <tr class="prop">
                            <td valign="top" class="name">Move Bundle:</td>
                            
                            <td valign="top" class="value">${moveBundleAssetInstance?.moveBundle}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Asset:</td>
                            
                            <td valign="top" class="value"><g:link controller="asset" action="show" id="${moveBundleAssetInstance?.asset?.id}">${moveBundleAssetInstance?.asset?.assetName?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Source Team:</td>
                            
                            <td valign="top" class="value"><g:link controller="projectTeam" action="show" id="${moveBundleAssetInstance?.sourceTeam?.id}">${moveBundleAssetInstance?.sourceTeam?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Target Team:</td>
                            
                            <td valign="top" class="value"><g:link controller="projectTeam" action="show" id="${moveBundleAssetInstance?.targetTeam?.id}">${moveBundleAssetInstance?.targetTeam?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${moveBundleAssetInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
