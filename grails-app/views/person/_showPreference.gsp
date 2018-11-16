<form id="userPreferencesForm">
	<table class="table table-striped col-md-6">
		<thead>
			<tr>
				<th class="col-md-2">Preference</th>
				<th class="col-md-8">Value</th>
				<th class="col-md-2" style="text-align: center;">Remove</th>
			</tr>
		</thead>
		<g:if test="${preferences.size() == 0}">
			<tr>
				<td colspan="3" style="text-align: center;">No preferences</td>
			</tr>
		</g:if>
		<g:else>
			<g:each in="${preferences}" var="pref">
				<tr id="pref_${pref.code}">
					<td nowrap="nowrap">${pref.label}</td>
					<td nowrap="nowrap">${pref.value}</td>
					<td style="text-align: center;">
						<g:if test="${ ! (pref.code in fixedPreferenceCodes) }">
							<span class="clear_filter spanAnchor" onclick="UserPreference.removeUserPreference('${pref.code}')">X</span>
						</g:if>
					</td>
				</tr>
			</g:each>
		</g:else>
	</table>

	<div class="buttons">
		<span class="button"><input id="prefButton" type="button" class="delete" onclick="UserPreference.resetPreferences()" value="Reset All"/> </span>
	</div>
</form>
