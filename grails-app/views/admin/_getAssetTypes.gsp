<div>
	<table>
		<thead>
			<tr>
				<th>Type</th>
				<th>Models</th>
				<th>Assets</th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${returnMap}" var="assets" status="i">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>
						${assets[0]}
					</td>
					<td>
						${assets[1]}
					</td>
					<td>
						${assets[2]}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>

</div>