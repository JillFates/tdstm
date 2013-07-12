<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>MoveBundleAsset List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create" >Create MoveBundleAsset</g:link></span>
        </div>
        <div class="body">
            <h1>MoveBundleAsset List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>

                   	        <g:sortableColumn property="id" title="Id" />

                   	        <th>Bundle</th>

                   	        <th>Asset</th>

                   	        <th>Source Team</th>

                   	        <th>Target Team</th>

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${moveBundleAssetInstanceList}" status="i" var="moveBundleAssetInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td><g:link action="show" id="${moveBundleAssetInstance.id}">${fieldValue(bean:moveBundleAssetInstance, field:'id')}</g:link></td>

                            <td>${fieldValue(bean:moveBundleAssetInstance, field:'moveBundle')}</td>

                            <td>${fieldValue(bean:moveBundleAssetInstance, field:'asset.assetName')}</td>

                            <td>${fieldValue(bean:moveBundleAssetInstance, field:'sourceTeamMt')}</td>

                            <td>${fieldValue(bean:moveBundleAssetInstance, field:'targetTeamMt')}</td>

                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${MoveBundleAsset.count()}" />
            </div>
        </div>
    </body>
</html>
