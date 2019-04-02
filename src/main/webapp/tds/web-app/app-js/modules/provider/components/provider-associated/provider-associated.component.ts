// Angular
import {Component} from '@angular/core';
// Service
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
// Model
import {ProviderAssociatedModel} from '../../model/provider-associated.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-provider-associated',
	templateUrl: 'provider-associated.component.html'
})
export class ProviderAssociatedComponent extends UIExtraDialog {

	public modalOptions: DecoratorOptions;

	constructor(
		public providerAssociated: ProviderAssociatedModel) {
		super('#providerAssociated');
		this.modalOptions = {isFullScreen: false, isResizable: false};
	}

	public confirm($event): void {
		this.close(true);
	}

	public cancel($event): void {
		this.close(false);
	}

}
