<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Edit Move Event</title>
    </head>
    <body>
        <div class="body">
            <h1>Edit Move Event</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		      <span class="menuButton"><g:link class="list" action="list">Events List</g:link></span>
		      <jsec:hasRole name="ADMIN">
		        <span class="menuButton"><g:link class="create" action="create">New Event</g:link></span>
		      </jsec:hasRole>
		    </div>
		    <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${moveEventInstance}">
            <div class="errors">
                <g:renderErrors bean="${moveEventInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${moveEventInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                        	<tr class="prop">
	                            <td valign="top" class="name">Project:</td>
	                            
	                            <td valign="top" class="value"><g:link controller="project" action="show" id="${moveEventInstance?.project?.id}">${moveEventInstance?.project?.encodeAsHTML()}</g:link></td>
                            
                        	</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:moveEventInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:moveEventInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="moveBundles">Move Bundles:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'moveBundles','errors')}">
									<ul>
									<g:each in="${moveBundles}" var="moveBundle">
										<g:if test="${moveEventInstance.moveBundles.contains(moveBundle)}">
											<input type="checkbox" name="moveBundle" value="${moveBundle.id}" checked="checked"> &nbsp;${moveBundle.name}<br>
										</g:if>
										<g:else>
											<input type="checkbox" name="moveBundle" value="${moveBundle.id}"> &nbsp;${moveBundle.name}<br>
										</g:else>
									</g:each>
									</ul>
                                </td>
                            </tr> 
                        	<tr class="prop">
				                <td valign="top" class="name">
				                  <label for="inProgress"><b>In Progress:&nbsp;<span style="color: red">*</span></b></label>
				                </td>
				                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'inProgress','errors')}">
				                  <g:select id="inProgress" name="inProgress" from="${moveEventInstance.constraints.inProgress.inList}" value="${moveEventInstance.inProgress}" ></g:select>
				                  <g:hasErrors bean="${moveEventInstance}" field="inProgress">
				                    <div class="errors">
				                      <g:renderErrors bean="${moveEventInstance}" as="list" field="inProgress"/>
				                    </div>
				                  </g:hasErrors>
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
