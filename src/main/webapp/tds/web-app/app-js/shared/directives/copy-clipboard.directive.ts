import {Directive, Input, Output, EventEmitter, HostListener} from '@angular/core';

/**
 * Being able to copy any text into the clipboard. Usage:
 * <button [copy-clipboard]="'text'" type="button">Copy to Clipboard</button>
 */

@Directive({selector: '[copy-clipboard]'})
export class CopyClipboardDirective {

	@Input('copy-clipboard') public payload: string;
	@Input('context') public context: string;
	@Output('copied') public copied: EventEmitter<string> = new EventEmitter<string>();

	@HostListener('click', ['$event'])
	public onClick(event: MouseEvent): void {
		event.preventDefault();
		if (!this.payload) {
			return;
		}
		this.copy();
	}

	/**
	 * Copy the data into the clipboard
	 */
	public copy(): void {
		let listener = (e: ClipboardEvent) => {
			let clipboard = e.clipboardData || window['clipboardData'];
			clipboard.setData('text', this.payload.toString());
			e.preventDefault();
			this.copied.emit(this.payload);
		};

		document.addEventListener('copy', listener, false)
		document.execCommand('copy');
		document.removeEventListener('copy', listener, false);
	}
}
