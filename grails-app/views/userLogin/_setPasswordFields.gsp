<div class="checkbox">
	<label>
		<input type="checkbox" onchange="PasswordValidation.togglePasswordVisibility(this)" id="showPasswordEditId"/> Hide password
	</label>
</div>

<g:if test="${changingPassword}">
	<div class="form-group has-feedback">
		<input type="hidden" id="personId" name="personId" value=""/>
		<input type="text" id="oldPasswordId" class="form-control passwordField" maxlength="25" name="oldPassword" value=""  placeholder="Old Password"/>
		<span class="glyphicon glyphicon-lock form-control-feedback"></span>
	</div>
</g:if>

<div class="form-group has-feedback">
	<input type="text" id="passwordId" class="form-control passwordField" name="password" autocorrect="off" autocapitalize="off" placeholder="Enter your <g:if test="${changingPassword}">New </g:if> password" onkeyup="PasswordValidation.checkPassword(this)"/>
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