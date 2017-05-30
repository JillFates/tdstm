export class FieldSettingsModel {
	field: string;
	label: string;
	tip: string;
	udf: boolean;
	shared: boolean;
	imp: 'C' | 'I' | 'N' | 'U';
	required: boolean;
	show: boolean;
	order?: number;
	type?: 'String' | 'Number' | 'Boolean' | '';
	length?: number;
	default?: any;
	control?: 'Select' | 'YesNo' | 'Number' | '';
	option?: Array<string> | any;

	constructor() {
		this.udf = true;
	}
}
