import {Component, Input, Output, EventEmitter} from '@angular/core';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-bulk-change-button',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulkChange/components/bulk-change-button/bulk-change-button.component.html',
	providers: [TranslatePipe]
})
export class BulkChangeButtonComponent {
	@Input() selectedItems: string[];
	@Output() operationResult = new EventEmitter<boolean>();

	constructor() {
		this.selectedItems = [];
	}
}