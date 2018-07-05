import {HeaderComponent} from '../../shared/modules/header/header.component';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {TagListComponent} from './components/tag-list/tag-list.component';

const TOP_MENU_PARENT_SECTION = 'menu-parent-projects';

export class AssetTagsRoutingStatesStates {
	public static readonly TAG_LIST = {
		name: 'tds.tag_list',
		url: '/tag/list'
	};
}

export const TagList: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetTagsRoutingStatesStates.TAG_LIST.name,
	url: AssetTagsRoutingStatesStates.TAG_LIST.url,
	data: {
		page: {
			title: 'ASSET_TAGS.MANAGE_TAGS',
			instruction: '',
			menu: ['GLOBAL.PROJECTS', 'ASSET_TAGS.MANAGE_TAGS'],
			topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-project-tags'}
		},
		requiresAuth: true
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: TagListComponent}
	}
};

export const ASSET_TAGS_STATES = [
	TagList,
];