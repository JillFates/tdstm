<g:select id = "manufacturers" name="manufacturer.id" from="${manufacturers}" optionKey="id" optionValue="name" noSelection="${[null:' Unassigned']}" 
value="${selectedManu}" onChange="selectModel(this.value)" />