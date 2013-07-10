<select id="${id}" name="${name}" style="width: 90px">
	<option value="" selected="selected">Select...</option>
	<optgroup label="SME pointers" id="smeGroup">
		<option value="SME1">SME1</option>
		<option value="SME2">SME2</option>
		<option value="Owner">Owner</option>
	</optgroup>
	<optgroup label="Team pointers" id="teamGroup">
		<g:each status="i" in="${availabaleRoles}" var="role">
			<option value="${role.id}">${role.description.substring(role.description.lastIndexOf(':') +1).trim()}</option>
		</g:each>
	</optgroup>
	<optgroup label="Staff names" id="staffGroup">
		<g:each status="i" in="${personList}" var="person">
			<option value="${person.id}">${person} </option>
		</g:each>
	</optgroup>
</select>