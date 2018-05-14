import {Component, EventEmitter, Input, Output, ViewChild, ElementRef, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {setTimeout} from 'timers';

declare var jQuery: any;

const DataSource = () => {
	let sort = 1;
	let collection: any[] = [];

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
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.html'
})
export class TDSComboBoxGroupComponent implements OnInit {
	// References
	@ViewChild('innerComboBoxGroup') innerComboBoxGroup: ComboBoxComponent;
	// Model
	@Output() modelChange = new EventEmitter<string>();
	@Input('model') model: any;

	private dataSource: any[];

	constructor(private sanitized: DomSanitizer) {}

	ngOnInit() {
		const teams = [
				{ id: 1, text: 'team 1', index: 1},
				{ id: 2, text: 'team 2', index: 2},
				{ id: 3, text: 'team 3', index: 3},
				{ id: 4, text: 'team 4', index: 4}
			];

		const persons = [
				{ id: 3, text: 'person 1', index: 1 },
				{ id: 4, text: 'person 2', index: 2 },
				{ id: 5, text: 'person 3', index: 3 },
				{ id: 6, text: 'person 4', index: 4 }
			];

		const sme = [
				{ id: 7, text: 'sme 1', index: 1},
				{ id: 8, text: 'sme 2', index: 2}
			];

		const data = DataSource();
		data.addArray(sme, 'sme');
		data.addArray(teams, 'team');
		data.addArray(persons, 'person');

		this.dataSource = data.getCollection();
	}
}