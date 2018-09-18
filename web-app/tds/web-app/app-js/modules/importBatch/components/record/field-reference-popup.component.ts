import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FieldInfoType} from './import-batch-record-fields.component';

@Component({
	selector: 'field-reference-popup',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/record/field-reference-popup.component.html'
})
export class FieldReferencePopupComponent implements OnInit {

	@Input ('mouseEvent') private mouseEvent;
	@Input ('type') private type;
	@Input ('domain') private domain;
	@Input ('results') private results;
	@Input ('gridData') private gridData;
	@Input ('gridGroups') private gridGroups;
	@Output ('onClose') onCloseEvent: EventEmitter<any> = new EventEmitter();
	@ViewChild('focusElement') popupEscFocusElement: ElementRef;

	protected margin = {horizontal: 2, vertical: 2};
	protected position = 'fixed';
	protected offset;

	protected FieldInfoType = FieldInfoType;

	ngOnInit(): void {
		this.offset = { left: this.mouseEvent.pageX, top: this.mouseEvent.pageY};
		// focus input element to help the on escape key exit.
		setTimeout( () => {
			if (this.popupEscFocusElement) {
				this.popupEscFocusElement.nativeElement.focus();
			}
		}, 300);
	}

	/**
	 * On Close.
	 */
	protected onClose(): void {
		this.onCloseEvent.emit();
	}

	/**
	 * Returns the proper popup field info title.
	 * @param {FieldInfoType} type
	 * @returns {string}
	 */
	protected getPopupTitle(): string {
		switch (this.type) {
			case FieldInfoType.CREATE: return 'Create Reference';
			case FieldInfoType.UPDATE: return 'Update Reference';
			case FieldInfoType.FIND: return 'Find Results';
			default: return '';
		}
	}
}