

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Move Bundle Team</title>
    </head>
    <body>
        <div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:bundleInstance?.project?.id]" >Staff</g:link></li>
            <li><g:link class="home" controller="asset">Assets </g:link></li>
            <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
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
          </ul>
		</div>
        <div class="body">
            <h1>Show Move Bundle Team</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" params="[bundleId:bundleInstance?.id]">Project Team List</g:link></span>
        	</div>
        	<br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name">Team Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectTeamInstance, field:'teamCode')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Team Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectTeamInstance, field:'name')}</td>
                            
                        </tr>
                        
						<tr class="prop">
                            <td valign="top" class="name">Comment:</td>
                            
                            <td valign="top" class="value">
                            <textarea rows="3" cols="80" readonly="readonly">${fieldValue(bean:projectTeamInstance, field:'comment')}</textarea>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Is Disbanded:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectTeamInstance, field:'isDisbanded')}</td>
                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">Team Members:</td>
                            
                            <td valign="top" class="value">
                            <g:each in="${teamMembers}" var="teamMember">
			 					${teamMember.company[0]}:${teamMember?.staff?.lastName}, ${teamMember?.staff?.firstName} - ${teamMember?.staff?.title}<br>
							</g:each>
                            </td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>
                            
                            <td valign="top" class="value"><tds:convertDateTime date="${projectTeamInstance?.dateCreated}" /> </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Updated:</td>
                            
                            <td valign="top" class="value"><tds:convertDateTime date="${projectTeamInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${projectTeamInstance?.id}" />
                    <input type="hidden" name="bundleId" value="${bundleInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
