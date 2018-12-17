
/**
 * Definition of field validation rules
 */
import {Injectable} from '@angular/core'
import {isNil, isEmpty} from 'ramda';

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
}
