<%--
	This is the template used to represent a single row in the Execution Phase of the Planning Dashboard

	model:
		assetCount
		unassignedCount
		percDone
		controller
		filter
		list

--%>
<g:set var="percUnassigned" value="${assetCount ? (unassignedCount/assetCount)*100 : 0}" />
<g:set var="percUnassigned" value="${(percUnassigned > 0 && percUnassigned < 1) ? 1 : Math.round(percUnassigned)}" />
<tr>
	<td nowrap="nowrap" style="text-align: right;">
		<g:if test="${unassignedCount == 0 }">
			<asset:image src="images/checked-icon.png" />
		</g:if><g:else>
			<g:link controller="${controller}" action="list"
				params="[filter:filter, plannedStatus:'Unassigned']"
				class="links">
				${unassignedCount} (${percUnassigned}%)
			</g:link>
		</g:else>
	</td>
	<g:each in="${list}" var="data">
		<td style="text-align: right;">
			<g:if test="${data.count == 0 }">
				<span class='colorGrey'>0</span>
			</g:if><g:else>
				<g:link controller="${controller}" action="list"
					params="[filter:filter, moveEvent:data.moveEvent]"
					class="links">
					${data.count}
				</g:link>
			</g:else>
		</td>
	</g:each>
	<td style="text-align: right;">
		<g:if test="${percDone > 0}">
			<g:link controller="${controller}" action="list"
				params="[filter:filter, plannedStatus:'Moved']"
				class="links">
				${percDone}%
			</g:link>
		</g:if>
	</td>
</tr>
