export class ScriptTestResultModel {
	consoleLog: string;
	data: Array<any>;
	isValid: boolean;
	error?: string;

	constructor() {
		this.consoleLog = null;
		this.data = null;
		this.isValid = false;
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
	top: string;
	left: string;
	height: string;
	width: string;
	scriptTestResult: ScriptTestResultModel;

	constructor() {
		this.top = '30px';
		this.left = '30px';
		this.height = '200px';
		this.width = '500px';
		this.scriptTestResult = new ScriptTestResultModel();
	};
}