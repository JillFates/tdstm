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
				<g:each in="${bundle.teamList}" var="teamList" status="i">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td> ${teamList.name[0]} </td>
						<td> ${teamList.role[0]} </td>
						<td>
							<table style="border: 0px;">
								<tr>
									<td><g:each in="${teamList.teamList[0]}" var="teamListtaff">
										<tr>
											<td> ${teamListtaff.company[0]}:${teamListtaff.name}</td>
										</tr>
										</g:each>
									 </td>
								</tr>
							</table>
						</td>
						<td>
							${teamList.assetSize[0]}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</g:if>
</g:each>