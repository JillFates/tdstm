<table>
   <g:each in="${prefMap}" var="pref">
       <tr  id="pref_${pref.getKey()}" >
     	 	<td class="personShow" nowrap="nowrap"><span>${pref.getValue()}</span></td><td class="personShow"><span class="clear_filter spanAnchor" onclick="removeUserPrefs('${pref.getKey()}')">X</span></td>
     	 </tr>
   </g:each>
</table>
<div class="buttons">
    <span class="button"><input type="button" class="edit" value="Reset All" onclick="resetPreference(${session.getAttribute('LOGIN_PERSON').id})"/></span>
    <span class="button"><input type="button" class="delete" onclick="jQuery('#userPrefDivId').dialog('close')" value="Cancel" /></span>
</div>