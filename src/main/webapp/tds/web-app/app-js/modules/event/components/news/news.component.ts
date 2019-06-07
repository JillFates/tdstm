import {Component, Input, OnInit} from '@angular/core';

@Component({
	selector: 'tds-news',
	template: `
		<kendo-tabstrip>
			<kendo-tabstrip-tab [title]="'Event News'">
			<ng-template kendoTabContent>
				<p>Tab 1 Content</p>
			</ng-template>
			</kendo-tabstrip-tab>
			<kendo-tabstrip-tab [title]="'Archive'" [selected]="true">
			<ng-template kendoTabContent>
				<p>Tab 2 Content</p>
			</ng-template>
			</kendo-tabstrip-tab>
		</kendo-tabstrip>
	`,
	styles: [`
		kendo-tabstrip p {
			margin: 0;
			padding: 8px;
		}
	`]
})
export class NewsComponent implements OnInit {
	constructor() {
		console.log('on constructor');
	}

	ngOnInit() {
		console.log('On init');
	}

}