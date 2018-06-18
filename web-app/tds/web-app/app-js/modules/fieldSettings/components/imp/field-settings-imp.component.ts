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
	public colors = FIELD_COLORS;

	constructor(public fieldSettinsService: FieldSettingsService) {
	}

	ngOnInit() {
		this.values = Object.keys(this.colors);
	}

	public onModelChange(value: string): void {
		this.modelChange.emit(value);
	}

	public mapLegacyColor(value: string): string {
		return this.fieldSettinsService.mapColorFromLegacyImportantSchema(value);
	}

}