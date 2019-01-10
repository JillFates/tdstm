export interface TDSButton {
	icon: string;
	title: string;
	tooltip?: string;
	hasAllPermissions?: boolean;
}

export enum TDSActionsButton {
	Add,
	Edit,
	Export,
	Cancel,
	Create,
	Close,
	Clone,
	Custom,
	Delete,
	Save
}
