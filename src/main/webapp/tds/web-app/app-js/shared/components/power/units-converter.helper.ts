import {PowerModel, PowerUnits} from './model/power.model';

const FACTOR = 120;
const [watts, amps] = PowerUnits;

const converter = {
	[watts]: (model: PowerModel): PowerModel => {
		let {namePlate, design, use} = model;

		return {
			namePlate: namePlate * FACTOR,
			design: design * FACTOR,
			use: use * FACTOR
		}
	} ,
	[amps]: (model: PowerModel): PowerModel => {
		let {namePlate, design, use} = model;

		return {
			namePlate: namePlate / FACTOR,
			design: design / FACTOR,
			use: use / FACTOR
		}
	}
};

export function convert(unit: string, model: PowerModel): PowerModel {
	return converter[unit] ? converter[unit](model)  : null;
}
