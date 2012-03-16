<table style="border: 0">
	<tr>
	
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='EditAndDelete'> ondblclick="editEntity('${redirectTo}', 'Application', ${applicationInstance?.id})"</tds:hasPermission>>
				<table>
					<tbody>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label>
							</td>
							<td style="font-weight:bold;">
								${applicationInstance.assetName}
							</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="3">${applicationInstance.description}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label>
							</td>
							<td>
								${applicationInstance.assetType}
							</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
							</td>
							<td>
								${applicationInstance.supportType}
							</td>
							<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
							</td>
							<td>
								${applicationInstance.appFunction}
							</td>
							<td class="label" nowrap="nowrap"><label for="userCount">Users</label>
							</td>
							<td>${applicationInstance.userCount}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label>
							</td>
							<td>
								${applicationInstance.appVendor}
							</td>
							<td class="label" nowrap="nowrap"><label for="sme">SME1</label>
							</td>
							<td>
								${applicationInstance.sme}
							</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
							</td>
							<td>
								${applicationInstance.environment}
							</td>
							<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label></td>
							<td>
								${applicationInstance.userLocations}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
							</td>
							<td>
								${applicationInstance.appVersion}
							</td>
							<td class="label" nowrap="nowrap"><label for="sme2">SME2</label>
							</td>
							<td>
								${applicationInstance.sme2}
							</td>
							<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
							</td>
							<td>
								${applicationInstance.criticality}
							</td>
							<td class="label" nowrap="nowrap"><label for="useFrequency">Use Frequency</label></td>
							<td>
								${applicationInstance.useFrequency}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label>
							</td>
							<td>
								${applicationInstance.appTech}
							</td>
							<td class="label" nowrap="nowrap"><label for="businessUnit">Bus Unit</label></td>
							<td>
								${applicationInstance.businessUnit}
							</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>
								${applicationInstance.moveBundle}
							</td>
							</td>
							<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label></td>
							<td>
								${applicationInstance.drRpoDesc}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appSource">Source</label>
							</td>
							<td>
								${applicationInstance.appSource}
							</td>
							<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label>
							</td>
							<td>
								${applicationInstance.appOwner}
							</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td>
								${applicationInstance.planStatus}
							</td>
							<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label></td>
							<td>${applicationInstance.drRtoDesc}
							</td>
						</tr>
						
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">License</label>
							</td>
							<td>
								${applicationInstance.license}
							</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td><tds:convertDate
									date="${applicationInstance?.retireDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap">Validation</td>
							<td>${applicationInstance.validation}
							</td>
							<td class="label" nowrap="nowrap"><label for="testProc">Test Proc</label></td>
							<td>${applicationInstance.testProc ? applicationInstance.testProc : '?'}
							</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td><tds:convertDate
									date="${applicationInstance?.maintExpDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="latency">Latency</label></td>
							<td>
								${applicationInstance.latency ? applicationInstance.latency : '?'}
							</td>
							<td class="label" nowrap="nowrap"><label for="startupProc">Startup Procs</label></td>
							<td>${applicationInstance.startupProc ? applicationInstance.startupProc : '?'}
							</td>
						</tr>
						<tr>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom1)?.length()>= 4 &&(applicationInstance.custom1)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom1}','help');" style="color:#00E">${applicationInstance.project.custom1!=null ? applicationInstance.project.custom1 : 'Custom1'}</a>
								</g:if>
								<g:else>
									 ${applicationInstance.project.custom1 ?: 'Custom1'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom2)?.length()>= 4 &&(applicationInstance.custom2)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom2}','help');" style="color:#00E">${applicationInstance.project.custom2!=null ? applicationInstance.project.custom2 : 'Custom2'}</a>
								</g:if>
								<g:else>
									 ${applicationInstance.project.custom2 ?: 'Custom2'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom3)?.length()>= 4 &&(applicationInstance.custom3)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom3}','help');" style="color:#00E">${applicationInstance.project.custom3!=null ? applicationInstance.project.custom3 : 'Custom3'}</a>
								</g:if>
								<g:else>
									${applicationInstance.project.custom3 ?: 'Custom3'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom4)?.length()>= 4 &&(applicationInstance.custom4)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom4}','help');" style="color:#00E">${applicationInstance.project.custom4!=null ? applicationInstance.project.custom4 : 'Custom4'}</a>
								</g:if>
								<g:else>
									 ${applicationInstance.project.custom4 ?: 'Custom4'}
								</g:else>
							</td>
						</tr>
						<tr>
						    <td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom5)?.length()>= 4 &&(applicationInstance.custom5)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom5}','help');" style="color:#00E">${applicationInstance.project.custom5 !=null ? applicationInstance.project.custom5 : 'Custom5'}</a>
								</g:if>
								<g:else>
									${applicationInstance.project.custom5 ?: 'Custom5'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom6)?.length()>= 4 &&(applicationInstance.custom6)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom6}','help');" style="color:#00E">${applicationInstance.project.custom6 !=null ? applicationInstance.project.custom6 : 'Custom6'}</a>
								</g:if>
								<g:else>
									 ${applicationInstance.project.custom6 ?: 'Custom6'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom7)?.length()>= 4 &&(applicationInstance.custom7)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom7}','help');" style="color:#00E">${applicationInstance.project.custom7!=null ? applicationInstance.project.custom7 : 'Custom7'}</a>
								</g:if>
								<g:else>
									 ${applicationInstance.project.custom7 ?: 'Custom7'}
								</g:else>
							</td>
							<td colspan="2" class="label" nowrap="nowrap">
								<g:if test="${(applicationInstance.custom8)?.length()>= 4 &&(applicationInstance.custom8)?.substring(0, 4)=='http'}">
											<a href="javascript:window.open('${applicationInstance.custom8}','help');" style="color:#00E">${applicationInstance.project.custom8!=null ? applicationInstance.project.custom8 : 'Custom8'}</a>
								</g:if>
								<g:else>
									${applicationInstance.project.custom8 ?: 'Custom8'}
								</g:else>
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">URL</label></td>
							<td colspan="7"><a href="${applicationInstance.url}" style="color:#00E">${applicationInstance.url}</a>
							</td>
						</tr>
						
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Entity Type</th>
							<th>Name</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${supportAssets}" var="support" status="i">
							<tr onclick="getEntityDetails('${redirectTo}','${support?.asset?.assetType}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support?.asset?.assetName}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support.type}
								</td>
								<td class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status }">
									${support.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div>
				<h1>Is dependent on:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Entity Type</th>
							<th>Name</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr onclick="getEntityDetails('${redirectTo}','${dependent.dependent?.assetType}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dataFlowFreq}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.dependent?.assetName}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.type}
								</td>
								<td class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status }">
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<g:if test="${assetCommentList.size() > 0 }">
	<tr>
		<td colspan="2">
			<div class="list">
			<table id="listCommentsTables">
			<thead>
			<tr>
				<th nowrap>Action</th>
				<th nowrap>Comment</th>
				<th nowrap>Comment Type</th>
				<th nowrap>Resolved</th>
				<th nowrap>Must Verify</th>
				<th nowrap>Category</th>  
				<th nowrap>Comment Code</th>                     
			</tr>
			</thead>
			<tbody id="listCommentsTbodyIds">
			<g:each status="i" in="${assetCommentList}"  var="commentList">
			<tr style="cursor: pointer;">
				<td ><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
				<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
				<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentType}</td>
				<td ><g:if test ="${commentList.commentType =='issue' && commentList.isResolved == 1}"><g:checkBox name="myCheckbox" value="${true}" disabled="true"/></g:if><g:else>&nbsp</g:else></td>
				<td ><g:if test ="${commentList.mustVerify == 1}"></g:if><g:else><g:checkBox name="myVerifyBox" value="${true}" disabled="true"/></g:else></td>
				<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
				<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.commentCode}</td>
			</tr>
			</g:each>
			</tbody>
			</table>
	      </div>
	      </td>
	</tr>
	</g:if>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${applicationInstance?.id}" />
					<tds:hasPermission permission='EditAndDelete'>
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editEntity('${redirectTo}','Application',${applicationInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					</tds:hasPermission>
					<g:if test="${assetComment == 'issue'}">
						<g:remoteLink controller="assetEntity" action="listComments" id="${applicationInstance.id}" before="setAssetId('${applicationInstance.id}');" onComplete="listCommentsDialog(e,'never');">
							<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="=0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</g:remoteLink>
					</g:if>
				    <g:elseif test="${assetComment == 'comment'}">
						<g:remoteLink controller="assetEntity" action="listComments" id="${applicationInstance.id}" before="setAssetId('${applicationInstance.id}');" onComplete="listCommentsDialog(e,'never');">
							<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</g:remoteLink>
				    </g:elseif>
					<g:else>
						<a href="javascript:createNewAssetComment(${applicationInstance.id});">
							<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</a>
				    </g:else>
				</g:form>
			</div></td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
