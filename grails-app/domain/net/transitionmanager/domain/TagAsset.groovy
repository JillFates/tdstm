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
		domainClass enumType: "string"
	}

	Map toMap() {
		[
			id          : id,
			tagId       : tag.id,
			Name        : tag.name,
			Description : tag.description,
			Color       : tag.color.name(),
			css         : tag.color.css,
			DateCreated : dateCreated,
		]
	}
}
