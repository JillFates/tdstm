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
                <div class="modal-content">
                    <div class="modal-header">
                        <button (click)="cancel()" type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
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
                        <button (click)="confirm()" type="submit" class="btn btn-primary pull-left"><span class="glyphicon glyphicon-ok"></span> {{confirmLabel}}</button>
                        <button (click)="cancel()" type="button" class="btn btn-default pull-right" data-dismiss="modal"><span class="glyphicon glyphicon-ban-circle"></span> {{cancelLabel}}</button>
                    </div>
                </div>
            </div>
        </div>
	`,
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
		const refControl =  'tdsUiPrompt';
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
		});
	};

	public cancel(): void {
		this.resolve(false);
		this.tdsUiPrompt.modal('hide');
	}

	public dismiss(): void {
		this.reject();
		this.tdsUiPrompt.modal('hide');
	}

	public confirm(): void {
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