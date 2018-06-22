import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FieldSettingsService } from '../../service/field-settings.service';
import { FIELD_COLORS } from '../../model/field-settings.model';

@Component({
	selector: 'field-settings-imp',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/imp/field-settings-imp.component.html',
	styles: [`
		span { padding:0 5px; cursor: pointer;}
    `]
})
export class FieldSettingsImportanceComponent implements  OnInit {
	@Input() model: string;
	@Input('edit') editMode: boolean;
	@Output() modelChange = new EventEmitter<string>();
	public values = [];

	constructor(public fieldSettinsService: FieldSettingsService) {
	}

	ngOnInit() {
		this.values = FIELD_COLORS;
	}

	public onModelChange(value: string): void {
		this.modelChange.emit(value);
	}
}