// Angular
import {Component, Inject, Input, OnInit, ViewEncapsulation} from '@angular/core';
// Model
import {FieldSettingsModel} from '../../model/field-settings.model';
// Component
import {ConfigurationCommonComponent} from '../configuration-common/configuration-common.component';
// Service
import {CustomDomainService} from '../../service/custom-domain.service';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';

/**
 *
 */
@Component({
	selector: 'selectlist-configuration-popup',
	templateUrl: 'selectlist-configuration-popup.component.html',
	encapsulation: ViewEncapsulation.None,
	exportAs: 'selectlistConfig',
	styles: [
			`.pointer {
            cursor: pointer;
        }`
	]
})

export class SelectListConfigurationPopupComponent extends ConfigurationCommonComponent implements OnInit {

	public items: any[] = [];
	public savedItems: any[] = [];
	public _newItem = '';
	private newItemValid = false;
	public show = false; // first time should open automatically.
	public defaultValue: string = null;
	public sortType: string;
	public ASCENDING_ORDER = 'asc';
	public DESCENDING_ORDER = 'desc';

	public field: FieldSettingsModel;
	public domain: string;

	/**
	 * Class constructor.
	 * @param customService Service to obtain distinct values.
	 */
	constructor(
		private customService: CustomDomainService,
		public dialogService: DialogService,
		public translate: TranslatePipe) {
		super(dialogService, translate);
	}

	ngOnInit() {
		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			disabled: () => this.items.length === 0 || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.field = this.data.fieldSettingsModel;
		this.domain = this.data.domain;

		this.setField(this.field);

		setTimeout(() => {
			this.setTitle(`List Configuration - ${this.field.field}`);
		});

		this.load();
	}

	/**
	 * Intializes current component based on the @field input parameter.
	 * Makes a service call to get pre-build distinct values from system.
	 */
	private load(): void {
		this.items = [];
		this.newItem = '';
		this.sortType = null;
		this.defaultValue = null;
		this.customService.getDistinctValues(this.domain, this.field)
			.subscribe((distinctValues: string[]) => {

				let indexOfBlank = distinctValues.indexOf('');
				// Add blank(empty) option if its a required field or remove it if opposite.
				if (this.field.constraints.required && indexOfBlank !== ValidationUtils.NOT_FOUND) { // If REQUIRED remove blank(empty) option
					distinctValues.splice(indexOfBlank, 1);
				} else if (!this.field.constraints.required && indexOfBlank === ValidationUtils.NOT_FOUND) { // If NOT REQUIRED and distinctValues have no empty option register it.
					if (this.field.constraints.values && this.field.constraints.values.indexOf('') === ValidationUtils.NOT_FOUND) { // If it's not yet on current field values add it to items list (not deletable)
						this.items.splice(0, 0, {deletable: false, value: ''});
					}
				}

				// build the option items list based on distinctValues and current stored values (maintain the order).
				if (this.field.constraints.values) {
					for (let option of this.field.constraints.values) {
						let indexOfDistinctValue = distinctValues.indexOf(option);
						if (indexOfDistinctValue === ValidationUtils.NOT_FOUND) {
							this.items.push({deletable: true, value: option});
						} else {
							distinctValues.splice(indexOfDistinctValue, 1);
							this.items.push({deletable: false, value: option});
						}
					}
				}

				// add any distinctValue that was not found on original field.constraint.values at the end.
				this.items = this.items.concat(distinctValues.map(o => {
					return {deletable: false, value: o};
				}));
				this.savedItems = this.items.slice();
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
			return {'background-color': '#f6f6f6'};
		}
	}

	/**
	 * See if the list has been modified since last save.
	 */
	isDirty(): boolean {
		return (JSON.stringify(this.items) !== JSON.stringify(this.savedItems) ||
			this.field.default !== this.defaultValue) || this.newItem.length > 0;
	}

	/**
	 * Function to handle event click on + Add button
	 * Adds a new item to the list.
	 */
	public onAdd(): void {
		if (this.newItemValid) {
			this.items.push({
				deletable: true,
				value: this.newItem
			});
			this.newItem = '';
			// sort if needed.
			if (this.sortType) {
				this.sortItems();
			}
		}
		this.items = this.items.slice(0);
	}

	/**
	 * Remove a specific item from the current config select list
	 * @param item The item to be removed.
	 */
	public onRemove(item: any): void {
		if (item.deletable) {
			this.items.splice(this.items.indexOf(item), 1);
		}
		this.items = this.items.slice(0);
	}

	/**
	 * Function to handle onclick Save button event.
	 * Saves the current select list configuration.
	 * Simply sets current list into the field model and it's default value.
	 */
	public onSave(): void {
		this.displayWarningMessage(this.translate.transform('FIELD_SETTINGS.WARNING_VALIDATION_CHANGE')).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				let fieldModel = {...this.field};
				fieldModel.constraints.values = this.items.map(i => i.value);
				this.customService.checkConstraints(this.domain, fieldModel)
					.subscribe(res => {
						if (res) {
							this.field.constraints.values = this.items.map(i => i.value);
							if (this.defaultValue != null) {
								this.field.default = this.defaultValue;
							}
							this.onAcceptSuccess({isDirty: this.isDirty()});
						}
					});
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
		this.items = this.items.slice(0);
	}

	/**
	 * Sort items by type.
	 */
	private sortItems(): void {
		this.items.sort(this.sortType === this.ASCENDING_ORDER ? ascendingSort : descendingSort);

		function ascendingSort(a, b) {
			if (a.value === null || b.value === null) {
				return -1;
			}
			if (a.value.toUpperCase() < b.value.toUpperCase()) {
				return -1;
			}
			if (a.value.toUpperCase() > b.value.toUpperCase()) {
				return 1;
			}
			return 0;
		}

		function descendingSort(a, b) {
			if (a.value === null || b.value === null) {
				return -1;
			}
			if (a.value.toUpperCase() > b.value.toUpperCase()) {
				return -1;
			}
			if (a.value.toUpperCase() < b.value.toUpperCase()) {
				return 1;
			}
			return 0;
		}
	}

	/*
	 * oluna: I added this setter to change the newItemValid property oly when the value changes instead of
	 * having a computed property that recalculates everytime. A better approach might be to have a catched property??
	 */
	@Input()
	set newItem(value: string) {
		this._newItem = value;
		this.checkNewItemValid();
	}

	get newItem(): string {
		return this._newItem;
	}

	/**
	 * Check if the _newItem value is not Blank and is not already in the list of existing values
	 */
	private checkNewItemValid(): void {
		const trimValue = this._newItem.trim();
		this.newItemValid = (trimValue.length >= 0) &&
			(this.items.filter((i) => i.value === trimValue).length === 0);
	}
}
