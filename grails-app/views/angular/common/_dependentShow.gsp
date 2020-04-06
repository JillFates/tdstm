<%@ page defaultCodec="html" %>

    <kendo-grid
            class="dependents-grid"
            [data]="gridSupportsData"
            [pageSize]="supports.take"
            [skip]="supports.skip"
            [sort]="supports.sort"
            [filter]="supports.filter"
            [sortable]="true"
            [pageable]="true"
            [filterable]="true"
            (dataStateChange)="dataSupportStateChange($event)"
    >
    <kendo-grid-column field="asset.assetClass" title="Class" width="40" [filterable]="false"></kendo-grid-column>
    <kendo-grid-column field="asset.name" title="Name"></kendo-grid-column>
    <kendo-grid-column field="dependent.moveBundle" title="Bundle"></kendo-grid-column>
    <kendo-grid-column field="type" title="Type"></kendo-grid-column>
    <kendo-grid-column field="status" title="Status"></kendo-grid-column>
</kendo-grid>
