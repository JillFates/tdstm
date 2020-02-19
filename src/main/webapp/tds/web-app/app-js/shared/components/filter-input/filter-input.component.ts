/**
 * Control to encapsulate the behaviour associated to filtering elements
 * It has an input where the user can type the search text and a clear icon button to clear the search entered
 * The intention is while the user is typing, disabling angular change detection events
 * just until the bounce time has expired or the user press the ENTER key.
 * The goal is to improve the performance of grids with a low of rows
 */

import {
	Component,
	ElementRef,
	EventEmitter,
	Input,
	NgZone,
	OnDestroy,
	OnInit,
	Output,
	SimpleChanges,
	ViewChild,
} from '@angular/core';

import {KEYSTROKE, SEARCH_QUITE_PERIOD} from '../../model/constants';

@Component({
	selector: 'tds-filter-input',
	template: `
		<div class="tds-filter-input">
			<input type="text"
				#filterInput
				[name]="name"
				[value]="value"
				[placeholder]="placeholder"
				input-paste (onPaste)="onPaste($event)"
				class="form-control">
			<span *ngIf="filterInput.value"
				(click)="onClearFilter()"
				[title]="'GLOBAL.CLEAR_FILTER' | translate"
				class="clear-filter fa fa-times form-control-feedback component-action-clear-filter"
				aria-hidden="true">
			</span>
		</div>
	`
})
export class TDSFilterInputComponent implements OnInit, OnDestroy {
	@Input() name = '';
	@Input() placeholder = '';
	@Input() value = ' ';
	@Output() filter: EventEmitter<string> = new EventEmitter<string>();
	@ViewChild('filterInput') filterInput: ElementRef;

	private previousSearch = '';
	private typingTimeout = null;
	private readonly NOT_ALLOWED_CHAR_REGEX = /ALT|ARROW|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;

	constructor(private zone: NgZone) {
	}

	ngOnInit() {
		/* The handler to react on keyup event for the search input
		 * is running outside of the angular zone in order to don't trigger
		 * the angular change detection process on every key stroked
		*/
		this.zone.runOutsideAngular(() => {
			this.filterInput.nativeElement
				.addEventListener('keyup', this.keyPressedListener.bind(this));
		});
	}

	/**
	 * Event handler to be attached to the listener input keypress event of the search input
	 * @param {KeyboardEvent} keyEvent - Key press event info
	*/
	private keyPressedListener(keyEvent: KeyboardEvent): void {
		this.onFilterKeyUp(keyEvent, this.filterInput.nativeElement.value);
	}

	/**
	 * On input changes update the value of the input control
	 * @param {SimpleChanges} changes - Object with the input properties updated bye the host component
	*/
	ngOnChanges(changes: SimpleChanges) {
		if (changes.value) {
			this.filterInput.nativeElement.value = changes.value.currentValue;
		}
	}

	/**
	 * On destroying the component remove the event listener associated
	*/
	ngOnDestroy() {
		this.filterInput.nativeElement.removeEventListener('keyup', this.keyPressedListener.bind(this));
	}

	/**
	 * Clear the entered search string and notify to the host component
	*/
	public onClearFilter(): void {
		this.filterInput.nativeElement.value = '';
		this.previousSearch = '';
		this.onFilter('');
	}

	/**
	 * If the previous search is the same to the current cancel the search
	 * Otherwise point the previous search to the new one search string
	 * @param {string} search - Current search value
	 * @return {boolean}  Boolean indicating if search is cancelled
	*/
	private preventFilterSearch(search: string): boolean {
		if (this.previousSearch === search) {
			return true;
		}

		this.previousSearch = search;
		return false;
	}

	/**
	 * Notify to the host component about a new search entered
	 * @param {string} search - Current search value
	*/
	private onFilter(search: string): void {
		/* Here the search is done so the notification to the host component is made
			within the angular zone in order to update the UI
		*/
		this.zone.run(() => this.filter.emit(search));
	}

	/**
	 * Handle the input keypress events of the search input
	 * Notify to the host component about a new search only if the guards
	 * defined on preventFilterSearch and the bounce timeout are met
	 * it ignores the input of special characters
	 * @param {KeyboardEvent} keyEvent - Key press event info
	 * @param {string} search - Current search value
	*/
	private onFilterKeyUp(keyEvent: KeyboardEvent, search: string): void {
		if (this.preventFilterSearch(search)) {
			return; // prevent search
		}

		if (keyEvent.code === KEYSTROKE.ENTER) {
			this.onFilter(search);
		} else if (!this.NOT_ALLOWED_CHAR_REGEX.test(keyEvent.code)) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = setTimeout(
				() => this.onFilter(search), SEARCH_QUITE_PERIOD
			);
		}
	}

	/**
	 * Handle the onPaste event of the input-paste directive
	 * Notify to the host component about a new search, validate previousSearch is different
	 * from new one
	 * @param {string} search - Current search value
	*/
	public onPaste(search: string): void {
		this.filterInput.nativeElement.value = search;

		if ( this.preventFilterSearch(search)) {
			return; // prevent search
		}
		clearTimeout(this.typingTimeout);
		this.typingTimeout = setTimeout(() => this.onFilter(search), SEARCH_QUITE_PERIOD);
	}
}
