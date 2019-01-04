import { Observable } from 'rxjs/Observable';
import { Scheduler } from 'rxjs/Scheduler';
import { ReplaySubject } from 'rxjs/ReplaySubject';
import { Observer } from 'rxjs/Observer';
import { Subscription } from 'rxjs/Subscription';

export function cache<T>(
	bufferSize: number = Number.POSITIVE_INFINITY,
	windowTime: number = Number.POSITIVE_INFINITY,
	scheduler?: Scheduler): Observable<T> {
	let subject: ReplaySubject<T>;
	let source = this;
	let refs = 0;
	let outerSub: Subscription;

	const getSubject = () => {
		subject = new ReplaySubject<T>(bufferSize, windowTime, scheduler);
		return subject;
	};

	return new Observable<T>((observer: Observer<T>) => {
		if (!subject) {
			subject = getSubject();
			outerSub = source.subscribe(
				(value: T) => subject.next(value),
				(err: any) => {
					let s = subject;
					subject = null;
					s.error(err);
				},
				() => subject.complete()
			);
		}

		refs++;

		if (!subject) {
			subject = getSubject();
		}
		let innerSub = subject.subscribe(observer);

		return () => {
			refs--;
			if (innerSub) {
				innerSub.unsubscribe();
			}
			if (refs === 0) {
				outerSub.unsubscribe();
			}
		};
	});
}

export interface CacheSignature<T> {
	(bufferSize?: number, windowTime?: number, scheduler?: Scheduler): Observable<T>;
}