/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import { Component } from '@angular/core';
import { NotifierService } from '../services/notifier.service';

@Component({
    selector: 'tds-ui-loader',
    template: '<div id="main-loader" *ngIf="showsLoader"><div id="loader-icon"><div class="loader"></div></div></div>'
})

export class UILoaderDirective {

    private showsLoader = false;

    constructor(private notifierService: NotifierService) {
        this.httpRequestHandlerInitial();
        this.httpRequestHandlerCompleted();
    }

    isShowing(): boolean {
        return this.showsLoader;
    }

    httpRequestHandlerInitial() {
        this.notifierService.on('httpRequestInitial', (event) => {
            this.showsLoader = true;
        });
    }

    httpRequestHandlerCompleted() {
        this.notifierService.on('httpRequestCompleted', (event) => {
            this.showsLoader = false;
        });
    }

}