import {
	Component,
	Input,
	Output,
	AfterViewInit,
	OnChanges,
	SimpleChanges,
	EventEmitter,
	ViewChild,
	ElementRef, HostListener
} from '@angular/core';
import * as go from 'gojs';
import {CATEGORY_ICONS_PATH, CTX_MENU_ICONS_PATH, STATE_ICONS_PATH} from '../../../modules/taskManager/components/common/constants/task-icon-path';
import {
	Adornment,
	Binding,
	Diagram,
	InputEvent,
	Overview,
	Panel,
	Placeholder,
	Shape,
	Spot,
	TextBlock
} from 'gojs';
import {ITaskGraphIcon} from '../../../modules/taskManager/model/task-graph-icon.model';
import {FA_ICONS} from '../../constants/fontawesome-icons';
import {ReplaySubject} from 'rxjs';
import {DiagramContextMenuComponent} from './context-menu/diagram-context-menu.component';
import {IDiagramContextMenuModel, IDiagramContextMenuOption} from './model/diagram-context-menu.model';
import {IGraphTask} from '../../../modules/taskManager/model/graph-task.model';
import {IDiagramLayoutModel} from './model/diagram-layout.model';

const enum NodeTemplateEnum {
	HIGH_SCALE,
	MEDIUM_SCALE,
	LOW_SCALE
}

const categoryColors = {
	physical: 'brown',
	shutdown: 'red',
	general: '#ddd',
	moveday: 'skyblue'
};

@Component({
	selector: 'tds-diagram-layout',
	template: `
		<div class="diagram-layout-container">
			<div
					id="diagram-layout"
					[style.width]="containerWidth"
					[style.height]="screenHeight"
					#diagramLayout></div>
			<div id="graph-control-btn-group">
				<button class="btn btn-block">
					<fa-icon [icon]="faIcons.faCog" size="lg"></fa-icon>
				</button>
				<button class="btn btn-block" (click)="zoomIn()">
					<fa-icon [icon]="faIcons.faSearchPlus" size="lg"></fa-icon>
				</button>
				<button class="btn btn-block" (click)="zoomOut()">
					<fa-icon [icon]="faIcons.faSearchMinus" size="lg"></fa-icon>
				</button>
			</div>
			<tds-task-context-menu
				[data]="ctxMenuData$ | async"
				#taskCtxMenu></tds-task-context-menu>
			<div
				id="overview-container"
				class="overview-container"
				[class.reset-overview-index]="resetOvIndex"
				#overviewContainer></div>
		</div>`,
})
export class DiagramLayoutComponent implements AfterViewInit, OnChanges {
	@Input() nodeDataArray = [];
	@Input() linksPath = [];
	@Input() nodeTemplateOpts: go.Node;
	@Input() linkTemplateOpts: go.Link;
	@Input() layoutOpts: go.Layout;
	@Input() containerWidth: string;
	@Input() containerHeight: string;
	@Input() currentUser: any;
	@Input() contextMenuOptions: IDiagramContextMenuOption;
	@Output() nodeClicked: EventEmitter<number> = new EventEmitter<number>();
	@Output() editClicked: EventEmitter<string | number> = new EventEmitter<string | number>();
	@Output() showTaskDetailsClicked: EventEmitter<string | number> = new EventEmitter<string | number>();
	@ViewChild('diagramLayout') diagramLayout: ElementRef;
	@ViewChild('overviewContainer') overviewContainer: ElementRef;
	@ViewChild('taskCtxMenu') taskCtxMenu: DiagramContextMenuComponent;
	stateIcons = STATE_ICONS_PATH;
	categoryIcons = CATEGORY_ICONS_PATH;
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	faIcons = FA_ICONS;
	diagram: go.Diagram;
	myModel: go.Model;
	direction = 0;
	diagramAvailable = false;
	tasks: any[];
	actualNodeTemplate: number;
	diagramOverview: Overview;
	resetOvIndex: boolean;
	ctxMenuData$: ReplaySubject<IDiagramContextMenuModel> = new ReplaySubject();
	screenHeight: any;

	constructor() {
		this.onResize();
	}

	/**
	 * Detect changes to update nodeDataArray and linksPath accordingly
	 **/
	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges) {
			if ((simpleChanges.nodeDataArray && simpleChanges.nodeDataArray)
				&& !(simpleChanges.nodeDataArray.firstChange && simpleChanges.nodeDataArray.firstChange)
			) {
				this.loadAll();
			}
		}
	}

	@HostListener('window:resize', ['$event'])
	onResize(event?: any) {
		this.screenHeight = `${window.innerHeight - 230}px`;
		console.log(this.screenHeight);
	}

	ngAfterViewInit() {
		if (this.nodeDataArray && this.linksPath) {
			this.loadAll();
		}
	}

	/**
	 * Load all data related to the diagram
	 **/
	loadAll(): void {
		this.loadModel();
		this.initialiseDiagramContainer();
		this.generateDiagram();

	}

	/**
	 * set the element used as container to hold the diagram
	 **/
	initialiseDiagramContainer(): void {
		if (!this.diagram) {
			this.diagram = new Diagram(this.diagramLayout.nativeElement);
		}
	}

	/**
	 * Node click handler to set the adornments or any other operation on nodes when clicked
	 **/
	onNodeClick(inputEvent: InputEvent, obj: any): void {
		if (obj && obj.part && obj.part.data) {
			obj.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		}
	}

	/**
	 * Load data model used by the diagram
	 **/
	loadModel(): void {
		if (this.nodeDataArray && this.linksPath) {
			this.myModel = new go.GraphLinksModel(this.nodeDataArray, this.linksPath);
		}
	}

	/**
	 * Generate Diagram canvas
	 **/
	generateDiagram(): void {
		this.diagram.startTransaction('generateDiagram');
		this.diagram.initialDocumentSpot = Spot.TopLeft;
		this.diagram.initialViewportSpot = Spot.TopLeft;
		this.diagram.undoManager.isEnabled = true;
		this.diagram.allowZoom = true;
		this.setDiagramNodeTemplate();
		this.setDiagramLinksTemplate();
		this.diagram.allowSelect = true;
		this.diagram.commitTransaction('generateDiagram');
		this.setTreeLayout();
		this.diagram.model = this.myModel;
		this.diagramAvailable = true;
		this.overrideMouseWheel();
		this.overviewTemplate();
		this.diagram.commandHandler.zoomToFit();
	}

	/**
	 * Diagram overview (Minimap)
	 **/
	overviewTemplate() {
		if (!this.diagramOverview) {
			this.diagramOverview = new Overview(this.overviewContainer.nativeElement);
			this.diagramOverview.observed = this.diagram;
			this.diagramOverview.contentAlignment = go.Spot.Center;
		}
	}

	/**
	 * Sets the Layered Digraph Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setLayeredDigraphLayout(opts?: any): void {
		// console.log('direction: ', this.direction);
		const ldl = new go.LayeredDigraphLayout();
		ldl.direction = 0;
		ldl.layerSpacing = 25;
		ldl.columnSpacing = 25;
		ldl.cycleRemoveOption = go.LayeredDigraphLayout.CycleDepthFirst;

		this.diagram.commit(d => d.layout = ldl);
	}

	/**
	 * Sets the Tree Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setTreeLayout(opts?: any): void {
		const treeLayout = new go.TreeLayout();
		treeLayout.angle = 0;
		treeLayout.layerSpacing = 35;
		this.diagram.commit(d => d.layout = treeLayout);
	}

	/**
	 * Sets the Force Directed Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setForceDirectedLayout(opts?: any): void {
		const forceDirectedLayout = new go.ForceDirectedLayout();
		forceDirectedLayout.arrangementSpacing = new go.Size(100, 105);
		this.diagram.commit(d => d.layout = forceDirectedLayout);
	}

	/**
	 * Sets the template for each node in the Diagram
	 **/
	setDiagramNodeTemplate(): void {
		this.diagram.nodeTemplate = this.setNodeTemplate();
	}

	/**
	 * Sets the template for each node link in the Diagram
	 **/
	setDiagramLinksTemplate(): void {
		this.diagram.linkTemplate = this.linkTemplate();
	}

	/**
	 * Links template configuration
	 * @param {Link} templateOpts > optional configuration to use for the template
	 * @param {Shape} linkShapeOpts > optional shape for links
	 **/
	linkTemplate(templateOpts?: go.Link, linkShapeOpts?: go.Shape): go.Link {

		// If template configuration is provided, return it as the template to use
		if (templateOpts) {
			if (linkShapeOpts) { templateOpts.add(linkShapeOpts); }
			return templateOpts;
		}

		const linkTemplate = new go.Link();
		linkTemplate.routing = go.Link.AvoidsNodes;
		linkTemplate.corner = 5;

		const linkShape = new go.Shape();
		linkShape.strokeWidth = 5;
		linkShape.stroke = '#ddd';

		linkTemplate.add(linkShape);

		return linkTemplate;
	}

	/**
	 * Links template configuration
	 **/
	setDirection(dir: any): void {
		// console.log('Direction: ', dir.target.value);
		this.direction = dir.target.value;
	}

	/**
	 * Node template configuration
	 * @param {any} templateOpts > optional configuration to use for the template
	 **/
	setNodeTemplate(templateOpts?: go.Node): go.Node {

		// If template configuration is provided, return it as the template to use
		if (templateOpts) {
			return templateOpts;
		}

		this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
		const node = new go.Node(go.Panel.Horizontal);
		node.selectionAdorned = true;
		node.add(this.containerPanel());
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		// node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		return node;
	}

	/**
	 * Node Adornment template configuration
	 * @param {go.Node} node > optional node to add the adornment to
	 **/
	selectionAdornmentTemplate(node?: go.Node): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Auto);
		selAdornmentTemplate.selectionAdorned = true;
		if (node) { selAdornmentTemplate.adornedObject = node; }

		const selAdornmentShape = new Shape();
		selAdornmentShape.figure = 'RoundedRectangle';
		selAdornmentShape.fill = null;
		selAdornmentShape.stroke = 'red';
		selAdornmentShape.strokeWidth = 4;

		const placeholder = new Placeholder();

		// placeholder.background = 'transparent';
		// placeholder.visible = true;
		selAdornmentTemplate.add(selAdornmentShape);
		selAdornmentTemplate.add(placeholder);

		return selAdornmentTemplate;
	}

	/**
	 * Node inner container to draw borders to the panel
	 **/
	containerShape(): go.Shape {
		const container = new go.Shape();
		container.figure = 'RoundedRectangle';
		container.strokeWidth = 2;
		container.stroke = '#ddd';
		container.fill = 'white';

		return container;
	}

	/**
	 * Node outer panel container to hold individual shapes related to the node
	 **/
	containerPanel(): go.Panel {
		const panel = new go.Panel(go.Panel.Auto);
		panel.background = '#fff';

		panel.add(this.containerShape());
		panel.add(this.panelBody());

		return panel;
	}

	/**
	 * Node panel body holding the node content
	 **/
	panelBody(): go.Panel {
		const panel = new go.Panel(go.Panel.Horizontal);

		panel.add(this.iconShape());
		panel.add(this.categoryIconShape());
		panel.add(this.textBlockShape());

		return panel;
	}

	/**
	 * Icon shape configuration
	 * @param {any} options > optional configuration to use for the icon shape
	 **/
	iconShape(options?: any): Shape {
		if (options) { return options; }

		const  iconShape = new Shape();
		iconShape.figure = 'RoundedRectangle';
		iconShape.margin = 5;
		iconShape.strokeWidth = 2;
		iconShape.stroke = 'white';
		iconShape.fill = 'white';
		iconShape.desiredSize = new go.Size(25, 25);
		iconShape.isGeometryPositioned = true;
		iconShape.position = new go.Point(0, 0);
		iconShape.bind(new Binding('background', 'status',
			(val: string) => this.getStatusBackground(this.stateIcons[val.toLowerCase()])));
		iconShape.bind(new Binding('geometry', 'status',
			(val: string) => this.getIcon(this.stateIcons[val.toLowerCase()])));

		return iconShape;
	}

	/**
	 * Icon shape configuration for 'category' property
	 * @param {any} options > optional configuration to use for the icon shape
	 **/
	categoryIconShape(options?: any): Shape {
		if (options) { return options; }

		const  categoryIconShape = new Shape();
		categoryIconShape.figure = 'RoundedRectangle';
		categoryIconShape.margin = 8;
		categoryIconShape.strokeWidth = 2;
		categoryIconShape.desiredSize = new go.Size(25, 25);
		categoryIconShape.stroke = '#ddd';
		categoryIconShape.fill = '#908f8f';
		categoryIconShape.bind(new Binding('geometry', 'category',
			(val: string) => this.getIcon(this.categoryIcons[val.toLowerCase()])));

		return categoryIconShape;
	}

	/**
	 * Text block configuration
	 * @param {any} options > optional configuration to use for the text block
	 **/
	textBlockShape(options?: any): TextBlock {
		if (options) { return options; }

		const textBlock = new TextBlock();
		textBlock.margin = 8;
		textBlock.stroke = 'black';
		textBlock.font = 'bold 16px sans-serif';
		// textBlock.wrap = TextBlock.WrapBreakAll;
		textBlock.bind(new Binding('text', 'comment'));
		return textBlock;
	}

	/**
	 * Icons handler to parse to geometry object to be used for the nodes
	 * @param {ITaskGraphIcon} icon > icon to use as the icon shape
	 **/
	getIcon(icon: ITaskGraphIcon): any {
		if (!icon) {
			return go.Geometry.parse(this.categoryIcons.unknown.icon);
			// return this.stateIcons.unknown;
		}
		return go.Geometry.parse(icon.icon);
		// return this.stateIcons[name];
	}

	/**
	 * Status background handler to get background color for an icon by status
	 * @param {ITaskGraphIcon} icon > icon from which to get background color
	 **/
	getStatusBackground(icon: ITaskGraphIcon): string {
		return icon.background;
	}

	/**
	 * Zoom in on the diagram
	 **/
	zoomIn(): void {
		this.diagram.commandHandler.increaseZoom(1.2);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	/**
	 * Zoom out on the diagram
	 **/
	zoomOut(): void {
		this.diagram.commandHandler.decreaseZoom(0.8);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	/**
	 * highlight all nodes on the diagram
	 **/
	highlightAllNodes(): void {
		this.diagram.commit(d => {
			d.selectCollection(d.nodes);
		});
	}

	/**
	 * highlight nodes by category name on the diagram
	 **/
	highlightNodesByCategory(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.category));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight nodes by status type on the diagram
	 **/
	highlightNodesByStatus(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.status));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight nodes by team on the diagram
	 **/
	highlightNodesByTeam(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.userSelectedCol3));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight filter, every node that has a description matching the text received will be highlighted
	 * @param {string} match > optional configuration to use for the template
	 **/
	highlightNodesByText(match: string): void {
		this.diagram.commit(d => {
			if (match.length <= 0) { return d.clearSelection(); }
			const highlightCollection = d.nodes.filter(f => f.data.comment.toLowerCase().includes(match.toLowerCase()));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * Override mousewheel handler to add the zooming scales
	 **/
	overrideMouseWheel(): void {
		// console.log('overriding mouse wheel event');
		const tool = this.diagram.currentTool;
		tool.standardMouseWheel = () => {
			go.Tool.prototype.standardMouseWheel.call(tool);
			this.setNodeTemplateByScale(this.diagram.scale, this.diagram.lastInput);
		};
	}

	/**
	 * update node templates depending on the actual scale
	 * @param {number} scale > actual zooming scale
	 * @param {InputEvent} inputEvent > triggered event object
	 **/
	setNodeTemplateByScale(scale?: number, inputEvent?: go.InputEvent): void {
		if (inputEvent.control) {
			if (scale >= 0.6446089162177968
					&& this.actualNodeTemplate !== NodeTemplateEnum.HIGH_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
				console.log('scale >= 0.6446089162177968');
				this.highScaleNodeTemplate();
			}
			if (scale < 0.6446089162177968 && scale > 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.MEDIUM_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.MEDIUM_SCALE;
				console.log('scale < 0.6446089162177968');
				this.mediumScaleNodeTemplate();
			}
			if (scale <= 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.LOW_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.LOW_SCALE;
				console.log('scale <= 0.4581115219913999');
				this.lowScaleNodeTemplate();
			}
		}
	}

	/**
	 * High scale node template, this is where nodes are most visible and provide more visual feedback
	 **/
	highScaleNodeTemplate(): void {
		this.diagram.commit(() => this.diagram.nodeTemplate = this.setNodeTemplate());
	}

	/**
	 * Medium scale node template, this is where nodes are visible but don't provide a lot of visual feedback
	 **/
	mediumScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		node.add(this.iconShape());

		node.add(this.categoryIconShape());
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	/**
	 * Low scale node template, this is where nodes are least visible and provide only color visual feedback
	 **/
	lowScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  shape = new go.Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new go.Size(25, 35);
		shape.bind(new go.Binding('fill', 'status',
			(status: string) => this.getStatusColor(status.toLowerCase())));
		node.add(shape);
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	/**
	 * Node bubble color based on category
	 **/
	getStatusColor(name: string): string {
		if (!this.stateIcons[name]) { return '#ddd'; }
		return this.stateIcons[name].background;
	}

	/**
	 * Node Context menu containing a variety of operations like 'edit task', 'show task detail', 'start', 'hold', etc
	 **/
	contextMenu(): any {
		const $ = go.GraphObject.make;
		return $(go.HTMLInfo,  // HTML element to contain the context menu
		{
								show: (obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool) => this.showCtxMenu(obj, diagram, tool),
								mainElement: this.taskCtxMenu.ctxMenu.nativeElement
							}
					); // end Adornment
	}

	/**
	 * handler to show context menu passing relevant data to context menu component
	 * @param {GraphObject} obj
	 * @param {Diagram} diagram
	 * @param {Tool} tool
	 **/
	showCtxMenu(obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool): void {
		if (this.taskCtxMenu) {
			const mousePt = diagram.lastInput.viewPoint;
			this.ctxMenuData$.next({
				selectedNode: obj.part.data,
				currentUser: this.currentUser,
				mousePt: {x: `${mousePt.x}px`, y: `${mousePt.y}px`},
				options: this.contextMenuOptions
			});
		}
	}

	/**
	 * handle to reset the overview index value to 0 so that it's not on top of all elements
	 **/
	resetOverviewIndex(): void {
		this.resetOvIndex = true;
	}

	/**
	 * handle to restore the overview index value so that it's on top of all elements
	 **/
	restoreOverviewIndex(): void {
		this.resetOvIndex = false;
	}

	/**
	 * update node on graph
	 **/
	updateNode(data: IGraphTask): void {
		this.diagram.commit(d => {
			const update = Object.assign({}, data);
			if (!update.key) { update.key = data.taskNumber; }
			console.log('update key: ', update.key, d.nodes.map(n => n.part.data.key));
			const node = d.nodes.filter(f => f.part.data.key === update.key).first();
			console.log('node: ', node.part.data);
			node.part.data = update;
			node.updateAdornments();
		});
	}

}
