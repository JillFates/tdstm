/**
 * Created by aaferreira on 15/08/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs/Rx';
import { UIRouterModule, RootModule } from '@uirouter/angular';
import { HttpModule, Http } from '@angular/http';

import { SharedModule } from '../../../shared/shared.module';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';

import { AssetExplorerReportSelectorComponent } from '../components/report-selector/asset-explorer-report-selector.component';
import { ReportModel, ReportType } from '../model/report.model';

describe('AssetExplorerReportSelectorComponent:', () => {
	let fixture: ComponentFixture<AssetExplorerReportSelectorComponent>;
	let comp: AssetExplorerReportSelectorComponent;
	let de: DebugElement;

	let mockData: ReportModel[] = [
		{
			id: 1,
			name: 'Finance Applications',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION', 'STORAGE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		},
		{
			id: 2,
			name: 'HR Applicattions',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION', 'DEVICE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 3,
			name: 'Legal Applications',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 4,
			name: 'My First Report',
			isOwner: true,
			isShared: false,
			isSystem: false,
			schema: {
				domains: ['DEVICE', 'DATABASE', 'STORAGE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'DEVICE',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 5,
			name: 'My First Shared Report',
			isOwner: true,
			isShared: true,
			isSystem: false
		}, {
			id: 6,
			name: 'Another user awesome report',
			isOwner: false,
			isShared: true,
			isSystem: false
		}
	];

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				FormsModule,
				SharedModule,
				DropDownListModule,
				HttpModule,
				UIRouterModule.forRoot(<RootModule>{
					useHash: true,
					states: [{
						name: 'tds',
						url: ''
					}]
				})
			],
			declarations: [AssetExplorerReportSelectorComponent],
			providers: [
				{
					provide: 'reports', useValue: Observable.from(mockData)
						.bufferCount(mockData.length)
						.map((items: ReportModel[]) => {
							return [
								{
									name: 'All',
									items: items,
									open: true,
									type: ReportType.ALL
								}, {
									name: 'Recent',
									items: [],
									open: false,
									type: ReportType.RECENT
								}, {
									name: 'Favorites',
									items: items.filter(r => r['isFavorite']),
									open: false,
									type: ReportType.FAVORITES
								}, {
									name: 'My Reports',
									items: items.filter(r => r.isOwner),
									open: false,
									type: ReportType.MY_REPORTS
								}, {
									name: 'Shared Reports',
									items: items.filter(r => r.isShared),
									open: false,
									type: ReportType.SHARED_REPORTS
								}, {
									name: 'System Reports',
									items: items.filter(r => r.isSystem),
									open: false,
									type: ReportType.SYSTEM_REPORTS
								}
							];
						})
				}
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(AssetExplorerReportSelectorComponent);
		comp = fixture.componentInstance;
	});

	it('should create component', () => expect(comp).toBeDefined());

});