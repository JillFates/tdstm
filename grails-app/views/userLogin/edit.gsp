

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>Edit UserLogin</title>
        <g:javascript library="jquery" />
			 <script type="text/javascript"> 
			 	   var flag 
				   $().ready(function() {  
				    $('#add').click(function() {
				     flag = !$('#availableRoleId option:selected').remove().appendTo('#assignedRoleId');
				     updateRole( 'add' );
				     return flag;  
				    });  
				    $('#remove').click(function() {
				     flag = !$('#assignedRoleId option:selected').remove().appendTo('#availableRoleId');
				     updateRole( 'remove' );  
				     return flag;  
				    });  
				   }); 
				   
				   function updateRole( val ) {
				   	 var personId = $('#person').val();
				     var assignedRoleId  = new Array(); 
				     var obj = document.editUserForm.assignedRole;
				     switch( val ) {
						case "add" :
							for ( var i = 0; i < obj.options.length; i++ ) {
				     			if ( obj.options[ i ].selected ) 
				     				assignedRoleId.push( obj.options[ i ].value ); 
				     		}
							break;
						case "remove" :
							for ( var i = 0; i < obj.options.length; i++ ) {
				     			if ( !obj.options[ i ].selected ) 
				     				assignedRoleId.push( obj.options[ i ].value ); 
				     		}
							break;
					 }				      				 
				     ${remoteFunction(controller:'userLogin', action:'addRoles', params:'\'assignedRoleId=\' + assignedRoleId +\'&person=\'+personId')}
				   } 
			  </script> 
    </head>
    <body>
   
        <div class="body">
            <h1>Edit UserLogin</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="list" action="list" id="${companyId}">UserLogin List</g:link></span>
	            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">New UserLogin</g:link></span>
        	</div>
        	<br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" name="editUserForm">
                <input type="hidden" name="id" value="${userLoginInstance?.id}" />
                <input type="hidden" name="companyId" value="${companyId}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="person">Person:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'person','errors')}">
                                    <g:select optionKey="id" from="${Person.list()}" id="person" name="person.id" value="${userLoginInstance?.person?.id}" ></g:select>
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
                                    <input type="text" id="username" name="username" value="${fieldValue(bean:userLoginInstance,field:'username')}"/>
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
                                    <input type="password" id="password" name="password" value=""/>
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
			                                <g:each in="${availableRoles}" var="availableRoles">
			                                	<option value="${availableRoles.id}">${availableRoles}</option>
			                                </g:each>
		                                </select>
	                                </td>
	                                <td valign="middle" style="vertical-align:middle" >
		                                <span style="white-space: nowrap;height: 100px;" > <a href="#" id="add">
										<img  src="${createLinkTo(dir:'images',file:'right-arrow.png')}" style="float: left; border: none;">
										</a></span><br><br><br><br>
		                                <span style="white-space: nowrap;"> <a href="#" id="remove"><img  src="${createLinkTo(dir:'images',file:'left-arrow.png')}" style="float: left; border: none;">
		                                </a></span>
	                                </td>
	                                <td valign="top" class="name">
		                                <select name="assignedRole" id="assignedRoleId" multiple="multiple" size="10" style="width: 250px">
				                                <g:each in="${assignedRoles}" var="assignedRole">
				                                	<option value="${assignedRole?.id}" selected="selected">${assignedRole}</option>
				                                </g:each>
				                                <g:each in="${updatedRoles}" var="updatedRole">
				                                	<option value="${updatedRole}" selected="selected">${RoleType.findById(updatedRole)}</option>
				                                </g:each>
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
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
