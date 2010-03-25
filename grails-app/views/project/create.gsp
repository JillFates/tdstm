<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Create Project</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<g:javascript library="ui.datepicker" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />

<script type="text/javascript">

      function updateMastersList(e){
      // The response comes back as a bunch-o-JSON
	
	      
	      // evaluate JSON
	      var rselect = document.getElementById('projectManagerId')
	      var mselect = document.getElementById('moveManagerId')
		      var projectPartner = document.getElementById('projectPartnerId');
		      var projectClient = document.getElementById('clientId');
		      var projectPartnerVal = projectPartner[projectPartner.selectedIndex].innerHTML;
		      var projectClientVal = projectClient[projectClient.selectedIndex].innerHTML;
		      
		      var compPmExeOptgroup = document.getElementById('compPmGroup')
		      var compMmExeOptgroup = document.getElementById('compMmGroup')
		      var clientPmExeOptgroup = document.getElementById('clientPmGroup')
		      var clientMmExeOptgroup = document.getElementById('clientMmGroup')
		      var partnerPmExeOptgroup = document.getElementById('partnerPmGroup')
		      var partnerMmExeOptgroup = document.getElementById('partnerMmGroup')
		      var custPmOptgroup
		      var custMmOptgroup
		      var clientPmOptgroup
		      var clientMmOptgroup
		      var partnerPmOptgroup
		      var partnerMmOptgroup
		      // create Option group for Customer
		      if(compPmExeOptgroup == null){
		      	custPmOptgroup = document.createElement('optgroup');
		      }else{
		      	custPmOptgroup = compPmExeOptgroup
		      }
		      if(compMmExeOptgroup == null){
		      	custMmOptgroup = document.createElement('optgroup');
		      }else{
		      	custMmOptgroup = compMmExeOptgroup
		      }
		      // create Option group for Client
		      if(clientPmExeOptgroup == null){
		      	clientPmOptgroup = document.createElement('optgroup');
		      }else{
		      	clientPmOptgroup = clientPmExeOptgroup
		      }
		      if(clientMmExeOptgroup == null){
		      	clientMmOptgroup = document.createElement('optgroup');
		      }else{
		      	clientMmOptgroup = clientMmExeOptgroup
		      }
		      // create Option group for Partner
		      if(partnerPmExeOptgroup == null){
		      	partnerPmOptgroup = document.createElement('optgroup');
		      }else{
		      	partnerPmOptgroup = partnerPmExeOptgroup
		      }
		      if(partnerMmExeOptgroup == null){
		      	partnerMmOptgroup = document.createElement('optgroup');
		      }else{
		      	partnerMmOptgroup = partnerMmExeOptgroup
		      }
		      // label assign for Customer
		      custPmOptgroup.label = "TDS";
			  custPmOptgroup.id = "compPmGroup";
		      custMmOptgroup.label = "TDS";
			  custMmOptgroup.id = "compMmGroup";
		      clientPmOptgroup.label = projectClientVal;
			  clientPmOptgroup.id = "clientPmGroup";
		      clientMmOptgroup.label = projectClientVal;
			  clientMmOptgroup.id = "clientMmGroup";
		      if(projectPartnerVal != "None" ){
			      partnerPmOptgroup.label = projectPartnerVal;
			      partnerPmOptgroup.id = "partnerPmGroup";
			      partnerMmOptgroup.label = projectPartnerVal;
			      partnerMmOptgroup.id = "partnerMmGroup";
		      } else {
		      	  partnerPmOptgroup.label = "";
			      partnerMmOptgroup.label = "";
		      }
		      try {
				rselect.appendChild(custPmOptgroup, null) // standards compliant; doesn't work in IE
				mselect.appendChild(custMmOptgroup, null) 
				rselect.appendChild(clientPmOptgroup, null) 
				mselect.appendChild(clientMmOptgroup, null) 
				rselect.appendChild(partnerPmOptgroup, null) 
				mselect.appendChild(partnerMmOptgroup, null) 
			  } catch(ex) {
			  	rselect.appendChild(custPmOptgroup) // IE only
				mselect.appendChild(custMmOptgroup) 
				rselect.appendChild(clientPmOptgroup) 
				mselect.appendChild(clientMmOptgroup) 
				rselect.appendChild(partnerPmOptgroup) 
				mselect.appendChild(partnerMmOptgroup)
			  }
	   		//  Clear all previous options
			  var l = rselect.length
			 // var compSatff = document.getElementById('companyManagersId').value
			  while (l > 1) {
				l--
				rselect.remove(l) 
				mselect.remove(l)
			  }
	      var managers = eval("(" + e.responseText + ")")
	      // Rebuild the select
	      if (managers) {
		      // assign Company staff
		      var compStaffLength = managers.compStaff.length
		      for (var i=0; i < compStaffLength; i++) {
			      var manager = managers.compStaff[i]
			      var popt = document.createElement('option');
			      popt.innerHTML = manager.name
			      popt.value = manager.id
			      var mopt = document.createElement('option');
			      mopt.innerHTML = manager.name
			      mopt.value = manager.id
			      try {
				      custPmOptgroup.appendChild(popt, null) // standards compliant; doesn't work in IE
				      custMmOptgroup.appendChild(mopt, null) 
			      } catch(ex) {
				      custPmOptgroup.appendChild(popt) // IE only
				      custMmOptgroup.appendChild(mopt) 
			      }
		      }
		      // Assign Client Staff 
		      var clientStaffLength = managers.clientStaff.length
		     // if(clientStaffLength == ""){
		      	//clientPmOptgroup.label = ""
		      	//clientMmOptgroup.label = ""
		      //}
		      for (var i=0; i < clientStaffLength; i++) {
			      var manager = managers.clientStaff[i]
			      var cpopt = document.createElement('option');
			      cpopt.innerHTML = manager.name
			      cpopt.value = manager.id
			      var cmopt = document.createElement('option');
			      cmopt.innerHTML = manager.name
			      cmopt.value = manager.id
			      try {
				      clientPmOptgroup.appendChild(cpopt, null) // standards compliant; doesn't work in IE
				      clientMmOptgroup.appendChild(cmopt, null) 
			      } catch(ex) {
				      clientPmOptgroup.appendChild(cpopt) // IE only
				      clientMmOptgroup.appendChild(cmopt) 
			      }
		      }
		      var partnerStaffLength = managers.partnerStaff.length
		      for (var i=0; i < partnerStaffLength; i++) {
			      var manager = managers.partnerStaff[i]
			      var ppopt = document.createElement('option');
			      ppopt.innerHTML = manager.name
			      ppopt.value = manager.id
			      var pmopt = document.createElement('option');
			      pmopt.innerHTML = manager.name
			      pmopt.value = manager.id
			      try {
				      partnerPmOptgroup.appendChild(ppopt, null) // standards compliant; doesn't work in IE
				      partnerMmOptgroup.appendChild(pmopt, null) 
			      } catch(ex) {
				      partnerPmOptgroup.appendChild(ppopt) // IE only
				      partnerMmOptgroup.appendChild(pmopt) 
			      }
		      }
	      }
      }
      function initialize(){
	      // This is called when the page loads to initialize Managers
	      var partnerVal = document.getElementById('projectPartnerId').value
	      var clientObj = document.getElementById('clientId').value
	      ${remoteFunction(action:'getPartnerStaffList', params:'\'client=\'+ clientObj +\'&partner=\'+partnerVal', onComplete:'updateMastersList(e)')}
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
    </script>
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

<div class="body">
<h1>Create Project</h1>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
</div>
<br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>

</g:if> 

<g:form action="save" method="post" name="createProjectForm" enctype="multipart/form-data">
	<div class="dialog">
	<table>
		<tbody>	
			<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="client"><b>Client:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value"><select id="clientId" name="client.id" onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'client=\'+ this.value +\'&partner=\'+document.getElementById(\'projectPartnerId\').value', onComplete:'updateMastersList(e)' )}">
					<g:each status="i" in="${clients}" var="clients">
						<option value="${clients.partyIdTo.id}">${clients.partyIdTo}</option>
					</g:each>
				</select></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="projectCode"><b>Project Code:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'projectCode','errors')}">
				<input type="text" id="projectCode" name="projectCode" maxlength="20" value="${fieldValue(bean:projectInstance,field:'projectCode')}" />
				<g:hasErrors bean="${projectInstance}" field="projectCode">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="projectCode" /></div>
				</g:hasErrors></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="name"><b>Project Name:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
				<input type="text" id="name" name="name" maxlength="64" value="${fieldValue(bean:projectInstance,field:'name')}" />
					<g:hasErrors
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
				<textarea rows="3" cols="40" id="description" name="description" 
				onkeydown="textCounter(document.createProjectForm.description,200);" 
				onkeyup="textCounter(document.createProjectForm.description,200);">${fieldValue(bean:projectInstance,field:'description')}</textarea>
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
				<textarea  rows="3" cols="40" name="comment"
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
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="startDate"
					value="<tds:convertDate date="${projectInstance?.startDate}"/>" onchange="setCompletionDate(this.value)"/> 
					<!--  <g:datePicker name="startDate" value="${projectInstance?.startDate}"
       noSelection="['':'']"></g:datePicker> -->
       			<g:hasErrors bean="${projectInstance}" field="startDate">
					<div class="errors">
						<g:renderErrors bean="${projectInstance}" as="list" field="startDate" />
					</div>
				</g:hasErrors>
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="completionDate">Completion
				Date:</label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15"
					style="width: 112px; height: 14px;" id="completionDateId"
					name="completionDate"
					value="<tds:convertDate date="${projectInstance?.completionDate}"/>" />
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
					onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'client=\'+ document.getElementById(\'clientId\').value +\'&partner=\'+this.value', onComplete:'updateMastersList(e)' )}">
					<option value="" selected="selected">None</option>
					<g:each status="i" in="${partners}" var="partners">
						<option value="${partners.partyIdTo.id}">${partners.partyIdTo}</option>
					</g:each>
				</select></td>
			</tr>
			
			<tr class="prop">
				<td valign="top" class="name"><label for="client">Partner Image:</label>
				</td>
				<td valign="top" class="value">
				<input type="file" name="partnerImage" id="partnerImage" />
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="projectManager">Project
				Manager:</label></td>
				<td valign="top" class="value"><select id="projectManagerId"
					name="projectManager">
					<option value="" selected="selected">Please Select</option>
					<optgroup label="TDS" id="compPmGroup">
						<g:each status="i" in="${managers}" var="managers">
							<option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName}
							<g:if test="${managers.partyIdTo.firstName}">,${managers.partyIdTo.firstName}</g:if> <g:if test="${managers.partyIdTo.title}"> - ${managers.partyIdTo.title}</g:if></option>
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
					<optgroup label="TDS" id="compMmGroup">
						<g:each status="i" in="${managers}" var="managers">
							<option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName}
							<g:if test="${managers.partyIdTo.firstName}">,${managers.partyIdTo.firstName}</g:if> <g:if test="${managers.partyIdTo.title}"> - ${managers.partyIdTo.title}</g:if>  </option>
						</g:each>
					</optgroup>
				</select> <input type="hidden" id="companyManagersId" value="${managers.size()}"/></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="trackChanges"><b>Track Changes:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'trackChanges','errors')}">
				<g:select id="trackChanges" name="trackChanges"
					from="${projectInstance.constraints.trackChanges.inList}"
					value="${projectInstance.trackChanges}"></g:select>
					 <g:hasErrors bean="${projectInstance}" field="trackChanges">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="trackChanges" /></div>
				</g:hasErrors></td>
			</tr>
			
			<tr class="prop">
				<td valign="top" class="name"><label for="workflowCode"><b>Workflow	Code:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top"
					class="value ${hasErrors(bean:projectInstance,field:'workflowCode','errors')}">
				<g:select id="workflowCode" name="workflowCode"
					from="${workflowCodes}"
					value="Please Select" noSelection="['':'Please Select']"></g:select>
					<g:hasErrors bean="${projectInstance}" field="workflowCode">
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" field="workflowCode" /></div>
				</g:hasErrors></td>
			</tr>


		</tbody>
	</table>
	</div>
	<div class="buttons">
		<span class="button">
			<input class="save" type="submit" value="Create" />
		</span> 
		<span class="button">
			<input type="button" class="delete" value="Cancel" 
			onclick="document.createProjectForm.action = 'cancel';document.createProjectForm.submit();"/>
		</span>
	</div>
</g:form></div>
<g:javascript>
      initialize();
    </g:javascript>
</body>
</html>
