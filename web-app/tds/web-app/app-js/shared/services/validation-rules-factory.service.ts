/**
 * Factory to create validation rules by component type
 */

import {Injectable} from '@angular/core'
import {CUSTOM_FIELD_TYPES} from '../model/constants';
import {ValidationRulesDefinitionsService} from './validation-rules-definitions.service';
import {
	NumberValidationConstraints,
	DateValidationConstraints,
	DateTimeValidationConstraints
} from '../model/validation-contraintes.model';

@Injectable()
export class ValidationRulesFactoryService {
	constructor(private validationRulesDefinitions: ValidationRulesDefinitionsService) {
	}

	/**
	 * Factory function to create the validations rules to apply, those vary by component
	 * @param factoryType - used to determinate the set of rules to create (Number, date, dateTime, etc...)
	 * @return array of validation functions to apply
	 */
	createFieldRulesFactory(factoryType: CUSTOM_FIELD_TYPES) {
		const customRules = {
			[CUSTOM_FIELD_TYPES.Number] : this.getRulesForNumberField(),
			[CUSTOM_FIELD_TYPES.Date] : this.getRulesForDateField(),
			[CUSTOM_FIELD_TYPES.DateTime] : this.getRulesForDateTimeField()
		};

		return customRules[factoryType] || this.getEmptyRules();
	}

	/**
	 *	Defines the set of validation rules for the fields of type number
	 * 	@return array of validation functions to apply
	 */
	private getRulesForNumberField()  {
		const defaultConstraints: NumberValidationConstraints = {
			allowNegative: false,
			max: null,
			min: null,
			required: false
		};

		return (constraints: NumberValidationConstraints) => {
			const params = <NumberValidationConstraints>{...defaultConstraints, ...constraints};
			const rules  = [...this.getCommonValidationRules(params)];

			// specific validation rules for numbers
			if (params.max !== null || params.min || null) {
				rules.push(this.validationRulesDefinitions.rangeValidationRule(params.max, params.min))
			}

			if (!params.allowNegative) {
				rules.push(this.validationRulesDefinitions.notNegativeValidationRule())
			}

			return rules;
		}
	}

	/**
	 *	Defines the set of validation rules for the fields of type date
	 * 	@return array of validation functions to apply
	 */
	private getRulesForDateField()  {
		const defaultConstraints: DateValidationConstraints = {
			required: false
		};

		return (constraints: DateValidationConstraints) => {
			const params = <DateValidationConstraints>{...defaultConstraints, ...constraints};
			const rules  = [...this.getCommonValidationRules(params)];
			// specific validation rules for dates
			// ...

			return rules;
		}
	}

	/**
	 *	Defines the set of validation rules for the fields of type date time
	 * 	@return array of validation functions to apply
	 */
	private getRulesForDateTimeField()  {
		const defaultConstraints: DateTimeValidationConstraints = {
			required: false
		};

		return (constraints: DateTimeValidationConstraints) => {
			const params = <DateTimeValidationConstraints>{...defaultConstraints, ...constraints};
			const rules  = [...this.getCommonValidationRules(params)];
			// specific validation rules for date time
			// ...

			return rules;
		}
	}

	/**
	 *  Get the validation rules that apply to all custom fields
	 * 	@return array of validation functions
	 */
	private getCommonValidationRules(constraints: any) {
		const rules = [];

		if (constraints.required) {
			rules.push(this.validationRulesDefinitions.requiredValidationRule());
		}

		return rules;
	}

	/**
	 *  Get the empty rules array to apply to custom controls which don't define any validation
	 * 	@return empty array
	 */
	private getEmptyRules() {
		return () => {
			return [];
		}
	}

}
