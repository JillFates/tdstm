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
	control?: 'List' | 'String' | 'YesNo' | '';
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

export class FieldImportance {
	public static C = {
		name: 'critical',
		color: '#F9FF90'
	};
	public static I = {
		name: 'important',
		color: '#D4F8D4'
	};
	public static N = {
		name: 'normal',
		color: '#DDDDDD'
	};
	public static U = {
		name: 'unimportant',
		color: '#F4F4F4'
	};
}
