import {Component, OnChanges, Input, Output, EventEmitter, SimpleChanges} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {NewsModel} from './../../model/news.model';
import { UserContextModel } from '../../../auth/model/user-context.model';

@Component({
	selector: 'tds-news',
	template: `
		<div class="event-news-component">
			<kendo-tabstrip>
				<kendo-tabstrip-tab [title]="'Event News'" [selected]="true">
				<ng-template kendoTabContent>
					<div *ngFor="let item of eventNews" class="row event-news">
						<div class="col-sm-5 date" (click)="onSelectedNews(item.id)">{{item.created | tdsDateTime: userTimeZone}}</div>
						<div class="col-sm-7 description pull-left" (click)="onSelectedNews(item.id)">{{item.text}}</div>
					</div>
				</ng-template>
				</kendo-tabstrip-tab>
				<kendo-tabstrip-tab [title]="'Archive'">
				<ng-template kendoTabContent>
					<div *ngFor="let item of archivedNews" class="row event-news">
						<div class="col-sm-5 date" (click)="onSelectedNews(item.id)">{{item.created | tdsDateTime: userTimeZone}}</div>
						<div class="col-sm-7 description pull-left" (click)="onSelectedNews(item.id)">{{item.text}}</div>
					</div>
				</ng-template>
				</kendo-tabstrip-tab>
			</kendo-tabstrip>
			<tds-button-create (click)="onCreateNews()" class="btn-primary" title="Add News"></tds-button-create>
		</div>
	`
})
export class NewsComponent implements OnChanges {
	@Input() news: Array<NewsModel> = [];
	@Output() selected: EventEmitter<number> = new EventEmitter<number>();
	@Output() create: EventEmitter<void> = new EventEmitter<void>();
	public archivedNews: Array<NewsModel> = [];
	public eventNews: Array<NewsModel> = [];
	public userTimeZone: string;

	constructor(private userContextService: UserContextService) {
		this.userContextService.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.userTimeZone = userContext.timezone;
			})
	}

	/**
	 * On changing input properties divide all the notices
	 * into two groups (event news and archived news)
 	 * @param {SimpleChanges} changes  host input properties
	*/
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.news && changes.news.currentValue) {
			this.eventNews = changes.news.currentValue.filter((item: NewsModel) => item.state === 'L');
			this.archivedNews = changes.news.currentValue.filter((item: NewsModel) => item.state === 'A');
		}
	}

	/**
	 * Selecting a news throw the event to notify to the host component
	 * to open the news details
 	 * @param {number} id  News id
	*/
	public onSelectedNews(id: number): void {
		this.selected.emit(id);
	}

	/**
	 * Clicking on Create New button throw the event to notify to the host component
	 * to open the create news view
 	 * @param {number} id  News id
	*/
	public onCreateNews(): void {
		this.create.emit();
	}
}