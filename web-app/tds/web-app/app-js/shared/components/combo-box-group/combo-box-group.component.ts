import {Component, EventEmitter, Input, Output, ViewChild, OnInit} from '@angular/core';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';

@Component({
	selector: 'tds-combobox-group',
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box-group/combo-box-group.component.html'
})
export class TDSComboBoxGroupComponent implements OnInit {
	@ViewChild('innerComboBoxGroup') innerComboBoxGroup: ComboBoxComponent;
	@Output() modelChange = new EventEmitter<string>();
	@Output() isFixedChange = new EventEmitter<number>();
	@Input('model') model: any;
	@Input('people') people: any;
	@Input('team') team: any;
	@Input('isFixed') isFixed: number;

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
		const team = this.team.map( (item) => ({ id: `@${item.id}`, text: item.description }));

		// concat the array categories to build the final data set
		this.source = this.source.concat( this.addCategoryToArray(sme, this.CATEGORY_BY_REFERENCE) );
		this.source = this.source.concat( this.addCategoryToArray(people, this.CATEGORY_BY_TEAM) );
		this.source = this.source.concat( this.addCategoryToArray(team, this.CATEGORY_BY_NAMED_STAFF) );
		this.data = [...this.source];
	}

	/**
	 * Add the category field to the items array
	 * @param {any[]} items array to work with
	 * @param {string} category name to add to the array
	 */
	addCategoryToArray(items: any[], category: string): any[] {
		return items.map((item, index) => Object.assign({},  item, { category, index } ))
	}

	/**
	 * On filter change regenerate the index in order to group properly the cagetories
	 * @param {string} search string to filter only items matching
	 */
	public onFilterChange(search: string): void {
		this.data = this.source.filter((s) => s.text.toLowerCase().indexOf(search.toLowerCase()) !== -1);

		const sme = this.setIndex(this.data, this.CATEGORY_BY_REFERENCE);
		const team = this.setIndex(this.data, this.CATEGORY_BY_TEAM);
		const people = this.setIndex(this.data, this.CATEGORY_BY_NAMED_STAFF);
		this.data = [...sme, ...team, ...people];
	}

	/**
	 * Notifiy to host on model change
	 * @param {any} event with  current model value
	 */
	public onValueChange(event: any): void {
		this.modelChange.next(event.id || null);
	}

	/**
	 * Notifiy to host whenever the checkbox change its value
	 * @param {any} event with checkbox current value
	 */
	public onChangeFixed(event: any): void {
		this.isFixedChange.next(event.target.checked ? 1 : 0);
	}

	/**
	 * Reset index value of data array
	 * @param {any[]} data array to work with
	 * @param {string} key to apply rule only to fields matching the key
	 */
	private setIndex(data: any[], key: string): any[] {
		return data.filter((item) => item.category === key)
			.map((item, index) => ({...item, index}) );
	}

	/**
	 * Determine if current element belongs to teams category
	 */
	public isTeamItemSelected(): boolean {
		return this.model && this.model.toString().startsWith('@');
	}
}