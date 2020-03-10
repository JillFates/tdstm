import {Directive, HostListener, Output, EventEmitter, Input} from '@angular/core';
declare var jQuery: any;

@Directive({
	selector: '[tds-handle-double-click]'
})
export class UIHandleDoubleClickDirective {
	private bannedClasses = [];
	@Output() doubleClick: EventEmitter<any> = new EventEmitter();

	@Input()
	set ignoreClasses(ignoreClasses: string[]) {
		this.bannedClasses = ignoreClasses;
	}
	get ignoreClasses(): string[] { return this.bannedClasses; }

	/**
	 * Listen for double click events, detecting one notify to the host component
	 * if target event belongs to some banned element this doesn't emit the event
	 * @param event
	 */
	@HostListener('dblclick', ['$event']) handleKeyboardEventUp(event: MouseEvent) {
		// banned by class
		const isChildOfBannedClass = this.bannedClasses.find((className: string) => {
			const name = event.target && event.target['className'] || '';

			const counter =  jQuery(event.target).parents(`.${className}`).length;
			const isBannedClass = this.bannedClasses.find((item: string) => name.indexOf(item) !== -1);

			return counter > 0 || Boolean(isBannedClass);
		});
		if (isChildOfBannedClass) {
			console.log('Ignored');
			return;
		}

		console.log('Attending');
		this.doubleClick.emit();
	}
}
