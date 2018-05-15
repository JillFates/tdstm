import {Component, EventEmitter, Input, Output, ViewChild, ElementRef, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {setTimeout} from 'timers';

declare var jQuery: any;

const DataSource = () => {
	let sort = 1;
	let collection: any[] = [];
	console.log('Init...');

	return {
		addArray: (a: any[], category: string) => {
			collection = [...collection].concat(a.map((b) => Object.assign({},  b, { category, sort } )));
			sort += 1;
		},
		getCollection: (): any[] => collection
	};
};

@Component({
	selector: 'tds-combobox-group',
	styleUrls: ['../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.css'],
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.html'
})
export class TDSComboBoxGroupComponent implements OnInit {
	// References
	@ViewChild('innerComboBoxGroup') innerComboBoxGroup: ComboBoxComponent;
	// Model
	@Output() modelChange = new EventEmitter<string>();
	@Input('model') model: any;
	CATEGORY_BY_REFERENCE = 'By Reference';
	CATEGORY_BY_TEAM = 'Team';
	CATEGORY_BY_NAMED_STAFF = 'Named Staff';

	private dataSource: any[];

	constructor(private sanitized: DomSanitizer) {}

	ngOnInit() {
		const teams = [
				{ id: 1, text: 'Account Manager', index: 1},
				{ id: 2, text: 'App Coordinator', index: 2},
				{ id: 3, text: 'Automatic', index: 3},
				{ id: 4, text: 'Backup Admin', index: 4}
			];

		const persons = [
				{ id: 3, text: 'Allan Haines', index: 1 },
				{ id: 4, text: 'Barry Allen', index: 2 },
				{ id: 5, text: 'Clark Kent', index: 3 },
				{ id: 6, text: 'Bruce Wayne', index: 4 }
			];

		const sme = [
				{ id: 7, text: 'SME 1', index: 1},
				{ id: 8, text: 'SME 2', index: 2},
				{ id: 8, text: 'Owner', index: 2}
			];

		const data = DataSource();
		data.addArray(sme, this.CATEGORY_BY_REFERENCE);
		data.addArray(teams, this.CATEGORY_BY_TEAM);
		data.addArray(persons, this.CATEGORY_BY_NAMED_STAFF);
		this.dataSource = data.getCollection();
	}
}