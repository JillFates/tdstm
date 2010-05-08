

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="projectHeader" />
  <title>MoveBundle List</title>
</head>
<body>

<div class="body">
<g:form>
  <h1>MoveBundle List</h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="list">
    
    <table>
      <thead>
        <tr>


          <g:sortableColumn property="name" title="Name" />

          <g:sortableColumn property="description" title="Description" />

          <g:sortableColumn property="operationalOrder" title="Order" />
          
          <th class="sortable"><a href="#">Asset Qty</a></th>

          <g:sortableColumn property="startTime" title="Start Time" />

          <g:sortableColumn property="completionTime" title="Completion Time" />


        </tr>
      </thead>
      <tbody>
        <g:each in="${moveBundleList}" status="i" var="moveBundle">
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">


            <td><g:link params="[projectId:projectId]" action="show" id="${moveBundle?.bundle?.id}">${moveBundle?.bundle?.name}</g:link></td>

            <td>${moveBundle?.bundle?.description}</td>

            <td>${moveBundle?.bundle?.operationalOrder}</td>
            
            <td>${moveBundle?.assetCount}</td>

            <td><tds:convertDateTime date="${moveBundle?.bundle?.startTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

            <td><tds:convertDateTime date="${moveBundle?.bundle?.completionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>


          </tr>
        </g:each>
      </tbody>
    </table>
  </div>
  <g:if test="${MoveBundle.findAll('from MoveBundle where project = '+projectId).size() > 10}">
  <div class="paginateButtons">
    <g:paginate total="${MoveBundle.findAll('from MoveBundle where project = '+projectId).size()}" max="20" />
  </div>
  </g:if>
  <input type="hidden" id="projectId" name="projectId" value="${projectId}"/>
  <div class="buttons"> <span class="button"><g:actionSubmit	class="save" action="Create" value="Create" /></span></div>
  </g:form>
</div>
</body>
</html>
