import {
	ChangeDetectionStrategy,
	Component,
	ElementRef,
	Input,
	OnInit,
} from '@angular/core';

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
			[ngClass]="{ 'btn': true, 'btn-default': true, 'tds-action-button': true, 'btn-action': true }"
			[title]="titleButton"><i class="fa fa-fw fa-{{button.icon}}"></i>
			<span>{{titleButton}}</span>
		</button>
	`,
	host: {
		'[class.tds-action-button--disabled]': 'disabled'
	},
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class TDSActionButton implements OnInit {
	@Input() action: TDSActionsButton ;
	@Input() title = '';
	@Input() tooltip = '';
	@Input() id = '';
	@Input() disabled = false;
	private button: TDSButton;
	private titleButton: string;

	constructor(
		private elementRef: ElementRef,
		private buttonsFactoryService: ButtonsFactoryService) {
	}

	ngOnInit() {
		const buttonSelector = this.elementRef.nativeElement.localName;
		this.button = this.buttonsFactoryService.create(this.action);

		if (!this.button) {
			throw new Error(`Unable to create button ${buttonSelector}`);
		}

		this.titleButton = this.title || this.button.title;
	}
}