import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
	moduleId: module.id,
	selector: 'field-settings-imp',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/imp/field-settings-imp.component.html',
	styles: [`
		span { padding:0 2px; cursor: pointer;}
        .C { background-color: #F9FF90;}
        .I { background-color: #D4F8D4;}
        .N { background-color: #FFF;}
        .U { background-color: #F3F4F6;}
    `]
})
export class FieldSettingsImportanceComponent {
	@Input() model: string;
	@Input('edit') editMode: boolean;
	@Output() modelChange = new EventEmitter<string>();

	private values = ['C', 'I', 'N', 'U'];

	protected onModelChange(value: string): void {
		this.modelChange.emit(value);
	}

}