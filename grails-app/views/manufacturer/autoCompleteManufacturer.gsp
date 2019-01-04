<table class="suggestionTable">
	<g:each in="${manufacturers}" var="manu">
		<tr id="manu_${manu.id}" class="trAnchor"
			onmouseover="this.style.backgroundColor='pink'"
			onmouseout="this.style.backgroundColor='#FCFCFC'"
			onclick="updateManu('${manu.name}')">
			<td>
				${manu.name}
			</td>
		</tr>
	</g:each>
</table>
