import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { ASSET_ENTITY_DIALOG_TYPES } from '../../../assetExplorer/model/asset-entity.model';

@Component({
	selector: 'tds-asset-show-create-button',
	template: `
		<clr-dropdown style="margin-right: 12px">
			<tds-button clrDropdownTrigger
									[type]="'button'"
									[title]="'Create'"
									[iconClass]="'is-solid'"
									[icon]="'plus'">
			</tds-button>
			<clr-dropdown-menu clrPosition="bottom-left" *clrIfOpen>
				<li (click)="onCreateAsset(ASSET_ENTITY_DIALOG_TYPES.APPLICATION)">
					<a
						[ngClass]="{'create-selected': createButtonState === ASSET_ENTITY_DIALOG_TYPES.APPLICATION}">{{ 'ASSETS.APPLICATION' | translate }}</a>
				</li>
				<li (click)="onCreateAsset(ASSET_ENTITY_DIALOG_TYPES.DATABASE);">
					<a
						[ngClass]="{'create-selected': createButtonState === ASSET_ENTITY_DIALOG_TYPES.DATABASE}">{{ 'ASSETS.DATABASE' | translate }}</a>
				</li>
				<li (click)="onCreateAsset(ASSET_ENTITY_DIALOG_TYPES.DEVICE);">
					<a
						[ngClass]="{'create-selected': createButtonState === ASSET_ENTITY_DIALOG_TYPES.DEVICE}">{{ 'ASSETS.DEVICE' | translate }}</a>
				</li>
				<li (click)="onCreateAsset(ASSET_ENTITY_DIALOG_TYPES.STORAGE);">
					<a
						[ngClass]="{'create-selected': createButtonState === ASSET_ENTITY_DIALOG_TYPES.STORAGE}">{{ 'ASSETS.STORAGE' | translate }}</a>
				</li>
			</clr-dropdown-menu>
		</clr-dropdown>
	`,
})
export class AssetViewShowCreateButtonComponent {
	@Output() createAsset = new EventEmitter<any>();
	@Input() createButtonState: ASSET_ENTITY_DIALOG_TYPES;
	ASSET_ENTITY_DIALOG_TYPES = ASSET_ENTITY_DIALOG_TYPES;

	onCreateAsset(assetEntityType: ASSET_ENTITY_DIALOG_TYPES): void {
		this.createAsset.emit(assetEntityType);
	}
}
