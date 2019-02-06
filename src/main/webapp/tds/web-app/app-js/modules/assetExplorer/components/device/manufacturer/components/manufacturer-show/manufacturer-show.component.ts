import { Component } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import { DeviceManufacturer} from '../../model/device-manufacturer.model';
import { ManufacturerEditComponent } from '../manufacturer-edit/manufacturer-edit.component';
import { PermissionService } from '../../../../../../../shared/services/permission.service';
import { Permission } from '../../../../../../../shared/model/permission.model';

@Component({
	selector: 'device-manufacturer',
	template: `
        <div class="modal fade in manufacturer-show-component" tds-handle-escape (escPressed)="cancelCloseDialog()" id="device-manufacturer-component" data-backdrop="static"
             tabindex="-1" role="dialog">
            <div class="modal-dialog modal-sm" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                        <h4 class="modal-title">Show Manufacturer</h4>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
                            <div class="box-body">
                                <div class="row">
                                    <div class="form-group">
                                        <div class="col-md-4">Name:</div>
                                        <div class="col-md-8">{{deviceManufacturer.name}}</div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group">
                                        <div class="col-md-4">AKA:</div>
                                        <div class="col-md-8">{{deviceManufacturer.aka}}</div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group">
                                        <div class="col-md-4">Description</div>
                                        <div class="col-md-8">{{deviceManufacturer.description}}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer form-group-center">
                        <button type="button" [disabled]="!canEditManufacturer()" class="btn btn-primary pull-left" (click)="onEditManufacturer()">
                            <span class="glyphicon glyphicon-pencil"></span> Edit
                        </button>
                        <button type="button" class="btn btn-default pull-right" (click)="cancelCloseDialog()">
                            <span class="glyphicon glyphicon-ban-circle"></span> Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
	`
})
export class ManufacturerShowComponent extends UIExtraDialog {
	aka: string;
	constructor(
		private dialogService: UIDialogService,
		public deviceManufacturer: DeviceManufacturer,
		private permissionService: PermissionService) {
		super('#device-manufacturer-component');
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * On EscKey Pressed close the dialog.
	*/
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Determine if user has permissions to edit manufacturer
	 */
	canEditManufacturer(): boolean {
		return this.permissionService.hasPermission(Permission.ManufacturerEdit)
	}

	/**
	 * On click Edit button
	 */
	onEditManufacturer(): void {
		if (!this.canEditManufacturer()) {
			return ;
		}

		this.dialogService.extra(ManufacturerEditComponent,
			[
				{
					provide: DeviceManufacturer,
					useValue: this.deviceManufacturer
				}
			], false, false)
			.then((result) => {
				this.close(result);
			}).catch((error) => console.log(error));
	}
}