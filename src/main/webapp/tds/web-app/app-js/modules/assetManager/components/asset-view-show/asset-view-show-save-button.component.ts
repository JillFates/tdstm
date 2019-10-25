import { Component, Input } from '@angular/core';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-asset-show-save-button',
	template: `
<div class="btn-group"
 *ngIf="false">
	<tds-button-custom [ngClass]="{'btn-default':!config.isDirty,'btn-success':config.isDirty}"
										 [small]="'small'" [id]="config.saveButtonId"
										 [title]="config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')">
	</tds-button-custom>
	<tds-button-custom [icon]="'angle'"></tds-button-custom>
<!--<tds-button-save-->
<!--(click)="save(config.saveButtonId)"-->
<!--[ngClass]="{'btn-default':!config.isDirty,'btn-success':config.isDirty}"-->
<!--[title]="config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS') "-->
	<!--[attr.id]="config.saveButtonId">-->
<!--</tds-button-save>-->
<!--<button type="button" class="btn dropdown-toggle"-->
<!--[ngClass]="{'btn': true, 'btn-default': !config.isDirty,'btn-success': config.isDirty, 'pull-right': true }"-->
<!--data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">-->
<!--<span class="caret asset-view-show-caret"></span>-->
<!--<span class="sr-only">Toggle Dropdown</span>-->
<!--</button>-->
<!--<ul class="dropdown-menu">-->
<!--<li *ngIf="config.canSave">-->
<!--<a (click)="onSave()">{{ 'GLOBAL.SAVE' | translate }}</a>-->
<!--</li>-->
<!--<li *ngIf="config.canSaveAs">,-->
<!--<a (click)="onSaveAs()">{{ 'GLOBAL.SAVE_AS' | translate }}</a>-->
<!--</li>-->
<!--</ul>-->
</div>
		<div class="btn-group"
				 *ngIf="config.canShowSaveButton">
			<tds-button-custom [ngClass]="{'btn-default':!config.isDirty,'btn-success':config.isDirty}"
												 [small]="'small'" [id]="config.saveButtonId"
												 [title]="config.canSave ? translateService.transform('GLOBAL.SAVE') : translateService.transform('GLOBAL.SAVE_AS')">
			</tds-button-custom>
			<tds-button-custom [ngClass]="{'btn-default': !config.isDirty,'btn-success': config.isDirty, 'pull-right': true }"
												 [icon]="'angle down'">
			</tds-button-custom>
			<ul class="dropdown-menu">
				<li *ngIf="config.canSave">
					<a (click)="onSave()">{{ 'GLOBAL.SAVE' | translate }}</a>
				</li>
				<li *ngIf="config.canSaveAs">
					<a (click)="onSaveAs()">{{ 'GLOBAL.SAVE_AS' | translate }}</a>
				</li>
			</ul>
		</div>
	`,
})
export class AssetViewShowSaveButtonComponent {
	@Input() config: any;

	constructor(protected translateService: TranslatePipe) {
	}

	save(saveButtonId: any): void {
		// todo: implement
	}

	onSave(): void {
		// implement
	}

	onSaveAs(): void {
		// implement
	}
}
