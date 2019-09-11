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
		tds-button-clone,
		tds-button-close,
		tds-button-create,
		tds-button-custom,
		tds-button-delete,
		tds-button-edit,
		tds-button-export,
		tds-button-filter,
		tds-button-save,
		tds-button-undo
	`,
	template: `
		<button *ngIf="button"
			type="button"
			[tabindex]="tabindex"
			[disabled]="disabled || !hasAllPermissions"
			[id]="id"
			[title]="tooltip || button.tooltip || titleButton"
			[ngClass]="buttonClasses">
				<div class="tds-action-button-container">
					<i class="{{configuredIconPrefixVendor + (icon || button.icon)}}"></i>
					<span *ngIf="titleButton" class="title">{{titleButton}}</span>
				</div>
				<ng-content></ng-content>
		</button>
	`,
	host: {
		'[class.tds-action-button--disabled]': 'disabled || !hasAllPermissions',
		'[class.tds-generic-button]': 'true'
	}
})
export class TDSActionButton implements OnInit, OnChanges {
	@Input() title = '';
	@Input() tooltip = '';
	@Input() icon = '';
	@Input() id = '';
	@Input() tabindex = '';
	@Input() disabled = false;
	@Input() isIconButton = false;
	@Input() permissions: string[];
	@Input() iconPrefixVendor = '';
	button: TDSButton;
	titleButton: string;
	hostClasses: any = [];
	hasAllPermissions: boolean;
	private buttonSelectorName: string;
	private defaultIconPrefixVendor = 'fa fa-fw fa-';

	constructor(
		private elementRef: ElementRef,
		private buttonsFactoryService: ButtonsFactoryService) {
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

	/**
	 * Get the prefix used by font awesome icons
	 */
	get configuredIconPrefixVendor() {
		return this.iconPrefixVendor !== '' ? this.iconPrefixVendor : this.defaultIconPrefixVendor;
	}

	/**
	 * Get the css classes used by the button, mixin the host classes into the inner button component
	 */
	get buttonClasses() {
		let buttonClasses =  {
			'btn': true,
			'btn-action': true,
			'tds-action-button': true,
			'not-has-all-permissions': !this.hasAllPermissions,
			'no-title-right': !this.titleButton
		};
		buttonClasses[this.buttonSelectorName] = true;

		const hostClasses = this.getHostClasses();
		const hasStyle = Array.from(this.hostClasses)
			.some((className: string) => className.startsWith('btn-'));

		if (!hasStyle) {
			hostClasses['btn-default'] = true;
		}

		buttonClasses = {...buttonClasses, ...hostClasses};

		const iconClasses = {
			'tds-action-button--just-icon': true
		};

		return this.isIconButton ? iconClasses : buttonClasses;
	}

	/**
	 * Get just the css classes used by the host
	 */
	getHostClasses() {
		const classes = {};

		Array.from(this.hostClasses)
			.forEach((className: string) =>  classes[className] = true);

		return classes;
	}
}
