import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
	moduleId: module.id,
	selector: 'field-settings-imp',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/imp/field-settings-imp.component.html',
	styles: [`
		span { padding:0 5px; cursor: pointer;}
    `]
})
export class FieldSettingsImportanceComponent {
	@Input() model: string;
	@Input('edit') editMode: boolean;
	@Output() modelChange = new EventEmitter<string>();

	private values = ['C', 'I', 'N', 'U'];

	public onModelChange(value: string): void {
		this.modelChange.emit(value);
	}

}