import {Component} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent {
	data: any;
	ctxOpts:  ITdsContextMenuOption;

	constructor() {
		// constructor

		this.data = {
			nodeDataArray: [
				{
					name: 'test',
					key: 'a',
					status: 'hold',
					tooltipData: {
						headerText: 'Test',
						headerBackgroundColor: '#0b63a8',
						headerTextColor: '#ffffff',
						data: [
							{
								label: 'Status',
								value: 'Hold'
							},
							{
								label: 'key',
								value: 'a'
							}
						]
					}
				},
				{
					name: 'test 2',
					key: 'b',
					status: 'started',
					tooltipData: {
						headerText: 'test 2',
						headerBackgroundColor: '#0b63a8',
						headerTextColor: '#ffffff',
						data: [
							{
								label: 'Status',
								value: 'Started'
							},
							{
								label: 'key',
								value: 'b'
							}
						]
					}
				},
				{
					name: 'test 3',
					key: 'c',
					status: 'pending'
				},
				{
					name: 'test 4',
					key: 'd',
					status: 'pending'
				},
				{
					name: 'test 5',
					key: 'e',
					status: 'pending'
				},
				{
					name: 'test 6',
					key: 'f',
					status: 'pending'
				},
				{
					name: 'test 7',
					key: 'g',
					status: 'pending'
				}
			],
			linkDataArray: [
				{
					from: 'a',
					to: 'b'
				},
				{
					from: 'b',
					to: 'c'
				},
				{
					from: 'c',
					to: 'd'
				},
				{
					from: 'd',
					to: 'e'
				},
				{
					from: 'e',
					to: 'f'
				},
				{
					from: 'f',
					to: 'g'
				},
				{
					from: 'g',
					to: 'a'
				}
			]
		};
		this.ctxOpts = {
			fields: [
				{
					label: 'test1',
					event: 'test1',
					icon: {
						icon: 'faUser'
					},
					status: 'stat',
					isAvailable: (n: any) => !(n.status === 'hold'),
					hasPermission: () => ['view', 'edit'].includes('view')
				},
				{
					label: 'test2',
					event: 'test2',
					icon: {
						icon: 'faUser'
					},
					status: 'stat',
					isAvailable: (n: any) => !(n.status === 'started'),
					hasPermission: () => ['view', 'edit'].includes('view')
				}
			]
		};
	}
}
