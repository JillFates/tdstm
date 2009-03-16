

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
                        
							<th>Team Code</th>

                   	        <th>Team Name</th>
							
							<th>Team Members</th>
							                        
                   	        <th>Date Created</th>
                        	
                        	<th>Last Updated</th>
                        	
                        	<th>Comment</th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${projectTeamInstanceList}" status="i" var="projectTeamInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${projectTeamInstance?.projectTeam.id}" params="[projectId:projectId]">${projectTeamInstance?.projectTeam?.teamCode}</g:link></td>
                        
                            <td>${projectTeamInstance?.projectTeam?.name}</td>
                            
                             
                            <td>
                            	<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
			 					${teamMember.company[0]}:${teamMember.name}<br>
								</g:each>
							</td>
                            
                            <td><tds:convertDateTime date="${projectTeamInstance?.projectTeam?.dateCreated}"/></td>

                            <td><tds:convertDateTime date="${projectTeamInstance?.projectTeam?.lastUpdated}"/></td>
                            
                            <td>${projectTeamInstance?.projectTeam?.comment}</td>
                            
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
