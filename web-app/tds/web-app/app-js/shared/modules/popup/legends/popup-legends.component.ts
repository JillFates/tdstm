import {Component} from '@angular/core';

@Component({
	moduleId: module.id,
	selector: 'popup-legends',
	templateUrl: '../tds/web-app/app-js/shared/modules/popup/legends/popup-legends.component.html',
})

export class PopupLegendsComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}