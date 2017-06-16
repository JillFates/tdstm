/**
 * Created by David Ontiveros on 5/31/2017.
 */
import { Component, Input, ViewChild, ViewEncapsulation } from '@angular/core';
import { SortableComponent } from '@progress/kendo-angular-sortable';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { CustomDomainService } from '../../service/custom-domain.service';

@Component({
	moduleId: module.id,
	selector: 'selectlist-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/select-list/selectlist-configuration-popup.component.html',
	encapsulation: ViewEncapsulation.None,
	styles: [``]
})

export class SelectListConfigurationPopupComponent {

	@Input() domain: string;
	@Input() field: FieldSettingsModel;
	@ViewChild('kendoSortableInstance') kendoSortableInstance: SortableComponent;

	public items: string[] = [];
	public newItem = '';
	public show = true; // first time should open automatically.
	public defaultValue: string = null;

	constructor(private customService: CustomDomainService) { }

	private load(): void {
		this.newItem = '';
		this.defaultValue = null;
		console.log(this.field.constraints.values);
		if (this.field.constraints.values) {
			this.items = this.field.constraints.values.slice();  // make a copy to work with.
			this.defaultValue = this.field.default;
		} else {
			this.customService.getDistinctValues(this.domain, this.field)
				.subscribe((value) => {
					this.items = value;
				});
		}
	}

	public getStyle(index) {
		if ((index % 2) === 0) {
			return { 'background-color': 'white' };
		}
	}

	public onAdd(): void {
		if (this.searchItem(this.newItem, false) === null) {
			this.items.push(this.newItem);
			this.newItem = '';
		}
	}

	public onRemove(item: string): void {
		this.searchItem(item, true);
	}

	public onSave(): void {
		let fieldModel = { ...this.field };
		fieldModel.constraints.values = this.items;
		this.customService.checkConstraints(this.domain, fieldModel)
			.subscribe(res => {
				if (res) {
					this.field.constraints.values = this.items;
					if (this.defaultValue != null) {
						this.field.default = this.defaultValue;
					}
					this.onToggle();
				}
			});

	}

	private searchItem(item: string, remove: boolean): string {
		for (let i = this.items.length - 1; i >= 0; i--) {
			if (this.items[i] === item) {
				if (remove) {
					this.items.splice(i, 1);
					if (this.defaultValue === item) {
						this.defaultValue = null;
					}
					return null;
				}
				return this.items[i];
			}
		}
		return null;
	}

	public onToggle(): void {
		this.show = !this.show;
		if (this.show) {
			this.load();
		} else {
			this.items = [];
		}
	}
}