/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import { Component} from '@angular/core';
import {NotifierService} from "../services/notifier.service";

@Component({
    selector: 'tds-ui-toast',
    template: ''
})

export class UIToastDirective {

    private showsLoader: boolean = false;

    constructor(private notifierService: NotifierService) {
        this.errorFailure();
    }

    errorFailure() {
        this.notifierService.on('errorFailure', (event) => {
            this.showsLoader = true;
        });
    }

}