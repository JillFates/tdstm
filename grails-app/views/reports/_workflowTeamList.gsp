<g:each in="${bundleMap}" var="bundle">
	<g:if test="${bundle.teamList.size()>0}">
		<h3>
			<b> ${bundle.name}
			</b>
		</h3>
		<br />
		<table style="width: 700px; margin-left: 100px">
			<thead>
				<tr>
					<th width="100px;">Team</th>
					<th width="100px;">Role</th>
					<th width="150px;">Team Members</th>
					<th width="50px;">Assets</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${bundle.teamList}" var="teams" status="i">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td> ${teams.name} </td>
						<td> ${teams.role} </td>
						<td>
							<table style="border: 0px;">
								<tr>
									<td><g:each in="${teams.teamList}" var="teamListStaff">
										<tr>
											<td> ${teamListStaff.company[0]}:${teamListStaff.name}</td>
										</tr>
										</g:each>
									 </td>
								</tr>
							</table>
						</td>
						<td>
							${teams.assetSize}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</g:if>
</g:each>
