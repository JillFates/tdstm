export class SetProject {
	static readonly type = '[Project] setProject';
	constructor(public payload: { id: number, name: string, logoUrl: string }) {}
}

export class SetDefaultProject {
	static readonly type = '[Project] setDefaultProject';
	constructor(public payload: { id: number, name: string, logoUrl: string }) {}
}