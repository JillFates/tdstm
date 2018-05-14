import { SortInfo } from '../utils/sort.utils';

export const COLUMN_MIN_WIDTH = 360;

export const BooleanFilterData: Array<string> = ['True', 'False'];
export const DefaultBooleanFilterData = 'All';

export const Flatten = filter => {
	const filters = filter.filters;
	if (filters) {
		return filters.reduce((acc, curr) => acc.concat(curr.filters ? Flatten(curr) : [curr]), []);
	}
	return [];
};

export enum ActionType {
	VIEW,
	CREATE,
	EDIT
};

export class GridColumnModel {
	label: string;
	property: string;
	type: string;
	format?: string;
	width: number;
	locked ? = false;
	hidden ? = false;
	filter?: any;
	headerStyle?: any;
	headerClass?: Array<string>;
	cellStyle?: any;
	cellClass?: string | Array<string>;
	sort?: SortInfo;

	constructor() {
		this.type = 'text';
		this.width = COLUMN_MIN_WIDTH;
	}
}
