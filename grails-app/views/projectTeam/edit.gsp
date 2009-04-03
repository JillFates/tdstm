

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Move Bundle Team</title>
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
          </ul>
		</div>
        <div class="body">
            <h1>Edit Move Bundle Team</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" params="[bundleId:bundleInstance?.id]">Project Team List</g:link></span>
        	</div>
        	<br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${projectTeamInstance?.id}" />
                <input type="hidden" name="bundleId" value="${bundleInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
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
							                <g:renderErrors bean="${projectTeamInstance}" as="list" field="name"/>
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
                                    <g:select id="isDisbanded" name="isDisbanded" from="${projectTeamInstance.constraints.isDisbanded.inList}" value="${projectTeamInstance.isDisbanded}" ></g:select>
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
		                                <select name="availableStaff" id="availableStaffId" multiple="multiple" size="10" style="width: 313px;">
			                                <g:each in="${availableStaff}" var="availableStaff">
			                                	<option value="${availableStaff?.staff.id}">${availableStaff.company[0]}:${availableStaff?.staff?.lastName}, ${availableStaff?.staff?.firstName} - ${availableStaff?.staff?.title}</option>
			                                </g:each> 
		                                </select>
	                                </td>
	                                <td valign="middle" style="vertical-align:middle;" style="width: auto;"  >
		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">
										<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;">
										</a></span><br><br><br><br>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove">
		                                <img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;">
		                                </a></span>
	                                </td>
	                                <td valign="top" style="width: auto;">
		                                <select name="teamMembers" id="teamMembersId" multiple="multiple" size="10" style="width: 313px;">
										<g:each in="${teamMembers}" var="teamMember">
			                                	<option value="${teamMember?.staff.id}" selected="selected">${teamMember.company[0]}:${teamMember?.staff?.lastName}, ${teamMember?.staff?.firstName} - ${teamMember?.staff?.title}</option>
										</g:each>  
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
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
