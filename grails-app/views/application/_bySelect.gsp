<select  id="${id}" class="${className}" name="${name}" style="width: 90px" onchange="changeHard(this.value, this.id)">
	<option value="" selected="selected">Select...</option>
	<optgroup label="By Reference" id="smeGroup">
		<option value="#SME1">SME1</option>
		<option value="#SME2">SME2</option>
		<option value="#Owner">Owner</option>
	</optgroup>
	<optgroup label="Team" id="teamGroup">
		<g:each status="i" in="${availabaleRoles}" var="role">
			<option value="@${role.id}">${role.description}</option>
		</g:each>
	</optgroup>
	<optgroup label="Named Staff" id="staffGroup">
		<g:each status="i" in="${personList}" var="person">
			<option value="${person.personId}">${person.fullName} </option>
		</g:each>
	</optgroup>
</select>
