export class Login {
	static readonly type = '[Auth] Login';
	constructor(public payload: { username: string, password: string }) {}
}

export class Logout {
	static readonly type = '[Auth] Logout';
}

export class Permissions {
	static readonly type = '[Auth] getPermissions';
}

export class LicenseInfo {
	static readonly type = '[Auth] getLicenseInfo';
}
