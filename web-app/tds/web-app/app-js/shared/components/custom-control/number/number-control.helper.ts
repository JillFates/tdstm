import {NumberConfigurationConstraintsModel} from '../../../../modules/fieldSettings/components/number/number-configuration-constraints.model';

export class NumberControlHelper {

	/**
	 * Builds a format based on the number constraints.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 * @returns {string}
	 */
	public static buildFormat(constraints: NumberConfigurationConstraintsModel): string {
		let format = '';
		if (constraints.precision > 0 || constraints.separator) {
			format = `n${(constraints.precision > 0 ? constraints.precision : '')}`;
		}
		return format;
	}

	/**
	 * Init the model values if is a new number field configuration.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 */
	public static initConfiguration(constraints: NumberConfigurationConstraintsModel): void {
		constraints.minRange = constraints.minRange ? constraints.minRange : 0;
		constraints.maxRange = constraints.maxRange ? constraints.maxRange : 1000;
		constraints.precision = constraints.precision ? constraints.precision : 0;
		constraints.separator = constraints.separator ? constraints.separator : false;
		constraints.allowNegative = constraints.allowNegative ? constraints.allowNegative : false;
	}

	/**
	 * Init the model values if is a new number field configuration.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 */
	public static buildConfiguration(
		minRange: number, maxRange: number, decimalPlaces: number, format: string,
		useThousandSeparator: boolean, allowNegatives: boolean, required: boolean): NumberConfigurationConstraintsModel {
			let constraints = new NumberConfigurationConstraintsModel();
			constraints.minRange = minRange;
			constraints.maxRange = maxRange;
			constraints.precision = decimalPlaces;
			constraints.separator = useThousandSeparator;
			constraints.allowNegative = allowNegatives;
			constraints.format = format;
			constraints.required = required;
			return constraints;
	}
}