import { Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {GridModel} from 'tds-component-library';
declare var jQuery: any;

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: 'dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {
	private userContext: any;
	public gridModel: GridModel;
	public showOnlyWIP;

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
}
