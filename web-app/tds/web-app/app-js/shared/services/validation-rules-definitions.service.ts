
/**
 * Definition of field validation rules
 */
import {Injectable} from '@angular/core'
import {isNil, isEmpty} from 'ramda';

import {DateUtils} from './../../shared/utils/date.utils';

@Injectable()
export class ValidationRulesDefinitionsService {
	/**
	 *	Defines the angular form  function to validate a required field has a value
	 * 	@return Curried function that determines if the rule is fulfilled
	 */
	requiredValidationRule(): Function {
		return (c: any) => {
			const err = {
				isRequired: {
					message: `Field is required`
				}
			};

			return () => isNil(c.value) || isEmpty(c.value) ? err : null;
		}
	}

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

	/**
	 *	Defines the rule function to validate that a date cannot be greather than a max date
	 * 	@return Curried function that determines if the provided range meets the rule
	 */
	maxDateValidationRule(maxDate: Date): Function {
		return (c: any) => {
			const err = {
				'maxDate': {
					given: c.value,
					max: maxDate
				}
			};

			return () => {
				// if value comes as string cast it to date
				const value = DateUtils.stringDateToDate(c.value);
				// (c.value && c.value.toDateString) ? c.value : new Date(DateUtils.getDateFromGMT(c.value));

				return !isNil(value) && (value > maxDate) ? err : null;
			};
		}
	}

	/**
	 *	Defines the rule function to validate that a date cannot be lower than a min date
	 * 	@return Curried function that determines if the provided range meets the rule
	 */
	minDateValidationRule(minDate: Date): Function {
		return (c: any) => {
			const err = {
				'minDate': {
					given: c.value,
					min: minDate
				}
			};

			return () => {
				// if value comes as string cast it to date
				const value = DateUtils.stringDateToDate(c.value);

				return !isNil(value) && (value < minDate) ? err : null;
			};
		}
	}

	/* Add validations rules as needed */
}
