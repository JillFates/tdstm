import { Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
declare var jQuery: any;

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: './dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {
	private userContext: any;

	constructor(
		private userContextService: UserContextService
	) {
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		console.log('hola mundos');
	}
}
