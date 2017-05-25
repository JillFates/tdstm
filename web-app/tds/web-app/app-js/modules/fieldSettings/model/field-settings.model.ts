export class FieldSettingsModel {
	key: string;
	label: string;
	help: string;
	shared: boolean;
	importance: 'C' | 'I' | 'N' | 'U';
	required: boolean;
	display: boolean;
	type: 'String' | 'Number' | 'Boolean';
	length?: number;
	default?: any;
	control?: 'Select' | 'YesNo' | 'Number';
	controlOpt?: Array<string> | any;
}
