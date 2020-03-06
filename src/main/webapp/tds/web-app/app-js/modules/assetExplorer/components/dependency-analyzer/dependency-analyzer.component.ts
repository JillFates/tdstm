import { Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {GridModel} from 'tds-component-library';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
declare var jQuery: any;

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: 'dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {
	private userContext: any;
	public gridModel: GridModel;
	public showOnlyWIP;
	icons = FA_ICONS;
	selectedBundle;
	teamHighlights$;
	selectedTags;

	constructor(
		private userContextService: UserContextService
	) {
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		console.log('hello world');
	}

	onShowOnlyWIPChange(event) {
		console.log('show only work in progress');
	}

	onRefeshData() {
		console.log('on refresh data');
	}

	cellClick(event) {
		console.log(' on cell clicked');
	}

	highlightByTeam(event) {
		console.log('something');
	}

	refreshDiagram() {
		console.log('refresh diagram');
	}
}