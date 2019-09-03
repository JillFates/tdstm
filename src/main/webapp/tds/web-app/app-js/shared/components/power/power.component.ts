import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';

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
	@Output() change: EventEmitter<any> = new EventEmitter<any>();
	constructor(
		private modelService: ModelService
	) {
		console.log('Constructor');
	}

	ngOnInit() {
		console.log('On init');
	}

	/**
	 * convert to standard units and report the change
	 */
	setStandardPower() {
		this.model.design = parseInt(this.model.namePlate.toString(), 10) * 0.5;
		this.model.use = parseInt(this.model.namePlate.toString(), 10) * 0.33;
		this.reportChanges();
	}

	/**
	 * Report about model changes to the host component
	 */
	reportChanges() {
		this.change.emit(this.model);
	}

	/**
	 * On change units execute the units conversion and report the change to the host component
	 * @param {string} unit
	 */
	onUnitsChange(unit: string): void {
		this.modelService.setPower(unit)
			.subscribe(() => {
				const converted = convert(unit, this.model);

				this.model.namePlate = converted.namePlate;
				this.model.design = converted.design;
				this.model.use = converted.use;
				this.reportChanges();
			});
	}

}