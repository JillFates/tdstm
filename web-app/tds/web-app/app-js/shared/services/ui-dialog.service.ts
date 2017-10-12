/**
 * UI Dialog Service its a service to open a component as Dialog.
 * UI Active Dialog its a singleton intance of the current opened dialog and provide the way to close it and access
 * its component
 */
import { Injectable, ComponentRef } from '@angular/core';
import { NotifierService } from './notifier.service';

@Injectable()
export class UIDialogService {
	constructor(private notifier: NotifierService) {

	}

	/**
	 * Method to open a dialog, returns a Promise that gonna be resolved ou rejected based on the UIActiveDialog Action
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 */
	open(component: any, params: Array<any>, size: 'sm' | 'md' | 'lg' = 'md'): Promise<any> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'dialog.open',
				component: component,
				size: size,
				params: params,
				resolve: resolve,
				reject: reject
			});
		});
	}
}

@Injectable()
export class UIActiveDialogService {
	componentInstance: ComponentRef<{}>;

	constructor(private notifier: NotifierService) {

	}

	/**
	 * Validate if componentInstance is defined
	 */
	isDialogOpen(): boolean {
		return this.componentInstance ? true : false;
	}

	/**
	 * Close the dialog and resolve the Promise
	 * @param value The value to be resolved by the Promise
	 */
	close(value?: any): void {
		if (this.isDialogOpen()) {
			this.notifier.broadcast({
				name: 'dialog.close',
				result: value
			});
		}
	}

	/**
	 * Dismiss the dialog and reject the promise
	 * @param value The value to be rejected by the Promise
	 */
	dismiss(value?: any): void {
		if (this.isDialogOpen()) {
			this.notifier.broadcast({
				name: 'dialog.dismiss',
				result: value
			});
		}
	}
}