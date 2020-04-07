<%@ page defaultCodec="html" %>
<td valign="top">
    <div class="view-dependencies">
        <kendo-grid
                class="dependents-grid"
                [data]="gridSupportsData"
                [pageSize]="supportsSate.take"
                [skip]="supportsSate.skip"
                [sort]="supportsSate.sort"
                [filter]="supportsSate.filter"
                [sortable]="true"
                [pageable]="{buttonCount: 5, info: true, pageSizes: [25, 50, 100]}"
                [filterable]="true"
                (dataStateChange)="dataSupportStateChange($event)"
            >
            <!-- Toolbar Template -->
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Supports</label>
            </ng-template>
            <kendo-grid-column field="assetClass" title="Class" width="80" >
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="name" title="Name">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="moveBundle" title="Bundle">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="type" title="Type">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="status" title="Status">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
        </kendo-grid>
    </div>
</td>
<td valign="top">
    <div class="view-dependencies">
        <kendo-grid
            class="dependents-grid"
            [data]="gridDependenciesData"
            [pageSize]="dependenciesState.take"
            [skip]="dependenciesState.skip"
            [sort]="dependenciesState.sort"
            [filter]="dependenciesState.filter"
            [sortable]="true"
            [pageable]="{buttonCount: 5, info: true, pageSizes: [25, 50, 100]}"
            [filterable]="true"
            (dataStateChange)="dataDependenciesStateChange($event)"
        >
            <ng-template kendoGridToolbarTemplate [position]="'top'">
                <label class="pad-top-2 pad-left-10 mar-bottom-3">Is Dependent On </label>
            </ng-template>
            <kendo-grid-column field="assetClass" title="Class" width="80">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="name" title="Name">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="moveBundle" title="Bundle">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="type" title="Type">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
            <kendo-grid-column field="status" title="Status">
                <ng-template kendoGridFilterCellTemplate let-filter let-column="column">
                    <kendo-grid-string-filter-cell [showOperators]="false" [column]="column" [filter]="filter"> </kendo-grid-string-filter-cell>
                </ng-template>
            </kendo-grid-column>
        </kendo-grid>
    </div>
</td>
