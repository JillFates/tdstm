<%@page expressionCodec="none" %>
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
	         	<td class="col_${model.id}">${columnList.get(column) ? model.(columnList.get(column)).name.join(',') : ''} </td>
	         </g:if>
	         
	         <g:elseif test="${column =='Front Image' && model.(columnList.get(column))}">
	         	<td class="col_${model.id}"><img src="${createLink(controller:'model', action:'retrieveFrontImage', id:model.id)}" style="height: 50px;width: 100px;"/></td>
	         </g:elseif>
	         
	         <g:elseif test="${column =='Rear Image' && model.(columnList.get(column))}">
	         	<td class="col_${model.id}"><img src="${createLink(controller:'model', action:'retrieveRearImage', id:model.id)}" style="height: 50px;width: 100px;"/></td>
	         </g:elseif>
	         
	         <g:elseif test="${i==0}">
	         	<td class="col_${model.id}">${columnList.get(column) ? ('<b>'+model.(columnList.get(column))+'</b>'  + '&nbsp;&nbsp;<a href="javascript:removeCol('+model.id+')"><span class="clear_filter"><u>X</u></span></a>'): ''}</td>
	         </g:elseif>
	         
	         <g:elseif test="${column =='Room Object'|| column =='Use Image' || column=='Source TDS'}">
	         	<td id="${columnList.get(column)+'_td_'+model.id}" class="col_${model.id}"><span  class="showAll showFrom_${model.id} ">
	         		<input type="checkbox" ${model.(columnList.get(column))==1 || model.(columnList.get(column))== true ? 'checked="checked"' : ''} disabled="disabled" ></span>
	         		<span class="editAll editTarget_${model.id}" style="display: none;">
	         			<input id="${columnList.get(column)+'_edit_'+model.id}"  type="checkbox" class="input_${model.id}" name="${columnList.get(column)}" ${model.(columnList.get(column))==1 || model.(columnList.get(column))== true ? 'checked="checked" value="0" ' : 'value="1"'} 
	         				onclick="if(this.checked){this.value = 1} else {this.value = 0 }">
	         		</span>
	         	</td>
	         </g:elseif>
	         
	         <g:elseif test="${column =='Power(Max/Design/Avg)'}">
	         	<td id="${columnList.get(column)+'_td_'+model.id}" class="col_${model.id}"><span class="showAll showFrom_${model.id} check">${model.powerNameplate +'/'+ model.powerDesign +'/'+ model.powerUse}</span>
		         	 <span class="editAll editTarget_${model.id}" style="display: none;">
			         	 <input type="text" id="${'powerNameplate_edit_'+model.id}" name="powerNameplate" class="input_${model.id}"  value="${model.powerNameplate?:''}" size="4">
			         	 <input type="text" id="${'powerDesign_edit_'+model.id}" name="powerDesign" class="input_${model.id}"  value="${model.powerDesign?:''}" size="4">
			         	 <input type="text" id="${'powerUse_edit_'+model.id}" name="powerUse" class="input_${model.id}"  value="${model.powerUse?:''}" size="4">
			         </span>
		        </td>
	         </g:elseif>
	         
	         <g:elseif test="${column =='Dimensions(inches)'}">
	         	<td id="height_td_${model.id}" class="col_${model.id}"><span class="showAll showFrom_${model.id} check">${model.height +'/'+ model.width +'/'+ model.depth}</span>
		         	 <span class="editAll editTarget_${model.id}" style="display: none;">
			         	 <input type="text" name="height" id="${'height_edit_'+model.id}" class="input_${model.id}"  value="${model.height?:''}" size="4">
			         	 <input type="text" name="width" id="${'width_edit_'+model.id}" class="input_${model.id}"  value="${model.width?:''}" size="4">
			         	 <input type="text" name="depth" id="${'depth_edit_'+model.id}" class="input_${model.id}"  value="${model.depth?:''}" size="4">
			         </span>
			    </td>
	         </g:elseif> 
	         
	         <g:elseif test="${column =='End Of Life Date'}">
	         	<td id="${columnList.get(column)+'_td_'+model.id}" class="col_${model.id}" nowrap="nowrap">
	         	 	<span class="showAll showFrom_${model.id} check"><tds:convertDate date="${model.(columnList.get(column))}" /></span>
	         		<span class="editAll editTarget_${model.id}" style="display: none;"><script type="text/javascript" charset="utf-8">
	                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					</script>
					<input type="text" class="dateRange input_${model.id}" size="15" style="width:112px;height:14px;" name="endOfLifeDate" id="${columnList.get(column)+'_edit_'+model.id}"
	                   value="<tds:convertDate date="${model?.endOfLifeDate}" />" />
		         	</span>	
	         	</td>
	         </g:elseif>
	         
	         <g:elseif test="${column =='Merge To'}">
	         	<td id="${columnList.get(column)+'_td_'+model.id}" class="col_${model.id}">
	         	<input type="radio" class="merge" name="mergeRadio" id="merge_${model.id}" onclick="switchTarget(${model.id})" />
	         	<g:if test="${j==0}">
		         	<script type="text/javascript" charset="utf-8">
						$("#merge_${model.id}").attr('checked', 'checked')
						switchTarget(${model.id})
					</script>
				</g:if>
	         	</td>
	         </g:elseif>
	         
	         <g:else>
	         	<td id="${columnList.get(column)+'_td_'+model.id}" class="col_${model.id}">
	         		<g:if test="${!['createdBy', 'updatedBy', 'validatedBy', 'assetType', 'manufacturer','frontImage', 'rearImage'].contains(columnList.get(column)) }">
			         	 <span class="showAll showFrom_${model.id} check">${columnList.get(column) ? model.(columnList.get(column)) : ''}</span>
			         	 <span class="editAll editTarget_${model.id}" style="display: none;">
	         	 			 <input type="text" id="${columnList.get(column)+'_edit_'+model.id}" name="${columnList.get(column)}" class="input_${model.id}" value="${columnList.get(column) ? model.(columnList.get(column)) : ''}"/>
	         	 		 </span>
	         	 	</g:if>
	         	 	<g:else>
	         	 	   <span >${columnList.get(column) ? model.(columnList.get(column)) : ''}</span>
	         	 	</g:else>
	         	</td>
	         </g:else>
	         
	       </g:each>
	     </tr>
     </g:each>
   </table>
    <div class="buttons">
    	<input type="button"  class="delete" value="Cancel" id="processData" onclick="jQuery('#showOrMergeId').dialog('close')"/>
		<input type="button" id="mergeModelId" class="save" value="Merge" onclick="mergeModel()"/> 
	 </div>
  </div>
</body>
</html>
