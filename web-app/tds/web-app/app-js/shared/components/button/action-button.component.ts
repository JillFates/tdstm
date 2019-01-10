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
	TDSActionsButton,
	TDSButton
} from './model/action-button.model';

import {ButtonsFactoryService} from '../../services/buttons-factory.service';

@Component({
	selector: 'tds-button',
	template: `
		<button *ngIf="button"
			type="button"
			[disabled]="disabled"
			[id]="id"
			[title]="tooltip || button.tooltip || titleButton"
			[ngClass]="buttonClasses">
				<i class="{{iconPrefixVendor + (icon || button.icon)}}"></i>
				<span class="title">{{titleButton}}</span>
				<ng-content></ng-content>
		</button>
	`,
	host: {
		'[class.tds-action-button--disabled]': 'disabled'
	}
})
export class TDSActionButton implements OnInit, OnChanges {
	@Input() action: TDSActionsButton;
	@Input() title = '';
	@Input() tooltip = '';
	@Input() icon = '';
	@Input() id = '';
	@Input() disabled = false;
	@Input() isIconButton = false;
	@Input() permissionsList: string[] = [];
	button: TDSButton;
	titleButton: string;
	hostClasses: any = [];

	constructor(
		private elementRef: ElementRef,
		private buttonsFactoryService: ButtonsFactoryService) {
	}

	ngOnInit() {
		this.hostClasses = this.elementRef.nativeElement.classList;
	}

	ngOnChanges(changes: SimpleChanges) {
		const action = pathOr(null, ['action', 'currentValue'], changes);
		const title = pathOr(null, ['title', 'currentValue'], changes);

		if (action !== null) {
			this.button = this.buttonsFactoryService.create(action, this.permissionsList);

			if (!this.button) {
				throw new Error(`Unable to create button ${action}`);
			}
			this.titleButton = this.title || this.button.title;
		}

		if (title !== null) {
			this.titleButton = this.title || this.button.title;
		}
	}

	get iconPrefixVendor() {
		return 'fa fa-fw fa-';  // prefix used by font awesome icons
	}

	get buttonClasses() {
		let buttonClasses =  {
			'btn': true,
			'btn-action': true,
			'tds-action-button': true,
			'not-has-all-permissions': !this.button.hasAllPermissions
		};

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

	getHostClasses() {
		const classes = {};

		Array.from(this.hostClasses)
			.forEach((className: string) =>  classes[className] = true);

		return classes;
	}
}