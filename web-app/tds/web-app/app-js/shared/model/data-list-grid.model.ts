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