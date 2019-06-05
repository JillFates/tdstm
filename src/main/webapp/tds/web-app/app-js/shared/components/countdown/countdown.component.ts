
import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
	selector: 'tds-countdown',
	template: `
		<div class="pie"></div>
	`,
	styles: [`
		.pie {
			width: 100px;
			height: 100px;
			border-radius: 50%;
			background: yellowgreen;
			background-image: linear-gradient(to right, transparent 50%, #655 0);
			color: #655;
		}
		.pie::before {
			content: '';
			display: block;
			margin-left: 50%;
			height: 100%;
			border-radius: 0 100% 100% 0 / 50%;
			background-color: inherit;
			transform-origin: left;
			animation: 
			spin 3s linear infinite,
			bg 6s step-end infinite
		}
		@keyframes spin {
			to { transform: rotate(.5turn); }
		}
		@keyframes bg {
			50% { background: #655; }
		}
	`]
})
export class CountdownComponent {
	constructor() {
		console.log('on constructor');
	}
}