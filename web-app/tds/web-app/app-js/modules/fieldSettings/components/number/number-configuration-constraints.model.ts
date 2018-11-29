export class NumberConfigurationConstraintsModel {
	required: boolean;
	maxRange: number;
	minRange: number;
	precision: number;
	separator: boolean;
	allowNegative: boolean;
	format: string;
	isDefaultConfig?: boolean;
}