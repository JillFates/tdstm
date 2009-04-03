

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Move Bundle Team List</title>
    </head>
    <body>
    	<div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:bundleInstance?.project?.id]" >Staff</g:link></li>
            <li><g:link class="home" controller="assetEntity" params="[projectId:bundleInstance?.project?.id]">Assets </g:link></li>
	<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:bundleInstance?.project?.id]">Import/Export</g:link> </li>
            <li><a href="#">Contacts </a></li>
            <li><a href="#">Applications </a></li>
            <li><g:link class="home" controller="moveBundle" params="[projectId:bundleInstance?.project?.id]">Move Bundles</g:link></li>
          </ul>
		</div>
    	<div class="menu2" style="background-color:#003366;">
          <ul>
			<li class="title1">Move Bundle: ${bundleInstance?.name}</li>
			<li><g:link class="home" controller="projectTeam" action="list" params="[bundleId:bundleInstance?.id]" >Team </g:link> </li>
			<li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:bundleInstance?.id]" >Bundle Asset Assignment</g:link> </li>
			<li><g:link class="home" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:bundleInstance?.id, rack:'UnrackPlan']" >Bundle Team Assignment </g:link> </li>
			</ul>
		</div>
        <div class="body">
            <h1>Move Bundle Team List</h1>
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
                        
                            <td><g:link action="show" id="${projectTeamInstance?.projectTeam.id}" params="[bundleId:bundleInstance?.id]">${projectTeamInstance?.projectTeam?.teamCode}</g:link></td>
                        
                            <td>${projectTeamInstance?.projectTeam?.name}</td>
                            
                             
                            <td>
                            	<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
			 					${teamMember.company[0]}:${teamMember?.staff?.lastName}, ${teamMember?.staff?.firstName} - ${teamMember?.staff?.title}<br>
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
            		<input type="hidden" name="bundleId" value="${bundleInstance?.id}">
					<span class="button"><g:actionSubmit class="create" action="Create" value="New Project Team" /></span>
					</g:form>
				</div>
			</div>            
    </body>
</html>
