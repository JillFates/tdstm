import {NumberConfigurationConstraintsModel} from '../../../../modules/fieldSettings/components/number/number-configuration-constraints.model';
import { formatNumber } from '@telerik/kendo-intl';

export class NumberControlHelper {

	/**
	 * Builds a format based on the number constraints.
	 * @param {NumberConfigurationConstraintsModel} constraints
	 * @returns {string}
	 */
	public static buildFormat(constraints: NumberConfigurationConstraintsModel): string {
		let format = '';
		if (constraints.decimalPlaces > 0 || constraints.useThousandSeparator) {
			format = `n${(constraints.decimalPlaces > 0 ? constraints.decimalPlaces : '')}`;
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
		constraints.decimalPlaces = constraints.decimalPlaces ? constraints.decimalPlaces : 0;
		constraints.useThousandSeparator = constraints.useThousandSeparator ? constraints.useThousandSeparator : false;
		constraints.allowNegatives = constraints.allowNegatives ? constraints.allowNegatives : false;
	}
}