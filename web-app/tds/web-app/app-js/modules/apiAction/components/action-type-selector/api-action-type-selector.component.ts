import {Component} from '@angular/core';

import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {APIActionType} from '../../model/api-action.model';

@Component({
	selector: 'api-action-type-selector',
	template: `
		<div tds-autofocus tds-handle-escape (escPressed)="onCancelDialog()" class="modal-content api-action-type-selector-component" >
			<div class="modal-header">
				<h4 class="modal-title">Please select</h4>
			</div>
			<div class="modal-body">
				<div>
					<input type="radio" [value]="actionTypeEnum.HTTP_API" name="actionType" [(ngModel)]="model.actionType"> WebAPI
				</div>
				<div>
					<input type="radio" [value]="actionTypeEnum.SCRIPT" name="actionType" [(ngModel)]="model.actionType"> Script
				</div>
			</div>
			<div class="modal-footer form-group-center">
				<tds-button-custom icon="step-forward" title="Next" class="btn-primary pull-left" (click)="onNext()">
				</tds-button-custom>

				<tds-button-cancel class="pull-right" (click)="onCancelDialog()">
				</tds-button-cancel>
			</div>
		</div>
	`
})
export class APIActionTypeSelectorComponent {
	protected actionTypeEnum = APIActionType;
	protected model = {
		actionType : APIActionType.HTTP_API
	};

	constructor(
		private permissionService: PermissionService,
		private activeDialog: UIActiveDialogService) {
	}

	onCancelDialog() {
		this.activeDialog.dismiss();
	}

	onNext() {
		this.activeDialog.close(this.model.actionType);
	}
}
