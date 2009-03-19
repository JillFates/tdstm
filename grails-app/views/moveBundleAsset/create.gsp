

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create MoveBundleAsset</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">MoveBundleAsset List</g:link></span>
        </div>
        <div class="body">
            <h1>Create MoveBundleAsset</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${moveBundleAssetInstance}">
            <div class="errors">
                <g:renderErrors bean="${moveBundleAssetInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="moveBundle">Move Bundle:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'moveBundle','errors')}">
                                    <g:select optionKey="id" from="${MoveBundle.list()}" name="moveBundle.id" value="${moveBundleAssetInstance?.moveBundle?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="asset">Asset:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'asset','errors')}">
                                    <g:select optionKey="id" from="${Asset.list()}" name="asset.id" value="${moveBundleAssetInstance?.asset?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sourceTeam">Source Team:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'sourceTeam','errors')}">
                                    <g:select optionKey="id" from="${ProjectTeam.list()}" name="sourceTeam.id" value="${moveBundleAssetInstance?.sourceTeam?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="targetTeam">Target Team:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'targetTeam','errors')}">
                                    <g:select optionKey="id" from="${ProjectTeam.list()}" name="targetTeam.id" value="${moveBundleAssetInstance?.targetTeam?.id}" ></g:select>
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
