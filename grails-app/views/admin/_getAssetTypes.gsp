<div>
	<table>
		<thead>
			<tr>
				<th>Unused</th>
				<th>Type</th>
				<th>Projects</th>
				<th>Models</th>
				<th>Assets</th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${assetTypes}" var="assets" status="i">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>
						<g:if test="${assets[5]}">
							<asset:image src="icons/tick.png" alt="Will be purged" />
						</g:if>
					</td>
					<td>
						${assets[0]}
					</td>
					<td>
						${assets[3]} ${assets[4] ? '+ '+assets[4] : '' }
					</td>
					<td>
						${assets[2]}
					</td>
					<td>
						${assets[1]}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>

</div>