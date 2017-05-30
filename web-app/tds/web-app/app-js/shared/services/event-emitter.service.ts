/**
 * Event Emitter is special class that allow us to bind the observables calls across the TDS App
 * using delivering events synchronously or asynchronously.
 */

import {Injectable, EventEmitter} from '@angular/core';

@Injectable()
export class EventEmitterService {
	// Event Storage
	private static emitters: { [ID: string]: EventEmitter<any> } = {};

	// Set the incoming event into the storage using its ID and marked as static to global use
	static get(ID: string): EventEmitter<any> {
		if (!this.emitters[ID]) {
			// Create a new Event if no one exist before
			this.emitters[ID] = new EventEmitter();
		}
		return this.emitters[ID];
	}

}