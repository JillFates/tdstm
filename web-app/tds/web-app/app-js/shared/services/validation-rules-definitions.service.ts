
/**
 * Definition of field validation rules
 */
import {Injectable} from '@angular/core'
import {isNil} from 'ramda';

@Injectable()
export class ValidationRulesDefinitionsService {
	/**
	 *	Defines the rule function to validate a range of numbers
	 * 	@return Curried function that determines if the provided range meets the rule
	 */
	rangeValidationRule(maxValue: number, minValue: number): Function {
		return (c: any) => {
			const err = {
				range: {
					given: c.value,
					max: maxValue || Number.MAX_VALUE,
					min: minValue || Number.MIN_VALUE
				}
			};

			return () => !isNil(c.value) && (c.value > maxValue || c.value < minValue) ? err : null;
		}
	}

	/**
	 *	Defines the rule function to determine if a number is not negative
	 * 	@return Curried function that determines if the provided value meets the rule
	 */
	notNegativeValidationRule(): Function {
		return (c: any) => {
			const err = {
				notNegative: {
					given: c.value
				}
			};

			return () => (!isNil(c.value) && c.value < 0) ? err : null;
		}
	}
}
