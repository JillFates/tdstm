<%@page defaultCodec="html" %>
<a (click)="showDetails = !showDetails">Toggle All Details</a>
<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
	<tbody>
		<tds:clrRowDetail field="${standardFieldSpecs.assetName}" value="${asset.assetName}" />
		<tds:clrRowDetail field="${standardFieldSpecs.description}" value="${asset.description}" />
		<tds:clrRowDetail field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" />
		<tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
		<tds:clrRowDetail field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" />
		<tds:clrRowDetail field="${standardFieldSpecs.userCount}" value="${asset.userCount}" />
		<tds:clrRowDetail field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
			<td>
				<a (click)="launchManageStaff(${asset.sme?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.sme}" />
                </a>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.environment}" value="${asset.environment}" />
		<tds:clrRowDetail field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" />
		<tds:clrRowDetail field="${standardFieldSpecs.appTech}" value="${asset.appTech}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
			<td>
				<a (click)="launchManageStaff(${asset.sme2?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.sme2}" />
                </a>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.criticality}" value="${asset.criticality}" />
		<tds:clrRowDetail field="${standardFieldSpecs.useFrequency}" value="${asset.useFrequency}" />
		<tds:clrRowDetail field="${standardFieldSpecs.appSource}" value="${asset.appSource}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
			<td>
				<a (click)="launchManageStaff(${asset.appOwner?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.appOwner}" />
                </a>
			</td>
		</tr>
		<tr>
			<th>
				<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
					${standardFieldSpecs.moveBundle.label} : Dep. Group
				</label>
			</th>
			<td>
				${asset?.moveBundle}
				<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}" />
		<tds:clrRowDetail field="${standardFieldSpecs.license}" value="${asset.license}" />
		<tds:clrRowDetail field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}" />
		<tds:clrRowDetail field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}" />
		<tds:clrRowDetail field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
			<td>{{ '${asset?.retireDate}' | tdsDate: userDateFormat }}</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.validation}" value="${asset.validation}" />
		<tds:clrRowDetail field="${standardFieldSpecs.testProc}" value="${asset.testProc}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
			<td>
				{{ '${asset?.maintExpDate}' | tdsDate: userDateFormat }}
			</td>
		</tr>
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.latency}" value="${asset.latency}"/>
			<td>${asset.latency == '?' ? '' : asset.latency}</td>
		</tr>
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}"/>
			<td>${asset.startupProc == '?' ? '' : asset.startupProc}</td>
		</tr>
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.url}" value="${asset.url}"/>
			<td class="valueNW ${standardFieldSpecs.url.imp}" ><tds:textAsLink  text="${asset.url}" target="_new"/></td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.shutdownBy}" value="${asset.shutdownBy}"/>
			<td class="valueNW ${standardFieldSpecs.shutdownBy.imp}" nowrap="nowrap">
				<g:if test="${shutdownById == -1}">
					${shutdownBy}
				</g:if>
				<g:else>
					<a (click)="launchManageStaff(${shutdownById})">
						${shutdownBy}
					</a>
				</g:else>
				<g:if test="${asset.shutdownFixed ==1 }">
					<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
					<label for="shutdownFixedId" >Fixed</label>
				</g:if>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
			<td>
				<g:if test="${startupById == -1}">
					${startupBy}
				</g:if>
				<g:else>
					<a (click)="launchManageStaff(${startupById})">
						${startupBy}
					</a>
				</g:else>
				<g:if test="${asset.startupFixed ==1 }">
					<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${asset.startupFixed}" checked="checked"/>
					<label for="startupFixedId" >Fixed</label>
				</g:if>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" />
		<tr>
			<tds:clrInputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
			<td>
				<g:if test="${testingById == -1}">
					${testingBy}
				</g:if>
				<g:else>
					<a (click)="launchManageStaff(${testingById})">
						${testingBy}
					</a>
				</g:else>
				<g:if test="${asset.testingFixed ==1 }">
					<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
					<label for="testingFixedId" >Fixed</label>
				</g:if>
			</td>
		</tr>
		<tds:clrRowDetail field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}" />
		<g:render template="/angular/common/customShow" model="[asset:asset,customs:customs]"></g:render>
        <g:render template="/angular/common/assetTags"></g:render>
	</tbody>
</table>

	

