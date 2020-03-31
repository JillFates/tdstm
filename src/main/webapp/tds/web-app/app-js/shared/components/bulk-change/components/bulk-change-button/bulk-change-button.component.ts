// Angular
import {Component, Input, Output, EventEmitter, ComponentFactoryResolver} from '@angular/core';
// Model
import {BulkChangeModel, BulkActionResult, BulkChangeType} from '../../model/bulk-change.model';
// Service
import {TranslatePipe} from '../../../../pipes/translate.pipe';
import {DialogService, ModalSize} from 'tds-component-library';
// Component
import {BulkChangeActionsComponent} from '../bulk-change-actions/bulk-change-actions.component';

@Component({
	selector: 'tds-bulk-change-button',
	template: `
		<tds-button-custom
			[flat]="flat"
			[icon]="'pencil'"
			[tooltip]="'Bulk Edit'"
			[id]="'btnBulkChange'"
			(click)="onClick()"
			[disabled]="!enabled">
		</tds-button-custom>
	`,
	providers: [TranslatePipe]
})
export class BulkChangeButtonComponent {
	@Input() enabled: boolean ;
	@Input() showEdit: boolean;
	@Input() showDelete: boolean;
	@Input() flat: boolean;
	@Input() bulkChangeType: BulkChangeType;
	@Input() viewId: number;
	@Output() operationResult = new EventEmitter<BulkActionResult>();
	@Output() clickBulk = new EventEmitter<void>();

	private selectedItems: number[];
	private selectedAssets: Array<any>;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService
	) {
		this.enabled = false;
		this.selectedItems = [];
		this.selectedAssets = [];
	}

	onClick() {
		this.clickBulk.emit();
	}

	/**
	 * Opens the dialog windows to Bulk Data
	 * @param data
	 */
	public bulkData(data: any): void {
		this.selectedItems = data && data.bulkItems ? data.bulkItems : null;
		this.selectedAssets = data && data.assetsSelectedForBulk ? data.assetsSelectedForBulk : null;
		if (this.selectedItems && this.selectedItems.length) {
			this.showBulkActions();
		}
	}

	showBulkActions() {
		const bulkChangeModel: BulkChangeModel = {
			selectedItems: this.selectedItems,
			selectedAssets: this.selectedAssets,
			affected: this.selectedItems.length,
			showDelete: this.showDelete,
			showEdit: this.showEdit,
			bulkChangeType: this.bulkChangeType,
			viewId: this.viewId
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: BulkChangeActionsComponent,
			data: {
				bulkChangeModel: bulkChangeModel
			},
			modalConfiguration: {
				title: 'Bulk Change',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe( (bulkOperationResult: any) => {
			this.operationResult.emit(bulkOperationResult);
		});
	}
}
