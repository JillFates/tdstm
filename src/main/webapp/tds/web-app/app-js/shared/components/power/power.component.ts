import {Component, Input, OnInit} from '@angular/core';

import {PowerModel, PowerUnits} from './model/power.model';
import {ModelService} from '../../../modules/assetExplorer/service/model.service';
import {convert} from './units-converter.helper';

@Component({
	selector: 'tds-power',
	templateUrl: 'power.component.html'
})
export class PowerComponent implements  OnInit {
	readonly units =   PowerUnits;
	@Input() model: PowerModel;
	constructor(
		private modelService: ModelService
	) {
		console.log('Constructor');
	}

	ngOnInit() {
		console.log('On init');
	}

	setStandardPower() {
		this.model.design = parseInt(this.model.namePlate.toString(), 10) * 0.5;
		this.model.use = parseInt(this.model.namePlate.toString(), 10) * 0.33;
	}

	onUnitsChange(unit: string): void {
		this.modelService.setPower(unit)
			.subscribe(() => {
				const converted = convert(unit, this.model);

				this.model.namePlate = converted.namePlate;
				this.model.design = converted.design;
				this.model.use = converted.use;
			});
	}

}