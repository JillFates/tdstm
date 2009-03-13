<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Create Project</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<g:javascript library="ui.datepicker" />
<g:javascript>

      function appendPartnerStaff(e) {
	      // The response comes back as a bunch-o-JSON
	
	      
	      // evaluate JSON
	      var rselect = document.getElementById('projectManagerId')
	      var mselect = document.getElementById('moveManagerId')
		  
		      var projectPartner = document.getElementById('projectPartnerId');
		      var projectPartnerVal = projectPartner[document.getElementById('projectPartnerId').selectedIndex].innerHTML;
		      
		      var pmExeOptgroup = document.getElementById('pmGroup')
		      var mmExeOptgroup = document.getElementById('mmGroup')
		      var pmOptgroup
		      var mmOptgroup
		      
		      if(pmExeOptgroup == null){
		      	pmOptgroup = document.createElement('optgroup');
		      }else{
		      	pmOptgroup = pmExeOptgroup
		      }
		      if(mmExeOptgroup == null){
		      	mmOptgroup = document.createElement('optgroup');
		      }else{
		      	mmOptgroup = mmExeOptgroup
		      }
		      
		      if(projectPartnerVal != "None" ){
			      pmOptgroup.label = projectPartnerVal;
			      pmOptgroup.id = "pmGroup";
			      mmOptgroup.label = projectPartnerVal;
			      mmOptgroup.id = "mmGroup";
		      } else {
		      	  pmOptgroup.label = "";
			      mmOptgroup.label = "";
		      }
		      try {
				rselect.appendChild(pmOptgroup, null) // standards compliant; doesn't work in IE
				mselect.appendChild(mmOptgroup, null) 
			  } catch(ex) {
				rselect.appendChild(pmOptgroup) // IE only
				mselect.appendChild(mmOptgroup) 
			  }
	   		//  Clear all previous options
			  var l = rselect.length
			  var compSatff = document.getElementById('companyManagersId').value
			  while (l > compSatff) {
				l--
				rselect.remove(l) 
				mselect.remove(l)
			  }
	      var managers = eval("(" + e.responseText + ")")
	      // Rebuild the select
	      if (managers) {
		
		      var length = managers.items.length
		      for (var i=0; i < length; i++) {
			      var manager = managers.items[i]
			      var popt = document.createElement('option');
			      popt.innerHTML = manager.name
			      popt.value = manager.id
			      var mopt = document.createElement('option');
			      mopt.innerHTML = manager.name
			      mopt.value = manager.id
			      try {
				      pmOptgroup.appendChild(popt, null) // standards compliant; doesn't work in IE
				      mmOptgroup.appendChild(mopt, null) 
			      } catch(ex) {
				      pmOptgroup.appendChild(popt) // IE only
				      mmOptgroup.appendChild(mopt) 
			      }
		      }
	      }
      }
      function initialize(){
	      // This is called when the page loads to initialize Managers
	      var partnerselect = document.getElementById('projectPartnerId')
	      var partnerVal = partnerselect.value 
	      if(partnerVal != ""){
	      	${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + partnerVal', onComplete:'appendPartnerStaff(e)')}
	      }
      }
      function textCounter(field, maxlimit) {
	      if (field.value.length > maxlimit) // if too long...trim it!
	      {
	      field.value = field.value.substring(0, maxlimit);
	      return false;
	      }
	      else
	      {
	      return true;
	      }
      }
      function setCompletionDate(startDate){
    	var completionDateObj = document.createProjectForm.completionDate;
    	if(completionDateObj.value == ""){
    		completionDateObj.value = startDate;
    	}
      }
    </g:javascript>
<%
	def currProj = session.getAttribute("CURR_PROJ");
	def projectId = currProj.CURR_PROJ;
	def currProjObj;
	if (projectId != null) {
		currProjObj = Project.findById(projectId);
	}
%>
</head>
<body>
<div class="menu2">
<ul>
	<li><g:link class="home" controller="projectUtil">Project </g:link>
	</li>
	<li><g:link class="home" controller="person" action="projectStaff"
		params="[projectId:currProjObj?.id]">Staff</g:link></li>
	<li><g:link class="home" controller="asset">Assets </g:link></li>
	<li><g:link class="home" controller="asset" action="assetImport">Import/Export</g:link>
	</li>
	<li><a href="#">Team </a></li>
	<li><a href="#">Contacts </a></li>
	<li><a href="#">Applications </a></li>
	<li><a href="#">Move Bundles </a></li>
</ul>
</div>
<div class="body">
<h1>Create Project</h1>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
</div>
<br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>

</g:if> <g:form action="save" method="post" name="createProjectForm">
	<div class="dialog">
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="client">Client:</label>
				</td>
				<td valign="top" class="value"><select id="client"
					name="client.id">
					<g:each status="i" in="${clients}" var="clients">
						<option value="${clients.partyIdTo.id}">${clients.partyIdTo}</option>
					</g:each>
				</select></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="projectCode">Project
				Code:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'projectCode','errors')}">
				<input type="text" id="projectCode" name="projectCode"
					maxlength="20"
					value="${fieldValue(bean:projectInstance,field:'projectCode')}" />
				<g:hasErrors bean="${projectInstance}" field="projectCode">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="projectCode" /></div>
				</g:hasErrors></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="name">Project
				Name:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
				<input type="text" id="name" name="name" maxlength="64"
					value="${fieldValue(bean:projectInstance,field:'name')}" /> <g:hasErrors
					bean="${projectInstance}" field="name">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="name" /></div>
				</g:hasErrors></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="description">Description:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'description','errors')}">
				<input type="text" id="description" name="description"
					maxlength="64"
					value="${fieldValue(bean:projectInstance,field:'description')}" />
				<g:hasErrors bean="${projectInstance}" field="description">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="description" /></div>
				</g:hasErrors></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
				<textarea rows="3" cols="40" name="comment"
					onkeydown="textCounter(document.createProjectForm.comment,200);"
					onkeyup="textCounter(document.createProjectForm.comment,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
				<g:hasErrors bean="${projectInstance}" field="comment">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="comment" /></div>
				</g:hasErrors></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="startDate">Start
				Date:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
				<link rel="stylesheet"
					href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15"
					style="width: 112px; height: 14px;" name="startDate"
					value="<tds:convertDate date="${projectInstance?.startDate}"/>"
					onchange="setCompletionDate(this.value)"> <!--  <g:datePicker name="startDate" value="${projectInstance?.startDate}"
       noSelection="['':'']"></g:datePicker> --><g:hasErrors
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
				<link rel="stylesheet"
					href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15"
					style="width: 112px; height: 14px;" id="completionDateId"
					name="completionDate"
					value="<tds:convertDate date="${projectInstance?.completionDate}"/>">
				<!--  <g:datePicker name="completionDate"
                    value="${projectInstance?.completionDate}" noSelection="['':'']"></g:datePicker> -->
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
					<g:each status="i" in="${partners}" var="partners">
						<option value="${partners.partyIdTo.id}">${partners.partyIdTo}</option>
					</g:each>
				</select></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="projectManager">Project
				Manager:</label></td>
				<td valign="top" class="value"><select id="projectManagerId"
					name="projectManager">
					<option value="" selected="selected">Please Select</option>
					<optgroup label="TDS">
						<g:each status="i" in="${managers}" var="managers">
							<option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName},
							${managers.partyIdTo.firstName} - ${managers.partyIdTo.title}</option>
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
						<g:each status="i" in="${managers}" var="managers">
							<option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName},
							${managers.partyIdTo.firstName} - ${managers.partyIdTo.title}</option>
						</g:each>
					</optgroup>
				</select> <input type="hidden" id="companyManagersId"
					value="${managers.size()}"></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="trackChanges">Track
				Changes:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'trackChanges','errors')}">
				<g:select id="trackChanges" name="trackChanges"
					from="${projectInstance.constraints.trackChanges.inList}"
					value="${projectInstance.trackChanges}"></g:select> <g:hasErrors
					bean="${projectInstance}" field="trackChanges">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="trackChanges" /></div>
				</g:hasErrors></td>
			</tr>


		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"><input
		class="save" type="submit" value="Create" /></span> <span class="button"><g:actionSubmit
		class="delete" action="Cancel" value="Cancel" /></span></div>
</g:form></div>
<g:javascript>
      initialize();
    </g:javascript>
</body>
</html>
