import {Directive, HostListener, Output, EventEmitter} from '@angular/core';

@Directive({
	selector: '[tds-handle-double-click]'
})
export class UIHandleDoubleClickDirective {
	@Output() doubleClick: EventEmitter<any> = new EventEmitter();

	/**
	 * Listen for double click events, detecting one notify to the host component
	 * @param event
	 */
	@HostListener('dblclick', ['$event']) handleKeyboardEventUp(event: KeyboardEvent) {
		this.doubleClick.emit(event);
	}
}
