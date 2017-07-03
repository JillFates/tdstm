import { Component } from '@angular/core';

@Component({
	moduleId: module.id,
	selector: 'popup-legends',
	templateUrl: '../tds/web-app/app-js/shared/modules/popup/legends/popup-legends.component.html',
	styles: [`
		table { width: 300px;}
        .C { background-color: #F9FF90;}
        .I { background-color: #D4F8D4;}
        .N { background-color: #FFF;}
        .U { background-color: #F3F4F6;}
    `]
})

export class PopupLegendsComponent {
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}