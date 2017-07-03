export class FieldSettingsModel {
	field: string;
	label: string;
	tip: string;
	udf: boolean;
	shared: boolean;
	imp: 'C' | 'I' | 'N' | 'U';
	show: boolean;
	order?: number;
	length?: number;
	default?: any;
	control?: 'Select List' | 'String' | 'YesNoUnknow' | '';
	constraints: ConstraintModel;
	style?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';

	constructor() {
		this.udf = true;
		this.imp = 'N';
	}
}
export class ConstraintModel {
	required: boolean;
	minSize?: Number;
	maxSize?: Number;
	values?: Array<string>;
}
