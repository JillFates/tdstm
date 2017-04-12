import { Component, Input, OnInit } from '@angular/core';

import { JqueryKendoGridModel } from './jquery-kendo-grid.model';

declare var jQuery: any;
declare var kendo: any;
@Component({
    moduleId: module.id,
    selector: 'jquery-kendo-grid-column',
    template: '',
    exportAs: 'JqueryKendoGridColumn'
})
export class JqueryKendoGridColumnComponent implements OnInit {
    @Input() title: string;
    @Input() field: string;
    @Input() width: number;
    @Input() hidden: boolean;
    @Input() template: string;

    constructor(private model: JqueryKendoGridModel) { }

    ngOnInit(): void {
        this.model.columns.push({
            title: this.title,
            field: this.field,
            width: this.width,
            hidden: this.hidden,
            template: this.template ? kendo.template(this.template) : null
        });
    }
}