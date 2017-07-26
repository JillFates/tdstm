/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import { Component, Injectable } from '@angular/core';
import { NotifierService } from '../services/notifier.service';
import { UILoaderService } from '../services/ui-loader.service';

@Component({
	selector: 'tds-ui-loader',
	template: '<div id="main-loader" *ngIf="loaderConfig.show"><div id="loader-icon"><div class="loader"></div></div></div>'
})

export class UILoaderDirective {

	private loaderConfig: any;

	constructor(private notifierService: NotifierService, private loaderService: UILoaderService) {
		this.httpRequestHandlerInitial();
		this.httpRequestHandlerCompleted();
		this.loaderConfig = this.loaderService.loaderConfig;
	}

	isShowing(): boolean {
		return this.loaderConfig.show;
	}

	httpRequestHandlerInitial() {
		this.notifierService.on('httpRequestInitial', (event) => {
			this.loaderService.show();
		});
	}

	httpRequestHandlerCompleted() {
		this.notifierService.on('httpRequestCompleted', (event) => {
			this.loaderService.hide();
		});
	}

}