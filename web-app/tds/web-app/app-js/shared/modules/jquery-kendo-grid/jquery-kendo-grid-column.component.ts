import { Component, Input, ViewChild, OnInit } from '@angular/core';

import { JqueryKendoGridService } from './jquery-kendo-grid.service';

@Component({
    moduleId: module.id,
    selector: 'jquery-kendo-grid-column',
    template: '',
    exportAs: 'JqueryKendoGridColumn',
})
export class JqueryKendoGridColumnComponent implements OnInit {
    @Input() title: string;
    @Input() field: string;
    @Input() width: number;
    @Input() hidden: boolean;
    @Input() template: string;

    constructor(private gridService: JqueryKendoGridService) { }

    ngOnInit(): void {
        this.gridService.model.columns.push({
            title: this.title,
            field: this.field,
            width: this.width,
            hidden: this.hidden,
            template: this.template
        });
    }
}