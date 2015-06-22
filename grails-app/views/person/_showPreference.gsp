<%@page import="com.tdssrc.grails.TimeUtil"%>

<script>
$(document).ready(function(){
	$('#timezoneImage').timezonePicker({
    target: '#dateTimezone'
  });
});
</script>

<form id="userPreferencesForm">
<div style="padding: 5px">
  <b>Time Zone:</b><g:select from="${timezones}" id="dateTimezone" value="${currTimeZone}" name="timezone"  optionKey="code" optionValue="label" />
  <b style="padding-left: 30px">Date Format:</b><g:select from="${TimeUtil.dateTimeFormats}" id="datetimeFormat" name="datetimeFormat" value="${currDateTimeFormat}" />
</div>

<img id="timezoneImage" src="${resource(dir:'images',file:'world_map_800.jpg')}" width="800" height="400" usemap="#timezoneMap"/>
<img class="timezone-pin" src="${resource(dir:'images',file:'pin.png')}" style="padding-top: 4px;" />

<map name="timezoneMap" id="timezoneMap">
    <g:each in="${areas}" var="tzName, tzInfo">
      <g:each in="${tzInfo['polys']}" var="coords">
        <area data-timezone="${tzName}" data-country="" data-country="${tzInfo['country']}" data-pin="${tzInfo['pin'].join(',')}" data-offset="${tzInfo['offset']}" shape="poly" coords="${coords.join(',')}" />
      </g:each>
      <g:each in="${tzInfo['rects']}" var="coords">
        <area data-timezone="${tzName}" data-country="" data-country="${tzInfo['country']}" data-pin="${tzInfo['pin'].join(',')}" data-offset="${tzInfo['offset']}" shape="rect" coords="${coords.join(',')}" />
      </g:each>
    </g:each>
</map>

<br>

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
         <tr  id="pref_${pref.getKey()}" >
          <td class="personShow" nowrap="nowrap"><span>${pref.getValue()}</span></td><td class="personShow"><span class="clear_filter spanAnchor" onclick="removeUserPrefs('${pref.getKey()}')">X</span></td>
         </tr>
     </g:each>
  </g:else>
</table>

<div class="buttons">
  <span class="button"><input id="prefButton" type="button" class="delete" onclick="resetPreference(${session.getAttribute('LOGIN_PERSON').id})" value="Reset All"/> </span>
  <span class="button"><input type="button" class="edit" value="Save" onclick="UserPreference.savePreferences('userPreferencesForm')" /> </span>
</div>
</form>

