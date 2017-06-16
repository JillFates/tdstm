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

	public items: any[] = [];
	public newItem = '';
	public show = false; // first time should open automatically.
	public defaultValue: string = null;

	constructor(private customService: CustomDomainService) { }

	private load(): void {
		this.newItem = '';
		this.defaultValue = null;
		if (this.field.constraints.values) {
			this.items = this.field.constraints.values.map(i => {
				return {
					deletable: true,
					value: i
				};
			});  // make a copy to work with.
			this.defaultValue = this.field.default;
		} else {
			this.customService.getDistinctValues(this.domain, this.field)
				.subscribe((value) => {
					this.items = value.map(i => {
						return {
							deletable: false,
							value: i
						};
					});
				});
		}
	}

	public getStyle(index) {
		if ((index % 2) === 0) {
			return { 'background-color': 'white' };
		}
	}

	public onAdd(): void {
		if (this.items.filter((i) => i.value === this.newItem).length === 0) {
			this.items.push({
				deletable: true,
				value: this.newItem
			});
			this.newItem = '';
		}
	}

	public onRemove(item: any): void {
		if (item.deletable) {
			this.items.splice(this.items.indexOf(item), 1);
		}
	}

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

	public onToggle(): void {
		this.show = !this.show;
		if (this.show) {
			this.load();
		} else {
			this.items = [];
		}
	}
}