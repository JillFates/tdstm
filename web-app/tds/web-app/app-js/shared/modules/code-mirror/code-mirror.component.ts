import { Component, AfterViewInit, ViewChild, ElementRef, Output, EventEmitter, Input } from '@angular/core';
import * as CodeMirror from 'codemirror/lib/codemirror';
import 'codemirror/mode/groovy/groovy';
import 'codemirror/mode/javascript/javascript';

declare var jQuery: any;
@Component({
	selector: 'code-mirror',
	template: '<textarea  #codeMirror></textarea>',
	exportAs: 'codeMirror'
})
export class CodeMirrorComponent implements AfterViewInit {
	@ViewChild('codeMirror') el: ElementRef;
	@Output() change = new EventEmitter<{ newValue: string, oldValue: string }>();
	@Input() model: string;
	@Input() mode;
	@Output() modelChange = new EventEmitter<string>();
	instance;

	ngAfterViewInit(): void {
		this.instance = CodeMirror.fromTextArea(this.el.nativeElement, {
			mode: this.mode,
			lineNumbers: true
		});
		this.instance.setValue(this.model);
		this.instance.on('change', () => {
			this.change.emit({ newValue: this.instance.getValue(), oldValue: this.model });
			this.modelChange.emit(this.instance.getValue());
		});
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
}