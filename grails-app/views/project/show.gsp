

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Project</title>
        <g:javascript library="prototype" />
        <g:javascript library="jquery"/>
	    <link type="text/css" rel="stylesheet" href="http://ui.jquery.com/testing/themes/base/ui.all.css" />
	    <script type="text/javascript" src="http://ui.jquery.com/testing/jquery-1.3.1.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.core.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.draggable.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.resizable.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.dialog.js"></script>
	    <script type="text/javascript">
	    $(document).ready(function(){
		        $("#dialog").dialog({ autoOpen: false });
	      	});
	    </script>
		<g:javascript>
			function editProject(){
				$("#dialog").dialog('option', 'width', 500)
				$("#dialog").dialog( "open" );
				var pmObj = document.getElementById("projectManagerId")
				var mmObj = document.getElementById("moveManagerId")
				var partnerObj = document.getElementById("projectPartnerId")
				<% if( projectPartner != null){ %>
					partnerObj.value = "${projectPartner?.partyIdTo.id}"
				<%} 
				if ( projectManager != null ) {
				%>
					pmObj.value = "${projectManager?.partyIdTo.id}"
				<% }
				if ( moveManager != null ) { %>
					mmObj.value = "${moveManager?.partyIdTo.id}"					
				<% } %>

	      	}
	      	function appendPartnerStaff(e) {
				// The response comes back as a bunch-o-JSON 
				//alert("make sure that the project isn't saved with a staff member from the previous partner");
				if(confirm(" Partner has been changed, Make sure that do you want to change the staff members ")){
				var managers = eval("(" + e.responseText + ")") 
				// evaluate JSON
				var rselect = document.getElementById('projectManagerId')
				var mselect = document.getElementById('moveManagerId')
				// Clear all previous options 
				var l = rselect.length
				var compSatff = document.getElementById('companyManagersId').value
				while (l > compSatff) { 
					l-- 
					rselect.remove(l) 
					mselect.remove(l) 
				}
					// Rebuild the select
				if (managers) { 
					var pmOptGroup = document.getElementById('pmOptGroupId')
					var mmOptGroup = document.getElementById('mmOptGroupId')
					var projectPartnerObj = document.getElementById('projectPartnerId');
					var projectPartnerVal = projectPartnerObj[document.getElementById('projectPartnerId').selectedIndex].innerHTML;
					pmOptGroup.style.visibility="visible";
					mmOptGroup.style.visibility="visible";
                    if(projectPartnerVal != "None" ){
                      pmOptGroup.label = projectPartnerVal;
                      mmOptGroup.label = projectPartnerVal;
                    } else {
                      pmOptGroup.label = "";
                      mmOptGroup.label = "";
                    }
					var length = managers.items.length
					for (var i=0; i < length; i++) {
						var manager = managers.items[i]
						var popt = document.createElement('option'); 
						popt.text = manager.name
						popt.value = manager.id
						var mopt = document.createElement('option'); 
						mopt.text = manager.name
						mopt.value = manager.id
						try { 
							rselect.add(popt, null) // standards compliant; doesn't work in IE
							mselect.add(mopt, null)
						} catch(ex) { 
							rselect.add(popt) // IE only
							mselect.add(mopt)
						} 
					}
				}
				}else{
					var partnerObj = document.getElementById("projectPartnerId")
					<% if( projectPartner != null){ %>
						partnerObj.value = "${projectPartner?.partyIdTo.id}"
					<%} %>
				}
			}
			
		</g:javascript>
    </head>
    <body>
        <div class="body">
            <h1>Show Project</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
            <jsec:hasRole name="ADMIN">
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
            </jsec:hasRole>
        	</div>
            <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog" id="updateShow">
                <table>
                    <tbody>
						
						<tr class="prop">
                            <td valign="top" class="name">Associated Client:</td>
                            
                            <td valign="top" class="value">${projectClient?.partyIdTo}</td>
                            
                        </tr>
                        
                    	<tr class="prop">
                            <td valign="top" class="name">Project Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Project Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'name')}</td>
                            
                        </tr>
                    	
                    	<tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'description')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Comment:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'comment')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Start Date:</td>
                            
                            <td valign="top" class="value"><my:convertDate date="${projectInstance?.startDate}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Completion Date:</td>
                            
                            <td valign="top" class="value"><my:convertDate date="${projectInstance?.completionDate}" /></td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Associated Partner:</td>
                            
                            <td valign="top" class="value">${projectPartner?.partyIdTo}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Project Manager:</td>
                            
                            <td valign="top" class="value">${projectManager?.partyIdTo?.lastName}, ${projectManager?.partyIdTo?.firstName} - ${projectManager?.partyIdTo?.title}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Move Manager:</td>
                            
                            <td valign="top" class="value">${moveManager?.partyIdTo?.lastName}, ${moveManager?.partyIdTo?.firstName} - ${moveManager?.partyIdTo?.title}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Track Changes:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'trackChanges')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value"><my:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
                        </tr> 
                         
                        <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value"><my:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
                        </tr> 
                        
                    </tbody>
                </table>
            </div>
            <div id="dialog" title="Edit Project" >
            	
			      <g:form name="editForm" method="post" action="update">

			        <input type="hidden" name="id" value="${projectInstance?.id}" />
			        <div class="dialog">
			          <table>
			            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Project Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:projectInstance,field:'name')}" maxlength="64"/>
                                <g:hasErrors bean="${projectInstance}" field="name">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="name"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
								<td valign="top" class="name"><label for="comment">Comment:</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
									<textarea name="comment"	onkeydown="textCounter(document.createProjectForm.comment,200);" onkeyup="textCounter(document.createProjectForm.comment,200);">
									${fieldValue(bean:projectInstance,field:'comment')}
									</textarea>
									<g:hasErrors
									bean="${projectInstance}" field="comment">
									<div class="errors"><g:renderErrors bean="${projectInstance}"
										as="list" field="comment" /></div>
								</g:hasErrors></td>
							</tr>
                        
                            <tr class="prop">
								<td valign="top" class="name"><label for="startDate">Start
								Date:</label></td>
								<td valign="top"
									class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
								<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
										<g:javascript library="ui.datepicker" />
										<script type="text/javascript" charset="utf-8">
											jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
										</script>
										<input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="startDate" value="<my:convertDate date="${projectInstance?.startDate}"/>">	
								<g:hasErrors
									bean="${projectInstance}" field="startDate">
									<div class="errors"><g:renderErrors bean="${projectInstance}"
										as="list" field="startDate" /></div>
								</g:hasErrors></td>
							</tr>
				
							<tr class="prop">
								<td valign="top" class="name"><label for="completionDate">Completion
								Date:</label></td>
								<td valign="top"
									class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
								<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
										<script type="text/javascript" charset="utf-8">
											jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
										</script>
										<input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="completionDate" value="<my:convertDate date="${projectInstance?.completionDate}"/>">
								
								<g:hasErrors bean="${projectInstance}" field="completionDate">
									<div class="errors"><g:renderErrors bean="${projectInstance}"
										as="list" field="completionDate" /></div>
								</g:hasErrors></td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name"><label for="projectPartner">Partner:</label>
								</td>
								<td valign="top" class="value"><select id="projectPartnerId"
									name="projectPartner"
									onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + this.value', onComplete:'appendPartnerStaff(e)' )}">
									<option value="" selected="selected">None</option>
									<g:each status="i" in="${companyPartners}" var="companyPartners">
										<option value="${companyPartners?.partyIdTo.id}">${companyPartners?.partyIdTo}</option>
									</g:each>
								</select></td>
							</tr>
			
							<tr class="prop">
								<td valign="top" class="name"><label for="projectManager">Project
								Manager:</label></td>
								<td valign="top" class="value"><select id="projectManagerId"
									name="projectManager">
									<option value="" selected="selected">Please Select</option>
									<optgroup label="TDS" >
									<g:each status="i" in="${companyStaff}" var="companyStaff">
										<option value="${companyStaff.partyIdTo.id}">${companyStaff.partyIdTo.lastName},
										${companyStaff.partyIdTo.firstName} - ${companyStaff.partyIdTo.title}</option>
									</g:each>
									</optgroup>
									<optgroup label="${projectPartner?.partyIdTo}" id="pmOptGroupId">
										<g:each status="i" in="${partnerStaff}" var="partnerStaff">
											<option value="${partnerStaff?.partyIdTo.id}">${partnerStaff.partyIdTo.lastName}, ${partnerStaff.partyIdTo.firstName} - ${partnerStaff.partyIdTo.title}</option>
										</g:each>
									</optgroup>
								</select></td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name"><label for="moveManager">Move
								Manager:</label></td>
								<td valign="top" class="value"><select id="moveManagerId"
									name="moveManager">
									<option value="" selected="selected">Please Select</option>
									<optgroup label="TDS">
									<g:each status="i" in="${companyStaff}" var="companyStaff">
										<option value="${companyStaff.partyIdTo.id}">${companyStaff.partyIdTo.lastName}, 
										${companyStaff.partyIdTo.firstName} - ${companyStaff.partyIdTo.title}</option>
									</g:each>
									</optgroup>
									<optgroup label="${projectPartner?.partyIdTo}" id="mmOptGroupId">
										<g:each status="i" in="${partnerStaff}" var="partnerStaff">
											<option value="${partnerStaff.partyIdTo.id}">${partnerStaff.partyIdTo.lastName}, ${partnerStaff.partyIdTo.firstName} - ${partnerStaff.partyIdTo.title}</option>
										</g:each>
									</optgroup>
								</select>
								<input type="hidden" id="companyManagersId" value="${companyStaff.size()}">
								</td>
							</tr>
							
							 <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="trackChanges">Track Changes:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'trackChanges','errors')}">
                                    <g:select id="trackChanges" name="trackChanges" from="${projectInstance.constraints.trackChanges.inList}" value="${projectInstance.trackChanges}" ></g:select>
                                <g:hasErrors bean="${projectInstance}" field="trackChanges">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="trackChanges"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="dateCreated">Date Created:</label>
	                                </td>
	                                <td valign="top" class="value"><my:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
	                        </tr> 
	                         
	                        <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="lastUpdated">Last Updated:</label>
	                                </td>
	                                <td valign="top" class="value"><my:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
	                        </tr> 
                            
                        </tbody>
			          </table>
			        </div>
			        <div class="buttons">
			          <span class="button">
			          <input class="save" type="submit" value="Update" />
			          </span>
			        </div>
			     </g:form>
			     </div>
           
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${projectInstance?.id}" />
                    <jsec:hasRole name="PROJECT_ADMIN">
                    <span class="button">
                    <input type="button" class="edit" value="Edit" onClick="return editProject()"/>
                    </span>
                    </jsec:hasRole>
                    <jsec:hasRole name="ADMIN">
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                    </jsec:hasRole>
                </g:form>
            </div>
            <div class="buttons">
		
		        <g:form name="callImport" controller="asset">
		          <input type="hidden" name="projectId" value="${projectInstance?.id}" />
		          <span class="button">
		            <g:actionSubmit class="edit" value="Import/Export" action="assetImport" />
		          </span>
		        </g:form>
		
		    </div>
    </body>
        </div>
    </body>
</html>
