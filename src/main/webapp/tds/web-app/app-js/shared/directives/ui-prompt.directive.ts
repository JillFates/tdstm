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
	template: `
		<div class="modal fade tds-ui-prompt" id="tdsUiPrompt" data-backdrop="static" tabindex="-1" role="dialog">
			<div class="modal-dialog modal-sm">
				<div class="tds-modal-content with-box-shadow">
					<div class="modal-header">
						<button aria-label="Close" class="close" type="button" (click)="cancel()">
							<clr-icon aria-hidden="true" shape="close"></clr-icon>
						</button>
                        <h4 class="modal-title">{{title}}</h4>
                    </div>
                    <div class="modal-body">
                        <form name="dialogActionForm" role="form" data-toggle="validator" class="form-horizontal left-alignment ng-pristine ng-valid">
                            <div class="box-body">
                                <p>{{message}}</p>
                            </div>
                            <!-- /.box-body -->
                        </form>
                    </div>
                    <div class="modal-footer form-group-center">
                        <tds-button-confirm theme="primary" (click)="confirm()">{{ confirmLabel}}</tds-button-confirm>
						<tds-button-cancel (click)="cancel()"  data-dismiss="modal"></tds-button-cancel>
					</div>
				</div>
			</div>
		</div>
	`,
	styles: [`
        .modal {
            background: none;
        }
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
	hideIconButtons: boolean;

	openNotifier: any;

	private CANCEL_STR = 'cancel';
	private CONFIRM_STR = 'confirm';

	constructor(private notifierService: NotifierService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		const refControl = 'tdsUiPrompt';
		this.tdsUiPrompt = jQuery(`#${refControl}`);
		this.tdsUiPrompt.on('hide.bs.modal', (event) => {
			if (this.resolve && event.target.id === refControl) {
				this.resolve(false);
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
			this.tdsUiPrompt.modal('hide');
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.title = event.title || 'Default Title';
			this.message = event.message || 'Default Message';
			this.confirmLabel = event.confirmLabel || 'Yes';
			this.cancelLabel = event.cancelLabel || 'No';
			this.tdsUiPrompt.modal('show');
			// by default show the icons, unless they are explicitilly set to be hidden
			this.hideIconButtons = event.hideIconButtons === true;
		});
	};

	public cancel(): void {
		this.checkMultipleModals(this.CANCEL_STR);
		this.resolve(false);
		this.tdsUiPrompt.modal('hide');
	}

	public dismiss(): void {
		this.reject();
		this.tdsUiPrompt.modal('hide');
	}

	public confirm(): void {
		this.checkMultipleModals(this.CONFIRM_STR);
		this.resolve(true);
		this.tdsUiPrompt.modal('hide');
	}

	public checkMultipleModals(scenario) {
		switch (scenario) {
			case this.CANCEL_STR: {
				this.addModalClass();
				break;
			}
			case this.CONFIRM_STR: {
				setTimeout(() => {
					let modals = jQuery('div.modal.fade.in');
					const body = document.getElementsByTagName('body')[0];
					if (body && modals.length === 0) {
						body.classList.remove('modal-open');
						body.style.paddingRight = '0';
					} else if ( modals.length >= 1) {
						this.addModalClass();
					}
				}, 500);
				break;
			}
		}
	}

	addModalClass() {
		setTimeout(() => {
			const body = document.getElementsByTagName('body')[0];
			body.className += ' modal-open';
		}, 500);
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
	open(title: string, message: string, confirmLabel: string, cancelLabel: string, hideIconButtons = false): Promise<boolean> {
		return new Promise((resolve, reject) => {
			this.notifier.broadcast({
				name: 'prompt.open',
				resolve: resolve,
				reject: reject,
				title: title,
				message: message,
				confirmLabel: confirmLabel,
				cancelLabel: cancelLabel,
				hideIconButtons: hideIconButtons
			});
		});
	}
}
