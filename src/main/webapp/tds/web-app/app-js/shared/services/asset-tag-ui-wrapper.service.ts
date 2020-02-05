import {Injectable} from '@angular/core';

@Injectable()
export class AssetTagUIWrapperService {

	/**
	 * Receive a selector that wrap up the tags for calculating
	 * the width of the container on also calculates how many
	 * tag can fit visible inside of it, if one or more tags are not correctly
	 * displayed, then it hides the tags and show '...' meaning that there are more
	 * tags on the same cell.
	 * */
	updateTagsWidth(selector, toCleanSelector): void {
		let allTagsDivs = document.querySelectorAll(selector);
		document.querySelectorAll(toCleanSelector).forEach( node => {
			node.parentNode.removeChild(node);
		});
		allTagsDivs.forEach( el => {
			let castedEl = (<HTMLElement>el);
			let tags = castedEl.querySelectorAll('span');
			let allTagsWidth = 0;
			let bestFit = 0;
			if (tags.length > 1 ) {
				tags.forEach( tag => {
					allTagsWidth += tag.offsetWidth;
				});
				if (allTagsWidth > castedEl.offsetWidth) {
					bestFit = this.testBestFit(castedEl.offsetWidth, tags);
					let span = document.createElement('span');
					span.innerHTML = '...';
					span.className = 'dots-for-tags';
					castedEl.insertBefore( span, castedEl.children[bestFit]);
				}
			}
		});
	}

	/**
	 * Calculates how many tags fit in a given width
	 * @returns max number of tags that fit
	 * */
	testBestFit(maxWidth, tags): number {
		let tagWidth = (tags.length * 5);
		let retVal = 0;
		let arr = Array.from(tags);
		for (let tag of arr) {
			tagWidth += (<HTMLElement>tag).offsetWidth;
			if ( tagWidth < maxWidth ) {
				retVal += 1;
			} else {
				return retVal;
			}
		}
		return retVal;
	}

	/**
	 * This functions helps add a span with 3 dots (...) to a set of tags, it calculates the size of how many tags
	 * can fit inside a given container width.
	 * @param {string} selector the parent container of the tags, from which we will get the width
	 * @param {string} toCleanSelector selector that contains the 3 dots span wich will be removed every time the calculation is made
	 * @param {string} parentSelector in this case, used for
	 **/
	updateTagsWidthForAssetShowView(selector, toCleanSelector, parentSelector): void {
		let allTagsDivs = document.querySelectorAll(selector);
		document.querySelectorAll(toCleanSelector).forEach( node => {
			node.parentNode.removeChild(node);
		});
		const maxSizeWidth = (<HTMLElement>document.querySelector(parentSelector)).offsetWidth;
		allTagsDivs.forEach( el => {
			let castedEl = (<HTMLElement>el);
			let tags = castedEl.querySelectorAll('div .label.tag');
			let allTagsWidth = 0;
			let bestFit = 0;
			if (tags.length > 1) {
				tags.forEach((tag: HTMLElement) => {
					allTagsWidth += tag.offsetWidth;
				});
				if (allTagsWidth > maxSizeWidth) {
					bestFit = this.testBestFit(maxSizeWidth, tags);
					let span = document.createElement('span');
					span.innerHTML = '...';
					span.className = 'dots-for-tags';
					castedEl.insertBefore(span, castedEl.children[bestFit]);
				}
			}
		});
		};
}
