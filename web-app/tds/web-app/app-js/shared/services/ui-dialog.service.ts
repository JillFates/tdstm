/**
 * UI Dialog Service its a service to open a component as Dialog.
 * UI Active Dialog its a singleton intance of the current opened dialog and provide the way to close it and access
 * its component
 */
import {Injectable, ComponentRef, HostListener, AfterViewInit, OnInit} from '@angular/core';
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
	open(component: any, params: Array<any>, size: 'sm' | 'md' | 'xlg' | 'lg' = 'md', enableEsc = false): Promise<any> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'dialog.open',
				component: component,
				size: size,
				params: params,
				resolve: resolve,
				reject: reject,
				escape: enableEsc
			});
		});
	}

	extra(component: any, params: Array<any>, enableEsc = false, draggable = false): Promise<any> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'dialog.extra',
				component: component,
				params: params,
				resolve: resolve,
				reject: reject,
				enableEsc: enableEsc,
				draggable: draggable
			});
		});
	}

	replace(component: any, params: Array<any>, size: 'sm' | 'md' | 'xlg' | 'lg' = 'md'): Promise<any> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'dialog.replace',
				component: component,
				size: size,
				params: params
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

declare var jQuery: any;
const ESCAPE_KEYCODE = 27;

export class UIExtraDialog {
	modalIntance: any;
	resolve: any;
	reject: any;
	cmpRef: ComponentRef<{}>;
	private enableEsc = false;

	constructor(private modalSelector: string) {}

	@HostListener('document:keydown', ['$event'])
	handleKeyboardEvent(event: KeyboardEvent) {
		if (event.keyCode === ESCAPE_KEYCODE && this.enableEsc) {
			this.onEscKeyPressed();
		}
	}

	/**
	 * This function should be overrided by child class if needed to perform specific actions.
	 */
	onEscKeyPressed(): void {
		// override if needed
	}

	open(resolve, reject, comp: ComponentRef<{}>, enableEsc: boolean, draggable: boolean) {
		this.resolve = resolve;
		this.reject = reject;
		this.cmpRef = comp;
		this.modalIntance = jQuery(this.modalSelector);
		this.enableEsc = enableEsc;
		// enable/disable exit on ESCAPE key
		this.modalIntance.modal({
			keyboard: !enableEsc
		}).modal('show');
		// make it draggable
		if (draggable) {
			this.modalIntance.draggable({
				handle: '.modal-header'
			});
		}
	}

	close(value?: any) {
		this.modalIntance.modal('hide');
		this.resolve(value);
		this.cmpRef.destroy();
	}

	dismiss(value?: any) {
		this.modalIntance.modal('hide');
		this.reject(value);
		this.cmpRef.destroy();
	}

}
