import {Component, Input, OnInit} from '@angular/core';

import {PowerModel, PowerUnits} from './model/power.model';
import {convert} from './units-converter.helper';

@Component({
	selector: 'tds-power',
	templateUrl: 'power.component.html'
})
export class PowerComponent implements  OnInit {
	readonly units =   PowerUnits;
	@Input() model: PowerModel;
	constructor() {
		console.log('Constructor');
	}

	ngOnInit() {
		console.log('On init');
		/*
		Request URL: http://localhost:8080/tdstm/project/setPower
		Request Method: POST
		Type: Watts/Amps
		 */
	}

	onUnitsChange(unit: string): void {
		const x = convert(unit, this.model);
		console.log('converted:');
		console.log(x);
	}

}