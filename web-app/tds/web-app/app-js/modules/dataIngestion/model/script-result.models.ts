export class ScriptTestResultModel {
	consoleLog: string;
	isValid: boolean;
	error?: string;
	domains: Array<any> = []; // this contains {domain, fields(columns, data(rows)}

	constructor() {
		this.consoleLog = null;
		this.isValid = false;
		this.domains = [];
	}
}

export class ScriptValidSyntaxResultModel {
	errors: Array <{
			endColumn: number,
			endLine: number,
			fatal: boolean,
			message: string,
			startColumn: number,
			startLine: number
		}>;
	validSyntax: boolean;

	constructor() {
		this.validSyntax = false;
	}
}

export class ScriptConsoleSettingsModel {
	top: number;
	left: number;
	height: number;
	width: number;
	scriptTestResult: ScriptTestResultModel;

	constructor() {
		this.top = 30;
		this.left = 30;
		this.height = 500;
		this.width = 600;
		this.scriptTestResult = new ScriptTestResultModel();
	};
}