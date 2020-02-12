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
	AfterViewInit,
	Output,
	SimpleChanges,
	ViewChild,
} from '@angular/core';
import { KEYSTROKE, SEARCH_QUITE_PERIOD } from '../../model/constants';
import { BooleanFilterData, GridColumnModel } from '../../model/data-list-grid.model';

@Component({
	selector: 'tds-filter-input',
	template: `
		<div class="tds-filter-input" [ngSwitch]="columnType">
			<!-- number -->
			<input *ngSwitchCase="'number'"
						 clrInput
						 #filterInput
						 type="number"
						 class="text-filter"
						 [name]="name"
						 [value]="value"
						 (change)="onFilter($event.target.value)"
						 [placeholder]="placeholder ? placeholder : 'GLOBAL.FILTER' | translate"
						 input-paste
						 (onPaste)="onPaste($event)"/>

			<!-- date -->
			<kendo-datepicker *ngSwitchCase="'date'"
												#filterInput
												[format]="dateFormat"
												[ngClass]="{'is-filtered': value}"
												[value]="value"
												(valueChange)="onFilter($event)"
												[style.width.%]="value ? 80 : 100">
			</kendo-datepicker>

			<!-- datetime -->
			<kendo-datepicker *ngSwitchCase="'datetime'"
												#filterInput
												[format]="dateFormat"
												[ngClass]="{'is-filtered': value}"
												[value]="value"
												(valueChange)="onFilter($event)"
												[style.width.%]="value ? 80 : 100">
			</kendo-datepicker>

			<!-- boolean -->
			<kendo-dropdownlist *ngSwitchCase="'boolean'"
													#filterInput
													[data]="booleanFilterData"
													[value]="value"
													(valueChange)="onFilter($event)"
													[style.width.%]="value ? 70 : 100">
			</kendo-dropdownlist>

			<!-- Text as well the default if not provided -->
			<input *ngSwitchDefault
						 clrInput
						 #filterInput
						 type="text"
						 class="text-filter"
						 [name]="name"
						 [value]="value"
						 [placeholder]="placeholder ? placeholder : 'GLOBAL.FILTER' | translate"
						 input-paste
						 (onPaste)="onPaste($event)"/>

			<!-- Clear filter button -->
			<tds-button
				*ngIf="value || value === false"
				(click)="onClearFilter()"
				[title]="'Clear Filter'"
				icon="times-circle"
				[small]="true"
				[flat]="true"
			>
			</tds-button>
		</div>

	`,
})
/**
 * TODO: dontiveros - This was already implemented on the component library as "<tds-grid-filter-input/>". We can remove this component later.
 * @Deprecated: This was already implemented on the component library as "<tds-grid-filter-input/>". We can remove this component later.
 */
export class TDSFilterInputComponent implements AfterViewInit, OnDestroy {
	@Input() name = '';
	@Input() placeholder = '';
	@Input() value: String | Date | boolean = '';
	@Input() columnType: string;
	@Input() dateFormat = '';
	@Input() column: GridColumnModel;
	@Output() filter: EventEmitter<string | Date | boolean> = new EventEmitter<string | Date | boolean>();
	@ViewChild('filterInput', { read: ElementRef, static: false })
	filterInput: ElementRef;
	public booleanFilterData = BooleanFilterData;
	private previousSearch = '';
	private typingTimeout = null;
	private readonly NOT_ALLOWED_CHAR_REGEX = /ALT|ARROW|F+|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;

	constructor(private zone: NgZone) {
	}

	ngAfterViewInit(): void {
		/* The handler to react on keyup event for the search input
	 	* is running outside of the angular zone in order to don't trigger
	 	* the angular change detection process on every key stroked
	 	*/
		if (this.isFilterInputAvailable()) {
			this.zone.runOutsideAngular(() => {
				this.filterInput.nativeElement.addEventListener(
					'keyup',
					this.keyPressedListener.bind(this)
				);
			});
		}
	}

	ngOnInit() {
		if (this.value === undefined) {
			this.value = '';
		}
	}

	/**
	 * On input changes update the value of the input control
	 * @param {SimpleChanges} changes - Object with the input properties updated bye the host component
	 */
	ngOnChanges(changes: SimpleChanges) {
		if (changes.value) {
			if (this.isFilterInputAvailable()) {
				this.filterInput.nativeElement.value = changes.value.currentValue;
			}
			// if (changes.value.currentValue.columnType) {
			// 	console.log(`Current: ${this.columnType}  New: ${changes.value.currentValue.columnType}`);
			// }
		}
	}

	/**
	 * On destroying the component remove the event listener associated
	 */
	ngOnDestroy() {
		if (this.isFilterInputAvailable()) {
			this.filterInput.nativeElement.removeEventListener(
				'keyup',
				this.keyPressedListener.bind(this)
			);
		}
	}

	/**
	 * Clear the entered search string and notify to the host component
	 */
	public onClearFilter(): void {
		if (this.isFilterInputAvailable()) {
			this.filterInput.nativeElement.value = null;
		}
		this.previousSearch = '';
		this.onFilter(null);
	}

	/**
	 * Notify to the host component about a new search entered
	 * @param {string} search - Current search value
	 */
	public onFilter(search: string | Date | boolean): void {
		/* Here the search is done so the notification to the host component is made
			within the angular zone in order to update the UI
		*/
		this.zone.run(() => this.filter.emit(search));
	}

	/**
	 * Handle the onPaste event of the input-paste directive
	 * Notify to the host component about a new search, validate previousSearch is different
	 * from new one
	 * @param {string} search - Current search value
	 */
	public onPaste(search: string): void {
		if (this.isFilterInputAvailable()) {
			this.filterInput.nativeElement.value = search;
			if (this.preventFilterSearch(search)) {
				return; // prevent search
			}
			clearTimeout(this.typingTimeout);
			this.typingTimeout = setTimeout(
				() => this.onFilter(search),
				SEARCH_QUITE_PERIOD
			);
		}
	}

	/**
	 * Determines if the current filter is available
	 */
	private isFilterInputAvailable(): boolean {
		return !!this.filterInput;
	}

	/**
	 * Event handler to be attached to the listener input keypress event of the search input
	 * @param {KeyboardEvent} keyEvent - Key press event info
	 */
	private keyPressedListener(keyEvent: KeyboardEvent): void {
		if (this.isFilterInputAvailable()) {
			this.onFilterKeyUp(keyEvent, this.filterInput.nativeElement.value);
		}
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
	 * Handle the input keypress events of the search input
	 * Notify to the host component about a new search only if the guards
	 * defined on preventFilterSearch and the bounce timeout are met
	 * it ignores the input of special characters
	 * @param {KeyboardEvent} keyEvent - Key press event info
	 * @param {string} search - Current search value
	 */
	private onFilterKeyUp(keyEvent: KeyboardEvent, search: string): void {
		if (this.isFilterInputAvailable()) {
			if (this.preventFilterSearch(search)) {
				return; // prevent search
			}
			if (keyEvent.code === KEYSTROKE.ENTER) {
				this.onFilter(search);
			} else if (!this.NOT_ALLOWED_CHAR_REGEX.test(keyEvent.code)) {
				clearTimeout(this.typingTimeout);
				this.typingTimeout = setTimeout(
					() => this.onFilter(search),
					SEARCH_QUITE_PERIOD
				);
			}
		}
	}
}
