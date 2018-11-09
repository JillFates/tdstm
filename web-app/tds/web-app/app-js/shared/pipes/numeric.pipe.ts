import {Pipe, PipeTransform} from '@angular/core';
import { formatNumber } from '@telerik/kendo-intl';
import {NumberConfigurationConstraintsModel} from '../../modules/fieldSettings/components/number/number-configuration-constraints.model';
import {NumberControlHelper} from '../components/custom-control/number/number-control.helper';

/**
 * Usage:
 *		{{ myDateValue | tdsNumber : {constraints} }}
 */
@Pipe({
	name: 'tdsNumber'
})
export class NumericPipe implements PipeTransform {

	/**
	 * Transform.
	 * @param {string} value
	 * @param args should be #NumberConfigurationConstraintsModel type object.
	 * @returns {any}
	 */
	transform(value: string, args?: any): any {
		if (!value) {
			return '';
		}
		let number = +value;
		let constraints: NumberConfigurationConstraintsModel = args ? args : new NumberConfigurationConstraintsModel();
		return formatNumber(number, NumberControlHelper.buildFormat(constraints));
	}
}