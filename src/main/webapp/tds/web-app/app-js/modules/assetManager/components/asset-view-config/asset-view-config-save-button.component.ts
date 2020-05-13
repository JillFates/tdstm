import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ViewModel } from '../../../assetExplorer/model/view.model';
import { Permission } from '../../../../shared/model/permission.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

/**
 * TODO: this is almost the same code for the asset-view-show-save-button.component.ts so probably we just
 * need to unified both and just use a single component.
 */
@Component({
	selector: 'tds-asset-view-config-save-button',
	template: `
		<div *ngIf="config.canShowSaveButton" class="btn-group">
			<tds-button
				[ngClass]="{'btn-secondary':!config.isDirty, 'btn-success': (config.isDirty && !config.disableSaveButton)}"
				[id]="config.saveButtonId"
				[disabled]="config.disableSaveButton"
				[title]="config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')"
				(click)="saveClick(config.saveButtonId)">
				{{config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')}}
			</tds-button>
			<clr-dropdown [clrCloseMenuOnItemClick]="true">
				<tds-button [icon]="'angle down'"
										[ngClass]="{'btn-secondary':!config.isDirty,'btn-success':config.isDirty}"
										clrDropdownTrigger>
				</tds-button>
				<clr-dropdown-menu clrPosition="bottom-left" *clrIfOpen>
					<button *ngIf="config.canSave" clrDropdownItem class="btn"
									[disabled]="config.canSave && !config.isDirty"
									(click)="onSave()">
						{{ 'GLOBAL.SAVE' | translate }}
					</button>
					<button *ngIf="config.canSaveAs" clrDropdownItem class="btn"
									(click)="onSaveAs()">
						{{ 'GLOBAL.SAVE_AS' | translate }}
					</button>
				</clr-dropdown-menu>
			</clr-dropdown>
		</div>
	`,
})
export class AssetViewConfigSaveButtonComponent {
	@Input() config: {
		isDirty: boolean,
		saveButtonId: string,
		canSave: boolean,
		canSaveAs: boolean,
		isEditAvailable: boolean,
		canShowSaveButton: boolean,
		disableSaveButton: boolean
	};
	@Output() save = new EventEmitter<any>();
	@Output() saveAs = new EventEmitter<any>();
	protected readonly SAVE_BUTTON_ID = 'btnSave';

	constructor(protected translateService: TranslatePipe) {}

	/**
	 * Determines which save operation to call based on the button id.
	 */
	saveClick(saveButtonId: string) {
		saveButtonId === this.SAVE_BUTTON_ID ? this.onSave() : this.onSaveAs()
	}

	/**
	 * On Save As button clicked, emit.
	 */
	onSaveAs(): void {
		this.saveAs.emit();
	}

	/**
	 * On Save button clicked, emit.
	 */
	onSave() {
		this.save.emit();
	}
}
