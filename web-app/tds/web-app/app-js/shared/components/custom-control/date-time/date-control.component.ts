import {
	Component,
	forwardRef,
	OnInit,
	OnChanges,
	Input,
	Output,
	EventEmitter,
	SimpleChanges
} from '@angular/core';
import {
	NG_VALUE_ACCESSOR,
	NG_VALIDATORS
} from '@angular/forms';

import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {DateUtils} from '../../../utils/date.utils';
import {PreferenceService} from '../../../services/preference.service';
import {TDSCustomControl} from '../common/custom-control.component';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';
import {DateValidationConstraints} from '../../../../shared/model/validation-contraintes.model';

@Component({
	selector: 'tds-date-control',
	template: `
		<kendo-datepicker
			[title]="title"
			[value]="getDateValue()"
			[min]="minimum"
			[max]="maximum"
			(blur)="onBlur()"
			[format]="displayFormat"
			[tabindex]="tabindex"
			(valueChange)="onValueChange($event)"
			class="form-control">
		</kendo-datepicker>
	`,
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => TDSDateControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => TDSDateControlComponent),
			multi: true
		}
	]
})
export class TDSDateControlComponent extends TDSCustomControl implements OnChanges  {
	@Input('minimum') minimum;
	@Input('maximum') maximum;
	@Output() blur: EventEmitter<any> = new EventEmitter();
	protected displayFormat: string;

	constructor(
		private userPreferenceService: PreferenceService,
		protected validationRulesFactory: ValidationRulesFactoryService) {
			super(validationRulesFactory);
			this.displayFormat = userPreferenceService.getUserDateFormatForKendo();
	}

	/**
	 * On blur emit the current value to the host component
	*/
	onBlur() {
		this.onTouched();
		this.blur.emit(this.getDateValue());
	}

	/**
	 * On value Change on the component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onValueChange(value: Date): void {
		if (value && value !== null) {
			this.value = DateUtils.formatDate(value, DateUtils.SERVER_FORMAT_DATE)
		} else {
			this.value = null;
		}
		console.log('Value:', this.value);
		this.onTouched();
	}

	ngOnChanges(inputs: SimpleChanges) {
		const dateConstraints: DateValidationConstraints = {
			required: this.required,
			maxDate: this.maximum,
			minDate: this.minimum
		};

		this.setupValidatorFunction(CUSTOM_FIELD_TYPES.Date, dateConstraints);
	}

	getDateValue(): any {
		if (!this.value) {
			return null;
		}

		let localDateFormatted = DateUtils.getDateFromGMT(this.value);
		return  DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATE);
	}
}