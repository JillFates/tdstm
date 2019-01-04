/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {
	Component, ComponentRef, ElementRef,
	Input, ViewChild, ViewContainerRef,
	OnDestroy, AfterViewInit
} from '@angular/core';

import { NotifierService } from '../services/notifier.service';
import { UIActiveDialogService } from '../services/ui-dialog.service';
import { ComponentCreatorService } from '../services/component-creator.service';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-dialog',
	template: `
        <div class="modal fade" id="tdsUiDialog" data-backdrop="static"
            style="overflow-y: auto" role="dialog">
            <div class="modal-dialog modal-{{size}}" role="document" #modalDialog>
                <div class="modal-content">
                    <div #view></div>
                </div>
            </div>
        </div>
        <div #extraDialog></div>`,
	styles: [``]
})
export class UIDialogDirective implements OnDestroy, AfterViewInit {
	@Input('name') name: string;
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;
	@ViewChild('extraDialog', { read: ViewContainerRef }) extraDialog: ViewContainerRef;
	@ViewChild('modalDialog') el: ElementRef;
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

	constructor(private notifierService: NotifierService, private activeDialog: UIActiveDialogService, private compCreator: ComponentCreatorService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		this.tdsUiDialog = jQuery('#tdsUiDialog');
		this.tdsUiDialog.on('hide.bs.modal', () => {
			if (this.reject) {
				this.reject();
			}
		});
		jQuery(this.el.nativeElement).draggable({
			handle: '.modal-header',
			containment: 'window'
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
			this.tdsUiDialog.modal('hide');
			if (this.cmpRef) {
				this.cmpRef.destroy();
				this.reject('OTHER_DIALOG_OPENED');
			}
			this.size = event.size;
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);
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
				for (let m = 0; m <= modalDialogs.length; m++) {
					jQuery(modalDialogs[m]).css({
						'left': (jQuery(window).width() - jQuery(modalDialogs[m]).width()) / 2
					});
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
			if (this.cmpRef) {
				this.cmpRef.destroy();
				// Pass params to override the current opened dialog behavior
				this.size = event.size;

				this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);
				this.activeDialog.componentInstance = this.cmpRef;
			}
		});

		this.closeNotifier = this.notifierService.on('dialog.close', event => {
			if (this.cmpRef) {
				this.el.nativeElement.style.top = 'initial';
				this.el.nativeElement.style.left = 'initial';
				this.resolve(event.result);
				this.tdsUiDialog.modal('hide');
				this.cmpRef.destroy();
			}
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
