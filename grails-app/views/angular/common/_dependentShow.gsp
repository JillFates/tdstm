<%@page defaultCodec="html" %> 

<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Class</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${supportAssets}" var="support" status="i">
							<tr (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${support.status}">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${support.status}">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${support.status}" style="min-width:80px">
									${support?.asset?.assetName}
								</td>
								<g:if test="${support?.asset?.moveBundle!=asset.moveBundle && support.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;">
										<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', absolute:true ,file:'error.png')}" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${support?.asset?.moveBundle!=asset.moveBundle }" >
									<td class="dep-${support.status}" style="position:relative;">
										<b>
											<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', absolute:true ,file:'error.png')}" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								</g:elseif>
								<g:else>
									<td class="dep-${support.status}">
										${support?.asset?.moveBundle}
								    </td>
								</g:else>
								<td class="dep-${support.status}" nowrap="nowrap">
									${support.type} &nbsp;
									<g:render template="/angular/common/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${support.status}">
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
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Class</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${dependent.status}">
									${dependent.dataFlowFreq}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${dependent.status}" style="min-width:80px">
									${dependent.dependent?.assetName}
								</td>
								<g:if test="${dependent.dependent?.moveBundle!=asset.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;">
										<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', absolute:true ,file:'error.png')}" width="17" height="18" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=asset.moveBundle }" >
									<td class="dep-${dependent.status}" style="position:relative;">
										<b>
											<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', absolute:true ,file:'error.png')}"  width="17" height="18" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								 </g:elseif>
								 <g:else>
									<td class="dep-${dependent.status}">
										${dependent.dependent?.moveBundle}
									</td>
								 </g:else>
								<td class="dep-${dependent.status}" nowrap="nowrap">
									${dependent.type}&nbsp;
									<g:render template="/angular/common/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${dependent.status}">
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>