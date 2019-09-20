import {Injectable} from '@angular/core';

@Injectable()
export class TagWrapperService {

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
}
