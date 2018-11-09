import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NumberConfigurationConstraintsModel} from '../../../../modules/fieldSettings/components/number/number-configuration-constraints.model';
import {NumberControlHelper} from './number-control.helper';
import { formatNumber } from '@telerik/kendo-intl';

@Component({
	selector: 'tds-number-control',
	template: `
		<div>
			<!-- [min]="0" [max]="" -->
            <kendo-numerictextbox [format]="outputFormat"
								  [(ngModel)]="numberValue"
                                  [min]="constraints.minRange" [max]="constraints.maxRange"
                                  [autoCorrect]="true"
								  (ngModelChange)="onValueChange($event)"
                                  class="form-control">
			</kendo-numerictextbox>
		</div>
	`
})
export class NumberControlComponent implements OnInit {
	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('constraints') constraints: any;
	protected numberValue: number;
	protected outputFormat: string;

	constructor() {
		this.outputFormat = '';
	}

	/**
	 * On Init build the number format.
	 */
	ngOnInit(): void {
		this.numberValue = +this.value;
		NumberControlHelper.initConfiguration(this.constraints);
		this.outputFormat = NumberControlHelper.buildFormat(this.constraints);
	}

	/**
	 * Emit the value changed.
	 * @param $event
	 */
	onValueChange($event: any): void {
		this.valueChange.emit(this.numberValue);
	}
}