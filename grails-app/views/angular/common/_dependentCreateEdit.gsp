<%@page import="com.tdsops.tm.enums.domain.AssetClass" %>

<td valign="top" colspan="2">
<%-- Supports Block --%>
<kendo-grid
		*ngIf="dataGridSupportsOnHelper"
		[data]="dataGridSupportsOnHelper.gridData"
		[sort]="dataGridSupportsOnHelper.state.sort"
		[sortable]="{mode:'single'}"
		(pageChange)="dataGridSupportsOnHelper.pageChange($event)"
		[resizable]="true"
		(sortChange)="dataGridSupportsOnHelper.sortChange($event)">

	<!-- Toolbar Template -->
	<ng-template kendoGridToolbarTemplate [position]="'top'">
		<label class="pad-top-7 pad-left-10">Supports On</label>
		<button (click)="onAddSupportsOn()" class="btn btn-default float-right mar-right-15 pad-top-6" type="button">
			<i class="fa fa-fw fa-plus-circle"></i> Add Suport
		</button>
	</ng-template>

	<!-- Columns -->
	<kendo-grid-column *ngFor="let column of supportOnColumnModel.columns"
					   field="{{column.property}}"
					   [headerClass]="column.headerClass ? column.headerClass : ''"
					   [headerStyle]="column.headerStyle ? column.headerStyle : ''"
					   [class]="column.cellClass ? column.cellClass : ''"
					   [style]="column.cellStyle ? column.cellStyle : ''"
					   [width]="!column.width ? COLUMN_MIN_WIDTH : column.width">

		<!-- Header Template -->
		<ng-template kendoGridHeaderTemplate>
			<label>{{column.label}}</label>
		</ng-template>

		<!-- Action -->
		<ng-template kendoGridCellTemplate *ngIf="column.type === 'action'" let-dataItem>
			<div class="k-grid-ignore-click" style="cursor: default;">
				<button *ngIf="dataItem.status.code === 'PENDING'"
						(click)="onPlayButton(dataItem)"
						class="btn btn-action btn-default"
						title="Delete">
					<i class="fa fa-play-circle-o"></i>
				</button>
			</div>
		</ng-template>

		<ng-template kendoGridCellTemplate *ngIf="column.property === 'dataFlowFreq'" let-dataItem let-rowIndex="rowIndex">
			<kendo-dropdownlist
					name="{{column.property + columnIndex + rowIndex}}" class="form-control" style="width: 100%;"
					[data]="dataFlowFreqList"
					[(ngModel)]="dataItem.context"
					required>
			</kendo-dropdownlist>
		</ng-template>

	</kendo-grid-column>
</kendo-grid>

</td>

<%-- Supports Block --%>
<td valign="top" colspan="2">
	<div style="width: auto;" >
		<span style="float: left;"><h1>Supports:</h1></span>
		<span><input type='button' class="addDepButton" value='Add' onclick="EntityCrud.addAssetDependencyRow('support');"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Bundle</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>

			<tbody id="supportList">
			<g:each in="${supportAssets}" var="support" status="i">
				<g:set var="type" value="${ AssetClass.getClassOptionForAsset(support?.asset) }"></g:set>
				<tr id='row_s_${i}_${support.id}'>
					<td><g:select name="dataFlowFreq_support_${support.id}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>

					<%-- Class --%>
					<td class="class-wrap-depend">
						<g:select name="entity_support_${support.id}" 
							id="entity_support_${support.id}" 
							onChange="EntityCrud.updateDependentAssetNameSelect(this.name)" 
							from="${assetClassOptions.entrySet()}" optionKey="key" optionValue="value"
							value="${AssetClass.getClassOptionForAsset(support?.asset)}"
						></g:select>
					</td>

					<%-- Support Asset Name Select2 control --%>
					<td id="assetListSupportTdId_${i}"  class='combo-td'>
						<input type="hidden" 
							id="asset_support_${support.id}"
							name="asset_support_${support.id}" 
							class="scrollSelect" 
							style="width:150px" 
							value="${support?.asset?.id}" 
							data-asset-id="${support?.asset?.id}" 
							data-asset-name="${support?.asset?.assetName}" 
							data-asset-type="${type}"
							data-slide="deps"  
							onchange="EntityCrud.updateDependentBundle(this.value,this.name,'${assetEntity?.moveBundle?.id}')"
						/>
					</td>


					<%-- Bundle select --%>
					<td class='combo-td'>
						<g:set var="supportBundle" value="${support?.asset?.moveBundle}"></g:set>
						<%-- Used to show bundle colors based on bundleConflicts --%>
						<g:if test="${supportBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
									optionKey="id" optionValue="name" style="background-color: red" onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')"/>
						</g:if>
						<g:elseif test="${supportBundle!=assetEntity.moveBundle }" >
							<g:select from="${moveBundleList}" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
								optionKey="id" optionValue="name" onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')"
								class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status } depBundle"
							></g:select>
						</g:elseif>
						<g:else>
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_support_${support.id}" value="${supportBundle?.id}" 
								optionKey="id" optionValue="name" onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity?.moveBundle?.id}','')"
								class="dep-${(support.status != 'Questioned' && support.status != 'Validated') ? 'Unknown' : support.status } depBundle">
							</g:select>	
						</g:else>
					</td>

					<%-- Dependency Type --%>
					<td nowrap>
						<g:select name="type_support_${support.id}" value="${support.type}" from="${dependencyType}" optionValue="value" />
						<g:render template="../assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'edit']"></g:render>
					</td>

					<%-- Status --%>
					<td>
						<g:select name="status_support_${support.id}" value="${support.status}" from="${dependencyStatus}" 
							optionValue="value" onchange="EntityCrud.changeDependentBundleColor(this.name,'','${assetEntity.moveBundle?.id}',this.value)"/>
					</td>

					<%-- Delete Row Icon --%>
					<td>
						<a href="javascript:EntityCrud.deleteAssetDependencyRow('row_s_${i}_${support.id}', '${whom}_supportAddedId')"><span class='clear_filter'>X</span></a>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>

<%-- Depends Block --%>
	<div style="width: auto;">
		<span style="float: left;"><h1>Is dependent on:&nbsp;&nbsp;</h1></span>
		<span><input type='button' class="addDepButton" value='Add' onclick="EntityCrud.addAssetDependencyRow('dependent');"></span>
		<br/>
		<table style="width: 100%;">
			<thead>
				<tr>
					<th>Frequency</th>
					<th>Class</th>
					<th>Name</th>
					<th>Bundle</th>
					<th>Type</th>
					<th>Status</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody id="dependentList">
			<g:each in="${dependentAssets}" var="dependent" status="i">
				<g:set var="type" value="${ AssetClass.getClassOptionForAsset(dependent?.dependent) }"></g:set>
				<tr id='row_d_${i}_${dependent.id}'>
					<%-- Dataflow --%>
					<td>
						<g:select name="dataFlowFreq_dependent_${dependent.id}" 
							value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}"
						></g:select>
					</td>

					<%-- Class option --%>
					<td class="class-wrap-depend">
						<g:select name="entity_dependent_${dependent.id}" 
							id="entity_dependent_${dependent.id}" 
							onChange="EntityCrud.updateDependentAssetNameSelect(this.name)" 
							from="${assetClassOptions.entrySet()}" optionKey="key" optionValue="value"
							value="${AssetClass.getClassOptionForAsset(dependent?.dependent)}"
						></g:select>
					</td>

					<%-- Dependent Asset Name Select2 controller --%>
					<td id="assetListDependentTdId_${i}"  class='combo-td'>
						<input type="hidden" 
							id="asset_dependent_${dependent.id}"
							name="asset_dependent_${dependent.id}"
							class="scrollSelect" 
							style="width:150px" 
							value="${dependent?.dependent?.id}"
							data-asset-id="${dependent?.dependent?.id}" 
							data-asset-name="${dependent?.dependent?.assetName}" 
							data-asset-type="${type}"
							data-slide="deps" 
							onchange="EntityCrud.updateDependentBundle(this.value, this.name, '${assetEntity?.moveBundle?.id}')" 
						/>
					</td>

					<%-- Move Bundle Select --%>
					<td class='combo-td'>
						<g:set var="depBundle" value="${dependent?.dependent?.moveBundle}"></g:set>
						<%--Used to show bundle colors based on bundleConflicts--%>
						<g:if test="${depBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" optionKey="id" 
							optionValue="name" style="background-color: red" onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"/>
						</g:if>
						<g:elseif test="${depBundle!=assetEntity.moveBundle }" >
							<g:select from="${moveBundleList}" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" 
								optionKey="id" optionValue="name" 
								class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status } depBundle"
								onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"
							></g:select>
						</g:elseif>
						<g:else>
							<g:select from="${moveBundleList}" class="depBundle" name="moveBundle_dependent_${dependent.id}" value="${depBundle?.id}" 
								optionKey="id" optionValue="name" 
								class="dep-${(dependent.status != 'Questioned' && dependent.status != 'Validated') ? 'Unknown' : dependent.status } depBundle"
								onchange="EntityCrud.changeDependentBundleColor(this.name,this.value,'${assetEntity.moveBundle?.id}','')"
							></g:select>
						</g:else>
					</td>

					<%-- Dependency Type --%>
					<td nowrap>
						<g:select name="type_dependent_${dependent.id}" value="${dependent.type}" from="${dependencyType}" optionValue="value"/>
						<g:render template="../assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'edit']"></g:render>
					</td>

					<%-- Dependency Status --%>
					<td>
						<g:select name="status_dependent_${dependent.id}" value="${dependent.status}" from="${dependencyStatus}" 
							optionValue="value" onchange="EntityCrud.changeDependentBundleColor(this.name,'','${assetEntity.moveBundle?.id}',this.value)"/>
					</td>

					<%-- Delete Row Icon --%>
					<td>
						<a href="javascript:EntityCrud.deleteAssetDependencyRow('row_d_${i}_${dependent.id}', '${whom}_dependentAddedId')"><span class='clear_filter'>X</span></a>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</td>