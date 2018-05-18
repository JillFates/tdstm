import {Component, EventEmitter, Input, Output, ViewChild, ElementRef, OnInit} from '@angular/core';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {setTimeout} from 'timers';

@Component({
	selector: 'tds-combobox-group',
	styleUrls: ['../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.css'],
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.html'
})
export class TDSComboBoxGroupComponent implements OnInit {
	@ViewChild('innerComboBoxGroup') innerComboBoxGroup: ComboBoxComponent;
	@Output() modelChange = new EventEmitter<string>();
	@Input('model') model: any;
	@Input('people') people: any;
	@Input('team') team: any;

	readonly CATEGORY_BY_REFERENCE = 'By Reference';
	readonly CATEGORY_BY_TEAM = 'Team';
	readonly CATEGORY_BY_NAMED_STAFF = 'Named Staff';
	protected source: any[] ;
	protected data: any;
	ngOnInit() {
		this.source = [];
		const sme = [
				{ id: '#SME1',   text: 'SME 1' },
				{ id: '#SME2',   text: 'SME 2' },
				{ id: '##Owner', text: 'Owner' }
			];
		const people = this.people.map( (item) => ({ id: item.personId, text: item.fullName }));
		const team = this.team.map( (item) => ({ id: item.id, text: item.description }));

		this.source = this.source.concat( this.addCategoryToArray(sme, this.CATEGORY_BY_REFERENCE) );
		this.source = this.source.concat( this.addCategoryToArray(people, this.CATEGORY_BY_TEAM) );
		this.source = this.source.concat( this.addCategoryToArray(team, this.CATEGORY_BY_NAMED_STAFF) );
		this.data = [...this.source];
	}

	addCategoryToArray(items: any[], category: string): any[] {
		return items.map((item, index) => Object.assign({},  item, { category, index } ))
	}

	handleFilter(search: string): void {
		this.data = this.source.filter((s) => s.text.toLowerCase().indexOf(search.toLowerCase()) !== -1);
	}
}