<g:if
	test="${project.customFieldsShown != 0 && project.customFieldsShown < 25}">
	<tr class="prop">

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom1)?.length()>= 4 && (assetEntity.custom1)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom1}','help');"
					style="color: #00E">
					${assetEntity.project.custom1!=null ? assetEntity.project.custom1 : 'Custom1'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom1 ?:'Custom1'}
			</g:else></td>

		<td width="60">
			${assetEntity.custom1 }
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom2)?.length()>= 4 && (assetEntity.custom2)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom2}','help');"
					style="color: #00E">
					${assetEntity.project.custom2!=null ? assetEntity.project.custom2 : 'Custom2'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom2 ?:'Custom2'}
			</g:else></td>
		<td>
			${assetEntity.custom2}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom3)?.length()>= 4 && (assetEntity.custom3)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom3}','help');"
					style="color: #00E">
					${assetEntity.project.custom3!=null ? assetEntity.project.custom3 : 'Custom3'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom3 ?:'Custom3'}
			</g:else></td>
		<td>
			${assetEntity.custom3}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom4)?.length()>= 4 && (assetEntity.custom4)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom4}','help');"
					style="color: #00E">
					${assetEntity.project.custom4!=null ? assetEntity.project.custom4 : 'Custom4'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom4 ?:'Custom4'}
			</g:else></td>
		<td>
			${assetEntity.custom4}
		</td>

	</tr>
</g:if>
<g:if
	test="${project.customFieldsShown > 4 && project.customFieldsShown < 25}">
	<tr class="prop">
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom5)?.length()>= 4 && (assetEntity.custom5)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom5}','help');"
					style="color: #00E">
					${assetEntity.project.custom5!=null ? assetEntity.project.custom5 : 'Custom5'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom5 ?:'Custom5'}
			</g:else></td>
		<td>
			${assetEntity.custom5}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom6)?.length()>= 4 && (assetEntity.custom6)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom6}','help');"
					style="color: #00E">
					${assetEntity.project.custom6!=null ? assetEntity.project.custom6 : 'Custom6'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom6 ?:'Custom6'}
			</g:else></td>
		<td>
			${assetEntity.custom6}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom7)?.length()>= 4 && (assetEntity.custom7)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom7}','help');"
					style="color: #00E">
					${assetEntity.project.custom7!=null ? assetEntity.project.custom7 : 'Custom7'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom7 ?:'Custom7'}
			</g:else></td>
		<td>
			${assetEntity.custom7}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom8)?.length()>= 4 && (assetEntity.custom8)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom8}','help');"
					style="color: #00E">
					${assetEntity.project.custom8!=null ? assetEntity.project.custom8 : 'Custom8'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom8 ?:'Custom8'}
			</g:else></td>
		<td>
			${assetEntity.custom8}
		</td>
	</tr>
</g:if>
<g:if
	test="${project.customFieldsShown > 8 && project.customFieldsShown < 25}">
	<tr class="prop">

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom9)?.length()>= 4 && (assetEntity.custom9)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom9}','help');"
					style="color: #00E">
					${assetEntity.project.custom9!=null ? assetEntity.project.custom9 : 'Custom9'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom9 ?:'Custom9'}
			</g:else></td>

		<td width="60">
			${assetEntity.custom9 }
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom10)?.length()>= 4 && (assetEntity.custom10)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom10}','help');"
					style="color: #00E">
					${assetEntity.project.custom10!=null ? assetEntity.project.custom10 : 'Custom10'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom10 ?:'Custom10'}
			</g:else></td>
		<td>
			${assetEntity.custom10}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom11)?.length()>= 4 && (assetEntity.custom11)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom11}','help');"
					style="color: #00E">
					${assetEntity.project.custom11!=null ? assetEntity.project.custom11 : 'Custom11'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom11 ?:'Custom11'}
			</g:else></td>
		<td>
			${assetEntity.custom11}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom12)?.length()>= 12 && (assetEntity.custom12)?.substring(0, 12)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom12}','help');"
					style="color: #00E">
					${assetEntity.project.custom12!=null ? assetEntity.project.custom12 : 'Custom12'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom12 ?:'Custom12'}
			</g:else></td>
		<td>
			${assetEntity.custom12}
		</td>

	</tr>
</g:if>
<g:if
	test="${project.customFieldsShown > 12 && project.customFieldsShown < 25}">
	<tr class="prop">
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom13)?.length()>= 4 && (assetEntity.custom13)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom13}','help');"
					style="color: #00E">
					${assetEntity.project.custom13!=null ? assetEntity.project.custom13 : 'Custom13'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom13 ?:'Custom13'}
			</g:else></td>
		<td>
			${assetEntity.custom13}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom14)?.length()>= 4 && (assetEntity.custom14)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom14}','help');"
					style="color: #00E">
					${assetEntity.project.custom14!=null ? assetEntity.project.custom14 : 'Custom14'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom14 ?:'Custom14'}
			</g:else></td>
		<td>
			${assetEntity.custom14}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom15)?.length()>= 4 && (assetEntity.custom15)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom15}','help');"
					style="color: #00E">
					${assetEntity.project.custom15!=null ? assetEntity.project.custom15 : 'Custom15'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom15 ?:'Custom15'}
			</g:else></td>
		<td>
			${assetEntity.custom15}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom16)?.length()>= 4 && (assetEntity.custom16)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom16}','help');"
					style="color: #00E">
					${assetEntity.project.custom16!=null ? assetEntity.project.custom16 : 'Custom16'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom16 ?:'Custom16'}
			</g:else></td>
		<td>
			${assetEntity.custom16}
		</td>
	</tr>
</g:if>
<g:if
	test="${project.customFieldsShown > 16 && project.customFieldsShown < 25}">
	<tr class="prop">

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom17)?.length()>= 4 && (assetEntity.custom17)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom17}','help');"
					style="color: #00E">
					${assetEntity.project.custom17!=null ? assetEntity.project.custom17 : 'Custom17'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom17 ?:'Custom17'}
			</g:else></td>

		<td width="60">
			${assetEntity.custom17 }
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom18)?.length()>= 4 && (assetEntity.custom18)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom18}','help');"
					style="color: #00E">
					${assetEntity.project.custom18!=null ? assetEntity.project.custom18 : 'Custom18'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom18 ?:'Custom18'}
			</g:else></td>
		<td>
			${assetEntity.custom18}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom19)?.length()>= 4 && (assetEntity.custom19)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom19}','help');"
					style="color: #00E">
					${assetEntity.project.custom19!=null ? assetEntity.project.custom19 : 'Custom19'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom19 ?:'Custom19'}
			</g:else></td>
		<td>
			${assetEntity.custom19}
		</td>

		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom20)?.length()>= 20 && (assetEntity.custom20)?.substring(0, 20)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom20}','help');"
					style="color: #00E">
					${assetEntity.project.custom20!=null ? assetEntity.project.custom20 : 'Custom20'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom20 ?:'Custom20'}
			</g:else></td>
		<td>
			${assetEntity.custom20}
		</td>

	</tr>
</g:if>
<g:if
	test="${project.customFieldsShown > 20 && project.customFieldsShown < 25}">
	<tr class="prop">
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom21)?.length()>= 4 && (assetEntity.custom21)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom21}','help');"
					style="color: #00E">
					${assetEntity.project.custom21!=null ? assetEntity.project.custom21 : 'Custom21'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom21 ?:'Custom21'}
			</g:else></td>
		<td>
			${assetEntity.custom21}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom22)?.length()>= 4 && (assetEntity.custom22)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom22}','help');"
					style="color: #00E">
					${assetEntity.project.custom22!=null ? assetEntity.project.custom22 : 'Custom22'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom22 ?:'Custom22'}
			</g:else></td>
		<td>
			${assetEntity.custom22}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom23)?.length()>= 4 && (assetEntity.custom23)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom23}','help');"
					style="color: #00E">
					${assetEntity.project.custom23!=null ? assetEntity.project.custom23 : 'Custom23'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom23 ?:'Custom23'}
			</g:else></td>
		<td>
			${assetEntity.custom23}
		</td>
		<td class="label" nowrap="nowrap"><g:if
				test="${(assetEntity.custom24)?.length()>= 4 && (assetEntity.custom24)?.substring(0, 4)=='http'}">
				<a href="javascript:window.open('${assetEntity.custom24}','help');"
					style="color: #00E">
					${assetEntity.project.custom24!=null ? assetEntity.project.custom24 : 'Custom24'}
				</a>
			</g:if> <g:else>
				${assetEntity.project.custom24 ?:'Custom24'}
			</g:else></td>
		<td>
			${assetEntity.custom24}
		</td>
	</tr>
</g:if>