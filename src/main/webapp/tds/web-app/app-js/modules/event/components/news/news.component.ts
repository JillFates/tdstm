import {Component, OnChanges, Input, SimpleChanges} from '@angular/core';
import {UserContextService} from '../../../security/services/user-context.service';
import {News} from './model/news.model';
import { UserContextModel } from 'web-app/app-js/modules/security/model/user-context.model';

@Component({
	selector: 'tds-news',
	template: `
		<kendo-tabstrip>
			<kendo-tabstrip-tab [title]="'Event News'" [selected]="true">
			<ng-template kendoTabContent>
				<div *ngFor="let newItem of eventNews" class="row">
					<div class="col-sm-3">{{newItem.created | tdsDateTime: userTimeZone}}</div>
					<div class="col-sm-9">{{newItem.text}}</div>
				</div>
			</ng-template>
			</kendo-tabstrip-tab>
			<kendo-tabstrip-tab [title]="'Archive'">
			<ng-template kendoTabContent>
				<div *ngFor="let newItem of archivedNews" class="row">
					<div class="col-sm-3">{{newItem.created | tdsDateTime: userTimeZone}}</div>
					<div class="col-sm-9">{{newItem.text}}</div>
				</div>
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
export class NewsComponent implements OnChanges {
	@Input() news: Array<News> = [];
	public archivedNews: Array<News> = [];
	public eventNews: Array<News> = [];
	public userTimeZone: string;

	constructor(private userContextService: UserContextService) {
		console.log('on constructor');
		this.userContextService.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.userTimeZone = userContext.timezone;
			})
	}

	ngOnChanges(changes: SimpleChanges) {
		console.log('on changes');
		console.log(changes);
		if (changes && changes.news && changes.news.currentValue) {
			this.eventNews = changes.news.currentValue.filter((item: News) => item.state === 'L');
			this.archivedNews = changes.news.currentValue.filter((item: News) => item.state === 'A');
		}
	}
}