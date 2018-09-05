export enum CheckboxStates {
	checked,
	indeterminate,
	unchecked
}

export interface CheckboxState {
	current: CheckboxStates;
	affectItems: boolean;
}
