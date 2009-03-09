<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Project Staff List</title>
<g:javascript library="prototype" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'jquery-1.3.1.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.core.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.draggable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.resizable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.dialog.js')}"></script>
	    <script type="text/javascript">
	    $(document).ready(function(){
		        $("#editPerson").dialog({ autoOpen: false });
	      	});
	    </script>
		<g:javascript>

	      	function editPersonDialog( e ) {

		      var person = eval('(' + e.responseText + ')')
		
		      	document.editForm.id.value = person.id
		      	document.editForm.firstName.value = person.firstName
		      	document.editForm.lastName.value = person.lastName
		      	document.editForm.nickName.value = person.nickName
		      	document.editForm.title.value = person.title
		      	document.editForm.active.value = person.active
		      
		      	$("#editPerson").dialog('option', 'width', 300)
				$("#editPerson").dialog( "open" );
		
		 	}
	      	</g:javascript>
</head>
<body>
<div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
            <li><g:link class="home" controller="asset">Assets </g:link></li>
            <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
            <li><a href="#">Team </a></li>
            <li><a href="#">Contacts </a></li>
            <li><a href="#">Applications </a></li>
            <li><a href="#">Move Bundles </a></li>
          </ul>
</div>
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

				<td><g:remoteLink controller="person" action="editStaff" id="${projectStaff?.staff.id}" onComplete ="editPersonDialog( e );">${projectStaff?.name}</g:remoteLink></td>

				<td>${projectStaff?.company[0]}</td>
				
				<td>${projectStaff?.role}</td>

			</tr>
		</g:each>
	</tbody>
</table>
</div>
	<div class="buttons"><g:form>
		<span class="button"><g:actionSubmit class="create"	value="Add" action="create" /></span>
	</g:form></div>
</div>
<div id="editPerson" style="display: none;">
            <g:form method="post" action="updateStaff" name="editForm" >
                <input type="hidden" name="id" value="" />
                <input type="hidden" name="projectId" value="${projectId}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
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
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input type="submit" class="save" value="Update"  /></span>
                </div>
            </g:form>
        </div>
</body>
</html>
