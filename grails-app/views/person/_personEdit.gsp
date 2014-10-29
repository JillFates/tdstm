<%--
  -- Used to present the user with their profile information that they can edit and change
  -- @params user - the user information
  --%>

<div id="personDialog" title="Edit Person ABC" style="display:none;" class="static-dialog">
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
			
			<tds:hasPermission permission='PersonExpiryDate'>
				<tr class="prop">
					<td valign="top" class="name">
						<label for="nickName"><b>Expiry Date:<span style="color: red">*</span></label>
					</td>
					<td valign="top" class="value">
						<script type="text/javascript">
							$(document).ready(function(){
							$("#expiryDateId").datetimepicker();
						  });
						</script>
						<input type="text" maxlength="64" id="expiryDateId" name="expiryDate"/>
						<input type="text" maxlength="64" id="expiryDateId" name="expiryDate" readonly="readonly" style="background: none;border: 0"/>
					</td>
				</tr>
			</tds:hasPermission>

			<tr class="prop">
				<td valign="top" class="name">
					<label for="title">Time Zone:</label>
				</td>
				<td valign="top" class="value">
					<g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}" 
					value="${session.getAttribute('CURR_TZ')?.CURR_TZ}"/>
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
			<tr>
				<td>
					Hide password:
				</td>
				<td>
					<input type="checkbox" onchange="togglePasswordVisibility(this)" id="showPasswordId"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">
					<label for="password">Old Password:</label>
				</td>
				<td valign="top" class="value">
					<input type="hidden" id="personId" name="personId" value=""/>
					<input type="text" maxlength="25" name="oldPassword" id="oldPasswordId" value=""/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">
					<label for="password">New Password:</label>
				</td>
				<td valign="top" class="value">
					<input type="text" maxlength="25" name="newPassword" onkeyup="checkPassword(this)" id="newPasswordId" value=""/>
				</td>
			</tr>
			<tr>
				<td>
					Requirements:
				</td>
				<td>
					<em id="usernameRequirementId">Password must not contain the username</em><br/>
					<em id="lengthRequirementId">Password must be at least 8 characters long</em><br/>
					<b id="passwordRequirementsId">Password must contain at least 3 of these requirements: </b><br/>
					<ul>
						<li><em id="uppercaseRequirementId">Uppercase characters</em></li>
						<li><em id="lowercaseRequirementId">Lowercase characters</em></li>
						<li><em id="numericRequirementId">Numeric characters</em></li>
						<li><em id="symbolRequirementId">Nonalphanumeric characters</em></li>
					</ul>
				</td>
			</tr>
		  </tbody>
		</table>
	  </div>
	  <div class="buttons">
		<span class="button"><input type="button" class="edit" value="Update" onclick="changePersonDetails()"/></span>
		<span class="button"><input type="button" class="delete" onclick="jQuery('#personDialog').dialog('close')" value="Cancel" /></span>
	  </div>
	</div>
</div>