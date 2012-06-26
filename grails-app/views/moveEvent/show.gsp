<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Show Move Event</title>
        <script type="text/javascript">
        function clearHistoricData( moveEventId ){
            $("#messageDiv").hide();
            $("#messageDiv").html("");
            var confirmStatus = confirm("Are you sure you want to permanently clear the dashboard data for this move event?")
            if(confirmStatus){
            	${remoteFunction(action:'clearHistoricData', params:'\'moveEventId=\' + moveEventId ', 
                    	onSuccess:"jQuery('#messageDiv').html('Dashboard History has been cleaned successfully');jQuery('#messageDiv').show()")}
            }
        }
        </script>
    </head>
    <body>
        <div class="body">
            <h1>Show Move Event</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		      <span class="menuButton"><g:link class="list" action="list">Events List</g:link></span>
		     <tds:hasPermission permission='MoveEventShowView '>
		        <span class="menuButton"><g:link class="create" action="create">New Event</g:link></span>
		     </tds:hasPermission>
		    </div>
		    <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div id="messageDiv" class="message" style="display: none;"></div>
            <div class="dialog">
                <table>
                    <tbody>
                    	<tr class="prop">
                            <td valign="top" class="name">Project:</td>
                            
                            <td valign="top" class="value"><g:link controller="project" action="show" id="${moveEventInstance?.project?.id}">${moveEventInstance?.project?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'name')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'description')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Move Bundles:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="m" in="${moveEventInstance.moveBundles}">
                                    <li><g:link controller="moveBundle" action="show" id="${m.id}">${m?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                        </tr>
                        <tr class="prop">
				            <td valign="top" class="name">Calculated Type:</td>
				
				            <td valign="top" class="value">
				            	<g:if test="${moveEventInstance.calcMethod != 'L'}">Manual</g:if>
								<g:else>Linear</g:else>
							</td>
						</tr>
                        <tr class="prop">
				            <td valign="top" class="name">Runbook Status:</td>
				
				            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'runbookStatus')}</td>
						</tr><tr class="prop">
				            <td valign="top" class="name">Runbook Version:</td>
				
				            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'runbookVersion')}</td>
						</tr><tr class="prop">
				            <td valign="top" class="name">Runbook bridge1 :</td>
				
				            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'runbookBridge1')}</td>
						</tr><tr class="prop">
				            <td valign="top" class="name">Runbook bridge2 :</td>
				
				            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'runbookBridge2')}</td>
						</tr><tr class="prop">
				            <td valign="top" class="name">Video Link::</td>
				
				            <td valign="top" class="value">${fieldValue(bean:moveEventInstance, field:'videolink')}</td>
						</tr>
                        <tr class="prop">
				            <td valign="top" class="name">Status:</td>
				
				            <td valign="top" class="value"><g:message code="event.inProgress.${moveEventInstance?.inProgress}" /></td>
						</tr>
                    
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                <tds:hasPermission permission='MoveEventEditView'>
                    <input type="hidden" name="id" id="moveEventId"  value="${moveEventInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('WARNING: Deleting this Event will remove any move news and any related step data?');" value="Delete" /></span>
                    <span class="button"><input type="button" class="delete" value="Clear Dashboard History" onclick="clearHistoricData( $('#moveEventId').val() )"/></span>
                 </tds:hasPermission>
                </g:form>
            </div>
        </div>
<script>
	currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
