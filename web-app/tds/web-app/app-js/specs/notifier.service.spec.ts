/**
 * Created by aaferreira on 15/02/2017.
 */
import {NotifierService} from '../shared/services/notifier.service';

describe('NotifierService:', () => {
	let notifier: NotifierService;

	beforeEach(() => {
		notifier = new NotifierService();
	});

	it('should be defined', () => {
		expect(notifier).toBeDefined();
	});

	it('should be able to listen for events', done => {
		notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			done();
		});
		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
	});

	it('should be able to pass data to listener', done => {
		notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			expect(event.data).toBe('NotifierTestData');
			done();
		});
		notifier.broadcast({
			name: 'NotifierTestEvent',
			data: 'NotifierTestData'
		});
	});

	it('should no call listener if it already has been unsuscribed', done => {
		let listerner = notifier.on('NotifierTestEvent', event => {
			expect(false).toBe(true); // should not go here
			done();
		});
		listerner(); // remove the listener;
		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
		// setup listerner again to resolve test
		listerner = notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			done();
		});
		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
	});

	it('should handle events with same key/name multiple time', done => {
		let control = {
			callbacksCalled: 0
		};
		notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			control.callbacksCalled++;
			if (control.callbacksCalled === 4) {
				done();
			}
		});

		notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			control.callbacksCalled++;
			if (control.callbacksCalled === 4) {
				done();
			}
		});

		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
	});

	it('should dismiss only selected listeners', done => {
		let control = {
			callbacksCalled: 0
		};
		let listener1 = notifier.on('NotifierTestEvent', event => {
			expect(event.name).toBe('NotifierTestEvent');
			control.callbacksCalled++;
			if (control.callbacksCalled === 2) {
				done();
			}
		});

		let listener2 = notifier.on('NotifierTestEvent', event => {
			expect(false).toBe(true); // should not go here
			done();
		});
		listener2(); // kill second listener;

		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
		notifier.broadcast({
			name: 'NotifierTestEvent'
		});
	});

});