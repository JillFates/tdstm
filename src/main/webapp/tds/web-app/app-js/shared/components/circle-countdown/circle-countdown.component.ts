import {Component, ElementRef, EventEmitter, Output, Renderer2, ViewChild} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {ContextMenuSelectEvent, MenuEvent} from '@progress/kendo-angular-menu';

type CircleCountdownOption = {
	[key: string]: {
		text: string,
		action: () => void
	}
}

@Component({
	selector: 'tds-circle-countdown',
	template: `
		<div kendoTooltip position="top" filter="span" [tooltipClass]="'ct-tooltip'" class="text-center k-align-self-center">
			<span title="Right click to start/reset">
				<div id="countdown" class="circle-timer-0" (click)="onRefreshCountdownClick()" #countdownContainer>
					<div id="countdown-number">{{refreshCount}}</div>
					<svg id="countdown-circle-container">
						<circle id="countdown-circle" r="13" cx="15" cy="15"></circle>
					</svg>
				</div>
				<kendo-contextmenu [target]="countdownContainer" (select)="onItemSelected($event)">
					 <kendo-menu-item
						 [disabled]="started"
						 [data]="circleTimerOptions.start"
						 [text]="circleTimerOptions.start.text"
						 [cssClass]="'btn circle-timer-ctx-item'">
					 </kendo-menu-item>
					 <kendo-menu-item
						 [disabled]="!started"
						 [data]="circleTimerOptions.stop"
						 [text]="circleTimerOptions.stop.text"
						 [cssClass]="'btn circle-timer-ctx-item'">
					 </kendo-menu-item>
				</kendo-contextmenu>
			</span>
		</div>`
})
export class CircleCountdownComponent {
	@Output() timeout: EventEmitter<any> = new EventEmitter();
	refreshCountdown$: Observable<number>;
	refreshCountdownSubscription: Subscription;
	refreshCountdownTime = 30;
	refreshCount: number;
	refreshAnimation = `countdown ${this.refreshCountdownTime}s linear infinite forwards`;
	@ViewChild('refreshCircle', {static: false}) refreshCircle: ElementRef;
	@ViewChild('countdownContainer', {static: false}) countdownContainer: ElementRef<any>;
	circleTimerOptions: CircleCountdownOption;
	started: boolean;

	constructor(private renderer: Renderer2) {
		this.circleTimerOptions = {
			start: {
				text: 'Start',
				action: () => this.startTimer()
			},
			stop: {
				text: 'Stop',
				action: () => this.stopTimer()
			}
		};
		this.refreshCount = Number(this.refreshCountdownTime);
	}

	onItemSelected(event: ContextMenuSelectEvent | MenuEvent) {
		event.item.data.action();
	}

	stopTimer(): void {
		this.started = false;
		this.refreshCount = Number(this.refreshCountdownTime);
		if (this.refreshCountdownSubscription) { this.refreshCountdownSubscription.unsubscribe(); }
		this.renderer.removeAttribute(this.countdownContainer.nativeElement, 'class');
		this.renderer.addClass(this.countdownContainer.nativeElement, `circle-timer-0`);
	};

	startTimer(): void {
		this.started = true;
		this.refreshCount = Number(this.refreshCountdownTime);
		this.refreshCountdown$ = Observable.interval(1000)
			.map(x => Math.floor(--this.refreshCount));

		this.setCircleAnimation(this.refreshCountdownTime);

		this.subscribeToCircleCountdown();
	};

	subscribeToCircleCountdown(): void {
		this.refreshCountdownSubscription = this.refreshCountdown$
			.subscribe(x => {
				if (x <= 0)  {
					this.refreshCount =  this.refreshCountdownTime;
					this.timeout.emit();
				} else {
					this.refreshCount = x;
				}
			});
	}

	onRefreshCountdownClick(): void {
		if (!this.refreshCountdownSubscription || !this.started) { return; }
		if (this.refreshCountdownTime === 120) {
			this.refreshCountdownTime = 30;
		} else {
			this.refreshCountdownTime += 30;
		}

		this.refreshCountdownSubscription.unsubscribe();

		this.startTimer();
	}

	setCircleAnimation(countDownTime: number): void {
		this.renderer.removeClass(this.countdownContainer.nativeElement, `circle-timer-${countDownTime - 30}`);
		this.renderer.addClass(this.countdownContainer.nativeElement, `circle-timer-${countDownTime}`);
	}
}
