import {
	ChangeDetectionStrategy,
	Component,
	ElementRef,
	Input,
	OnInit,
} from '@angular/core';
import {ButtonsFactory, TDSButton} from './buttons-factory.helper';

@Component({
	selector: ButtonsFactory.getButtonsSelector(),
	template: `
		<button *ngIf="button"
			[ngClass]="{'tds-action-button': true, 'btn-action': true}"
			[title]="titleButton"><i class="fa fa-fw fa-{{button.icon}}"></i>
			<span>{{titleButton}}</span>
		</button>
	`,
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class TDSActionButton implements OnInit {
	@Input() title = '';
	@Input() tooltip = '';
	private button: TDSButton;
	private titleButton: string;

	constructor(private elementRef: ElementRef) {
	}

	ngOnInit() {
		const buttonSelector = this.elementRef.nativeElement.localName;
		this.button = ButtonsFactory.create(buttonSelector);

		if (!this.button) {
			throw new Error(`Unable to create button ${buttonSelector}`);
		}
		this.titleButton = this.title || this.button.title;
	}
}