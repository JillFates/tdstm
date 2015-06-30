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

<div class="buttons">
  <span class="button"><input type="button" class="edit" value="Select" onclick="Project.setTimeZone('dateTimezone', 'timezone')" /> </span>
</div>
</form>

