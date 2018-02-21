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

	/**
	 * Shows the message as a small popup in front of the user.
	 * @param alertType [WARNING, SUCCESS, DANGER, ERROR]
	 * @param message
	 */
	showPopUp(alertType, message) {
		this.showsPopUp = true;
		this.alertModel.alertType = alertType;
		this.alertModel.message = message;
	}

	eventListeners() {
		this.notifierService.on(AlertType.DANGER, (event) => {
			this.showPopUp(AlertType.DANGER, event.message);
		});
		this.notifierService.on(AlertType.WARNING, (event) => {
			this.showPopUp(AlertType.WARNING, event.message);
		});
		this.notifierService.on(AlertType.SUCCESS, (event) => {
			this.showPopUp(AlertType.SUCCESS, event.message);
		});
		this.notifierService.on(AlertType.INFO, (event) => {
			this.showPopUp(AlertType.INFO, event.message);
		});
	}

	/**
	 * Cleans the status and hide all reference to the popups
	 */
	hidePopUp() {
		this.showsPopUp = false;
		this.alertModel.alertType = this.alertType.EMPTY;
		this.alertModel.message = '';
	}

	onCloseDialog() {
		this.hidePopUp();
	}

}