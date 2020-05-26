import {
	Component,
	ViewChild,
	ElementRef,
	Output,
	EventEmitter,
	Input,
	OnInit,
} from '@angular/core';
import * as CodeMirror from 'codemirror/lib/codemirror';

@Component({
	selector: 'code-mirror',
	template: '<textarea #codeMirror></textarea>',
	exportAs: 'codeMirror',
})
export class CodeMirrorComponent implements OnInit {
	@ViewChild('codeMirror', { static: true }) el: ElementRef;
	@Output() change = new EventEmitter<{
		newValue: string;
		oldValue: string;
	}>();
	@Input() model: string;
	@Input() mode;
	@Output() modelChange = new EventEmitter<string>();
	public instance;

	/**
	 * This stores (cache) the line errores currently present in the component.
	 * @type {any[]}
	 */
	private currentErrorLines: Array<number> = [];

	/**
	 * Get Code Mirror when initializing the component
	 */
	ngOnInit(): void {
		this.instance = CodeMirror.fromTextArea(this.el.nativeElement, {
			mode: this.mode,
			lineNumbers: true,
		});
		this.instance.setValue(this.model);
		this.instance.on('change', () => {
			this.change.emit({
				newValue: this.instance.getValue(),
				oldValue: this.model,
			});
			this.modelChange.emit(this.instance.getValue());
		});
	}

	/**
	 * Disable the Input
	 * @param {boolean} disable
	 */
	public setDisabled(disable: boolean): void {
		if (this.instance && this.instance.options) {
			this.instance.options.disableInput = disable;
			this.instance.options.readOnly = disable;
		}
	}

	/**
	 * Adds Syntax Error class to the given line numbers (lines index starts at 0)
	 * @param {Array<number>} lineNumbers
	 */
	public addSyntaxErrors(lineNumbers: Array<number>) {
		if (!this.currentErrorLines) {
			this.currentErrorLines = [];
		}
		for (let line of lineNumbers) {
			const lineHandle = this.instance.addLineClass(
				line,
				'background',
				'line-with-syntax-errors'
			);
			this.currentErrorLines.push(lineHandle);
		}
	}

	/**
	 * Clears out ALL the Syntax Error class of the given line numbers stored in currentErrorLines.
	 */
	public clearSyntaxErrors(): void {
		for (let line of this.currentErrorLines) {
			this.instance.removeLineClass(
				line,
				'background',
				'line-with-syntax-errors'
			);
		}
		this.currentErrorLines = [];
	}
}
