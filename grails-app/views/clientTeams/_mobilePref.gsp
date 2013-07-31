<div style="background-color: blue">
	<g:form controller="project" action="addUserPreference"
		name="userPrefForm" method="post">
		<input type="hidden" name="mobileSelect" value="true">
		<table style="border: 0px">
			<tr>
				<td><b>
						${person}
				</b></td>
			</tr>
			<g:each in="${personTeams}" var="team">
				<tr>
					<td>
						${team.description.substring(team.description.lastIndexOf(':') +1).trim()}
					</td>
				</tr>
			</g:each>
			<tr>
				<td>Project : <g:select from="${projects}" name="selectProject"
						optionKey="projectCode" optionValue="name" id="projectId"
						value="${project.projectCode}" onChange="submitForm()" />
				</td>
			</tr>
			<tr>
				<td>Refresh : <select id="selectTimedBarId"
					onchange="setTimer(this.value)">
						<option value="0">Manual</option>
						<option value="30">30 sec</option>
						<option value="60">1 Min</option>
						<option value="120">2 Min</option>
						<option value="180">3 Min</option>
						<option value="240">4 Min</option>
						<option value="300">5 Min</option>
				</select></td>
			</tr>
			<tr>
				<td>
					<g:link class="mmlink" action="listTasks" params="[viewMode:'web', tab:tab]">Switch to Full Site</g:link>
					<br />
				</td>
			</tr>
			<tr>
				<td><g:link class="mmlink" controller="auth" action="signOut">Sign out</g:link></td>
			</tr>
		</table>
	</g:form>
</div>
<script type="text/javascript">
	function submitForm() {
		document.userPrefForm.submit()
	}
</script>