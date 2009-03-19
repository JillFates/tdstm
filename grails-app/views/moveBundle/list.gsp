

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>MoveBundle List</title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
      <span class="menuButton"><g:link class="create" action="create">New MoveBundle</g:link></span>
    </div>
    <div class="body">
      <h1>MoveBundle List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <table>
          <thead>
            <tr>

              <th></th>

              <th>Assoiciated Assets</th>

              <g:sortableColumn property="name" title="Name" />

              <g:sortableColumn property="description" title="Description" />

              <g:sortableColumn property="startTime" title="Start Time" />

              <g:sortableColumn property="finishTime" title="Finish Time" />

            </tr>
          </thead>
          <tbody>
            <g:each in="${moveBundleInstanceList}" status="i" var="moveBundleInstance">
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                <td>
                  <g:link class="home" controller="moveBundleAsset" params="[id:moveBundleInstance.id]" action="moveBundlelist">Move Bundles Asset</g:link>
                </td>

                <td><g:each in="${MoveBundleAsset.findAll('from MoveBundleAsset m where m.moveBundle.id = '+moveBundleInstance?.id)}" var="moveBundle">
                    ${moveBundle?.asset.assetName}<br>
                  </g:each>
                </td>


                <td><g:link action="show" id="${moveBundleInstance.id}" title="Edit  '${fieldValue(bean:moveBundleInstance, field:'name')}'">${fieldValue(bean:moveBundleInstance, field:'name')}</g:link></td>

                <td>${fieldValue(bean:moveBundleInstance, field:'description')}</td>

                <td>${fieldValue(bean:moveBundleInstance, field:'startTime')}</td>

                <td>${fieldValue(bean:moveBundleInstance, field:'finishTime')}</td>

              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
      <div class="paginateButtons">
        <g:paginate total="${MoveBundle.count()}" />
      </div>
    </div>
  </body>
</html>
