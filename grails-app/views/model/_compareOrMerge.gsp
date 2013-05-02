<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
</head>
<body>
  <div class="body">
   <table>
     <g:each in="${columnList.keySet()}" var="column" status="i">
	     <tr class="${ i== 0 ? 'headClass' : (i % 2) == 0 ? 'odd' : 'even'}">
	       <td > <b>${column} </b></td>
	       <g:each in="${models}" var="model" status="j">
	         <g:if test="${column =='AKA'}">
	         	<td class="col_${model.id}">${columnList.get(column) ? model.(columnList.get(column)).name.join(',') : ''}</td>
	         </g:if>
	         <g:elseif test="${column =='Front Image' && model.(columnList.get(column))}">
	         	<td class="col_${model.id}"><img src="${createLink(controller:'model', action:'getFrontImage', id:model.id)}" style="height: 50px;width: 100px;"/></td>
	         </g:elseif>
	         <g:elseif test="${column =='Rear Image' && model.(columnList.get(column))}">
	         	<td class="col_${model.id}"><img src="${createLink(controller:'model', action:'getRearImage', id:model.id)}" style="height: 50px;width: 100px;"/></td>
	         </g:elseif>
	         <g:elseif test="${i==0}">
	         	<td class="col_${model.id}">${columnList.get(column) ? ('<b>'+model.(columnList.get(column))+'</b>'  + '&nbsp;&nbsp;<a href="javascript:removeCol('+model.id+')"><span class="new_clear_filter"><u>X</u></span></a>'): ''}</td>
	         </g:elseif>
	         <g:elseif test="${column =='Room Object'|| column =='Use Image' || column=='Source TDS'}">
	         	<td class="col_${model.id}"><input type="checkbox" ${model.(columnList.get(column))==1 || model.(columnList.get(column))== true ? 'checked="checked"' : ''} disabled="disabled" ></td>
	         </g:elseif>
	         <g:elseif test="${column =='Power(Max/Design/Avg)'}">
	         	<td class="col_${model.id}">${model.powerNameplate +'/'+ model.powerDesign +'/'+ model.powerUse}</td>
	         </g:elseif>
	         <g:elseif test="${column =='Dimensions(inches)'}">
	         	<td class="col_${model.id}">${model.height +'/'+ model.width +'/'+ model.depth}</td>
	         </g:elseif> 
	         <g:elseif test="${column =='End Of Life Date'}">
	         	<td class="col_${model.id}"><tds:convertDate date="${model.(columnList.get(column))}" 
	         		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
	         </g:elseif>
	         <g:elseif test="${column =='Merge To'}">
	         	<td class="col_${model.id}"><input type="radio" class="merge" name="mergeRadio" id="merge_${model.id}" ${j==0 ? 'checked' : '' }/></td>
	         </g:elseif>
	         <g:else>
	         	<td class="col_${model.id}">${columnList.get(column) ? model.(columnList.get(column)) : ''}</td>
	         </g:else>
	       </g:each>
	     </tr>
     </g:each>
   </table>
    <div class="buttons">
    	<input type="button"  class="save" value="Cancel" id="processData" onclick="jQuery('#showOrMergeId').dialog('close')"/>
		<input type="button" id="mergeModelId" class="save" value="Merge" onclick="meegeModel()"/> 
	 </div>
  </div>
</body>
</html>