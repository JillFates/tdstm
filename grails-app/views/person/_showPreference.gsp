<form id="userPreferencesForm">
	<table>
		<g:if test="${prefMap.size() == 0}">
			<tr>
				<th>Preferences: Name / Value</th>
			</tr>
			<tr>
				<td>No preferences</td>
			</tr>
		</g:if>
		<g:else>
			<tr>
				<th colspan="2">Preferences: Name / Value</th>
			</tr>
			<g:each in="${prefMap}" var="pref">
				<tr id="pref_${pref.getKey()}">
					<td nowrap="nowrap"><span>${pref.getValue()}</span></td><td><span class="clear_filter spanAnchor" onclick="UserPreference.removeUserPrefs('${pref.getKey()}')">X</span></td>
				</tr>
			</g:each>
		</g:else>
	</table>

	<div class="buttons">
		<span class="button"><input id="prefButton" type="button" class="delete" onclick="UserPreference.resetPreference(${tds.currentPersonId()})" value="Reset All"/> </span>
	</div>
</form>
