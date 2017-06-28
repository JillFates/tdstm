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
	public domains: DomainModel[] = [];
	selectedTab = '';
	editing = false;

	constructor( @Inject('fields') fields: Observable<DomainModel[]>) {
		fields.subscribe(
			(result) => {
				this.domains = result;
				if (this.domains.length > 0) {
					this.selectedTab = this.domains[0].domain;
				}
			},
			(err) => console.log(err));
	}

	protected onTabChange(domain: string): void {
		this.selectedTab = domain;
	}

	protected isTabSelected(domain: string): boolean {
		return this.selectedTab === domain;
	}
}