<g:select id="${rackDomId}" name="${rackDomName}" tabindex="${tabindex}" 
	from="${options}" value="${rackId}" 
	optionKey="id" optionValue="value"
	noSelection="${[0:' Please Select...']}" 
	class="${clazz} assetSelect"  
	onchange="EntityCrud.updateOnRackSelection(this, '${sourceTarget}', '${forWhom}')"
/>
