/**
 * Notifier is special class that allow us to bind the events calls across the TDS App
 */

import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';

@Injectable()
export class NotifierService {

	private subject = new Subject();

	broadcast(event) {
		this.subject.next(event);
	}

	on(eventName, callback) {
		let subscription = this.subject.filter((value: any, index) => {
			return value.name === eventName;
		}).subscribe(callback);
		return () => {
			subscription.remove(subscription);
			subscription.unsubscribe();
		};
	}
}