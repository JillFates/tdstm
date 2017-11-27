import { Component, AfterViewInit, ViewChild, ElementRef, Output, EventEmitter, Input } from '@angular/core';
import * as CodeMirror from 'codemirror/lib/codemirror';
import 'codemirror/mode/groovy/groovy';

declare var jQuery: any;
@Component({
	selector: 'code-mirror',
	template: '<textarea name="" id="" cols="30" rows="10" #codeMirror></textarea>',
	exportAs: 'codeMirror'
}) export class CodeMirrorComponent implements AfterViewInit {
	@ViewChild('codeMirror') el: ElementRef;
	@Output() change = new EventEmitter<{ newValue: string, oldValue: string }>();
	@Input() model: string;
	@Output() modelChange = new EventEmitter<string>();
	instance;

	ngAfterViewInit(): void {
		console.log(this.el, CodeMirror);
		this.instance = CodeMirror.fromTextArea(this.el.nativeElement, {
			mode: 'groovy',
			lineNumbers: true
		});

		this.instance.setValue(this.model);
		this.instance.on('change', () => {
			this.change.emit({ newValue: this.instance.getValue(), oldValue: this.model });
			this.modelChange.emit(this.instance.getValue());
		});
	}

}