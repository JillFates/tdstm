<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Show Event</title>
    </head>
    <body>
		<tds:subHeader title="Event Details" crumbs="['Planning','Event', 'Details']"/>
        <div class="body">
            <div class="nav" style="border: 1px solid #CCCCCC; height: 25px">
		      <span class="menuButton"><g:link class="list" action="list">Events List</g:link></span>
				<span class="menuButton"><g:link class="create" controller="task" action="taskGraph"
					params="[moveEventId: moveEventInstance.id]">View Task Graph</g:link></span>
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
                            <td class="name">Project:</td>
                            <td class="valueNW"><g:link controller="project" action="show" id="${moveEventInstance?.project?.id}">${moveEventInstance?.project?.encodeAsHTML()}</g:link></td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Name:</td>
                            <td class="valueNW"><b>${fieldValue(bean:moveEventInstance, field:'name')}</b></td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Description:</td>
                            <td class="valueNW">${fieldValue(bean:moveEventInstance, field:'description')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Bundles:</td>
                            <td style="text-align:left;" class="valueNW">
                                <ul>
                                <g:each var="m" in="${moveEventInstance.moveBundles}">
                                    <li><g:link controller="moveBundle" action="show" id="${m.id}">${m?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                        </tr>
                        <tr class="prop">
				            <td class="name">Calculated Type:</td>
				
				            <td class="valueNW">
				            	<g:if test="${moveEventInstance.calcMethod != 'L'}">Manual</g:if>
								<g:else>Linear</g:else>
							</td>
						</tr>
                        <tr class="prop">
				            <td class="name">Runbook Status:</td>
				            <td class="valueNW">${fieldValue(bean:moveEventInstance, field:'runbookStatus')}</td>
						</tr><tr class="prop">
				            <td class="name">Runbook bridge1 :</td>
				            <td class="valueNW">${fieldValue(bean:moveEventInstance, field:'runbookBridge1')}</td>
						</tr><tr class="prop">
				            <td class="name">Runbook bridge2 :</td>
				            <td class="valueNW">${fieldValue(bean:moveEventInstance, field:'runbookBridge2')}</td>
						</tr><tr class="prop">
				            <td class="name">Video Link:</td>
				            <td class="valueNW">${fieldValue(bean:moveEventInstance, field:'videolink')}</td>
						</tr>
                        <tr class="prop">
				            <td  class="name">News Bar Mode:</td>
				            <td class="valueNW"><g:message code="event.newsBarMode.${moveEventInstance?.newsBarMode}" /></td>
						</tr>
                        <tr class="prop">
				            <td  class="name">Estimated Start:</td>
				            <td class="valueNW"><tds:convertDateTime date="${moveEventInstance?.estStartTime}" /></td>
						</tr>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                <tds:hasPermission permission='MoveEventEditView'>
                    <input type="hidden" name="id" id="moveEventId"  value="${moveEventInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('WARNING: Deleting this Event will remove any news and any related step data?');" value="Delete" /></span>
                    <span class="button"><input type="button" class="delete" value="Clear Dashboard History" onclick="clearHistoricData( $('#moveEventId').val())"/></span>
                    <span class="button"><input type="button" class="delete" value="Delete Generated Tasks" onclick="clearTaskData( $('#moveEventId').val(), 'delete')"/></span>
                    <span class="button"><input type="button" class="edit" value="Mark Assets Moved" onclick="markAssetsMoved( $('#moveEventId').val())"/></span>
                 </tds:hasPermission>
                </g:form>
            </div>
        </div>
<script>
	currentMenuId = "#eventMenu";
	$(".menu-parent-planning-event-detail-list").addClass('active');
	$(".menu-parent-planning").addClass('active');
	function clearHistoricData( moveEventId ){
        $("#messageDiv").hide();
        $("#messageDiv").html("");
        var confirmStatus = confirm("Are you sure you want to permanently clear the dashboard data for this move event?")
        if(confirmStatus){
        	${remoteFunction(action:'clearHistoricData', params:'\'moveEventId=\' + moveEventId ', 
				onSuccess:"jQuery('#messageDiv').html('Dashboard History has been cleaned successfully');jQuery('#messageDiv').show()")}
        }
	}

   	function clearTaskData(moveEventId, type){
        $("#messageDiv").hide();
        $("#messageDiv").html("");
       	var confirmStatus=confirm("This process ONLY deletes tasks that were generated prior to the Cookbook feature. Any tasks that were " +
            "created in the Cookbook must be deleted in the Cookbook History tab.\n\nAre you sure you want to permanently " +
            type.toUpperCase() + " the recipe generated tasks for this move event?")
 		if (confirmStatus) {
		   	$("#messageDiv").show();
	      	$("#messageDiv").html((type=='reset' ? 'Resetting':'Deleting')+' tasks for the move event');
     		$.ajax({
    			url:'../clearTaskData',type:'POST',
    			data: {'moveEventId':moveEventId, 'type':type},
    			success: function(data, status, xhr) {
					var url = xhr.getResponseHeader('X-Login-URL');
					if (url) {
						window.location.href = url;
					} else {
	   					$('#messageDiv').html(xhr.status==200 ? data  : "Unexpected error occurred");						
					}
    			},
    			error: function(xhr, textStatus, errorThrown) {
    				$('#messageDiv').html("An unexpected error occurred and update was unsuccessful.");
    			}
    		});		
       	}
   	}

   	function markAssetsMoved( moveEventId ){
   		 $("#messageDiv").hide();
     	 $("#messageDiv").html("");
     	 var confirmStatus = confirm("Change asset locations to targets? (No undo, please backup prior)")
     	 if(confirmStatus){
			$("#messageDiv").show();
			$('#messageDiv').html("Setting assets to Moved, please wait...")			
     		jQuery.ajax({
    			url: '../markEventAssetAsMoved',
    			data: {'moveEventId':moveEventId},
    			success: function(data) {
    				var text = isNaN(data) ? '' : 'assets marked as moved.'
    				$('#messageDiv').html(''+data+' '+text+'')
    			},
    			error: function(jqXHR, textStatus, errorThrown) {
    				$('#messageDiv').html("An unexpected error occurred and update was unsuccessful.")
    			}
    		});
         }
   	}
   	function toogleGenDetails(){
		if($('#rightTriangle').is(':visible')){
			$("#rightTriangle").hide();
			$("#downTriangle").show();
			$("#generateDetailsSpan").show();
		}else {
			$("#rightTriangle").show();
			$("#downTriangle").hide();
			$("#generateDetailsSpan").hide();
		}
   	}
</script>
    </body>
</html>
