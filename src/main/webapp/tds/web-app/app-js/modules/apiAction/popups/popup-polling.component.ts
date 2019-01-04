import { Component } from '@angular/core';

@Component({
	selector: 'popup-polling',
	templateUrl: '../tds/web-app/app-js/modules/apiAction/popups/popup-polling.component.html',
	styles: [`
		div { width: 320px; padding: 10px; }
    `]
})

export class PopupPollingComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}