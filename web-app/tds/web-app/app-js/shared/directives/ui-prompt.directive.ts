/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {
	Component, OnDestroy, AfterViewInit, Injectable
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
	title: string;
	message: string;
	confirmLabel: string;
	cancelLabel: string;
	tdsUiPrompt: any;

	resolve: any;
	reject: any;

	openNotifier: any;

	constructor(private notifierService: NotifierService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		this.tdsUiPrompt = jQuery('#tdsUiPrompt');
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
		this.tdsUiPrompt.modal('hide');
		this.resolve(false);
	}

	protected dismiss(): void {
		this.tdsUiPrompt.modal('hide');
		this.reject();
	}

	protected confirm(): void {
		this.tdsUiPrompt.modal('hide');
		this.resolve(true);
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