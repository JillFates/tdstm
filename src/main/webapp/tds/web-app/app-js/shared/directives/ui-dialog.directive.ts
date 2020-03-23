import { Subscription } from 'rxjs';
import { DialogService } from 'tds-component-library';
import { KEYSTROKE } from './../model/constants';
/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {
	Component, ComponentRef, ElementRef,
	Input, ViewChild, ViewContainerRef,
	OnDestroy, AfterViewInit
} from '@angular/core';

import { NotifierService } from '../services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../services/ui-dialog.service';
import { ComponentCreatorService } from '../services/component-creator.service';
import {ModalType} from '../model/constants';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-dialog',
	template: `
        <div class="modal fade" id="tdsUiDialog" data-backdrop="static"
            style="overflow-y: auto" role="dialog">
            <div class="modal-dialog modal-{{size}}" role="document" #modalDialog>
                <div class="tds-modal-content">
                    <div #view></div>
                </div>
            </div>
        </div>
        <div #extraDialog></div>`,
	styles: [``]
})
export class UIDialogDirective implements OnDestroy, AfterViewInit {
	@Input('name') name: string;
	@ViewChild('view', { read: ViewContainerRef, static: true }) view: ViewContainerRef;
	@ViewChild('extraDialog', { read: ViewContainerRef, static: true  }) extraDialog: ViewContainerRef;
	@ViewChild('modalDialog', {static: false}) el: ElementRef;
	keyboard = false;
	size = 'md';
	tdsUiDialog: any;

	cmpRef: ComponentRef<{}>; // Instance of the component

	resolve: any;
	reject: any;

	openNotifier: any;
	closeNotifier: any;
	dismissNotifier: any;
	replaceNotifier: any;
	extraNotifier: any;

	constructor(
		private notifierService: NotifierService,
		private activeDialog: UIActiveDialogService,
		private compCreator: ComponentCreatorService,
		private dialogService: UIDialogService,
		public mainDialogService: DialogService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		this.tdsUiDialog = jQuery('#tdsUiDialog');
		this.tdsUiDialog.on('hide.bs.modal', () => {
			if (this.reject) {
				this.reject();
			}
		});
	}

	/**
	 * Clear resources on destroy
	 */
	ngOnDestroy(): void {
		if (this.cmpRef) {
			this.tdsUiDialog.modal('hide');
			this.cmpRef.destroy();
		}
		this.openNotifier();
		this.closeNotifier();
		this.dismissNotifier();
		this.replaceNotifier();
		this.extraNotifier();
	}

	/**
	 * Register the listener to handle dialog events
	 */
	private registerListeners(): void {
		this.openNotifier = this.notifierService.on('dialog.open', event => {
			// make sure UI has no other open dialog
			if (this.tdsUiDialog) {
				this.tdsUiDialog.modal('hide');
			}
			if (this.cmpRef) {
				this.cmpRef.destroy();
				this.reject('OTHER_DIALOG_OPENED');
			}
			this.size = event.size;
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);
			if (event.modalType === ModalType.EDIT) {
				this.cmpRef['_component'].editMode = true;
			} else {
				this.cmpRef['_component'].editMode = false;
			}
			this.keyboard = event.escape;
			this.activeDialog.componentInstance = this.cmpRef;
			this.tdsUiDialog.data('bs.modal').options.keyboard = this.keyboard;
			this.tdsUiDialog.modal({
				keyboard: this.keyboard
			}).modal('show');
		});

		jQuery(window).on('show.bs.modal', (e) => {
			let modalDialog = jQuery(jQuery(e.target).find('.modal-dialog'));
			jQuery(modalDialog).css({
				'visibility': 'hidden !important'
			});
		});

		// Show the Dialog on the center of the screen
		jQuery(window).on('shown.bs.modal', (e) => {
			let modalDialog = jQuery(jQuery(e.target).find('.modal-dialog'));
			let isUIConfirm = jQuery(modalDialog).parent().parent().find('.tds-ui-prompt');
			jQuery(modalDialog).css({
				'display': 'block',
				'visibility': 'hidden'
			}).css({
				'top': ((isUIConfirm && isUIConfirm.length > 0) ? 100 : 30) + 'px',
				'left': (jQuery(window).width() - modalDialog.width()) / 2
			}).css({
				'visibility': 'visible'
			});
		});

		// hides completely the dialog so it can be calculated again
		jQuery(window).on('hidden.bs.modal', (e) => {
			let modalDialog = jQuery(jQuery(e.target).find('.modal-dialog'));
			jQuery(modalDialog).css({
				'display': 'none',
				'visibility': 'hidden',
				'width': ''
			});
		});

		// On resize the windows, recalculate the center position of all open dialog
		jQuery(window).resize(function () {
			let modalDialogs = jQuery(document).find('.modal-dialog');
			if (modalDialogs) {
				let valid = true;
				try {
					let dynamicContent = jQuery(jQuery(document).find('.modal-dialog').parent()[0])
					valid = !dynamicContent.hasClass('dynamic-host-component');
				} catch (e) {
					//
				}
				if (valid) {
					for (let m = 0; m <= modalDialogs.length; m++) {
						jQuery(modalDialogs[m]).css({
							'left': (jQuery(window).width() - jQuery(modalDialogs[m]).width()) / 2
						});
					}
				}
			}
		});

		this.extraNotifier = this.notifierService.on('dialog.extra', event => {
			const cmpRef = this.compCreator.insert(event.component, event.params, this.extraDialog);
			const instance = cmpRef.instance as any;
			instance.open(event.resolve, event.reject, cmpRef, event.enableEsc, event.draggable);
		});

		/**
		 * Listener to replace the current dialog for another, this is helpful for Asset since it is compiled on fly
		 * use dialog.open for a normal context.
		 * @type {() => void}
		 */
		this.replaceNotifier = this.notifierService.on('dialog.replace', event => {
			let componentExist = false;
			// Before to replace a dialog, we need to ensure it exist on the UI
			if (this.cmpRef) {
				if (this.cmpRef.location && this.cmpRef.location.nativeElement.localName) {
					componentExist = jQuery(this.cmpRef.location.nativeElement.localName).length > 0;
				}
			}

			if (componentExist) {
				this.cmpRef.destroy();
				// Pass params to override the current opened dialog behavior
				this.size = event.size;

				this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);
				this.activeDialog.componentInstance = this.cmpRef;
			} else {
				this.dialogService.open(event.component, event.params, event.size).finally(() => {
					// We destroy completely the instance and restore to the original layout
					this.cmpRef = undefined;
					setTimeout(() => {
						jQuery('.layout-top-nav').css('padding-right', 0);
					}, 400);
				});
			}
		});

		this.closeNotifier = this.notifierService.on('dialog.close', event => {			
			let subscr: Subscription;
			let acti = false;
			subscr = this.mainDialogService
				.activatedDropdown
				.subscribe(res => {
					console.log('res:', res);
					if (res === true) {
						console.log(event);
						event.name = 'dialog.open';
						subscr.unsubscribe();
						acti = true;
						return;
					} else {
						event.name = 'dialog.open';
						subscr.unsubscribe();
						acti = false;
					}
				});

			setTimeout(() => {
				if (acti === false) {
					if (this.cmpRef) {
						this.el.nativeElement.style.top = 'initial';
						this.el.nativeElement.style.left = 'initial';
						this.resolve(event.result);
						this.tdsUiDialog.modal('hide');
						this.cmpRef.destroy();
					}
				}				
			}, 1000);

		});

		this.dismissNotifier = this.notifierService.on('dialog.dismiss', event => {			
			if (this.cmpRef) {
				this.el.nativeElement.style.top = 'initial';
				this.el.nativeElement.style.left = 'initial';
				this.reject(event.result);
				this.tdsUiDialog.modal('hide');
				this.cmpRef.destroy();
			}
		});

	};
}
