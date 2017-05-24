/**
 * Notifier is special class that allow us to bind the events calls across the TDS App
 */

import {Injectable} from '@angular/core';
import {Observable, Observer} from 'rxjs/Rx';

import {AlertModel} from '../model/alert.model';

@Injectable()
export class NotifierService {

	private alertModel: Array<AlertModel>;

	observable: Observable<any>;
	observer: Observer<any>;

	constructor() {
		this.observable = Observable.create((observer: Observer<any>) => {
			this.observer = observer;
		}).share();
	}

	broadcast(event) {
		if (this.observer) {
			this.observer.next(event);
		}
	}

	on(eventName, callback) {
		let subscription = this.observable.filter((event) => {
			return event.name === eventName;
		}).subscribe(callback);
		return () => {
			subscription.remove(subscription);
			subscription.unsubscribe();
		};
	}
}