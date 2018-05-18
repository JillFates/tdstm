import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import { Person } from './model/person.model';

@Component({
	selector: 'add-person',
	templateUrl: '../tds/web-app/app-js/shared/components/add-person/add-person.component.html',
	styles: [`
			div.modal-body,
			div.box-body
			{
				padding-bottom: 0px;
            }
			div.modal-title {
                width: 453px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
			}
	`]
})
export class AddPersonComponent extends UIExtraDialog {
	constructor(
		public personModel: Person) {
		super('#add-person-component');
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	onEscKeyPressed(): void {

		this.cancelCloseDialog();
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	protected onUpdateComment(): void {
		this.close(this.personModel);
	}

}