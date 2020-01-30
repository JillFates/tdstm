import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ViewModel } from '../../../assetExplorer/model/view.model';
import { Permission } from '../../../../shared/model/permission.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { PermissionService } from '../../../../shared/services/permission.service';

@Component({
	selector: 'tds-asset-view-config-save-button',
	template: `
		<div *ngIf="shouldDisplayDropdownButton(); else saveButton"
				 class="btn-group">
			<tds-button *ngIf="model.isOwner || (model.isSystem && isSystemSaveAvailable(true))"
									[id]="'btnSave'"
									[disabled]="(!assetExplorerService.isSaveAvailable(this.model) || !isValid) || !isDirty()"
									[title]="'GLOBAL.SAVE' | translate"
									(click)="onSave()"
									[ngClass]="{'btn-secondary':!isDirty() || !isSaveAsAvailable() || !isValid,'btn-success':isValid && isDirty()}">
				{{'GLOBAL.SAVE' | translate}}
			</tds-button>
			<tds-button *ngIf="!model.isOwner && !model.isSystem"
									[id]="'btnSaveAs'"
									[disabled]="!isSaveAsAvailable() || !isValid"
									[title]="'GLOBAL.SAVE_AS' | translate"
									(click)="onSaveAs()"
									[ngClass]="{'btn-secondary':!isDirty() || !isSaveAsAvailable() || !isValid,'btn-success':isValid && isDirty()}">
				{{'GLOBAL.SAVE_AS' | translate}}
			</tds-button>
			<clr-dropdown>
				<tds-button [title]="''" icon="angle down" clrDropdownTrigger
										[ngClass]="{'btn-secondary':!isDirty() || !isSaveAsAvailable() || !isValid,'btn-success':isValid && isDirty()}"></tds-button>
				<clr-dropdown-menu clrPosition="bottom-left" *clrIfOpen>
					<button clrDropdownItem class="btn"
									[disabled]="!isDirty()"
									(click)="onSave()">
						{{ 'GLOBAL.SAVE' | translate }}
					</button>
					<button clrDropdownItem class="btn"
									(click)="onSaveAs()">
						{{ 'GLOBAL.SAVE_AS' | translate }}
					</button>
				</clr-dropdown-menu>
			</clr-dropdown>
		</div>
		<ng-template #saveButton>
			<tds-button [id]="'btnSave'"
									*ngIf="model.isOwner || (model.isSystem && isSystemSaveAvailable(true))"
									[disabled]="!this.assetExplorerService.isSaveAvailable(this.model) || !isValid"
									[ngClass]="{'btn-secondary':!isDirty() || !isSaveAsAvailable() || !isValid,'btn-success':isValid && isDirty()}"
									(click)="onSave()">
				{{ 'GLOBAL.SAVE' | translate }}
			</tds-button>
			<tds-button [id]="'btnSaveAs'"
									*ngIf="model.id && ((model.isSystem && isSystemSaveAvailable(false)) || (!model.isOwner && !model.isSystem))"
									[disabled]="!isSaveAsAvailable() || !isValid"
									[ngClass]="{'btn-secondary':!isDirty() || !isSaveAsAvailable() || !isValid,'btn-success':isValid && isDirty()}"
									(click)="onSaveAs()">
				{{ 'GLOBAL.SAVE_AS' | translate }}
			</tds-button>
		</ng-template>
	`,
})
export class AssetViewConfigSaveButtonComponent {
	@Input() model: ViewModel;
	@Input() isValid: boolean;
	@Input() dataSignature: string;
	@Output() save = new EventEmitter<any>();
	@Output() saveAs = new EventEmitter<any>();

	constructor(
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService) {}

	isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.model);
		// TODO: hasPendingChanges
		// if (this.state && this.state.$current && this.state.$current.data) {
		// 	this.state.$current.data.hasPendingChanges = result && !this.collapsed;
		// }
		return result;
	}

	isSaveAsAvailable(): boolean {
		return this.model.id ?
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
				this.permissionService.hasPermission(Permission.AssetExplorerSaveAs) :
			this.assetExplorerService.isSaveAvailable(this.model);
	}

	isSystemSaveAvailable(edit): boolean {
		return edit ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs);
	}

	onSaveAs(): void {
		if (this.isSaveAsAvailable()) {
			this.saveAs.emit();
		}
	}

	onSave() {
		if (this.assetExplorerService.isSaveAvailable(this.model)) {
			this.save.emit();
		}
	}

	/**
	 * Returns true if dropdown button should be displayed, otherwise false.
	 */
	shouldDisplayDropdownButton(): boolean {
		return this.model.id && (this.model.isOwner || (
				this.model.isSystem && (this.isSystemSaveAvailable(true) && this.isSystemSaveAvailable(false))
			)
		);
	}
}
