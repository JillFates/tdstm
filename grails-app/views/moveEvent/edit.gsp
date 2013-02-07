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
		     <tds:hasPermission permission='MoveEventEditView '>
		        <span class="menuButton"><g:link class="create" action="create">New Event</g:link></span>
		      </tds:hasPermission>
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
							<td colspan="3"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
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
								<td rowspan="10" width="40%">
									<label for="runbookRecipe">Runbook Recipe:</label><br/>
									<textarea name="runbookRecipe" id="runbookRecipe" cols="80" rows="30" wrap="hard">${moveEventInstance.runbookRecipe}</textarea>
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
                                    <label for="runbookStatus">Runbook Status:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'runbookStatus','errors')}">
                                    <g:select id="runbookStatus" name="runbookStatus" from="${moveEventInstance.constraints.runbookStatus.inList}" value="${moveEventInstance.runbookStatus}" ></g:select>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Runbook version:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'runbookVersion','errors')}">
                                    <input type="text" id="runbookVersion" name="runbookVersion" value="${fieldValue(bean:moveEventInstance,field:'runbookVersion')}"/>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Runbook bridge1 :</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'runbookBridge1','errors')}">
                                    <input type="text" id="runbookBridge1" name="runbookBridge1" value="${fieldValue(bean:moveEventInstance,field:'runbookBridge1')}"/>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Runbook bridge1 :</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'runbookBridge2','errors')}">
                                    <input type="text" id="runbookBridge2" name="runbookBridge2" value="${fieldValue(bean:moveEventInstance,field:'runbookBridge1')}"/>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Video Link:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'videolink','errors')}">
                                    <input type="text" id="videolink" name="videolink" value="${fieldValue(bean:moveEventInstance,field:'videolink')}"/>
                                </td>
                            </tr> 
                            <tr class="prop">
				                <td valign="top" class="name">
				                  <label for="calcMethod"><b>Calculated Type:&nbsp;<span style="color: red">*</span></b></label>
				                </td>
				                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'calcMethod','errors')}">
				                  <g:select id="calcMethod" name="calcMethod" from="${moveEventInstance.constraints.calcMethod.inList}" valueMessagePrefix="step.calcMethod" value="${moveEventInstance.calcMethod}" ></g:select>
				                  <g:hasErrors bean="${moveEventInstance}" field="calcMethod">
				                    <div class="errors">
				                      <g:renderErrors bean="${moveEventInstance}" as="list" field="calcMethod"/>
				                    </div>
				                  </g:hasErrors>
				                </td>
			              	</tr>
                        	<tr class="prop">
				                <td valign="top" class="name">
				                  <label for="inProgress"><b>Status:&nbsp;<span style="color: red">*</span></b></label>
				                </td>
				                <td valign="top" class="value ${hasErrors(bean:moveEventInstance,field:'inProgress','errors')}">
				                  <g:select id="inProgress" name="inProgress" from="${moveEventInstance.constraints.inProgress.inList}" value="${moveEventInstance.inProgress}" valueMessagePrefix="event.inProgress"></g:select>
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
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('WARNING: Deleting this Event will remove any move news and any related step data?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
<script>
	currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
