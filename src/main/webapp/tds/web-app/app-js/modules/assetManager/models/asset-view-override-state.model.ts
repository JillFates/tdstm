import { IconDefinition } from '@fortawesome/fontawesome-common-types';
import { faLayerMinus, faLayerPlus } from '@fortawesome/pro-solid-svg-icons';

export interface OverrideState {
	isOverwritten: boolean;
	icon?: IconDefinition;
	color?: string;
	tooltip?: string;
}

export const ASSET_OVERRIDE_CHILD_STATE: OverrideState = {
	icon: faLayerMinus,
	color: 'green',
	isOverwritten: true,
	tooltip: 'Revert to Project View',
};
export const ASSET_OVERRIDE_PARENT_STATE: OverrideState = {
	icon: faLayerPlus,
	color: 'blue',
	isOverwritten: true,
	tooltip: 'Display Personal System View'
};
export const ASSET_NOT_OVERRIDE_STATE: OverrideState = {
	isOverwritten: false
};
