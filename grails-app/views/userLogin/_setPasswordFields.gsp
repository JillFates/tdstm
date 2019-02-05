<g:if test="${fromDialog}">

	<tr class='passwordsEditFields'>
		<td>Hide password:</td>
		<td>
			<input type="checkbox" onchange="PasswordValidation.togglePasswordVisibility(this)" id="showPasswordEditId" checked/>
		</td>
	</tr>
	<g:if test="${changingPassword}">
		<tr class="prop js-password">
			<td valign="top" class="name">
				<label for="oldPasswordId">Old Password:</label>
			</td>
			<td valign="top" class="value">
				<input type="hidden" id="personId" name="personId" value=""/>
				<input type="password" id="oldPasswordId" class="passwordField" maxlength="25" name="oldPassword" value=""/>
			</td>
		</tr>
	</g:if>
	<tr class="prop passwordsEditFields">
		<td valign="top" class="name">
			<label for="passwordId">
				<g:if test="${changingPassword}">New </g:if>Password:&nbsp;
			</label>
		</td>
		<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
			<input type="password" id="passwordId" class="passwordField" onkeyup="PasswordValidation.checkPassword(this)" name="password" value="" autocomplete="off" />

			<g:hasErrors bean="${userLoginInstance}" field="password">
				<div class="errors">
					<g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
				</div>
			</g:hasErrors>
		</td>
	</tr>
	<tr class="passwordsEditFields">
		<td>Requirements:</td>
		<td>
			<em id="usernameRequirementId">Password must not contain the username<b class="ok"></b></em><br/>
			<em id="lengthRequirementId" size="${minPasswordLength}">Password must be at least ${minPasswordLength} characters long<b class="ok"></b></em><br/>
			<b id="passwordRequirementsId">Password must contain at least 3 of these requirements:<b class="ok"></b></b><br/>
			<ul>
				<li><em id="uppercaseRequirementId">Uppercase characters<b class="ok"></b></em></li>
				<li><em id="lowercaseRequirementId">Lowercase characters<b class="ok"></b></em></li>
				<li><em id="numericRequirementId">Numeric characters<b class="ok"></b></em></li>
				<li><em id="symbolRequirementId">Nonalphanumeric characters<b class="ok"></b></em></li>
			</ul>
		</td>
	</tr>
	<tr class="passwordsEditFields">
		<td valign="top" class="name">
			<label for="passwordId">
				Confirm <g:if test="${changingPassword}">new </g:if>password:&nbsp;
			</label>
		</td>
		<td valign="top" class="value ${hasErrors(bean:userLoginInstance,field:'password','errors')}">
			<input type="password" id="confirmPasswordId" class="passwordField" onkeyup="PasswordValidation.confirmPassword($('#passwordId')[0], this)" name="confirmPassword" value="" autocomplete="off" />

			<g:hasErrors bean="${userLoginInstance}" field="password">
				<div class="errors">
					<g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
				</div>
			</g:hasErrors>
		</td>
	</tr>
	<tr class="passwordConfirmField passwordsEditFields">
		<td></td>
		<td>
			<em id="retypedPasswordMatchRequirementId">Password should match<b class="ok"></b></em><br/>
		</td>
	</tr>
</g:if>

<g:if test="${!fromDialog}">
<div class="checkbox">
	<label>
		<input type="checkbox" onchange="PasswordValidation.togglePasswordVisibility(this)" id="showPasswordEditId" checked/> Hide password
	</label>
</div>

<g:if test="${changingPassword}">
	<div class="form-group has-feedback">
		<input type="hidden" id="personId" name="personId" value=""/>
		<input type="password" id="oldPasswordId" class="form-control passwordField" maxlength="25" name="oldPassword" value=""  placeholder="Old Password"/>
		<span class="glyphicon glyphicon-lock form-control-feedback"></span>
	</div>
</g:if>

<div class="form-group has-feedback">
	<input type="password" id="passwordId" class="form-control passwordField" name="password" autocorrect="off" autocapitalize="off" placeholder="Enter your <g:if test="${changingPassword}">New </g:if> password" onkeyup="PasswordValidation.checkPassword(this)"/>
	<span class="glyphicon glyphicon-lock form-control-feedback"></span>
	<g:hasErrors bean="${userLoginInstance}" field="password">
		<div class="message">
			<g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
		</div>
	</g:hasErrors>
</div>


<em id="usernameRequirementId">Password must not contain the username<b class="ok"></b></em><br/>
<em id="lengthRequirementId" size="${minPasswordLength}">Password must be at least ${minPasswordLength} characters long<b class="ok"></b></em><br/>
<em id="passwordRequirementsId">Password must contain at least 3 of these requirements:</em><br/>
<ul>
	<li><em id="uppercaseRequirementId">Uppercase characters<b class="ok"></b></em></li>
	<li><em id="lowercaseRequirementId">Lowercase characters<b class="ok"></b></em></li>
	<li><em id="numericRequirementId">Numeric characters<b class="ok"></b></em></li>
	<li><em id="symbolRequirementId">Nonalphanumeric characters<b class="ok"></b></em></li>
</ul>

<div class="form-group has-feedback">
	<input type="password" id="confirmPasswordId" class="form-control passwordField" name="confirmPassword" autocorrect="off" autocapitalize="off" placeholder="Confirm <g:if test="${changingPassword}">new </g:if>password" onkeyup="PasswordValidation.confirmPassword($('#passwordId')[0], this)"/>
	<span class="glyphicon glyphicon-lock form-control-feedback"></span>
	<g:hasErrors bean="${userLoginInstance}" field="password">
		<div class="message">
			<g:renderErrors bean="${userLoginInstance}" as="list" field="password"/>
		</div>
	</g:hasErrors>
</div>
	<em id="retypedPasswordMatchRequirementId">Password should match<b class="ok"></b></em><br/>
</g:if>