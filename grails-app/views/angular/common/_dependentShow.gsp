<%@ page defaultCodec="html" %>
<td valign="top">
    <div class="view-dependencies">
        <kendo-grid
                class="dependents-grid"
                [pageSize]="gridDependenciesData.state.take"
                [skip]="gridSupportsData.state.skip"
                [filterable]="true"
                (pageChange)="gridSupportsData.pageChange($event)"
                [data]="gridSupportsData.gridData"
                [sort]="gridSupportsData.state.sort"
                [sortable]="false"
                [resizable]="true"
                (sortChange)="gridSupportsData.sortChange($event)"
                [pageable]="{buttonCount: 5, info: true, pageSizes: [25, 50, 100]}"
            >
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Supports:</label>
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

                <!-- Default Generic Filter Template -->
                <ng-template kendoGridFilterCellTemplate let-filter>
                    <input type="text" (keyup)="gridSupportsData.onFilter(column)" class="form-control"
                           name="{{column.property}}" [(ngModel)]="column.filter"
                           placeholder="Filter" value="">
                   <span *ngIf="column.filter" (click)="gridSupportsData.clearValue(column)" style="cursor:pointer;color:#656565;pointer-events:all;  margin-top: 3px;" class="fa fa-times form-control-feedback" aria-hidden="true"></span>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetType'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.assetType}}
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'name'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.name}}
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'moveBundle.name'" let-dataItem let-rowIndex="rowIndex">
                    <div style="background-color: lightpink;" *ngIf="dataItem.moveBundle.id != currentShowAsset?.moveBundleId && dataItem.status === 'Validated'" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.moveBundle.name}} <img src="/tdstm/assets/icons/error.png" border="0" title="The linked assets have conflicting bundles.">
                    </div>
                    <div *ngIf="dataItem.moveBundle.id != currentShowAsset?.moveBundleId; else normalBundleName" class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.moveBundle.name}} <img src="/tdstm/assets/icons/error.png" border="0" title="The linked assets have conflicting bundles.">
                    </div>
                    <ng-template #normalBundleName>
                        <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                            {{dataItem.moveBundle.name}}
                        </div>
                    </ng-template>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'type'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showDependencyView(dataItem.assetId, currentShowAsset.id)">
                        {{dataItem.type}}
                        <img *ngIf="dataItem.comment.length > 0" src="/tdstm/assets/icons/comment.png" border="0" title="{{dataItem.comment}}">
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showDependencyView(dataItem.assetId, currentShowAsset.id)">
                        {{dataItem.status}}
                    </div>
                </ng-template>

            </kendo-grid-column>
        </kendo-grid>
    </div>
</td>
<td valign="top">
    <div class="view-dependencies">
        <kendo-grid
                class="dependents-grid"
                [pageSize]="gridDependenciesData.state.take"
                [skip]="gridDependenciesData.state.skip"
                [filterable]="true"
                (pageChange)="gridDependenciesData.pageChange($event)"
                [data]="gridDependenciesData.gridData"
                [sort]="gridDependenciesData.state.sort"
                [sortable]="false"
                [resizable]="true"
                (sortChange)="gridDependenciesData.sortChange($event)"
                [pageable]="{buttonCount: 5, info: true, pageSizes: [25, 50, 100]}"
        >
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Is dependent on:</label>
            </ng-template>
            <!-- Columns -->
            <kendo-grid-column *ngFor="let column of dependentOnColumnModel.columns"
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

                <!-- Default Generic Filter Template -->
                <ng-template kendoGridFilterCellTemplate let-filter>
                    <input type="text" (keyup)="gridSupportsData.onFilter(column)" class="form-control"
                           name="{{column.property}}" [(ngModel)]="column.filter"
                           placeholder="Filter" value="">
                    <span *ngIf="column.filter" (click)="gridSupportsData.clearValue(column)" style="cursor:pointer;color:#656565;pointer-events:all;  margin-top: 3px;" class="fa fa-times form-control-feedback" aria-hidden="true"></span>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'assetType'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.assetType}}
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'name'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.name}}
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'moveBundle.name'" let-dataItem let-rowIndex="rowIndex">
                    <div style="background-color: lightpink;" *ngIf="dataItem.moveBundle.id != currentShowAsset?.moveBundleId && dataItem.status === 'Validated'" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.moveBundle.name}} <img src="/tdstm/assets/icons/error.png" border="0" title="The linked assets have conflicting bundles.">
                    </div>
                    <div *ngIf="dataItem.moveBundle.id != currentShowAsset?.moveBundleId; else normalBundleName" class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                        {{dataItem.moveBundle.name}} <img src="/tdstm/assets/icons/error.png" border="0" title="The linked assets have conflicting bundles.">
                    </div>
                    <ng-template #normalBundleName>
                        <div class="cell-template dep-{{dataItem.status}}" (click)="showAssetDetailView(dataItem.assetClass.toUpperCase(), dataItem.assetId)">
                            {{dataItem.moveBundle.name}}
                        </div>
                    </ng-template>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'type'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showDependencyView(currentShowAsset.id, dataItem.assetId)">
                        {{dataItem.type}}
                        <img *ngIf="dataItem.comment.length > 0" src="/tdstm/assets/icons/comment.png" border="0" title="{{dataItem.comment}}">
                    </div>
                </ng-template>

                <ng-template kendoGridCellTemplate *ngIf="column.property === 'status'" let-dataItem let-rowIndex="rowIndex">
                    <div class="cell-template dep-{{dataItem.status}}" (click)="showDependencyView(currentShowAsset.id, dataItem.assetId)">
                        {{dataItem.status}}
                    </div>
                </ng-template>

            </kendo-grid-column>
        </kendo-grid>


    </div>
</td>
