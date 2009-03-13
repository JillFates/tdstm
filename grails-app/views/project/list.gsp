

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Project List</title>
 <% def currProj = session.getAttribute("CURR_PROJ");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId);
    }
    %>
</head>
<body>
<g:if test="${currProjObj}">
		 <div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
            <li><g:link class="home" controller="asset">Assets </g:link></li>
            <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
            <li><g:link class="home" controller="projectTeam" action="list" params="[projectId:currProjObj?.id]" >Team </g:link> </li>
            <li><a href="#">Contacts </a></li>
            <li><a href="#">Applications </a></li>
            <li><a href="#">Move Bundles </a></li>
          </ul>
        </div>
        </g:if>
<div class="body"><br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list"><g:form action="addUserPreference" method="post">
	<table>
		<thead>
			<tr>
				<th></th>

				<g:sortableColumn property="projectCode" title="Project Code" />

				<g:sortableColumn property="name" title="Name" />

				<g:sortableColumn property="dateCreated" title="Date Created" />

				<g:sortableColumn property="lastUpdated" title="Last Updated" />

				<g:sortableColumn property="comment" title="Comment" />

			</tr>
		</thead>
		<tbody>
			<g:each in="${projectInstanceList}" status="i" var="projectInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

					<td>
					<g:if test="${currProjObj?.id == projectInstance?.id}" >
						<input type="radio" name="selectProject" id="selectProjectId" checked="checked" value="${fieldValue(bean:projectInstance, field:'projectCode')}">
					</g:if>
					<g:else>
						<input type="radio" name="selectProject" id="selectProjectId" value="${fieldValue(bean:projectInstance, field:'projectCode')}">
					</g:else>
					</td>

					<td><g:link controller="project" action="show" id="${projectInstance.id}">${fieldValue(bean:projectInstance, field:'projectCode')}</g:link></td>

					<td>${fieldValue(bean:projectInstance, field:'name')}</td>

					<td><tds:convertDateTime date="${projectInstance?.dateCreated}"/> </td>

					<td><tds:convertDateTime date="${projectInstance?.lastUpdated}"/></td>

					<td>${fieldValue(bean:projectInstance, field:'comment')}</td>

				</tr>
			</g:each>
		</tbody>
	</table>
	<div class="buttons">
				<span class="button"><input class="select" type="submit" name="submit" value="Select"> </span> </div>
</g:form></div>
</div>
</body>
</html>
