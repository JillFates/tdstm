

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Project Team List</title>
    </head>
    <body>
    	<div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
            <li><g:link class="home" controller="asset">Assets </g:link></li>
            <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
            <li><g:link class="home" controller="projectTeam" action="list" params="[projectId:projectId]" >Team </g:link> </li>
            <li><a href="#">Contacts </a></li>
            <li><a href="#">Applications </a></li>
            <li><a href="#">Move Bundles </a></li>
          </ul>
		</div>
        <div class="body">
            <h1>Project Team List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
							<g:sortableColumn property="teamCode" title="Team Code" />

                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="dateCreated" title="Date Created" />
                        	
                        	<g:sortableColumn property="lastUpdated" title="Last Updated" />
                        	
                        	<g:sortableColumn property="comment" title="Comment" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${projectTeamInstanceList}" status="i" var="projectTeamInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${projectTeamInstance.id}">${fieldValue(bean:projectTeamInstance, field:'teamCode')}</g:link></td>
                        
                            <td>${fieldValue(bean:projectTeamInstance, field:'name')}</td>
                            
                            <td><tds:convertDateTime date="${projectTeamInstance?.dateCreated}"/></td>

                            <td><tds:convertDateTime date="${projectTeamInstance?.lastUpdated}"/></td>
                            
                            <td>${fieldValue(bean:projectTeamInstance, field:'comment')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
            		<g:form>
            		<input type="hidden" name="projectId" value="${projectId}">
					<span class="button"><g:actionSubmit class="create" action="Create" value="New Project Team" /></span>
					</g:form>
				</div>
			</div>            
    </body>
</html>
