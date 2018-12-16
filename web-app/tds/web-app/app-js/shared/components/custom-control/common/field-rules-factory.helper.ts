import {
	getNotNegativeValidationRule,
	getRangeValidationRule
} from './validation-rules.helper';

interface NumberConstraints {
	allowNegative: boolean;
	max: number;
	min: number;
	required: boolean;
}

/**
 * Factory function to create the validations rules to apply
 * @param componentType - used to determinate the set of rules to create
 * @return array of validation functions to apply
 */
export function fieldRulesFactory(componentType: string) {
	return {
		create() {
			switch (componentType) {
				case 'number' :
					return getRulesForNumberField();

				default:
					// no rules
					return getEmptyRules();
			}
		}
	}
}

/**
*	Defines the set of validation rules for the fields of type number
* 	@return array of validation functions to apply
*/
function getRulesForNumberField()  {
	const defaultConstraints: NumberConstraints = {
		allowNegative: false,
		max: null,
		min: null,
		required: false
	};

	return (constraints: NumberConstraints) => {
		const rules  = [];
		const params = <NumberConstraints>{...defaultConstraints, ...constraints};

		if (params.max !== null || params.min || null) {
			rules.push(getRangeValidationRule(params.max, params.min))
		}

		if (!params.allowNegative) {
			rules.push(getNotNegativeValidationRule())
		}

		return rules;
	}
}

/**
 *  Get the empty rules array to apply to custom controls which don't define any validation
 * 	@return empty array
 */
function getEmptyRules() {
	return () => {
		return [];
	}
}
