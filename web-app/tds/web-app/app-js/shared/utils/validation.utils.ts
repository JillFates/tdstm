/**
 * Created by David Ontiveros on 7/5/2017.
 */

import {Injectable} from '@angular/core';

/**
 * Intended to be a helper validation class that can be used
 * across all components in order to make common validations.
 */
@Injectable()
export class ValidationUtils {

	/**
	 *
	 * @param value Value to be compared.
	 * @returns {boolean} True if it's a valid number, False otherwise.
	 */
	public static isValidNumber(value: any): boolean {
		if (isNaN(value) ) {
			return false;
		}
		return true;
	}
}
