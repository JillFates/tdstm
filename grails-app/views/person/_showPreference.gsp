<script>
$(document).ready(function(){
	if(!$("#prefButton").length){
		var dialog = $("#userPrefDivId").dialog();
		var titlebar = dialog.parents('.ui-dialog').find('.ui-dialog-title');
		titlebar.append('<input id="prefButton" type="button" class="prefButton" onclick="resetPreference(${session.getAttribute('LOGIN_PERSON').id})" value="Reset All"/>');
	}
});

</script>

<table>
   <g:each in="${prefMap}" var="pref">
       <tr  id="pref_${pref.getKey()}" >
     	 	<td class="personShow" nowrap="nowrap"><span>${pref.getValue()}</span></td><td class="personShow"><span class="clear_filter spanAnchor" onclick="removeUserPrefs('${pref.getKey()}')">X</span></td>
     	 </tr>
   </g:each>
</table>
