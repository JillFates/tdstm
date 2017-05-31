/**
 * Created by David Ontiveros on 5/31/2017.
 */
import {Component, Input} from '@angular/core';

@Component({
	moduleId: module.id,
	selector: 'control-config-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/grid/control-config-popup-component.html',
})

export class ControlConfigPopupComponent {

	@Input() controlType: string;
	private show = false;

	public onToggle(): void {
		this.show = !this.show;
	}
}