<%--
  -- Used to present the user with their profile information that they can edit and change
  -- @params user - the user information
  --%>
<%@page import="net.transitionmanager.security.Permission"%>
<div id="personDialog" title="Edit Person" style="display:none;" class="static-dialog">
	<div class="dialog">
		<table>
		  <tbody>
			<tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">
					<label for="firstNameId"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value">
					<input type="text" maxlength="64" id="firstNameId" name="firstName"/>
				</td>
			</tr>

			<tr class="prop" style="display:none;">
				<td valign="top" class="name">
					<label for="prefUsernameId"><b>User Name:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value">
					<input type="text" maxlength="64" id="prefUsernameId" name="username" value="${user.username}"/>
				</td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="middleNameId">Middle Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="middleNameId" name="middleName"/>
			  </td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="lastNameId">Last Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="lastNameId" name="lastName"/>
			  </td>
			</tr>

			<tr class="prop">
			  <td valign="top" class="name">
				<label for="nickNameId">Nick Name:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="64" id="nickNameId" name="nickName"/>
			  </td>
			</tr>
			<tr class="prop">
			  <td valign="top" class="name">
				<label for="titleId">Title:</label>
			  </td>
			  <td valign="top" class="value">
				<input type="text" maxlength="34" id="titleId" name="title"/>
			  </td>
			</tr>
			<tr class="prop">
			  <td valign="top" class="name">
				<label for="emailId">Email:</label>
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
					<tds:hasPermission permission="${Permission.AdminMenuView}">
						<g:select name="startPage" value="${tds.startPage()}"
						         from="${['Project Settings','Planning Dashboard','Admin Portal', 'User Dashboard']}" />
					</tds:hasPermission>
					<tds:lacksPermission permission="${Permission.AdminMenuView}">
						<g:select name="startPage" value="${tds.startPage()}"
						          from="${['Project Settings','Planning Dashboard', 'User Dashboard']}" />
					</tds:lacksPermission>
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">
					<label for="powerTypeId">Power In:</label>
				</td>
				<td valign="top" class="value">
					<g:select name="powerType" id="powerTypeId" from="${['Watts','Amps']}" value="${tds.powerType()}"/>
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
