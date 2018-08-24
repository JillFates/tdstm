export enum CheckboxStates {
	checked,
	indeterminate,
	unchecked
}

export interface CheckboxState {
	currentState: CheckboxStates;
	affectItems: boolean;
}
