import { Component, Input, ViewChild, ElementRef, AfterViewInit, OnChanges, SimpleChanges } from '@angular/core';

import { JqueryKendoGridModel } from './jquery-kendo-grid.model';

declare var jQuery: any;

@Component({
    moduleId: module.id,
    selector: 'jquery-kendo-grid',
    template: `<div #grid><ng-content select="jquery-kendo-grid-column"></ng-content></div>`,
    exportAs: 'JqueryKendoGrid',
    providers: [JqueryKendoGridModel]
})
export class JqueryKendoGridComponent implements AfterViewInit, OnChanges {
    @ViewChild('grid') el: ElementRef;

    @Input() pageable: any;
    @Input() sortable: any;
    @Input() sort: any;
    @Input('page-size') pageSize: number;
    @Input() data: Array<any>;
    @Input() columns: Array<any>;

    kendoGrid: any;

    constructor(private model: JqueryKendoGridModel) { }

    ngAfterViewInit(): void {
        this.model.pageable = this.pageable;
        this.model.sortable = this.sortable;
        this.model.columns = this.model.columns.concat(this.columns || []);
        this.model.dataSource = {
            pageSize: this.pageSize,
            transport: {
                read: e => e.success(this.data ? this.data : [])
            },
            sort: this.sort
        };
        this.initializeKendoGrid(this.model);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.data.currentValue) {
            this.refresh();
        }
    }

    private initializeKendoGrid(config: any): void {
        this.kendoGrid = jQuery(this.el.nativeElement).kendoGrid(config).data('kendoGrid');
    }

    refresh(): void {
        this.kendoGrid.dataSource.read();
    }
}