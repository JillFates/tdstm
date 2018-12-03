import {NumberConfigurationConstraintsModel} from '../../../../modules/fieldSettings/components/number/number-configuration-constraints.model';

export class NumberControlHelper {

	public static readonly DEFAULT_NUMBER_FORMAT = '0';

	/**
	 * Builds a format based on the number constraints.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 * @returns {string}
	 */
	public static buildFormat(constraints: NumberConfigurationConstraintsModel): string {
		let format = this.DEFAULT_NUMBER_FORMAT;
		if (constraints.precision > 0) {
			if (constraints.separator) {
				format = `n${constraints.precision}`;
			} else {
				format = '0.';
				for (let i = 0; i < constraints.precision; i++) {
					format = format.concat('0');
				}
			}
		} else if (constraints.separator) {
			format = 'n';
		}
		return format;
	}

	/**
	 * Init the model values if is a new number field configuration.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 */
	public static initConfiguration(constraints: NumberConfigurationConstraintsModel): void {
		if (!constraints.minRange && !constraints.maxRange && !constraints.precision && !constraints.separator
			&& !constraints.allowNegative && !constraints.format) {
			constraints.isDefaultConfig = true;
		}
		constraints.minRange = constraints.minRange ? constraints.minRange : 0;
		constraints.maxRange = constraints.maxRange ? constraints.maxRange : null;
		constraints.precision = constraints.precision ? constraints.precision : 0;
		constraints.separator = constraints.separator ? constraints.separator : false;
		constraints.allowNegative = constraints.allowNegative ? constraints.allowNegative : false;
		constraints.format = constraints.format ? constraints.format : this.buildFormat(constraints);
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

	/**
	 * Deletes the Number Constraints
	 * @param {NumberConfigurationConstraintsModel} constraints
	 */
	public static cleanNumberConstraints(constraints: NumberConfigurationConstraintsModel): void {
		delete constraints.minRange;
		delete constraints.maxRange;
		delete constraints.precision;
		delete constraints.separator;
		delete constraints.allowNegative;
		delete constraints.format;
		delete constraints.isDefaultConfig;
	}
}