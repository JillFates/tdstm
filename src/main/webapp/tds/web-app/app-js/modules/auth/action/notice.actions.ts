export class PostNotices {
	static readonly type = '[Auth] PostNotices';
}

export class PostNoticeAcceptance {
	static readonly type = '[Auth] Post Notices Acceptance';
	constructor(public payload: { id: number}) {}
}

export class PostNoticeRemove {
	static readonly type = '[Auth] Post Notices Remove';
	constructor(public payload: { id: number}) {}
}
