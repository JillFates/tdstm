import {CUSTOM_FIELD_TYPES as CUSTOM_FIELD_CONTROL_TYPE} from '../../../shared/model/constants';
export {CUSTOM_FIELD_CONTROL_TYPE}

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
	control?: CUSTOM_FIELD_CONTROL_TYPE;
	constraints: ConstraintModel;
	style?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
	errorMessage?: string;
	toBeDeleted?: boolean;

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
	public static Y = {
		name: 'yellow',
		color: '#FAFF9B'
	};
	public static G = {
		name: 'green',
		color: '#D4F8D4'
	};
	public static B = {
		name: 'blue',
		color: '#A9D6F2'
	};
	public static P = {
		name: 'pink',
		color: '#FFA5B4'
	};
	public static O = {
		name: 'orange',
		color: '#FFC65E'
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

export const FIELD_COLORS = ['Y', 'G', 'P', 'B', 'O', 'N', 'U'];

export const FIELD_NOT_FOUND = 'Field Not Found';
