/**
 * Created by David Ontiveros on 5/31/2017.
 */
import { Component, Input, ViewChild, ViewEncapsulation } from '@angular/core';
import { SortableComponent } from '@progress/kendo-angular-sortable';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { CustomDomainService } from '../../service/custom-domain.service';

declare var jQuery: any;
/**
 *
 */
@Component({
	selector: 'selectlist-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/select-list/selectlist-configuration-popup.component.html',
	encapsulation: ViewEncapsulation.None,
	exportAs: 'selectlistConfig',
	styles: [
		`.pointer { cursor: pointer; }`
	]
})

/**
 *
 */
export class SelectListConfigurationPopupComponent {

	/**
	 * Asset type: DB, Application, etc..
	 */
	@Input() domain: string;
	/**
	 * Custom Field to edit.
	 */
	@Input() field: FieldSettingsModel;
	@ViewChild('kendoSortableInstance') kendoSortableInstance: SortableComponent;

	public items: any[] = [];
	public newItem = '';
	public show = false; // first time should open automatically.
	public defaultValue: string = null;
	protected sortType: string;
	private ASCENDING_ORDER = 'asc';
	private DESCENDING_ORDER = 'desc';

	/**
	 * Class constructor.
	 * @param customService Service to obtain distinct values.
	 */
	constructor(private customService: CustomDomainService) { }

	/**
	 * Intializes current component based on the @field input parameter.
	 * Makes a service call to get pre-build distinct values from system.
	 */
	private load(): void {
		this.newItem = '';
		this.sortType = null;
		this.defaultValue = null;
		console.log(this.field.constraints.values);
		this.customService.getDistinctValues(this.domain, this.field)
			.subscribe((value: string[]) => {

				let indexOfBlank = value.indexOf('');
				if (this.field.constraints.required && indexOfBlank !== -1) {
					this.items.splice(indexOfBlank, 1);
				} else if (!this.field.constraints.required && indexOfBlank === -1) {
					this.items.splice(0, 0, '');
				}
				if (this.field.constraints.values) {
					for (let item of this.field.constraints.values) {
						if (value.indexOf(item) === -1) {
							this.items.push( {deletable: true, value: item} );
						} else {
							this.items.push( {deletable: false, value: item} );
						}
					}
				}
			});
		this.defaultValue = this.field.default;
	}

	/**
	 * Helper method to set styling to items rows on a odd-even alternate pattern.
	 * @param index
	 * @returns {{background-color: string}}
	 */
	public getStyle(index) {
		if ((index % 2) === 0) {
			return { 'background-color': '#f6f6f6' };
		}
	}

	/**
	 * Function to handle event click on + Add button
	 * Adds a new item to the list.
	 */
	public onAdd(): void {
		if (this.items.filter((i) => i.value === this.newItem).length === 0) {
			this.items.push({
				deletable: true,
				value: this.newItem
			});
			this.newItem = '';
			// sort if needed.
			if (this.sortType) {
				this.sortItems();
			}
			setTimeout(function () {
				jQuery('#newItem').focus();
			});
		}
	}

	/**
	 * Remove a specific item from the current config select list
	 * @param item The item to be removed.
	 */
	public onRemove(item: any): void {
		if (item.deletable) {
			this.items.splice(this.items.indexOf(item), 1);
		}
	}

	/**
	 * Function to handle onclick Save button event.
	 * Saves the current select list configuration.
	 * Simply sets current list into the field model and it's default value.
	 */
	public onSave(): void {
		let fieldModel = { ...this.field };
		fieldModel.constraints.values = this.items.map(i => i.value);
		this.customService.checkConstraints(this.domain, fieldModel)
			.subscribe(res => {
				if (res) {
					this.field.constraints.values = this.items.map(i => i.value);
					if (this.defaultValue != null) {
						this.field.default = this.defaultValue;
					}
					this.onToggle();
				}
			});

	}

	/**
	 * Function to handle onclick Sort button event.
	 * Auto-sort the items array, sort order is alphabetic ascending (up).
	 */
	public toggleSort(): void {
		if (!this.sortType || this.sortType === this.DESCENDING_ORDER) {
			this.sortType = this.ASCENDING_ORDER;
		} else {
			this.sortType = this.DESCENDING_ORDER;
		}
		this.sortItems();
	}

	private sortItems(): void {
		this.items.sort(this.sortType === this.ASCENDING_ORDER ? ascendingSort : descendingSort);

		function ascendingSort(a, b) {
			if (a.value.toUpperCase() < b.value.toUpperCase()) {
				return -1;
			}
			if (a.value.toUpperCase() > b.value.toUpperCase()) {
				return 1;
			}
			return 0;
		}

		function descendingSort(a, b) {
			if (a.value.toUpperCase() > b.value.toUpperCase()) {
				return -1;
			}
			if (a.value.toUpperCase() < b.value.toUpperCase()) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * Function to handle onclick open/close gear icon button event.
	 * If it's open event, it preloads and initializes local variables.
	 */
	public onToggle(): void {
		this.show = !this.show;
		if (this.show) {
			this.load();
		} else {
			this.items = [];
		}
	}
}