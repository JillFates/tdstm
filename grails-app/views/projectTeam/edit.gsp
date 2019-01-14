<%@page import="net.transitionmanager.domain.ProjectTeam" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Edit Move Bundle Team</title>
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

        <div class="body">
            <h1>Edit Move Bundle Team</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" params="[bundleId:bundleInstance?.id]">Project Team List</g:link></span>
        	</div>
        	<br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${projectTeamInstance?.id}" />
                <input type="hidden" name="bundleId" value="${bundleInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        	<tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="teamCode"><b>Team Code:&nbsp;<span style="color: red">*</span></b></label>
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
                                    <label for="name"><b>Team Name:&nbsp;<span style="color: red">*</span></b></label>
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
                                    <label for="role"><b>Role:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'role','errors')}">
                                    <g:select id="role" name="role" from="${projectTeamInstance.constrainedProperties.role.inList}" valueMessagePrefix="ProjectTeam.role" value="${projectTeamInstance.role}" ></g:select>
                                    <g:hasErrors bean="${projectTeamInstance}" field="role">
						            <div class="errors">
						                <g:renderErrors bean="${projectTeamInstance}" as="list"  field="role" />
						            </div>
						            </g:hasErrors>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'comment','errors')}">
                                    <textarea rows="3" cols="80" id="comment" name="comment"  onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)">${fieldValue(bean:projectTeamInstance,field:'comment')}</textarea>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isDisbanded">Is Disbanded:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectTeamInstance,field:'isDisbanded','errors')}">
                                    <g:select id="isDisbanded" name="isDisbanded" from="${projectTeamInstance.constrainedProperties.isDisbanded.inList}" value="${projectTeamInstance.isDisbanded}" ></g:select>
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
			                                	<option value="${availableStaff?.staff.id}"><g:if test="${availableStaff.company[0]}">${availableStaff.company[0]}:</g:if>${availableStaff?.staff?.lastNameFirstAndTitle}</option>
			                                </g:each>
		                                </select>
	                                </td>
	                                <td valign="middle" style="vertical-align:middle;width: auto;"  >
		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">
										<asset:image src="images/right-arrow.png" style="float: left; border: none;"/>
										</a></span><br/><br/><br/><br/>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove">
		                                <asset:image src="images/left-arrow.png" style="float: left; border: none;"/>
		                                </a></span>
	                                </td>
	                                <td valign="top" style="width: auto;">
		                                <select name="teamMembers" id="teamMembersId" multiple="multiple" size="10" style="width: 313px;">
										<g:each in="${teamMembers}" var="teamMember">
			                                	<option value="${teamMember?.staff.id}" selected="selected"><g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if>${teamMember?.staff?.lastNameFirstAndTitle}</option>
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
         <script type="text/javascript">
        /*
         * validate the text area size
        */
        function textCounter(fieldId, maxlimit) {
        	var value = $("#"+fieldId).val()
            if (value.length > maxlimit) { // if too long...trim it!
            	$("#"+fieldId).val(value.substring(0, maxlimit));
            	return false;
            } else {
            	return true;
            }
        }
        </script>
        <script>
	        currentMenuId = "#eventMenu";
			$("#eventMenuId a").css('background-color','#003366')
	   </script>
    </body>
</html>
