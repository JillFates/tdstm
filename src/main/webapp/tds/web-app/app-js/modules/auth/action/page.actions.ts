export class SetPageChange {
	static readonly type = '[Auth] Set Page change';
	constructor(public payload: { path?: string }) {}
}
