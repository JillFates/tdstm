<%-- 
   Used to render the SELECT for source/target Chassis 
   @param domId - DOM id to set the control to
   @param domName - the form control name
   @param domClass - class name(s) to assign to the control
   @param sourceTarget - S)ource or T)arget
   @param value - the value of the currently selected option
   @param options - the list of mapped options [id:key, value:option]
   @param forWhom - Edit or Create (think we can eventually get rid of that puppy)
--%>
<g:select id="${domId}" name="${domName}" 
	from="${options}" value="${value}" 
	optionKey="id" optionValue="value"
	noSelection="${[0:'Please Select...']}" 
	class="${domClass} assetSelect useChassis${sourceTarget}"  
	onchange="EntityCrud.updateOnChassisSelection(this, '${sourceTarget}', '${forWhom}')"
/>
