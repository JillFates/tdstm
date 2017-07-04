export class FieldSettingsModel {
	field: string;
	label: string;
	tip: string;
	udf: number;
	shared: number;
	imp: 'C' | 'I' | 'N' | 'U';
	show: number;
	order?: number;
	length?: number;
	default?: any;
	control?: 'Select List' | 'String' | 'YesNoUnknow' | '';
	constraints: ConstraintModel;
	style?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';

	constructor() {
		this.udf = 1;
		this.imp = 'N';
	}
}
export class ConstraintModel {
	required: Number;
	minSize?: Number;
	maxSize?: Number;
	values?: Array<string>;
}
