

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Project Team</title>
        <g:javascript library="jquery" />
        <script type="text/javascript">  
			   $().ready(function() {  
			    $('#add').click(function() {  
			     return !$('#availableStaffId option:selected').remove().appendTo('#teamMembersId');  
			    });  
			    $('#remove').click(function() {  
			     return !$('#teamMembersId option:selected').remove().appendTo('#availableStaffId');  
			    });  
			   });  
		</script>
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
            <h1>Create Project Team</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        	<input type="hidden" name="projectId" value="${projectId}">
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="teamCode">Team Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'teamCode','errors')}">
                                    <input type="text" id="teamCode" name="teamCode" value="${fieldValue(bean:projectTeamInstance,field:'teamCode')}"/>
                                    <g:hasErrors bean="${projectTeamInstance}" field="teamCode">
						            <div class="errors">
						                <g:renderErrors bean="${projectTeamInstance}" as="list" field="teamCode"/>
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr>
                                                    
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Team Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:projectTeamInstance,field:'name')}"/>
                                    <g:hasErrors bean="${projectTeamInstance}" field="name">
						            <div class="errors">
						                <g:renderErrors bean="${projectTeamInstance}" as="list"  field="name" />
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'comment','errors')}">
                                    <textarea rows="3" cols="80" id="comment" name="comment">${fieldValue(bean:projectTeamInstance,field:'comment')}</textarea>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isDisbanded">Is Disbanded:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'isDisbanded','errors')}">
                                    <g:select id="isDisbanded" name="isDisbanded" from="${projectTeamInstance.constraints.isDisbanded.inList}" value="${projectTeamInstance.isDisbanded}"></g:select>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="value" colspan="2">
                                <table style="border: none;">
                                <tr>
                               <td valign="top" class="name">
                                    <label >Available Staff:</label>
                                </td>
                                <td valign="top" class="name">
                                    <label >&nbsp;</label>
                                </td>
                                <td valign="top" class="name">
                                    <label >Team Members:</label>
                                </td>
                                </tr>
                                <tr >
	                                <td valign="top" style="width: 10">
		                                <select name="availableStaff" id="availableStaffId" multiple="multiple" size="10" style="width: 250px">
			                                <g:each in="${projectStaff}" var="projectStaff">
			                                	<option value="${projectStaff?.staff.id}">${projectStaff.company[0]}:${projectStaff.name}</option>
			                                </g:each> 
		                                </select>
	                                </td>
	                                <td valign="middle" style="vertical-align:middle;" style="width: auto;"  >
		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">Assign &gt;&gt;</a></span><br><br>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove">&lt;&lt; Remove</a></span>
	                                </td>
	                                <td valign="top" style="width: auto;">
		                                <select name="teamMembers" id="teamMembersId" multiple="multiple" size="10" style="width: 250px">
		                                </select>
	                                </td>
                                </tr>
                                </table>
                                </td>
                                
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
