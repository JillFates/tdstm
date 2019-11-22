import {
	Component,
	OnChanges,
	Input,
	Output,
	EventEmitter,
	SimpleChanges,
} from '@angular/core';
import { UserContextService } from '../../../auth/service/user-context.service';
import { NewsModel } from './../../model/news.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { Permission } from '../../../../shared/model/permission.model';
import { PermissionService } from '../../../../shared/services/permission.service';
import { DateUtils } from '../../../../shared/utils/date.utils';
import {
	PREFERENCES_LIST,
	PreferenceService,
} from '../../../../shared/services/preference.service';

@Component({
	selector: 'tds-news',
	template: `
		<div
			class="event-news-component"
			*ngIf="getDynamicConfiguration(); let config"
		>
			<tds-button-create
				(click)="onCreateNews()"
				[disabled]="!isCreateAvailable() || isDisabled"
				class="btn-primary"
				title="Add News"
			></tds-button-create>
			<kendo-tabstrip>
				<kendo-tabstrip-tab
					[title]="'Event News'"
					[selected]="true"
					[disabled]="isDisabled"
				>
					<ng-template kendoTabContent>
						<div
							*ngFor="let item of eventNews"
							class="row event-news"
						>
							<div
								[ngStyle]="{
									cursor: config.isEditAvailable
										? 'pointer'
										: 'text'
								}"
								class="col-sm-5 date"
								(click)="onSelectedNews(item)"
							>
								{{
									item.created
										| tdsDateTime
											: userTimeZone
											: dateTimeFormat
								}}
							</div>
							<div
								[ngStyle]="{
									cursor: config.isEditAvailable
										? 'pointer'
										: 'text'
								}"
								class="col-sm-7 description pull-left"
								(click)="onSelectedNews(item)"
							>
								{{ item.text }}
							</div>
						</div>
					</ng-template>
				</kendo-tabstrip-tab>
				<kendo-tabstrip-tab [title]="'Archive'" [disabled]="isDisabled">
					<ng-template kendoTabContent>
						<div
							*ngFor="let item of archivedNews"
							class="row event-news"
						>
							<div
								[ngStyle]="{
									cursor: config.isEditAvailable
										? 'pointer'
										: 'text'
								}"
								class="col-sm-5 date"
								(click)="onSelectedNews(item)"
							>
								{{
									item.created
										| tdsDateTime
											: userTimeZone
											: dateTimeFormat
								}}
							</div>
							<div
								[ngStyle]="{
									cursor: config.isEditAvailable
										? 'pointer'
										: 'text'
								}"
								class="col-sm-7 description pull-left"
								(click)="onSelectedNews(item)"
							>
								{{ item.text }}
							</div>
						</div>
					</ng-template>
				</kendo-tabstrip-tab>
			</kendo-tabstrip>
		</div>
	`,
})
export class NewsComponent implements OnChanges {
	@Input() news: Array<NewsModel> = [];
	@Input() isDisabled: Boolean = false;
	@Output() selected: EventEmitter<number> = new EventEmitter<number>();
	@Output() create: EventEmitter<void> = new EventEmitter<void>();
	public archivedNews: Array<NewsModel> = [];
	public eventNews: Array<NewsModel> = [];
	public userTimeZone: string;
	public dateTimeFormat: string;
	public dateFormat: string;

	constructor(
		private preferenceService: PreferenceService,
		private userContextService: UserContextService,
		private permissionService: PermissionService
	) {
		this.preferenceService
			.getPreferences(
				PREFERENCES_LIST.CURR_TZ,
				PREFERENCES_LIST.CURRENT_DATE_FORMAT
			)
			.subscribe(preferences => {
				this.userTimeZone = preferences.CURR_TZ;
				this.dateFormat =
					preferences.CURR_DT_FORMAT ||
					this.preferenceService.getUserDateFormat();
				this.dateTimeFormat = `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
			});
	}

	/**
	 * On changing input properties divide all the notices
	 * into two groups (event news and archived news)
	 * @param {SimpleChanges} changes  host input properties
	 */
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.news && changes.news.currentValue) {
			this.eventNews = changes.news.currentValue.filter(
				(item: NewsModel) => item.state === 'L'
			);
			this.archivedNews = changes.news.currentValue.filter(
				(item: NewsModel) => item.state === 'A'
			);
		}
	}

	/**
	 * Selecting a news throw the event to notify to the host component
	 * to open the news details
	 * @param {any} id  News selected
	 */
	public onSelectedNews(news: any): void {
		if (this.isEditAvailable()) {
			this.selected.emit(news);
		}
	}

	/**
	 * Clicking on Create New button throw the event to notify to the host component
	 * to open the create news view
	 * @param {number} id  News id
	 */
	public onCreateNews(): void {
		this.create.emit();
	}

	public isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NewsCreate);
	}

	public isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NewsEdit);
	}

	/**
	 * Group all the dynamic informaction required by the view in just one function
	 * @return {any} Object with the values required dynamically by the view
	 */
	public getDynamicConfiguration(): any {
		return {
			isEditAvailable: this.isEditAvailable(),
			isCreateAvailable: this.isCreateAvailable(),
		};
	}
}
