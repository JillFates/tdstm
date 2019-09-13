import {
	Component,
	OnInit,
	Input,
	Output,
	AfterViewInit,
	OnChanges,
	SimpleChanges,
	EventEmitter,
	Renderer2, ViewChild, ElementRef
} from '@angular/core';
import * as go from 'gojs';
import {CATEGORY_ICONS_PATH, CTX_MENU_ICONS_PATH, STATE_ICONS_PATH} from '../../constants/icons-path';
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
import {icon} from '@fortawesome/fontawesome-svg-core';
import {FA_ICONS} from '../../constants/fontawesome-icons';

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
					id="diagram-layout-container"
					[style.width]="containerWidth"
					[style.height]="containerHeight"></div>
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
				<div id="ctx-menu" #ctxMenu>
					<ul>
						<li id="start">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.start.icon" [styles]="{ color: ctxMenuIcons.start.color }">
								</fa-icon>
								Start
							</button>
						</li>
						<li id="done">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.done.icon" [styles]="{ color: ctxMenuIcons.done.color }">
								</fa-icon>
								Done
							</button>
						</li>
						<li id="hold">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.hold.icon" [styles]="{ color: ctxMenuIcons.hold.color }">
								</fa-icon>
								Hold
							</button>
						</li>
						<li id="invoke">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.invoke.icon" [styles]="{ color: ctxMenuIcons.invoke.color }">
								</fa-icon>
								Invoke
							</button>
						</li>
						<li id="edit">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.edit.icon" [styles]="{ color: ctxMenuIcons.edit.color }">
								</fa-icon>
								Edit
							</button>
						</li>
						<li id="view">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.view.icon" [styles]="{ color: ctxMenuIcons.view.color }">
								</fa-icon>
								View
							</button>
						</li>
						<li id="assign-to-me">
							<button class="btn">
								<fa-icon [icon]="ctxMenuIcons.assignToMe.icon" [styles]="{ color: ctxMenuIcons.assignToMe.color }">
								</fa-icon>
								Assign to me
							</button>
						</li>
					</ul>
				</div>
				<div
					id="overview-container"
					class="overview-container"
					[class.reset-overview-index]="resetOvIndex"></div>
		</div>
	`,
	// styleUrls: ['../../../../../css/shared/components/diagram-layout.component.scss']
})
export class DiagramLayoutComponent implements OnInit, AfterViewInit, OnChanges {
	@Input() name: string;
	@Input() model: any;
	@Input() nodeDataArray = [];
	@Input() linksPath = [];
	@Input() nodeTemplateOpts: go.Node;
	@Input() linkTemplateOpts: go.Link;
	@Input() layoutOpts: go.Layout;
	@Input() containerWidth: string;
	@Input() containerHeight: string;
	@Output() nodeClicked: EventEmitter<number> = new EventEmitter<number>();
	@Output() editClicked: EventEmitter<number> = new EventEmitter<number>();
	@Output() showTaskDetailsClicked: EventEmitter<number> = new EventEmitter<number>();
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
	@ViewChild('ctxMenu') ctxMenu: ElementRef;
	shouldShowCtxMenu = false;

	constructor(private renderer: Renderer2) { /* Constructor */ }

	ngOnInit() {
		// document.addEventListener('contextmenu', (e) => {
		// 		e.preventDefault();
		// 		return false;
		// 	});
	}

	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges && (simpleChanges.nodeDataArray && simpleChanges.linksPath)
				&& !(simpleChanges.nodeDataArray.firstChange && simpleChanges.linksPath.firstChange)
		) {
			// console.log('simpleChange: ', simpleChanges);
			this.loadModel();
			this.initialiseDiagramContainer();
			this.generateDiagram();
		}
	}

	ngAfterViewInit() {
		if (this.nodeDataArray && this.linksPath) {
			this.loadModel();
			// console.log('diagram', this.nodeDataArray, this.linksPath);
			this.initialiseDiagramContainer();
			this.generateDiagram();
		}
	}

	initialiseDiagramContainer(): void {
		if (!this.diagram) {
			this.diagram = new Diagram('digraph-layout-container');
		}
	}

	onNodeClick(inputEvent: InputEvent, obj: any): void {
		console.log('node clicked', obj.selectionAdornmentTemplate);
		// if (obj && obj.part && obj.part.data) { this.nodeClicked.emit(obj.part.data.id); }
		if (obj && obj.part && obj.part.data) {
			obj.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		}
	}

	loadModel(): void {
		// console.log('loadModel');
		if (this.nodeDataArray && this.linksPath) {
			// console.log('load model: ', this.nodeDataArray, this.linksPath);
			this.myModel = new go.GraphLinksModel(this.nodeDataArray, this.linksPath);
		}
	}

	generateDiagram(): void {
		// console.log('generate');
		this.diagram.startTransaction('generateDiagram');
		this.diagram.initialDocumentSpot = Spot.TopLeft;
		this.diagram.initialViewportSpot = Spot.TopLeft;
		this.diagram.undoManager.isEnabled = true;
		this.diagram.allowZoom = true;
		// this.diagram.initialAutoScale = go.Diagram.UniformToFill;
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

	overviewTemplate() {
		this.diagramOverview = new Overview('overview-container');
		this.diagramOverview.observed = this.diagram;
		this.diagramOverview.contentAlignment = go.Spot.Center;
	}

	layeredDigraphLayout(opts?: any): void {
		// console.log('direction: ', this.direction);
		const ldl = new go.LayeredDigraphLayout();
		ldl.direction = 0;
		ldl.layerSpacing = 25;
		ldl.columnSpacing = 25;
		ldl.cycleRemoveOption = go.LayeredDigraphLayout.CycleDepthFirst;

		this.diagram.commit(d => d.layout = ldl);
	}

	setTreeLayout(opts?: any): void {
		const treeLayout = new go.TreeLayout();
		treeLayout.angle = 0;
		treeLayout.layerSpacing = 35;
		this.diagram.commit(d => d.layout = treeLayout);
	}

	setForceDirectedLayout(opts?: any): void {
		const forceDirectedLayout = new go.ForceDirectedLayout();
		forceDirectedLayout.arrangementSpacing = new go.Size(100, 105);
		this.diagram.commit(d => d.layout = forceDirectedLayout);
	}

	setDiagramNodeTemplate(): void {
		this.diagram.nodeTemplate = this.setNodeTemplate();
	}

	setDiagramLinksTemplate(): void {
		this.diagram.linkTemplate = this.linkTemplate();
	}

	linkTemplate(templateOpts?: go.Link, linkShapeOpts?: go.Shape): go.Link {

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

	setDirection(dir: any): void {
		// console.log('Direction: ', dir.target.value);
		this.direction = dir.target.value;
	}

	setNodeTemplate(): go.Node {
		console.log('default template');
		this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
		const node = new go.Node(go.Panel.Horizontal);
		// node.background = '#ddd'; // '#3c8dbc';
		// // node.background = 'lightblue';
		node.selectionAdorned = true;
		// node.add(this.containerShape());
		node.add(this.containerPanel());
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		// node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		return node;
	}

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

	containerShape(): go.Shape {
		const container = new go.Shape();
		container.figure = 'RoundedRectangle';
		container.strokeWidth = 2;
		container.stroke = '#ddd';
		container.fill = 'white';

		return container;
	}

	containerPanel(): go.Panel {
		const panel = new go.Panel(go.Panel.Auto);
		panel.background = '#fff';

		panel.add(this.containerShape());
		panel.add(this.panelBody());

		return panel;
	}

	panelBody(): go.Panel {
		const panel = new go.Panel(go.Panel.Horizontal);

		panel.add(this.iconShape());
		panel.add(this.categoryIconShape());
		panel.add(this.textBlockShape());

		return panel;
	}

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
		iconShape.bind(new Binding('background', 'icon',
			(val: any) => this.getStatusBackground(this.stateIcons[val])));
		iconShape.bind(new Binding('geometry', 'icon',
			(val: any) => this.getIcon(this.stateIcons[val])));

		console.log('icon position: ', iconShape.isGeometryPositioned);

		return iconShape;
	}

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
			(val: any) => this.getIcon(this.categoryIcons[val])));

		console.log('category icon shape position: ', categoryIconShape.position);
		return categoryIconShape;
	}

	textBlockShape(options?: any): TextBlock {
		if (options) { return options; }

		const textBlock = new TextBlock();
		textBlock.margin = 8;
		textBlock.stroke = 'black';
		textBlock.font = 'bold 16px sans-serif';
		// textBlock.wrap = TextBlock.WrapBreakAll;
		textBlock.bind(new Binding('text', 'label'));
		return textBlock;
	}

	getIcon(icon: ITaskGraphIcon): any {
		if (!icon) {
			console.log('notFound: ', icon);
			return go.Geometry.parse(this.categoryIcons.unknown.icon);
			// return this.stateIcons.unknown;
		}
		return go.Geometry.parse(icon.icon);
		// return this.stateIcons[name];
	}

	getStatusBackground(icon: ITaskGraphIcon): string {
		return icon.background;
	}

	zoomIn(): void {
		this.diagram.commandHandler.increaseZoom(1.2);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	zoomOut(): void {
		this.diagram.commandHandler.decreaseZoom(0.8);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	highlightAllNodes(): void {
		this.diagram.commit(d => {
			d.selectCollection(d.nodes);
		});
	}

	highlightNodesByCategory(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.category));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	highlightNodesByStatus(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.status));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	highlightNodesByText(match: string): void {
		this.diagram.commit(d => {
			if (match.length <= 0) { return d.clearSelection(); }
			const highlightCollection = d.nodes.filter(f => f.data.label.toLowerCase().includes(match.toLowerCase()));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	overrideMouseWheel(): void {
		// console.log('overriding mouse wheel event');
		const tool = this.diagram.currentTool;
		tool.standardMouseWheel = () => {
			go.Tool.prototype.standardMouseWheel.call(tool);
			this.setNodeTemplateByScale(this.diagram.scale, this.diagram.lastInput);
		};
	}

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

	highScaleNodeTemplate(): void {
		this.diagram.commit(() => this.diagram.nodeTemplate = this.setNodeTemplate());
	}

	mediumScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  iconShape = new go.Shape();
		iconShape.figure = 'RoundedRectangle';
		iconShape.margin = 3;
		iconShape.strokeWidth = 2;
		iconShape.alignment = go.Spot.LeftCenter;
		iconShape.stroke = 'white';
		iconShape.desiredSize = new go.Size(25, 25);
		iconShape.bind(new go.Binding('background', 'color'));
		iconShape.bind(new go.Binding('geometry', 'icon',
			(val: any) => this.getIcon(val)));
		node.add(iconShape);

		const  categoryIconShape = new go.Shape();
		categoryIconShape.figure = 'RoundedRectangle';
		categoryIconShape.margin = 3;
		categoryIconShape.strokeWidth = 2;
		categoryIconShape.alignment = go.Spot.LeftCenter;
		categoryIconShape.desiredSize = new go.Size(25, 25);
		categoryIconShape.stroke = '#7a7a7a';
		categoryIconShape.bind(new go.Binding('geometry', 'category',
			(val: any) => this.getIcon(val)));
		node.add(categoryIconShape);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	lowScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  shape = new go.Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new go.Size(25, 35);
		shape.bind(new go.Binding('fill', 'category', this.getCategoryColor));
		node.add(shape);
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	getCategoryColor(name: string): string {
		const color = categoryColors[name];
		return color ? color : '#3c8dbc';
	}

	contextMenu(): any {
		const $ = go.GraphObject.make;
		return $(go.HTMLInfo,  // that has one button
			{ show: () => this.showCtxMenu, mainElement: this.ctxMenu.nativeElement }
							// $('ContextMenuButton',
							// 		$(go.TextBlock, { text: 'Change Color', stroke: '#3c8dbc'}),
							// 		{ click: (e: go.InputEvent, obj: go.GraphObject) => this.changeColor(e, obj, this.diagram) }),
							// $('ContextMenuButton',
							// 		$(go.TextBlock, { text: 'Show Detail', stroke: '#3c8dbc'}),
							// 		{ click: (e: go.InputEvent, obj: go.GraphObject) => this.showTaskDetails(obj, this.diagram) }),
							// $('ContextMenuButton',
							// 		$(go.TextBlock, { text: 'Edit', stroke: '#3c8dbc'}),
							// 		{ click: (e: go.InputEvent, obj: go.GraphObject) => this.editTask(obj, this.diagram) })
							// more ContextMenuButtons would go here
					); // end Adornment
	}

	showCtxMenu(obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool): void {
		this.renderer.setStyle(this.ctxMenu.nativeElement, 'display', 'block');

		this.shouldShowCtxMenu = true;

		const mousePt = diagram.lastInput.viewPoint;
		this.renderer.setStyle(this.ctxMenu.nativeElement, 'left', `${mousePt.x}px`);
		this.renderer.setStyle(this.ctxMenu.nativeElement, 'top', `${mousePt.y}px`);
	}

	showTaskDetails(obj: go.GraphObject, diagram: go.Diagram): void {
		const nodeData = obj.part.data;
		this.showTaskDetailsClicked.emit(nodeData.id);
	}

	editTask(obj: go.GraphObject, diagram: go.Diagram): void {
		const nodeData = obj.part.data;
		this.editClicked.emit(nodeData.id);
	}

	changeColor(e: go.InputEvent, obj: go.GraphObject, diagram) {
		diagram.commit(d => {
			// get the context menu that holds the button that was clicked
			const contextmenu = obj.part;
			console.log('menu: ', contextmenu);
			// get the node data to which the Node is data bound
			const nodedata = contextmenu.data;
			// compute the next color for the node
			let newcolor = 'lightblue';
			switch (nodedata.color) {
				case 'lightblue': newcolor = 'lightgreen'; break;
				case 'lightgreen': newcolor = 'lightyellow'; break;
				case 'lightyellow': newcolor = 'orange'; break;
				case 'orange': newcolor = 'lightblue'; break;
			}
			// modify the node data
			// this evaluates data Bindings and records changes in the UndoManager
			d.model.set(nodedata, 'color', newcolor);
		}, 'changed color');

	}

	resetOverviewIndex(): void {
		this.resetOvIndex = true;
	}

	restoreOverviewIndex(): void {
		this.resetOvIndex = false;
	}

}
