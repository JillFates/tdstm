/**
 * Created by David Ontiveros on 5/31/2017.
 */
import {Component, Input, ViewChild} from '@angular/core';
import { SortableComponent } from '@progress/kendo-angular-sortable';

@Component({
	moduleId: module.id,
	selector: 'selectlist-configuration-popup',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/popup/selectlist-configuration-popup.component.html',
	styles: [``]
})

export class SelectListConfigurationPopupComponent {

	@Input() field: any;
	@ViewChild('kendoSortableInstance') kendoSortableInstance: SortableComponent;

	public items: string[] = [];
	public newItem = '';
	public show = true; // first time should open automatically.
	public defaultValue: string = null;

	private load(): void {
		this.newItem = '';
		this.defaultValue = null;
		if (this.field.option) {
			this.items = this.field.option.slice();  // make a copy to work with.
			this.defaultValue = this.field.default;
		}
	}

	public getStyle(index) {
		if ((index % 2) === 0) {
			return {'background-color': 'white'};
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
		this.field.option = this.items;
		if (this.defaultValue != null) {
			this.field.default = this.defaultValue;
		}
		this.onToggle();
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