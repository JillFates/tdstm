

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create UserLogin</title>
        <g:javascript library="jquery" />
			 <script type="text/javascript">  
			   $().ready(function() {  
			    $('#add').click(function() {  
			     return !$('#availableRoleId option:selected').remove().appendTo('#assignedRoleId');  
			    });  
			    $('#remove').click(function() {  
			     return !$('#assignedRoleId option:selected').remove().appendTo('#availableRoleId');  
			    });  
			   });  
			  </script>          
    </head>
    <body>
    <div class="menu2">
		<ul>
			<li><g:link class="home" controller="partyGroup" action="show" id="${companyId}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${companyId}">Staff</g:link></li>
			<li><g:link class="home" controller="application" id="${companyId}">Applications </g:link></li>
			<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li>
		</ul>
	</div>
        <div class="body">
            <h1>Create UserLogin</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list" id="${companyId}">UserLogin List</g:link></span>
        </div>
        <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        	<input name="companyId" type="hidden" value="${companyId}" >
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="person">Person:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'person','errors')}">
                                <g:if test="${personInstance}">
	                                <g:select optionKey="id" from="${personInstance}" name="person.id" value="${personInstance?.id}" ></g:select>
	                                <input type="hidden" name="personId" value="${personInstance?.id}" >
                                </g:if>
                                <g:else>
                                    <g:select optionKey="id" from="${Person.list()}" name="person.id" value="${userLoginInstance?.person?.id}" ></g:select>
                                </g:else>
                                <g:hasErrors bean="${userLoginInstance}" field="person">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="person"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="username">Username:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'username','errors')}">
                                    <input type="text" maxlength="25" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
                                <g:hasErrors bean="${userLoginInstance}" field="username">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="username"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="password">Password:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
                                    <input type="password" maxlength="25" id="password" name="password" value=""/>
                                <g:hasErrors bean="${userLoginInstance}" field="password">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'active','errors')}">
                                    <g:select id="active" name="active" from="${userLoginInstance.constraints.active.inList}" value="${userLoginInstance.active}" ></g:select>
                                <g:hasErrors bean="${userLoginInstance}" field="active">
					            <div class="errors">
					                <g:renderErrors bean="${userLoginInstance}" as="list" field="active"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                            <tr class="prop">
                                <td valign="top" class="value" colspan="2">
                                <table style="border: none;">
                                <tr>
                               <td valign="top" class="name">
                                    <label >Available Roles:</label>
                                </td>
                                <td valign="top" class="name">
                                    <label >&nbsp;</label>
                                </td>
                                <td valign="top" class="name">
                                    <label >Assigned Roles:</label>
                                </td>
                                </tr>
                                <tr>
	                                <td valign="top" class="name">
		                                <select name="availableRole" id="availableRoleId" multiple="multiple" size="10" style="width: 250px">
			                                <g:each in="${RoleType.list()}" var="availableRoles">
			                                	<option value="${availableRoles.id}">${availableRoles}</option>
			                                </g:each>
		                                </select>
	                                </td>
	                                <td valign="middle" style="vertical-align:middle" >
		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">
										<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;">
										</a></span><br><br><br><br>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove">
		                                <img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;">
		                                </a></span>
	                                </td>
	                                <td valign="top" class="name">
		                                <select name="assignedRole" id="assignedRoleId" multiple="multiple" size="10" style="width: 250px">
			                                <g:if test="${assignedRole}">
				                                <g:each in="${assignedRole}" var="assignedRole">
				                                	<option value="${assignedRole}" selected="selected">${RoleType.findById(assignedRole)}</option>
				                                </g:each>
			                                </g:if>
			                                <g:else>
			                                	<option value="USER" selected="selected">${RoleType.findById('USER')}</option>
			                                </g:else>
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
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
