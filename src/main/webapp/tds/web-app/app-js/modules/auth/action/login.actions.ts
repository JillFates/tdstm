export class Login {
	static readonly type = '[Auth] Login';
	constructor(public payload: { username: string, password: string, authority?: string }) {}
}

export class LoginInfo {
	static readonly type = '[Auth] LoginInfo';
	constructor(public payload: { buildVersion: string}) {}
}

export class Logout {
	static readonly type = '[Auth] Logout';
}

export class SessionExpired {
	static readonly type = '[Auth] SessionExpired';
}

export class GetPermissions {
	static readonly type = '[Auth] getPermissions';
}

export class GetLicense {
	static readonly type = '[Auth] getLicense';
}
