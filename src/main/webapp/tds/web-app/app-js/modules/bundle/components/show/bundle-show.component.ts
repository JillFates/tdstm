import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';

@Component({
	selector: `bundle-show`,
	templateUrl: 'bundle-show.component.html',
})
export class BundleShowComponent implements OnInit {
	constructor() {
	}

	ngOnInit() {
		console.log("yo");
	}
}