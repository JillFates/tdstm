import {Component, ViewChild, ElementRef, Output, EventEmitter, Input, OnInit} from '@angular/core';

@Component({
	selector: 'code-mirror',
	template: '<textarea  #codeMirror></textarea>',
	exportAs: 'codeMirror'
})
export class CodeMirrorComponent implements OnInit {
	@ViewChild('codeMirror') el: ElementRef;
	@Output() change = new EventEmitter<{ newValue: string, oldValue: string }>();
	@Input() model: string;
	@Input() mode;
	@Output() modelChange = new EventEmitter<string>();
	instance;
	CodeMirror;

	/**
	 * Get Code Mirror when initializing the component
	 */
	ngOnInit(): void {
		this.getCodeMirrorLibrary().then(component => {
			this.CodeMirror = component.default;

			this.instance = this.CodeMirror.fromTextArea(this.el.nativeElement, {
				mode: this.mode,
				lineNumbers: true
			});
			this.instance.setValue(this.model);
			this.instance.on('change', () => {
				this.change.emit({newValue: this.instance.getValue(), oldValue: this.model});
				this.modelChange.emit(this.instance.getValue());
			});
		})
	}

	/**
	 * Disable the Input
	 * @param {boolean} disable
	 */
	public setDisabled(disable: boolean): void {
		this.instance.options.disableInput = disable;
	}

	public addSyntaxErrors(lineNumbers: Array<number>) {
		for (let line of lineNumbers) {
			this.instance.addLineClass(line, 'background', 'line-with-syntax-errors');
		}
	}

	public clearSyntaxErrors(): void {
		const elems = this.instance.display.lineDiv.querySelectorAll('.line-with-syntax-errors');
		for (let elem of elems) {
			elem.classList.remove('line-with-syntax-errors');
		}
	}

	/**
	 * Get Code Mirror Async
	 * @returns {any}
	 */
	public getCodeMirrorLibrary(): any {
		return import(
			/* webpackChunkName: "codemirror" */
			/* webpackMode: "lazy" */
			'codemirror/lib/codemirror')
			.catch(error => 'An error occurred while loading the component');
	}
}