import { Component } from '@angular/core';

@Component({
	selector: 'popup-legends',
	templateUrl: '../tds/web-app/app-js/shared/modules/popup/legends/popup-legends.component.html',
	styles: [`
		table { width: 300px;}
    `]
})

export class PopupLegendsComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}