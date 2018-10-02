import {Component, Input} from '@angular/core';

@Component({
	selector: 'tds-popup-asset-message',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/popups/popup-asset-message.component.html',
	styles: [`
		div { width: 180px; padding: 10px; }
    `]
})

export class PopupAssetMessageComponent {
	@Input() message: string;
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}