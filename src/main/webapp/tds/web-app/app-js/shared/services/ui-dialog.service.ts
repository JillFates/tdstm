/**
 * UI Dialog Service its a service to open a component as Dialog.
 * UI Active Dialog its a singleton intance of the current opened dialog and provide the way to close it and access
 * its component
 */
import {Injectable, ComponentRef, HostListener, OnDestroy} from '@angular/core';

import { NotifierService } from './notifier.service';
import {MODAL_SIZE, DEFAULT_DIALOG_SIZE, DEFAULT_ENABLE_ESC} from '../model/constants';

@Injectable()
export class UIDialogService {
	constructor(private notifier: NotifierService) {

	}

	/**
	 * Method to open a dialog, returns a Promise that gonna be resolved ou rejected based on the UIActiveDialog Action
	 * @param component
	 * @param {Array<any>} params
	 * @param {MODAL_SIZE} size
	 * @param {boolean} enableEsc
	 * @returns {Promise<any>}
	 */
	open(
		component: any, params: Array<any>, size: MODAL_SIZE = DEFAULT_DIALOG_SIZE, enableEsc = DEFAULT_ENABLE_ESC): Promise<any> {
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

	extra(component: any, params: Array<any>, enableEsc = DEFAULT_ENABLE_ESC, draggable = false): Promise<any> {
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

	replace(component: any, params: Array<any>, size: MODAL_SIZE = DEFAULT_DIALOG_SIZE): Promise<any> {
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

export class UIExtraDialog implements OnDestroy {
	modalIntance: any;
	resolve: any;
	reject: any;
	cmpRef: ComponentRef<{}>;
	private enableEsc = false;
	private currentActiveModalDivIndex = 0;

	constructor(private modalSelector: string) {}

	ngOnDestroy(): void {
		this.processMultipleDialogBackgrounds(true);
	}

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
		this.dismiss();
	}

	open(resolve, reject, comp: ComponentRef<{}>, enableEsc: boolean, draggable: boolean) {
		this.resolve = resolve;
		this.reject = reject;
		this.cmpRef = comp;
		this.modalIntance = jQuery(this.modalSelector);
		this.enableEsc = enableEsc;
		// enable/disable exit on ESCAPE key
		this.modalIntance.modal({
			keyboard: this.enableEsc
		}).modal('show');
		// make it draggable
		if (draggable) {
			this.modalIntance.draggable({
				handle: '.modal-header',
				containment: 'window'
			});
		}
		this.processMultipleDialogBackgrounds();
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

	/**
	 * Fixes Darker Background Issues when opening multiple dialogs.
	 */
	private processMultipleDialogBackgrounds(closingDialog?: boolean): void {
		let divs = jQuery('tds-ui-dialog div.modal.fade.in');
		let index = 1;
		for (let div of divs) {
			if (!closingDialog) {
				this.currentActiveModalDivIndex = index;
				jQuery(div).addClass('no-background');
				if (index === divs.length) {
					jQuery(div).removeClass('no-background');
				}
			} else if (index === this.currentActiveModalDivIndex - 1) {
				jQuery(div).removeClass('no-background');
			}
			index++;
		}
	}
}
