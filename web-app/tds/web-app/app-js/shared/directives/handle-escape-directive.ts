import {Directive, HostListener, Output, EventEmitter} from '@angular/core';
import {KEYSTROKE} from '../model/constants';

// Set the focus to the attached host element
@Directive({ selector: '[tds-handle-escape]' })
export class UIHandleEscapeDirective {
	@Output() escPressed: EventEmitter<void> = new EventEmitter();
	@HostListener('keyup', ['$event']) handleKeyboardEventUp(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.escPressed.emit();
		}
	}
}