import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

@Component({
	moduleId: module.id,
	selector: 'field-settings-list',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/list/field-settings-list.component.html'
})
export class FieldSettingsListComponent {
	public fieldSettings: any = {};

	constructor( @Inject('fields') fields: Observable<DomainModel[]>) {
		fields.subscribe(
			(result) => {
				result.forEach(element => {
					this.fieldSettings[element.domain] = element;
				});
			},
			(err) => console.log(err));
	}
}