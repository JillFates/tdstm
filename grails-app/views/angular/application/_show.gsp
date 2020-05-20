<%@page defaultCodec="html" %>
<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
	<tbody [ngClass]="{'one-column':!showDetails, 'three-column':showDetails}">
		<tds:clrRowDetail style="order: 5" field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}" />
		<tds:clrRowDetail style="order: 10" field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
		<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}" />
		<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.userCount}" value="${asset.userCount}" />
		<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}" />
		<tr style="order: 30">
			<tds:clrInputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
			<td>
				<a (click)="launchManageStaff(${asset.sme?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.sme}" />
                </a>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 35" field="${standardFieldSpecs.environment}" value="${asset.environment}" />
		<tds:clrRowDetail style="order: 40" field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" />
		<tds:clrRowDetail style="order: 45" field="${standardFieldSpecs.appTech}" value="${asset.appTech}" />
		<tr style="order: 50">
			<tds:clrInputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
			<td>
				<a (click)="launchManageStaff(${asset.sme2?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.sme2}" />
                </a>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.criticality}" value="${asset.criticality}" />
		<tds:clrRowDetail style="order: 60" field="${standardFieldSpecs.useFrequency}" value="${asset.useFrequency}" />
		<tds:clrRowDetail style="order: 65" field="${standardFieldSpecs.appSource}" value="${asset.appSource}" />
		<tr style="order: 70">
			<tds:clrInputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
			<td>
				<a (click)="launchManageStaff(${asset.appOwner?.id})">
					<tds:nameAndCompany client="${client}" person="${asset.appOwner}" />
                </a>
			</td>
		</tr>
		<tr style="order: 75">
			<th class="${standardFieldSpecs.moveBundle.imp?:''}">
				${standardFieldSpecs.moveBundle.label} : Dep. Group
			</th>
			<td>
				${asset?.moveBundle}
				<g:if test="${dependencyBundleNumber}">:</g:if>
				<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 80" field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}" />
		<tds:clrRowDetail style="order: 85" field="${standardFieldSpecs.license}" value="${asset.license}" />
		<tds:clrRowDetail style="order: 90" field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}" />
		<tds:clrRowDetail style="order: 95" field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}" />
		<tds:clrRowDetail style="order: 100" field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" />
		<tr style="order: 105">
			<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
			<td>{{ '${asset?.retireDate}' | tdsDate: userDateFormat }}</td>
		</tr>
		<tds:clrRowDetail style="order: 110" field="${standardFieldSpecs.validation}" value="${asset.validation}" />
		<tds:clrRowDetail style="order: 115" field="${standardFieldSpecs.testProc}" value="${asset.testProc}" />
		<tr style="order: 120">
			<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
			<td>
				{{ '${asset?.maintExpDate}' | tdsDate: userDateFormat }}
			</td>
		</tr>
		<tr style="order: 125">
			<tds:clrInputLabel field="${standardFieldSpecs.latency}" value="${asset.latency}"/>
			<td>${asset.latency == '?' ? '' : asset.latency}</td>
		</tr>
		<tr style="order: 130">
			<tds:clrInputLabel field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}"/>
			<td>${asset.startupProc == '?' ? '' : asset.startupProc}</td>
		</tr>
		<tr style="order: 135">
			<tds:clrInputLabel field="${standardFieldSpecs.url}" value="${asset.url}"/>
			<td><tds:textAsLink  text="${asset.url}" target="_new"/></td>
		</tr>
		<tds:clrRowDetail style="order: 140" field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />
		<tr style="order: 145">
			<tds:clrInputLabel field="${standardFieldSpecs.shutdownBy}" value="${asset.shutdownBy}"/>
			<td nowrap="nowrap">
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
		<tds:clrRowDetail style="order: 150" field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}" />
		<tr style="order: 155">
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
		<tds:clrRowDetail style="order: 160" field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}" />
		<tr style="order: 165">
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
		<tds:clrRowDetail style="order: 170" field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}" />
		<g:render template="/angular/common/customShow" model="[asset:asset,customs:customs]"></g:render>
	</tbody>
</table>

<g:render template="/angular/common/assetTags"></g:render>

	

