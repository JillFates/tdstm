interface ValidationConstraints {
	required: boolean;
}

// constraints for custom number control
export interface NumberValidationConstraints extends ValidationConstraints{
	allowNegative: boolean;
	max: number;
	min: number;
}

// constraints for custom date control
export interface DateValidationConstraints extends ValidationConstraints{
	maxDate: Date;
	minDate: Date;
}


// constraints for custom date time control
export interface DateTimeValidationConstraints extends ValidationConstraints{
}
