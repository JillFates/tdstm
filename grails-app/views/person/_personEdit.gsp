<%--
  -- Used to present the user with their profile information that they can edit and change
  -- @params user - the user information
  --%>

<div id="personDialog" title="Edit Person" style="display:none;" class="static-dialog">
	<div class="dialog">
		<table>
		  <tbody>
			<tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">
					<label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value">
					<input type="text" maxlength="64" id="firstNameId" name="firstName"/>
				</td>
			</tr>
			
			<tr class="prop" style="display:none;">
				<td valign="top" class="name">
					<label for="username"><b>User Name:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value">
					<input type="text" maxlength="64" id="prefUsernameId" name="username" value="${user.username}"/>
				</td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="middleName">Middle Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="middleNameId" name="middleName"/>
			  </td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="lastName">Last Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="lastNameId" name="lastName"/>
			  </td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="nickName">Nick Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="nickNameId" name="nickName"/>
			  </td>
			</tr>
			<tr class="prop">
			  <td valign="top" class="name">
				<label for="title">Title:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="34" id="titleId" name="title"/>
			  </td>
			</tr>
			<tr class="prop">
			  <td valign="top" class="name">
				<label for="nickName">Email:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="emailId" name="email"/>
			  </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">
					<label for="startPage">Start Page:</label>
				</td>
				<td valign="top" class="value">
					<g:if test="${RolePermissions.hasPermission('AdminMenuView')}">
						<g:select name="startPage" id="startPage" from="${['Project Settings','Current Dashboard','Admin Portal', 'User Dashboard']}" 
						value="${session.getAttribute('START_PAGE')?.START_PAGE}"/>
					</g:if>
					<g:else>
						<g:select name="startPage" id="startPage" from="${['Project Settings','Current Dashboard', 'User Dashboard']}" 
							value="${session.getAttribute('START_PAGE')?.START_PAGE}"/>
					</g:else>
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">
					<label for="title">Power In:</label>
				</td>
				<td valign="top" class="value">
					<g:select name="powerType" id="powerTypeId" from="${['Watts','Amps']}" 
					value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE}"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">
					<label for="title">Model Score:</label>
				</td>
				<td valign="top" class="value">
					<input type="text" name ="modelScore" id ="modelScoreId" readonly="readonly" value="${person?.modelScore}"/>
				</td>
			</tr>
			
			<g:render template="../userLogin/setPasswordFields" model="${[changingPassword:true, minPasswordLength:minPasswordLength, fromDialog:true]}" />
		  </tbody>
		</table>
	  </div>
	  <div class="buttons">
		<span class="button"><input type="button" class="edit" value="Update" onclick="changePersonDetails()"/></span>
		<span class="button"><input type="button" class="delete" onclick="jQuery('#personDialog').dialog('close')" value="Cancel" /></span>
	  </div>
	</div>
</div>
