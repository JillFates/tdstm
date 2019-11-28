/**
 * UI Loader is a tool that helps to show a loader indicator on any action performed
 * idea is to attach to request call or even a simple action
 * the directive is the one who is being injected in the UI
 * however a implemented service will be in charge of passing the emitter to this directive
 */

import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { NotifierService } from '../services/notifier.service';
import { AlertType, AlertModel } from '../model/alert.model';
import { KEYSTROKE } from '../model/constants';

@Component({
	selector: 'tds-ui-toast',
	template: `
		<div class="message-wrapper">
			<div
				class="message-wrapper-container"
				*ngIf="showsPopUp && !disabledPopUp"
			>
				<div
					class="alert alert-dismissable fadeIn"
					[ngClass]="{
						'alert-danger':
							alertModel.alertType === alertType.DANGER,
						'alert-warning':
							alertModel.alertType === alertType.WARNING,
						'alert-success':
							alertModel.alertType === alertType.SUCCESS,
						'alert-info': alertModel.alertType === alertType.INFO
					}"
					[ngSwitch]="alertModel.alertType"
				>
					<i *ngSwitchCase="alertType.DANGER" class="icon fa fa-ban">
					</i>

					<i
						*ngSwitchCase="alertType.WARNING"
						class="icon fa fa-warning"
					>
					</i>

					<i
						*ngSwitchCase="alertType.SUCCESS"
						class="icon fa fa-check"
					>
					</i>

					<i *ngSwitchCase="alertType.INFO" class="icon fa fa-info">
					</i>

					<span *ngSwitchDefault></span>

					<h5>
						{{ alertModel.message }}
					</h5>
					<button
						#closePopUpDialog
						type="button"
						class="close"
						(click)="onCloseDialog()"
						aria-hidden="true"
					>
						<clr-icon shape="times"></clr-icon>
					</button>
				</div>
			</div>
		</div>
	`,
})
export class UIToastDirective {
	@ViewChild('closePopUpDialog', { static: false })
	closePopUpDialog: ElementRef;
	protected alertModel = AlertModel;
	protected alertType = AlertType;

	public showsPopUp = false;
	public disabledPopUp = false;

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
		setTimeout(() => {
			if (this.closePopUpDialog) {
				this.closePopUpDialog.nativeElement.focus();
			}
		}, 300);
	}

	eventListeners() {
		// TODO: Use state management for this
		this.notifierService.on('alertTypeDisable', (event: any) => {
			this.disabledPopUp = event.disable;
		});
		this.notifierService.on(AlertType.DANGER, event => {
			this.showPopUp(AlertType.DANGER, event.message);
		});
		this.notifierService.on(AlertType.DANGER, event => {
			this.showPopUp(AlertType.DANGER, event.message);
		});
		this.notifierService.on(AlertType.WARNING, event => {
			this.showPopUp(AlertType.WARNING, event.message);
		});
		this.notifierService.on(AlertType.SUCCESS, event => {
			this.showPopUp(AlertType.SUCCESS, event.message);
		});
		this.notifierService.on(AlertType.INFO, event => {
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

	/**
	 * Detect if the use has pressed the on Escape to close the popup.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(
		event: KeyboardEvent
	) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.hidePopUp();
		}
	}
}
