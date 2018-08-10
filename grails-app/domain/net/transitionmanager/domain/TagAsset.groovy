package net.transitionmanager.domain

import com.tds.asset.AssetEntity

class TagAsset {
	Date      dateCreated

	static belongsTo = [tag: Tag, asset: AssetEntity]

	static constraints = {
		tag unique: ['asset']
	}

	static mapping = {
		version false
		id column: 'tag_asset_id'
		tag lazy: false
		asset lazy: false
		domainClass enumType: "string"
	}

	Map toMap() {
		[
			id          : id,
			tagId       : tag.id,
			name        : tag.name,
			description : tag.description,
			color       : tag.color.name(),
			css         : tag.color.css,
			dateCreated : dateCreated,
		]
	}
}
