import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Observable, Subscription} from 'rxjs';

@Component({
	selector: 'tds-circle-countdown',
	template: ``
})
export class CircleCountdownComponent implements OnInit {

	refreshCountdown$: Observable<number>;
	refreshCountdownSubscription: Subscription;
	refreshCountdownTime = 30;
	refreshCount: number;
	refreshAnimation = `countdown ${this.refreshCountdownTime}s linear infinite forwards`;
	@ViewChild('refreshCircle') refreshCircle: ElementRef;

	constructor() {
		// constructor
	}

	ngOnInit(): void {
		this.setRefreshTime();
	}

	setRefreshTime(): void {
		this.refreshCount = this.refreshCountdownTime;
		this.refreshCountdown$ = Observable.interval(1000)
			.map(x => Math.floor(--this.refreshCount));

		this.refreshCountdownSubscription = this.refreshCountdown$
			.subscribe(x => this.refreshCount =  x <= 0 ? this.refreshCountdownTime : x);
	}

	onRefreshCountdownClick(): void {
		this.refreshCountdownTime += 30;

		this.refreshAnimation = `countdown ${this.refreshCountdownTime}s linear infinite forwards`;

		this.refreshCountdownSubscription.unsubscribe();

		this.setRefreshTime();
	}
}
