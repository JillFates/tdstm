import { Injectable } from '@angular/core';

import { JqueryKendoGridModel } from './jquery-kendo-grid.model';

@Injectable()
export class JqueryKendoGridService {
    model: JqueryKendoGridModel;

    constructor() {
        this.model = new JqueryKendoGridModel();
    }
}