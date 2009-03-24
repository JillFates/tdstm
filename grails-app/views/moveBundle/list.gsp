

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main" />
  <title>MoveBundle List</title>
</head>
<body>
<div class="menu2">
  <ul>
    <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
    <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
    <li><g:link class="home" controller="asset">Assets </g:link></li>
    <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
    <li><a href="#">Contacts </a></li>
    <li><a href="#">Applications </a></li>
    <li><g:link class="home" controller="moveBundle" params="[projectId:projectId]">Move Bundles</g:link> </li>
  </ul>
</div>
<div class="body">
  <h1>MoveBundle List</h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="list">
    <g:form>
    <table>
      <thead>
        <tr>


          <g:sortableColumn property="name" title="Name" />

          <g:sortableColumn property="description" title="Description" />

          <g:sortableColumn property="operationalOrder" title="Operational Order" />

          <g:sortableColumn property="startTime" title="Start Time" />

          <g:sortableColumn property="completionTime" title="Completion Time" />


        </tr>
      </thead>
      <tbody>
        <g:each in="${moveBundleInstanceList}" status="i" var="moveBundleInstance">
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">


            <td><g:link params="[projectId:projectId]" action="show" id="${moveBundleInstance.id}">${fieldValue(bean:moveBundleInstance, field:'name')}</g:link></td>

            <td>${fieldValue(bean:moveBundleInstance, field:'description')}</td>

            <td>${fieldValue(bean:moveBundleInstance, field:'operationalOrder')}</td>

            <td><tds:convertDateTime date="${moveBundleInstance?.startTime}" /></td>

            <td><tds:convertDateTime date="${moveBundleInstance?.completionTime}" /></td>


          </tr>
        </g:each>
      </tbody>
    </table>
  </div>
  <div class="paginateButtons">
    <g:paginate total="${MoveBundle.count()}" />
  </div>
  <input type="hidden" id="projectId" name="projectId" value="${projectId}"/>
  <div class="buttons"> <span class="button"><g:actionSubmit	class="save" action="Create" value="Create" /></span></div>
  </g:form>
</div>
</body>
</html>
