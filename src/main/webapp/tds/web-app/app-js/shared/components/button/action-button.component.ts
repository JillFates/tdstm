import {
	ElementRef,
	Component,
	Input,
	OnInit,
	OnChanges,
	SimpleChanges
} from '@angular/core';

import {pathOr} from 'ramda';

import {
	TDSButton
} from './model/action-button.model';

import {ButtonsFactoryService} from '../../services/buttons-factory.service';

@Component({
	selector: `
		tds-button-add,
		tds-button-cancel,
		tds-button-check-syntax,
		tds-button-clone,
		tds-button-close,
		tds-button-collapse,
		tds-button-confirm,
		tds-button-create,
		tds-button-custom,
		tds-button-delete,
		tds-button-edit,
		tds-button-expand,
		tds-button-export,
		tds-button-filter,
		tds-button-save,
		tds-button-sync,
		tds-button-script,
		tds-button-undo,
	`,
	template: `
<!--        [ngClass]="hostClasses.length ? hostClasses.value : ''"-->
		<tds-button
				[id]="id"
				[type]="type"
				[theme]="theme"
				[small]="small"
				[inverse]="inverse"
				[outline]="outline"
				[flat]="flat"
				[icon]="icon || button.icon || ''"
				[title]="tooltip || button.tooltip || ''"
				[disabled]="disabled || !hasAllPermissions"
				[tabindex]="tabindex"
				class="action-button"
				[iconClass]="iconClass">
			{{ displayLabel ? titleButton : '' }}
		</tds-button>
	`,
	host: {
		'[class.tds-generic-button]': 'true'
	}
})
export class TDSActionButton implements OnInit, OnChanges {
	@Input() disabled = false;
	@Input() displayLabel = true;
	@Input() flat: boolean;
	@Input() icon = '';
	@Input() iconClass = '';
	@Input() iconPrefixVendor = '';
	@Input() id = '';
	@Input() inverse: boolean;
	@Input() isIconButton = false;
	@Input() outline: boolean;
	@Input() permissions: string[];
	@Input() small: string;
	@Input() tabindex = '';
	@Input() theme: string;
	@Input() title = '';
	@Input() tooltip = '';
	@Input() type: string;
	button: TDSButton;
	titleButton: string;
	hostClasses: any = [];
	hasAllPermissions: boolean;
	private buttonSelectorName: string;

	constructor(private elementRef: ElementRef, private buttonsFactoryService: ButtonsFactoryService) {
		this.hasAllPermissions = false;
	}

	/**
	* Based on the selector name, creates the corresponding button
	*/
	ngOnInit() {
		this.hostClasses = this.elementRef.nativeElement.classList || [];
		this.buttonSelectorName = this.elementRef.nativeElement.localName;
		this.button = this.buttonsFactoryService.create(this.buttonSelectorName, this.permissions || []);
		this.hasAllPermissions = this.button.hasAllPermissions;

		if (!this.button) {
			throw new Error(`Unable to create button ${this.buttonSelectorName}`);
		}
		this.titleButton = this.title || this.button.title;
	}

	/**
	 * On input changes set the corresponding button title and flag permissions
	 */
	ngOnChanges(changes: SimpleChanges) {
		const title = pathOr(null, ['title', 'currentValue'], changes);

		if (title !== null) {
			this.titleButton = this.title || this.button.title;
		}
	}
}
