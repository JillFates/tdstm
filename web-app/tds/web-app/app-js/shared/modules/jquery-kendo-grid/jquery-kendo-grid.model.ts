import { Injectable } from '@angular/core';

@Injectable()
export class JqueryKendoGridModel {
    columns: Array<any>;
    pageable: any;
    sortable: any;
    dataSource: any;

    constructor() {
        this.columns = [];
    }
}
