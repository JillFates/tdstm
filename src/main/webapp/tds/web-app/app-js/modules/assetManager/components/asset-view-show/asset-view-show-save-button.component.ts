import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-asset-show-save-button',
	template: `
		<div *ngIf="config.canShowSaveButton" class="btn-group">
			<tds-button [ngClass]="{'btn-secondary':!config.isDirty,'btn-success':config.isDirty}"
									[id]="config.saveButtonId"
									[icon]="'add-text'"
									[title]="config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')"
									(click)="saveClick(config.saveButtonId)">
<!--				{{config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')}}-->
			</tds-button>
			<clr-dropdown>
				<tds-button [title]="''" icon="angle down" clrDropdownTrigger></tds-button>
				<clr-dropdown-menu clrPosition="bottom-left" *clrIfOpen>
					<li *ngIf="config.canSave">
						<a (click)="onSave()">{{ 'GLOBAL.SAVE' | translate }}</a>
					</li>
					<li *ngIf="config.canSaveAs">
						<a (click)="onSaveAs()">{{ 'GLOBAL.SAVE_AS' | translate }}</a>
					</li>
				</clr-dropdown-menu>
			</clr-dropdown>
		</div>
	`,
})
export class AssetViewShowSaveButtonComponent {
	@Input() config: any;
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

	onSave(): void {
		this.save.emit();
	}

	onSaveAs(): void {
		this.saveAs.emit();
	}
}
