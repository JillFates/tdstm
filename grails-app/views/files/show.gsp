<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog" ondblclick="editApp(${filesInstance?.id})">
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;">${filesInstance.assetName}</td>

							<td class="label" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="3">${filesInstance.description}</td>

						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App
									Type</label></td>
							<td>${filesInstance.assetType}</td>

							<td class="label" nowrap="nowrap"><label for="supportType">Support
							</label></td>
							<td>${filesInstance.supportType}</td>

							<td class="label" nowrap="nowrap"><label for="fileFormat">
									Format </label></td>
							<td>${filesInstance.fileFormat}</td>

						</tr>

						<tr class="prop">

							<td class="label" nowrap="nowrap"><label for="environment">Environment
							</label></td>
							<td>${filesInstance.environment}</td>

							<td class="label" nowrap="nowrap"><label for="fileSize">Size
							</label></td>
							<td>${filesInstance.fileSize}</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>${filesInstance.moveBundle}</td>
						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan
									Status</label>
							</td>
							<td>${filesInstance.planStatus}</td>
						</tr>

					</tbody>
				</table>
			</div></td>
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
							<td class="dep-${support.status}">${support?.dataFlowFreq}</td>
							<td class="dep-${support.status}">${support?.asset?.assetType}</td>
							<td class="dep-${support.status}">${support?.asset?.assetName}</td>
							<td class="dep-${support.status}">${support.type}</td>
							<td class="dep-${support.status}">${support.status}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
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
							<td class="dep-${dependent.status}">${dependent.dataFlowFreq}</td>
							<td class="dep-${dependent.status}">${dependent.dependent?.assetType}</td>
							<td class="dep-${dependent.status}">${dependent.dependent?.assetName}</td>
							<td class="dep-${dependent.status}">${dependent.type}</td>
							<td class="dep-${dependent.status}">${dependent.status}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	</tr>
	<tr>
	     <tr>
			<td colspan="2">
				<div class="list">
				<table id="listCommentsTable">
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
			</td>
				
			</div>
	     </tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${filesInstance?.id}" />
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editEntity('${redirectTo}','Files', ${filesInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
					<g:if test="${assetComment == 'issue'}">
						<g:remoteLink controller="assetEntity" action="listComments" id="${filesInstance.id}" before="setAssetId('${filesInstance.id}');" onComplete="listCommentsDialog(e,'never');">
							<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="=0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</g:remoteLink>
					</g:if>
				    <g:elseif test="${assetComment == 'comment'}">
						<g:remoteLink controller="assetEntity" action="listComments" id="${filesInstance.id}" before="setAssetId('${filesInstance.id}');" onComplete="listCommentsDialog(e,'never');">
							<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</g:remoteLink>
					</g:elseif>
					<g:else>
						<a href="javascript:createNewAssetComment(${filesInstance.id});">
							<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/>&nbsp&nbspComment
						</a>
				    </g:else>
				</g:form>
			</div>
		</td>
	</tr>
</table>