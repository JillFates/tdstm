<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project Staff List</title>
<g:javascript library="prototype" />
<g:javascript library="jquery"/>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />

    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.resizable"/>
    <jq:plugin name="ui.dialog"/>
	    <script type="text/javascript">
	    $(document).ready(function(){
		        $("#editPerson").dialog({ autoOpen: false });
	      	});
	    
	    $(document).ready(function(){
	    		if('${submit}'){
				    $("#addProjectStaff").dialog({ autoOpen: true });
				    $("#addProjectStaff").dialog('option', 'width', 650)
		        } else {
		        	$("#addProjectStaff").dialog({ autoOpen: false });
		        }
	      	});
	    $(document).ready(function(){
		        $("#createPerson").dialog({ autoOpen: false });
	      	});
	      	
	    </script>
	    <script type="text/javascript">
	    
	    </script>
		<g:javascript>

	      	function editPersonDialog( e ) {

		      var person = eval('(' + e.responseText + ')')
		        document.editForm.company.value = person.companyId
		      	document.editForm.id.value = person.id
		      	document.editForm.firstName.value = person.firstName
		      	document.editForm.lastName.value = person.lastName
		      	document.editForm.nickName.value = person.nickName
		      	document.editForm.title.value = person.title
		      	document.editForm.active.value = person.active
		      	document.editForm.roleType.value = person.role
		      
		      	$("#editPerson").dialog('option', 'width', 350)
				$("#editPerson").dialog( "open" );
		
		 	}
		 	
		 	// function for add staff form dialog
		 	function showAddProjectStaff(){
		 		$("#addProjectStaff").dialog('option', 'width', 650)
				$("#addProjectStaff").dialog( "open" );	
		 	}
		 	// function for create staff form dialog
		 	function createProjectStaff(){
		 		$("#createPerson").dialog('option', 'width', 350)
				$("#createPerson").dialog( "open" );	
		 	}
		 	
		 	// function to submit the Add staff form
		 	function addProjectStaff(i){
		 		
		 		var roleType = document.getElementById("roleType_"+i).value;
				if( roleType == "" ){
					alert("Please Select Role");
					return false;
				}else{
					return true;					
				}
		 	}
		 	// function to validate CreateForm
		 	function validateCreateForm(){
		 		
		 		var firstName = document.createForm.firstName.value;
		 		var roleType = document.createForm.roleType.value;
		 		var companyVal = document.createForm.company.value;
		 		if( companyVal == "" ){
		 		    alert("please select Company ");
		 			return false;
		 		} else if( firstName != "" ){
					if(roleType != "null" && roleType != ""){
						return true;
					}else{
						alert("please select Role ");
						return false;
					}
				} else {
					alert("First Name can not be Blank");
					return false;					
				}
		 	}
		 	// function to validate CreateForm
		 	function validateEditForm(){
		 		
		 		var firstName = document.editForm.firstName.value;
		 		var roleType = document.editForm.roleType.value;
		 		var companyVal = document.createForm.company.value;
		 		if( companyVal == "" ){
		 		    alert("please select Company ");
		 			return false;
		 		}else if( firstName != "" ){
					if(roleType != "null" && roleType != ""){
						return true;
					}else{
						alert("please select Role ");
						return false;
					}
				}else{
					alert("First Name can not be Blank");
					return false;					
				}
		 	}
	      	</g:javascript>
</head>
<body>

<div class="body">
<h1>Project Staff List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>

<div class="list">
<table>
	<thead>
		<tr>

			<th>Staff Name </th>

			<th>Company</th>

			<th>Role</th>

		</tr>
	</thead>
	<tbody>
		<g:each in="${projectStaff}" status="i" var="projectStaff">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><g:remoteLink controller="person" action="editStaff" id="${projectStaff?.staff.id}" params="[role:projectStaff?.role.id]" onComplete ="editPersonDialog( e );">${projectStaff?.name}</g:remoteLink></td>

				<td>${projectStaff?.company[0]}</td>
				
				<td>${projectStaff?.role}</td>

			</tr>
		</g:each>
	</tbody>
</table>
</div>
	<div class="buttons">
		<span class="button"><input type="button" class="create" value="Add" onclick="showAddProjectStaff()"/></span>
	</div>
</div>
<div id="editPerson" style="display: none;" title="Edit Staff">
            <g:form method="post" action="updateStaff" name="editForm" onsubmit="return validateEditForm()">
                <input type="hidden" name="id" value="" />
                <input type="hidden" name="projectId" value="${projectId}" />
                <div class="dialog">
                    <table>
                        <tbody>
                     <tr class="prop">
                                <td valign="top" class="name">
                                    <label>Company:</label>
                                </td>
                                <td valign="top" class="value ">
                               
								<select name="company" id="companyId">
	                                <g:each in="${projectCompanies}" status="i" var="company">
	                                	<option value="${company?.partyIdTo.id}">${company?.partyIdTo}</option>
	                                </g:each>
                                </select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName">First Name:</label>
                                </td>
                                <td valign="top" class="value ">
                                    <input type="text" maxlength="64" id="firstName" name="firstName" value=""/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastName">Last Name:</label>
                                </td>
                                <td valign="top" class="value">
                                    <input type="text" maxlength="64" id="lastName" name="lastName" value=""/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="nickName">Nick Name:</label>
                                </td>
                                <td valign="top" class="value ">
                                    <input type="text" maxlength="64" id="nickName" name="nickName" value=""/>
                                </td>
                            </tr> 
                        	 <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="title">Title:</label>
                                </td>
                                <td valign="top" class="value">
                                    <input type="text" maxlength="34" id="title" name="title" value=""/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ">
                                <select name="active" id="active" >
                                <g:each in="${Person.constraints.active.inList}" status="i" var="active">
                                	<option value="${active}">${active}</option>
                                </g:each>
                                </select>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Role:</label>
                                </td>
                                <td valign="top" class="value ">
                               <tds:personRoleSelect name="roleType" id="roleType" optionKey="id" from="${RoleType.list()}" value="${roleType?.id}" isNew="true" ></tds:personRoleSelect>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input type="submit" class="save" value="Update"  /></span>
                </div>
            </g:form>
</div>
<div class="body" id="addProjectStaff" style="display: none;" title="Add staff to project" >
<div >
<table>
	<thead>
		<tr>

			<th>Company</th>
			<th>Name</th>
			<th>Title</th>
			<th>Role&nbsp;<span style="color: red">*</span></th>
			<th>Action</th>

		</tr>
	</thead>
	<tbody>
		<g:each in="${companiesStaff}" status="i" var="companiesStaff">
		<g:formRemote method="post" before="return addProjectStaff($i)" name="addSatffForm_$i" url="${[action:'saveProjectStaff']}" >
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<input type="hidden" name="projectId" value="${projectId}">
				<td>${companiesStaff?.company[0]}</td>
				
				<td><input type="hidden" name="person" value="${companiesStaff?.staff.id}" >${companiesStaff?.name}</td>
				
				<td>${companiesStaff?.staff.title}</td>
				
				<td><tds:personRoleSelect name="roleType" id="roleType_$i" optionKey="id" from="${RoleType.list()}" value="${roleType?.id}" isNew="true" ></tds:personRoleSelect> </td>
				
				<td><input value="Add" type="submit" name="submit"> </td>
				
			</tr>
			
		</g:formRemote>
		</g:each>
	</tbody>
</table>
</div>
	<div class="buttons" style="width: 99%">
	<g:form>
		<span class="button"><input class="create"	type="button" value="Create New Staff" onclick="createProjectStaff()"/></span>
		<span class="button" style="padding-left:55%" ><input class="delete" type="button" value="Close" onclick="$('#addProjectStaff').dialog('close')"/></span>
	</g:form>
</div>
</div>
<div id="createPerson" style="display: none;" title="Create New Staff">
<g:formRemote method="post" before="return validateCreateForm()" name="createForm" url="${[action:'savePerson']}" >
                <input type="hidden" name="projectId" value="${projectId}" />
                <div class="dialog">
                    <table>
                        <tbody>
	                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName">Company:</label>
                                </td>
                                <td valign="top" class="value ">
                                
								<select name="company" id="companyId">
	                                <g:each in="${projectCompanies}" status="i" var="company">
	                                	<option value="${company?.partyIdTo.id}">${company?.partyIdTo}</option>
	                                </g:each>
                                </select>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName"><b>First Name:<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ">
                                    <input type="text" maxlength="64" id="firstName" name="firstName" value=""/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastName">Last Name:</label>
                                </td>
                                <td valign="top" class="value">
                                    <input type="text" maxlength="64" id="lastName" name="lastName" value=""/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="nickName">Nick Name:</label>
                                </td>
                                <td valign="top" class="value ">
                                    <input type="text" maxlength="64" id="nickName" name="nickName" value=""/>
                                </td>
                            </tr> 
                        	 <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="title">Title:</label>
                                </td>
                                <td valign="top" class="value">
                                    <input type="text" maxlength="34" id="title" name="title" value=""/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ">
                                <select name="active" id="active" >
                                <g:each in="${Person.constraints.active.inList}" status="i" var="active">
                                	<option value="${active}">${active}</option>
                                </g:each>
                                </select>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><b>Role:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ">
                               <tds:personRoleSelect name="roleType" id="roleType" optionKey="id" from="${RoleType.list()}" value="${roleType?.id}" isNew="true" ></tds:personRoleSelect>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input type="submit" class="save" value="Create"  /></span>
                </div>
            </g:formRemote>
</div>
</body>
</html>
