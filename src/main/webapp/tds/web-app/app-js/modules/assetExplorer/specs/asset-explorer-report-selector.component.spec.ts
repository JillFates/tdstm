/**
 * Created by aaferreira on 15/08/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { SharedModule } from '../../../shared/shared.module';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';

import { AssetExplorerViewSelectorComponent } from '../components/view-selector/asset-explorer-view-selector.component';
import {AssetExplorerService} from '../../assetManager/service/asset-explorer.service';

describe('AssetExplorerViewSelectorComponent:', () => {
	let fixture: ComponentFixture<AssetExplorerViewSelectorComponent>;
	let comp: AssetExplorerViewSelectorComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				HttpModule,
				FormsModule,
				SharedModule,
				DropDownListModule
			],
			declarations: [AssetExplorerViewSelectorComponent],
			providers: [
				AssetExplorerService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(AssetExplorerViewSelectorComponent);
		comp = fixture.componentInstance;
	});

	it('should create component', () => expect(comp).toBeDefined());

});