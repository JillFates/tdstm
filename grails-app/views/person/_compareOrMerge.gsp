<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
</head>
<body>
	<div class="body">
		<table>
			<tr class="odd">
				<td colspan="3" style="text-align: center;"><b>Person
						Information</b></td>
			</tr>
			<g:each in="${columnList.keySet()}" var="column" status="i">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td ><b>
							${column}
					</b></td>
					<g:each in="${personsMap.keySet()}" var="person" status="j">
						<g:if test="${column =='Merge To'}">
							<td class="col_${person.id} compareColumn"><input type="radio"
								class="merge" name="mergeRadio" id="merge_${person.id}"
								${j==0 ? 'checked="checked"' : '' } /></td>
						</g:if>
						<g:elseif test="${column =='Black Out Dates'}">
							<td class="col_${person.id}">
								<g:each in="${person.(columnList.get(column))?.exceptionDay}" var="expDay">
									<tds:convertDate date="${expDay}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />,
								</g:each> 
							</td>
						</g:elseif>
						<g:elseif test="${column =='Roles'}">
							<td class="col_${person.id}">
								<g:each in="${person.getPersonRoles(personsMap[person])}" var="role">
									${role.description.substring(role.description.lastIndexOf(':') +1).trim()},
								</g:each> 
							</td>
						</g:elseif>
						<g:else>
							<td id="${columnList.get(column)+'_td_'+person.id}"
								class="col_${person.id}"><span>
									${columnList.get(column) ? person.(columnList.get(column)) : ''}
							</span></td>
						</g:else>
					</g:each>
				</tr>
			</g:each>
			
			<tr class="odd">
				<td colspan="3" style="text-align: center;"><b>Login
						Information</b></td>
			</tr>
			
			<g:each in="${loginInfoColumns.keySet()}" var="column" status="i">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td><b>
							${column}
					</b></td>
					<g:each in="${userLogins}" var="userLogin" status="j">
						<td id="${loginInfoColumns.get(column)+'_td_'+userLogin?.id}"
							class="col_${userLogin?.id}"><g:if
								test="${['createdDate', 'lastLogin', 'lastPage', 'expiryDate'].contains(loginInfoColumns.get(column)) }">
								<span><tds:convertDateTime
										date="${userLogin?.(loginInfoColumns.get(column))}"
										timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
								</span>
							</g:if> <g:else>
								<span>
									${loginInfoColumns.get(column) ? userLogin?.(loginInfoColumns.get(column)) : ''}
								</span>
							</g:else></td>
					</g:each>
				</tr>
			</g:each>
		</table>
		
		<div class="buttons">
			<input type="button" class="save" value="Cancel" id="processData" onclick="jQuery('#showOrMergeId').dialog('close')" /> 
			<input type="button" id="mergeModelId" class="save" value="Merge" onclick="mergePerson()" />
		</div>
	</div>
</body>
</html>