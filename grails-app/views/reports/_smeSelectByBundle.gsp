

<g:if test="${forWhom != 'conflict'}">
	<tr>
		<td>SME : </td>
		<td>
			<g:select from="${smeList}" id="smeByModel" name="smeByModel" style="width:178px;" optionKey="id" optionValue="${{it.lastNameFirst}}" 
				noSelection="${['null':'All']}" value="${selectedSme}"/>
		</td>
	</tr>
</g:if>
<g:if test="${forWhom !='migration' }">
<tr>
	<td>App Owner : 
	<g:if test="${forWhom != 'conflict'}">
		</td>
		<td>
	</g:if>
		<g:select from="${appOwnerList}" id="appOwner" name="appOwner" style="width:178px;" optionKey="id" 
			optionValue="${{it.lastNameFirst}}" noSelection="${['null':'All']}" value="${selectedOwner}"/>
	</td>
</tr>
</g:if>
