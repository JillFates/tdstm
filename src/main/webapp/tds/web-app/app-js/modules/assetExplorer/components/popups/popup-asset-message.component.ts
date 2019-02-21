import {Component, Input} from '@angular/core';

@Component({
	selector: 'tds-popup-asset-message',
	template: `
        <span style="cursor:pointer;" #popUpPolling (click)="onToggle()"><i class="fa  fa-fw fa-question-circle"></i></span>
        <kendo-popup [anchor]="popUpPolling" [popupClass]="'content popup'" *ngIf="show">
            <div>
                <label>
                    {{message}}
                </label>
            </div>
        </kendo-popup>
	`,
	styles: [`
		div { width: 180px; padding: 10px; }
    `]
})

export class PopupAssetMessageComponent {
	@Input() message: string;
	public show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}