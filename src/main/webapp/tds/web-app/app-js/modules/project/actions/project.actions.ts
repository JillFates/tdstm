export class SetProject {
	static readonly type = '[Project] setProject';
	constructor(public payload: { id: number, name: string, logoUrl: string }) {}
}