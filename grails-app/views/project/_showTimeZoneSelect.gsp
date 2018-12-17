<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="net.transitionmanager.domain.Project" %>
<%@page import="net.transitionmanager.domain.UserPreference" %>

<script>
	$(document).ready(function(){
		$('#timezoneImage').timezonePicker({
			target: '#dateTimezone'
		});
	});
</script>

<form id="userTimezoneForm">
	<div style="padding: 5px">
		<b>Time Zone:</b>
		<g:select from="${timezones}" id="dateTimezone" value="${currTimeZone}" name="timezone"  optionKey="code" optionValue="label" />
		<g:if test="${userPref}">
			<b style="padding-left: 30px">Date Format:</b>
			<g:select from="${TimeUtil.dateTimeFormatTypes}" id="datetimeFormat" name="datetimeFormat" value="${currDateTimeFormat}" />
		</g:if>
	</div>

	<img id="timezoneImage" src="${assetPath(src: 'images/world_map_800.jpg')}" width="800" height="400" usemap="#timezoneMap"/>
	<asset:image src="images/pin.png" style="padding-top: 4px;" />

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
		<g:if test="${userPref}">
			<span class="button"><input type="button" class="save" value="Save" onclick="UserPreference.savePreferences('userTimezoneForm')" /> </span>
		</g:if>
		<g:else>
			<span class="button"><input type="button" class="edit" value="Select" onclick="Project.setTimeZone('dateTimezone', 'timezone')" /> </span>
		</g:else>
	</div>
</form>
