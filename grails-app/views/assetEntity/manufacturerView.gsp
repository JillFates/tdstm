<g:select id = "manufacturers" name="manufacturer.id" class="assetSelect" from="${manufacturers}" optionKey="id" optionValue="name" noSelection="${[null:' Unassigned']}" 
value="${selectedManu}" onChange="selectModel(this.value,'${forWhom}')" />
