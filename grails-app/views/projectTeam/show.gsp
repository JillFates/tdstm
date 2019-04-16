<%@page import="net.transitionmanager.project.ProjectTeam" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Show Move Bundle Team</title>
    </head>
    <body>

        <div class="body">
            <h1>Show Move Bundle Team</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" params="[bundleId:bundleInstance?.id]">Project Team List</g:link></span>
        	</div>
        	<br/>
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
				            <td valign="top" class="name">Role:</td>

				            <td valign="top" class="value"><g:if test="${projectTeamInstance?.role}"> <g:message code="ProjectTeam.role.${projectTeamInstance?.role}" /></g:if></td>
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
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if>${teamMember?.staff?.lastNameFirstAndTitle}<br/>
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
        <script>
	        currentMenuId = "#eventMenu";
			$("#eventMenuId a").css('background-color','#003366')
	   </script>
    </body>
</html>
