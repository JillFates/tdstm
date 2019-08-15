export class SetEvent {
	static readonly type = '[Event] setEvent';
	constructor(public payload: { id: number, name: string }) {}
}
