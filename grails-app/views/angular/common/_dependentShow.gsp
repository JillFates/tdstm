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
                    <div class="has-feedback" style="margin-bottom:0px;">
                        <div *ngIf="column.property !== 'action'">
                            <input type="text" (keyup)="gridSupportsData.onFilter(column)" class="form-control"
                                   name="{{column.property}}" [(ngModel)]="column.filter"
                                   placeholder="Filter" value="">
                            <span *ngIf="column.filter" (click)="gridSupportsData.clearValue(column)"
                                  style="cursor:pointer;color:#656565;pointer-events:all"
                                  class="fa fa-times form-control-feedback" aria-hidden="true"></span>
                        </div>
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
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Supports:</label>
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
                    <div class="has-feedback" style="margin-bottom:0px;">
                        <div *ngIf="column.property !== 'action'">
                            <input type="text" (keyup)="gridDependenciesData.onFilter(column)" class="form-control"
                                   name="{{column.property}}" [(ngModel)]="column.filter"
                                   placeholder="Filter" value="">
                            <span *ngIf="column.filter" (click)="gridDependenciesData.clearValue(column)"
                                  style="line-height: 28px;cursor:pointer;color:#656565;pointer-events:all"
                                  class="fa fa-times form-control-feedback" aria-hidden="true"></span>
                        </div>
                    </div>
                </ng-template>

            </kendo-grid-column>
        </kendo-grid>


    </div>
</td>
