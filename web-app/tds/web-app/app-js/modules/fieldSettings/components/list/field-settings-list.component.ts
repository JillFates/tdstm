import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { FieldSettingsModel } from '../../model/field-settings.model';

@Component({
	moduleId: module.id,
	selector: 'field-settings-list',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/list/field-settings-list.component.html'
})
export class FieldSettingsListComponent {
	public fieldSettings: FieldSettingsModel[];
	public assetClass = 'Devices';

	constructor( @Inject('fields') fields: Observable<FieldSettingsModel[]>) {
		fields.subscribe(
			(result) => {
				this.fieldSettings = result;
			},
			(err) => console.log(err));
	}
}