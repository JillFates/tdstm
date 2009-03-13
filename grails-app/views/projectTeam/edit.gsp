

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit ProjectTeam</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">ProjectTeam List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New ProjectTeam</g:link></span>
        </div>
        <div class="body">
            <h1>Edit ProjectTeam</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${projectTeamInstance}">
            <div class="errors">
                <g:renderErrors bean="${projectTeamInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${projectTeamInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${projectTeamInstance?.dateCreated}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${projectTeamInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${projectTeamInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:projectTeamInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'comment','errors')}">
                                    <input type="text" id="comment" name="comment" value="${fieldValue(bean:projectTeamInstance,field:'comment')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="teamCode">Team Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'teamCode','errors')}">
                                    <input type="text" id="teamCode" name="teamCode" value="${fieldValue(bean:projectTeamInstance,field:'teamCode')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="project">Project:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'project','errors')}">
                                    <g:select optionKey="id" from="${Project.list()}" name="project.id" value="${projectTeamInstance?.project?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isDisbanded">Is Disbanded:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'isDisbanded','errors')}">
                                    <g:select id="isDisbanded" name="isDisbanded" from="${projectTeamInstance.constraints.isDisbanded.inList}" value="${projectTeamInstance.isDisbanded}" noSelection="['':'']"></g:select>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
