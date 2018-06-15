export enum CHECK_ACTION {
	NONE = -1,
	VALID = 0,
	UNKNOWN = 1,
	INVALID = 2,
	IN_PROGRESS = 3,
};

export class OperationStatusModel {
	state: CHECK_ACTION;
	value: any;

	constructor() {
		this.state = CHECK_ACTION.UNKNOWN;
		this.value = null;
	}
}
