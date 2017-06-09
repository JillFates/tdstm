/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import {Component} from '@angular/core';
import {NotifierService} from '../services/notifier.service';
import {AlertType, AlertModel} from '../model/alert.model';

@Component({
	selector: 'tds-ui-toast',
	templateUrl: '../tds/web-app/app-js/shared/directives/ui-toast.directive.html'
})

export class UIToastDirective {

	private showsPopUp = false;
	private alertModel = AlertModel;
	private alertType = AlertType;

	constructor(private notifierService: NotifierService) {
		this.eventListeners();
	}

	eventListeners() {
		this.notifierService.on(AlertType.DANGER, (event) => {
			this.showsPopUp = true;
			this.alertModel.alertType = this.alertType.DANGER;
			this.alertModel.message = event.message;
		});
	}

	onCloseDialog() {
		this.alertModel.alertType = this.alertType.EMPTY;
		this.alertModel.message = '';
	}

}