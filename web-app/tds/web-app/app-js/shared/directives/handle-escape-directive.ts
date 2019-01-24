import {Directive, ElementRef, OnInit, HostListener, Output, EventEmitter, Renderer2} from '@angular/core';
import {KEYSTROKE} from '../model/constants';

// controls that will be ignored
enum WhiteListControls {
	FileDialog,
	ConfirmationDialog,
	KendoDropDownOpenState
}

const IS_LIST_OPEN_ATTR = 'is-list-open';

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
			const triggerControl = this.getTriggerControl(event);

			// esc is pressed and the control not belongs to the whitelist control
			if (isEscPressed && !(triggerControl in WhiteListControls)) {
				this.escPressed.emit(event);
			}
		}
	}

	/**
	 * Determines if the control which started the key press events belongs to the whitelist controls
	 * @event contains the control which started the key event
	 * @returns {WhiteListControls} Returns the item of the white list control, otherwise it returns null
	 */
	private getTriggerControl(event: any): WhiteListControls {
		if (Boolean(event.target && event.target['type'] === 'file')) {
			return WhiteListControls.FileDialog;
		}

		if (event.target['id'] === 'tdsUiPrompt') {
			return WhiteListControls.ConfirmationDialog;
		}

		// is it a kendo drop down?
		if (event.target.classList.contains('k-dropdown-wrap')) {
			// is it the list items open ?
			const isListOpen = event.target.attributes.getNamedItem(IS_LIST_OPEN_ATTR);

			if (!isListOpen || isListOpen.value === 'false') {
				return null;
			} else {
				// esc key was pressed when the dropdown list opened
				UIHandleEscapeDirective.setIsDropdownListOpen(event.target, false);
				return WhiteListControls.KendoDropDownOpenState;
			}
		}

		return null;
	}

	/**
	 * Set the attribute to the dropdownlist that indicates if the list of the dropdown is open
	 * @event contains list control
	 * @event value value to set on
	 */
	static setIsDropdownListOpen(list: any, value: boolean): void {
		list.setAttribute(IS_LIST_OPEN_ATTR, value);
	}
}