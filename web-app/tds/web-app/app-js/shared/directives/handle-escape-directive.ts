import {Directive, HostListener, Output, EventEmitter} from '@angular/core';
import {KEYSTROKE} from '../model/constants';

@Directive({ selector: '[tds-handle-escape]' })
export class UIHandleEscapeDirective {
	@Output() escPressed: EventEmitter<any> = new EventEmitter();
	@HostListener('keyup', ['$event']) handleKeyboardEventUp(event: KeyboardEvent) {
		if (event) {
			const isEscPressed = event.code === KEYSTROKE.ESCAPE;
			// handle issue pressing esc key on kendo-upload file dialog
			const isFileDialog = Boolean(event.target && event.target['type'] === 'file');
			const isConfirmationDialog = event.target['id'] === 'tdsUiPrompt';
			const isKendoDropdown = Boolean(event.target && event.target['classList'] && event.target['classList'][0] === 'k-dropdown-wrap');

			if (!isFileDialog &&  !isConfirmationDialog && !isKendoDropdown && isEscPressed) {
				this.escPressed.emit(event);
			}
		}
	}
}