
<g:each in="${upcomingEvents.keySet()}" var="event">
	<g:set var="moveEvent" value="${upcomingEvents[event].moveEvent}" />
	<tr>
		<g:if test="${project=='All'}">
			<td>
				${moveEvent.project.name}
			</td>
		</g:if>
		<td><g:link action="index" params="[moveEvent:moveEvent.id]">
				${moveEvent.name}
			</g:link></td>
		<td>
		<tds:convertDate date="${moveEvent.eventTimes.start}" />
			
		</td>
		<td>
			${upcomingEvents[event]?.daysToGo+' days'}
		</td>
		<td>
			${upcomingEvents[event]?.teams}
		</td>
	</tr>
</g:each>

