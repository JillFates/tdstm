/**
 * Helper used to convert among Amp and Watts units
 */
import {PowerModel, PowerUnits} from './model/power.model';

const FACTOR = 120;
const [watts, amps] = PowerUnits;

const converter = {
	[watts]: (model: PowerModel): PowerModel => {
		let {powerNameplate, design, use} = model;

		return {
			powerNameplate: powerNameplate * FACTOR,
			design: design * FACTOR,
			use: use * FACTOR
		}
	} ,
	[amps]: (model: PowerModel): PowerModel => {
		let {powerNameplate, design, use} = model;

		return {
			powerNameplate: powerNameplate / FACTOR,
			design: design / FACTOR,
			use: use / FACTOR
		}
	}
};

/**
 * Convert units among Watts and Amps
 * @param {string} unit
 * @param {PowerModel} model
 * @returns {PowerModel}
 */
export function convert(unit: string, model: PowerModel): PowerModel {
	return converter[unit] ? converter[unit](model)  : null;
}
