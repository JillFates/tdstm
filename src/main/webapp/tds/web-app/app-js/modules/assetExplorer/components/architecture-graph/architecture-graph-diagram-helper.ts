import {
	Binding, GraphObject,
	Layout,
	Link,
	Margin,
	Node,
	Panel,
	Picture, Point,
	Shape,
	Size,
	Spot,
	TextBlock,
	TreeLayout,
	LayeredDigraphLayout, default as go, Adornment, Placeholder, InputEvent
} from 'gojs';
import {
	ITdsContextMenuOption
} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {IconModel, IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
// import {ILinkPath} from '../../../taskManager/components/neighborhood/neighborhood.component';
import {IArchitectureGraphAsset, IAssetLink, IAssetNode} from '../../model/architecture-graph-asset.model';
import {ASSET_ICONS} from '../../model/asset-icon.constant';

export class ArchitectureGraphDiagramHelper {
	params: any;
	refCycles: string[];
	private readonly cyclicalColor = '#1945dd';
	private readonly arrowColor = '#c3c3c3';

	constructor(private cycles: number[][]) {
		this.refCycles = [];
			// Architecture Graph Diagram Helper Constructor
		cycles.forEach((cycle: number[]) => {
			this.refCycles.push(cycle.join('#'));
		});
	}

	static isDeviceVirtualServer(type: string): boolean {
		return ['vm', 'virtual'].indexOf(type) !== -1;
	}

	static isDeviceServer(type: string): boolean {
		return ['server', 'appliance', 'blade'].indexOf(type) !== -1;
	}

	static isDeviceStorage(type: string): boolean {
		return ['array', 'disk', 'nas', 'san', 'san switch', 'storage', 'tape', 'tape library',
			'virtual tape library'].indexOf(type) !== -1;
	}

	static isDeviceNetwork(type: string): boolean {
		return ['encoder', 'load balancer', 'modem', 'module', 'multiplexer',
			'network', 'probe', 'receiver', 'router', 'switch', 'telecom',
			'terminal server', 'vpn'].indexOf(type) !== -1;
	}

	/**
	 * Diagram data object
	 */
	diagramData(params?: any): IDiagramData {
		const d = this.data(params.data);
		this.params = params;
		return {
			nodeDataArray: d.nodeDataArray,
			linkDataArray: d.linkDataArray,
			currentUserId: params.currentUserId,
			ctxMenuOptions: this.contextMenuOptions(),
			nodeTemplate: params.iconsOnly ?
				this.iconOnlyNodeTemplate({ isExpandable: params.extras.isExpandable && params.extras.isExpandable })
				: this.nodeTemplate({ isExpandable: params.extras.isExpandable && params.extras.isExpandable }),
			linkTemplate: this.linkTemplate(),
			lowScaleTemplate: params.iconsOnly ?
				this.iconOnlyNodeTemplate({ isExpandable: params.extras.isExpandable && params.extras.isExpandable })
				: this.lowScaleNodeTemplate(),
			mediumScaleTemplate: params.iconsOnly ?
				this.iconOnlyNodeTemplate({ isExpandable: params.extras.isExpandable && params.extras.isExpandable })
				: this.mediumScaleNodeTemplate(),
			layout: this.layout(),
			rootNode: params.rootAsset,
			extras: params.extras
		};
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	data(data?: any): any {
		const nodeDataArr = [];
		const linksPath = [];

		const dataCopy: IArchitectureGraphAsset = Object.assign({}, data);

		dataCopy.nodes.map((t: IAssetNode) => {
			t.key = t.id;
			t.iconPath = ASSET_ICONS[t.assetClass.toLowerCase()] && ASSET_ICONS[t.assetClass.toLowerCase()].icon;
			nodeDataArr.push(t);
		});
		dataCopy.links
			.forEach((link: IAssetLink) => linksPath.push(this.getLinksPath(link)));
		// console.log('tasks found', this.tasks.length, 'tasks dependencies', linksPath);
		return {
			nodeDataArray: nodeDataArr,
			linkDataArray: linksPath,
		};
	}

	/**
	 * Load events to fill events dropdown
	 **/
	getLinksPath(link: any): any {
		const t = Object.assign({}, link);
		if (t) {
			return {
				from: t.parentId,
				to: t.childId
			};
		}
		return null;
	}

	currentUser(): Node {
		return null;
	}

	nodeTemplate(opts?: any): Node {
		const node = new Node(Panel.Horizontal);
		node.margin = new Margin(1, 1, 1, 1);

		const panel = new Panel(Panel.Auto);
		panel.background = '#fff';
		panel.padding = new Margin(0, 0, 0, 0);

		const panelBody = new Panel(Panel.Vertical);
		panel.padding = new Margin(0, 0, 0, 0);
		panel.margin = new Margin(0, 0, 0, 0);

		// Picture Icon
		const iconPicture = new Picture();
		iconPicture.desiredSize = new Size(50, 50);
		iconPicture.bind(new Binding('source', '',
			(val: string) => {
				return this.getIconPath(val);
				}
		));
		iconPicture.imageAlignment = Spot.Center;

		// TextBlock
		const textBlock = new TextBlock();
		textBlock.textAlign = 'center';
		textBlock.desiredSize = new Size(60, 10);
		textBlock.stroke = '#000';
		textBlock.maxLines = 1;
		textBlock.wrap = TextBlock.None;
		textBlock.overflow = TextBlock.OverflowEllipsis;
		textBlock.bind(new Binding('text', 'name'));

		// hide if the node if the name is empty
		textBlock.bind(new Binding('visible', 'name', (val: string) => !!val));

		textBlock.mouseOver = (e: InputEvent, obj: TextBlock) => {
			if (obj.text && obj.text.length > 10) {
				obj.toolTip = this.textBlockTooltip(obj.text);
			}
		};

		panelBody.add(iconPicture);
		panelBody.add(textBlock);

		const shape = new Shape();
		shape.figure = 'RoundedRectangle';
		shape.fill = 'transparent';
		shape.strokeWidth = 4;
		shape.desiredSize = new Size(65, 65);
		shape.bind(new Binding('stroke', 'id', (val: any) => {
			if (val === this.params.rootAsset || val === this.params.rootAsset) {
				return 'red';
			} else {
				return 'transparent';
			}
		}));
		panel.add(shape);
		panel.add(panelBody);
		node.add(panel);

		node.bind(new Binding('selectionAdorned', 'id', (val: any) => {
			return val !== this.params.rootAsset;
		}));

		if (opts.isExpandable) {
			node.isTreeExpanded = false;
			const expandButton = GraphObject.make('TreeExpanderButton');
			node.add(expandButton);
		}

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	/**
	 * textblock tooltip
	 * @param tooltipText
	 */
	textBlockTooltip(tooltipText: string) {
		const $ = go.GraphObject.make;
		return $(Adornment,
			$(TextBlock,
				{
					text: tooltipText,
					background: '#fff',
					stroke: '#0077b8',
					font: '14px sans-serif'
				}
			)
		); // end Adornment
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
		if (this.params.iconsOnly) {
			selAdornmentShape.desiredSize = new Size(60, 60);
		} else {
			selAdornmentShape.desiredSize =  new Size(65, 65);
		}

		const placeholder = new Placeholder();

		selAdornmentTemplate.add(selAdornmentShape);
		selAdornmentTemplate.add(placeholder);

		return selAdornmentTemplate;
	}

	iconOnlyNodeTemplate(opts?: any): Node {
		const node = new Node(Panel.Horizontal);
		node.margin = new Margin(1, 1, 1, 1);

		const panel = new Panel(Panel.Auto);
		panel.background = '#fff';
		panel.padding = new Margin(0, 0, 0, 0);

		const panelBody = new Panel(Panel.Vertical);
		panel.padding = new Margin(0, 0, 0, 0);
		panel.margin = new Margin(0, 0, 0, 0);

		// Picture Icon
		const iconPicture = new Picture();
		iconPicture.desiredSize = new Size(50, 50);
		iconPicture.bind(new Binding('source', 'assetClass',
			(val: string) => this.getIconPath(val)));
		iconPicture.imageAlignment = Spot.Center;

		panelBody.add(iconPicture);

		const shape = new Shape();
		shape.figure = 'RoundedRectangle';
		shape.fill = 'transparent';
		shape.strokeWidth = 4;
		shape.desiredSize = new Size(55, 55);
		shape.bind(new Binding('stroke', 'id', (val: any) => {
			if (val === this.params.rootAsset || val === this.params.rootAsset) {
				return 'red';
			} else {
				return 'transparent';
			}
		}));
		panel.add(shape);
		panel.add(panelBody);
		node.add(panel);

		node.bind(new Binding('selectionAdorned', 'id', (val: any) => {
			return val !== this.params.rootAsset;
		}));

		if (opts.isExpandable) {
			node.isTreeExpanded = false;
			const expandButton = GraphObject.make('TreeExpanderButton');
			node.add(expandButton);
		}

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	private getIconPath(node: any): string {
		const name = (node.assetClass || '').toLowerCase();
		const type = (node.type || '').toLowerCase();
		let icon;
		if (name === 'device') {
			if (ArchitectureGraphDiagramHelper.isDeviceVirtualServer(type)) {
				icon = ASSET_ICONS['virtualServer'];
			} else if (ArchitectureGraphDiagramHelper.isDeviceServer(type)) {
				icon = ASSET_ICONS['server'];
			} else if (ArchitectureGraphDiagramHelper.isDeviceStorage(type)) {
				icon = ASSET_ICONS['physicalStorage'];
			} else if (ArchitectureGraphDiagramHelper.isDeviceNetwork(type)) {
				icon = ASSET_ICONS['network'];
			} else {
				icon = ASSET_ICONS['device'];
			}
		} else {
			icon = ASSET_ICONS[name && name.toLowerCase()];
		}
		return !!icon ? icon.icon : ASSET_ICONS.application.icon;
	}

	/**
	 * Determines is the current node belongs to the cyclical references
	 * @param from
	 * @param to
	 */
	isCyclicalReference(from: number, to: number): boolean {
		const found = this.refCycles.find((cycle: string) => {
			const currentNode = `${from}#${to}`;
			return cycle.indexOf(currentNode) !== -1;
		});
		return Boolean(found);
	}

	linkTemplate(): Link {
		const linkTemplate = new Link();
		linkTemplate.layerName = 'Background';

		const linkShape = new Shape();
		linkShape.strokeWidth = 2;

		linkShape.bind(new Binding('stroke', '',
			(val: any) => (this.isCyclicalReference(val.from, val.to)) ? this.cyclicalColor :  '#ddd'));

		linkShape.bind(new Binding('strokeDashArray', '',
			(val: any) => (this.isCyclicalReference(val.from, val.to)) ? [5, 10 ] :  null));

		const arrowHead = new Shape();
		arrowHead.toArrow = 'Standard';

		arrowHead.bind(new Binding('stroke', '',
			(val: any) => (this.isCyclicalReference(val.from, val.to)) ? this.cyclicalColor :  this.arrowColor));

		arrowHead.bind(new Binding('fill', '',
			(val: any) => (this.isCyclicalReference(val.from, val.to)) ? this.cyclicalColor :  this.arrowColor));

		linkTemplate.add(linkShape);
		linkTemplate.add(arrowHead);
		linkTemplate.selectionAdornmentTemplate = this.linkSelectionAdornmentTemplate();

		return linkTemplate;
	}

	/**
	 * Node Adornment template configuration
	 * @param {go.Node} node > optional node to add the adornment to
	 **/
	linkSelectionAdornmentTemplate(node?: go.Node): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Link);
		selAdornmentTemplate.selectionAdorned = true;
		if (node) { selAdornmentTemplate.adornedObject = node; }

		const linkShape = new go.Shape();
		linkShape.isPanelMain = true;
		linkShape.strokeWidth = 5;
		linkShape.stroke = 'red';
		const arrowHead = new Shape();
		arrowHead.strokeWidth = 4;
		arrowHead.stroke = '#af1102';
		arrowHead.toArrow = 'Standard';

		selAdornmentTemplate.add(linkShape);
		selAdornmentTemplate.add(arrowHead);
		return selAdornmentTemplate;
	}

	layout(): Layout {
		const diagraph = new LayeredDigraphLayout();
		diagraph.direction = 90;
		diagraph.layerSpacing = 100;
		return diagraph;
	}

	lowScaleNodeTemplate(): Node {
		const node = new Node(Panel.Horizontal);

		const  shape = new Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new Size(25, 35);
		shape.fill = '#ddd';
		node.add(shape);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => console.log('click');
		return node;
	}

	mediumScaleNodeTemplate(): Node {

		const node = new Node(Panel.Horizontal);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => console.log('click');
		return node;
	}

	contextMenuOptions(): ITdsContextMenuOption {
		return null;
	}

	icons(): IconModel {
		return null;
	}

	ctxMenuActionDispatched(action: string): void {
		// TODO
	}

	nodeClicked(): void {
		// TODO
	}

	animationFinished(): void {
		// TODO
	}
}
