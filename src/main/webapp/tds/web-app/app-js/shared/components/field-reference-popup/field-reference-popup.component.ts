import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FieldInfoType} from '../../../modules/importBatch/components/record/import-batch-record-fields.component';

@Component({
	selector: 'field-reference-popup',
	template: `
		<kendo-popup #infoPopup class="fields-info-popup"
             [margin]="margin"
             [popupClass]="'content popup'"
             [offset]="offset"
             [positionMode]="position">
		    <div class="field-reference-popup popup-content" tds-handle-escape (escPressed)="onClose($event)">
		        <div class="popup-header">
		            <button #focusElement autofocus [ngClass]="BTN_POPUP_ESC_TRIGGER_CLASS" class="invisible-button"></button>
		            <label class="fields-info-popup-label">
		                {{getPopupTitle()}}
		            </label>
		            <i class="glyphicon glyphicon-remove" (click)="onClose($event)"></i>
		        </div>
		        <div class="popup-body">
		            <!-- Find Type-->
		            <div class="find-field-info" *ngIf="type === FieldInfoType.FIND">
		                <kendo-grid [data]="gridData"
		                            [groupable]="false" [group]="gridGroups">
		                    <kendo-grid-column [title]="'Domain'" field="domainIndex" [width]="1">
		                        <ng-template kendoGridGroupHeaderTemplate let-field="field" let-value="value" let-group="group">
		                            <span class="domain-group-header">Domain: {{group.items[0].domainName}}</span>
		                            <span *ngIf="group.items[0].recordsFound">Records Found: {{group.items[0].recordsFound}}</span>
		                        </ng-template>
		                    </kendo-grid-column>
		                    <kendo-grid-column [title]="'Field Name'" field="fieldName"></kendo-grid-column>
		                    <kendo-grid-column [title]="'Operator'" field="operator"></kendo-grid-column>
		                    <kendo-grid-column [title]="'Value'" field="value"></kendo-grid-column>
		                </kendo-grid>
		                <div class="footer-results">
		                    <label>Results:</label>
		                    <span *ngIf="results">
		                            <span *ngFor="let id of results" class="badge">{{id}}</span>
		                        </span>
		                    <span *ngIf="!results"><i>No results found</i></span>
		                </div>
		            </div>
		            <!-- Create/Update Type-->
		            <div class="create-update-field-info" *ngIf="type === FieldInfoType.CREATE || type === FieldInfoType.UPDATE">
		                <div>
		                    <label>Domain: {{domain}}</label>
		                </div>
		                <kendo-grid [data]="gridData">
		                    <kendo-grid-column [title]="'Field Name'" field="fieldName"></kendo-grid-column>
		                    <kendo-grid-column [title]="'Value'" field="value"></kendo-grid-column>
		                </kendo-grid>
		            </div>
		        </div>
		    </div>
		</kendo-popup>
	`
})
export class FieldReferencePopupComponent implements OnInit {
	public static POPUP_ESC_TRIGGER_CLASS = 'field-reference-esc-trigger';

	@Input ('mouseEvent') private mouseEvent;
	@Input ('offset') public offset;
	@Input ('type') public type;
	@Input ('domain') private domain;
	@Input ('results') private results;
	@Input ('gridData') private gridData;
	@Input ('gridGroups') private gridGroups;
	@Output ('onClose') onCloseEvent: EventEmitter<any> = new EventEmitter();
	@ViewChild('focusElement') popupEscFocusElement: ElementRef;

	public margin = {horizontal: 2, vertical: 2};
	public position = 'fixed';
	public FieldInfoType = FieldInfoType;

	public BTN_POPUP_ESC_TRIGGER_CLASS = FieldReferencePopupComponent.POPUP_ESC_TRIGGER_CLASS;

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
	public onClose($event): void {
		this.onCloseEvent.emit($event);
		$event.stopPropagation();
	}

	/**
	 * Returns the proper popup field info title.
	 * @param {FieldInfoType} type
	 * @returns {string}
	 */
	public getPopupTitle(): string {
		switch (this.type) {
			case FieldInfoType.CREATE: return 'Create Reference';
			case FieldInfoType.UPDATE: return 'Update Reference';
			case FieldInfoType.FIND: return 'Find Results';
			default: return '';
		}
	}
}