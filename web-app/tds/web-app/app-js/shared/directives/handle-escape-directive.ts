import {Directive, HostListener, Output, EventEmitter} from '@angular/core';
import {KEYSTROKE} from '../model/constants';

@Directive({ selector: '[tds-handle-escape]' })
export class UIHandleEscapeDirective {
	@Output() escPressed: EventEmitter<void> = new EventEmitter();
	@HostListener('keyup', ['$event']) handleKeyboardEventUp(event: KeyboardEvent) {
		if (event) {
			const isEscPressed = event.code === KEYSTROKE.ESCAPE;
			// handle issue pressing esc key on kendo-upload file dialog
			const isFileDialog = Boolean(event.target && event.target['type'] === 'file');

			if (!isFileDialog &&  isEscPressed) {
				this.escPressed.emit();
			}
		}
	}
}