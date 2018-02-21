/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import {Component, Injectable} from '@angular/core';
import {NotifierService} from '../services/notifier.service';
import {UILoaderService} from '../services/ui-loader.service';
import {LOADER_IDLE_PERIOD} from '../model/constants';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-loader',
	template: '<div id="main-loader" *ngIf="loaderConfig.show"><div id="loader-icon"><div class="loader"></div></div></div>'
})

export class UILoaderDirective {

	private loaderConfig: any;

	constructor(private notifierService: NotifierService, private loaderService: UILoaderService) {
		this.httpRequestHandlerInitial();
		this.httpRequestHandlerCompleted();
		this.httpRequestHandlerCompletedWithErrors();
		this.loaderConfig = this.loaderService.loaderConfig;
	}

	isShowing(): boolean {
		return this.loaderConfig.show;
	}

	isInProgress(): boolean {
		return this.loaderConfig.inProgress;
	}

	httpRequestHandlerInitial() {
		this.notifierService.on('httpRequestInitial', (event) => {
			this.loaderService.initProgress();
			// Reduce the blackout of several calls and show only after LOADER_IDLE_PERIOD
			if (!this.isShowing()) {
				setTimeout(() => {
					if (this.isInProgress()) {
						this.loaderService.show();
					}
				}, LOADER_IDLE_PERIOD);
			}
		});
	}

	httpRequestHandlerCompleted() {
		this.notifierService.on('httpRequestCompleted', (event) => {
			this.loaderService.stopProgress();
			this.loaderService.hide();
		});
	}

	/**
	 * When the server finish with an error, we should put the app in a stable state
	 * so the user can continue working
	 */
	httpRequestHandlerCompletedWithErrors() {
		this.notifierService.on('httpRequestHandlerCompletedWithErrors', (event) => {
			this.loaderService.stopProgress();
			this.loaderService.hide();
			// TM-9413 - An Error should not close a dialog
			/* this.notifierService.broadcast({
				name: 'dialog.dismiss',
			}); */
		});
	}

}