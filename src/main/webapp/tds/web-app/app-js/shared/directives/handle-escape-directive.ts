import {Directive, ElementRef, OnInit, HostListener, Output, EventEmitter, Renderer2} from '@angular/core';
import {KEYSTROKE} from '../model/constants';

@Directive({ selector: '[tds-handle-escape]' })
export class UIHandleEscapeDirective implements OnInit {
	@Output() escPressed: EventEmitter<any> = new EventEmitter();

	constructor(private el: ElementRef, private renderer: Renderer2) {
	}

	ngOnInit() {
		// the form needs to have tabindex defined in order it can be able to detect keyboard events
		this.renderer.setAttribute(this.el.nativeElement, 'tabindex',  '0');
	}

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