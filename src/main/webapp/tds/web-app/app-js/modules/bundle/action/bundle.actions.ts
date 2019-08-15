export class SetBundle {
	static readonly type = '[Bundle] setBundle';
	constructor(public payload: { id: number, name: string }) {}
}
