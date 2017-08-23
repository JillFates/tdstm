/**
 * Created by aaferreira on 15/08/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs/Rx';
import { HttpModule } from '@angular/http';
import { UIRouterModule, RootModule } from '@uirouter/angular';

import { SharedModule } from '../../../shared/shared.module';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';

import { AssetExplorerReportSelectorComponent } from '../components/report-selector/asset-explorer-report-selector.component';
import {AssetExplorerService} from '../service/asset-explorer.service';

describe('AssetExplorerReportSelectorComponent:', () => {
	let fixture: ComponentFixture<AssetExplorerReportSelectorComponent>;
	let comp: AssetExplorerReportSelectorComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				HttpModule,
				FormsModule,
				SharedModule,
				DropDownListModule,
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
				AssetExplorerService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(AssetExplorerReportSelectorComponent);
		comp = fixture.componentInstance;
	});

	it('should create component', () => expect(comp).toBeDefined());

});