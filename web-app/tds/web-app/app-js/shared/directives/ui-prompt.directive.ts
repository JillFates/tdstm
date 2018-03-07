/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {
	Component, OnDestroy, AfterViewInit, Injectable, HostListener
} from '@angular/core';

import { NotifierService } from '../services/notifier.service';
import { UIActiveDialogService } from '../services/ui-dialog.service';
declare var jQuery: any;

@Component({
	selector: 'tds-ui-prompt',
	templateUrl: '../tds/web-app/app-js/shared/directives/ui-prompt.directive.html',
	styles: [`
		.modal { background:none;}
	`]
})
export class UIPromptDirective implements OnDestroy, AfterViewInit {
	@HostListener('document:keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		// prevent press any key on this component
		if (this.tdsUiPrompt && this.tdsUiPrompt.length > 0) {
			if (this.tdsUiPrompt[0].id === (<HTMLTextAreaElement>event.target).id) {
				event.preventDefault();
				event.stopImmediatePropagation();
			}
		}
	}
	title: string;
	message: string;
	confirmLabel: string;
	cancelLabel: string;
	tdsUiPrompt: any;
	resolve: any;
	reject: any;
	canExit: boolean ;

	openNotifier: any;

	constructor(private notifierService: NotifierService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		this.tdsUiPrompt = jQuery('#tdsUiPrompt');
		this.tdsUiPrompt.on('hide.bs.modal', () => {
			if (this.canExit && this.reject) {
				this.reject();
			}
		});

		// prevent closing
		this.tdsUiPrompt.on('hide.bs.modal.prevent', (event) => {
			if (!this.canExit) {
				event.preventDefault();
				event.stopImmediatePropagation();
			}
		});

	}

	/**
	 * Clear resources on destroy
	 */
	ngOnDestroy(): void {
		this.tdsUiPrompt.modal('hide');
		this.openNotifier();
	}

	/**
	 * Register the listener to handle dialog events
	 */
	private registerListeners(): void {
		this.openNotifier = this.notifierService.on('prompt.open', event => {
			// make sure UI has no other open dialog
			this.canExit = false;
			this.tdsUiPrompt.modal('hide');
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.title = event.title || 'Default Title';
			this.message = event.message || 'Default Message';
			this.confirmLabel = event.confirmLabel || 'Yes';
			this.cancelLabel = event.cancelLabel || 'No';
			this.tdsUiPrompt.modal('show');
		});
	};

	protected cancel(): void {
		this.canExit = true;
		this.resolve(false);
		this.tdsUiPrompt.modal('hide');
	}

	protected dismiss(): void {
		this.canExit = true;
		this.reject();
		this.tdsUiPrompt.modal('hide');
	}

	protected confirm(): void {
		this.canExit = true;
		this.resolve(true);
		this.tdsUiPrompt.modal('hide');
	}
}

@Injectable()
export class UIPromptService {
	constructor(private notifier: NotifierService) {

	}

	/**
	 * Method to open a dialog, returns a Promise that gonna be resolved ou rejected based on the UIActiveDialog Action
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 */
	open(title: string, message: string, confirmLabel: string, cancelLabel: string): Promise<boolean> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'prompt.open',
				resolve: resolve,
				reject: reject,
				title: title,
				message: message,
				confirmLabel: confirmLabel,
				cancelLabel: cancelLabel
			});
		});
	}
}