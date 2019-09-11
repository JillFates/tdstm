import {Component, Input, Output, EventEmitter} from '@angular/core';

import {PowerModel, PowerUnits} from './model/power.model';
import {ModelService} from '../../../modules/assetExplorer/service/model.service';
import {convert} from './units-converter.helper';

@Component({
	selector: 'tds-power',
	templateUrl: 'power.component.html'
})
export class PowerComponent {
	readonly units =   PowerUnits;
	@Input() model: PowerModel;
	@Input() tabindex: string;
	@Output() change: EventEmitter<any> = new EventEmitter<any>();
	constructor(
		private modelService: ModelService
	) {
		console.log('Constructor');
	}

	/**
	 * convert to standard units and report the change
	 */
	setStandardPower() {
		this.model.design = Math.floor(this.model.powerNameplate * 0.5);
		this.model.use = Math.floor(this.model.powerNameplate * 0.33);
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

				this.model.powerNameplate = converted.powerNameplate;
				this.model.design = converted.design;
				this.model.use = converted.use;
				this.reportChanges();
			});
	}

}