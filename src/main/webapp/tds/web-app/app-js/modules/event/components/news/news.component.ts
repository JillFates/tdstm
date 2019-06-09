import {Component, OnChanges, Input, Output, EventEmitter, SimpleChanges} from '@angular/core';
import {UserContextService} from '../../../security/services/user-context.service';
import {NewsModel} from './../../model/news.model';
import { UserContextModel } from 'web-app/app-js/modules/security/model/user-context.model';

@Component({
	selector: 'tds-news',
	template: `
		<kendo-tabstrip>
			<kendo-tabstrip-tab [title]="'Event News'" [selected]="true">
			<ng-template kendoTabContent>
				<div *ngFor="let item of eventNews" class="row">
					<div class="col-sm-3">{{item.created | tdsDateTime: userTimeZone}}</div>
					<div class="col-sm-9" (click)="onSelectedNews(item.id)">{{item.text}}</div>
				</div>
			</ng-template>
			</kendo-tabstrip-tab>
			<kendo-tabstrip-tab [title]="'Archive'">
			<ng-template kendoTabContent>
				<div *ngFor="let item of archivedNews" class="row">
					<div class="col-sm-3">{{item.created | tdsDateTime: userTimeZone}}</div>
					<div class="col-sm-9" (click)="onSelectedNews(item.id)">{{item.text}}</div>
				</div>
			</ng-template>
			</kendo-tabstrip-tab>
		</kendo-tabstrip>
		<tds-button-create (click)="onCreate()"></tds-button-create>
	`,
	styles: [`
		kendo-tabstrip p {
			margin: 0;
			padding: 8px;
		}
	`]
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

	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.news && changes.news.currentValue) {
			this.eventNews = changes.news.currentValue.filter((item: NewsModel) => item.state === 'L');
			this.archivedNews = changes.news.currentValue.filter((item: NewsModel) => item.state === 'A');
		}
	}

	onSelectedNews(id: number): void {
		this.selected.emit(id);
	}

	onCreate(): void {
		this.create.emit();
	}
}