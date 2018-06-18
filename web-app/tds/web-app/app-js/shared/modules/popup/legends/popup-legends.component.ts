import { Component } from '@angular/core';
import { FIELD_COLORS} from '../../../../modules/fieldSettings/model/field-settings.model';

@Component({
	selector: 'popup-legends',
	templateUrl: '../tds/web-app/app-js/shared/modules/popup/legends/popup-legends.component.html',
	styles: [`
		table { width: 300px;}
    `]
})

export class PopupLegendsComponent {
	public colors = FIELD_COLORS;
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}