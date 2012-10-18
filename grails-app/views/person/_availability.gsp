
<html>
<head>
	<script type="text/javascript">
	$(document).ready(
		        Calendar.setup({
		          dateField     : 'date',
		          parentElement : 'calendar'
		        })
	)
	</script>
</head>


<div class="menu4">
	<ul>
		<li><a href="#" class="mobmenu " onclick="loadPersonDiv(${person.id},'generalInfo')">General</a></li>
		<li><a href="#" class="mobmenu mobselect" onclick="loadPersonDiv(${person.id},'availability')">Availability</a></li>
		<li><a href="#" class="mobmenu" onclick="loadPersonDiv(${person.id},'tdsUtility')">TDS</a></li>
	</ul>
</div>

<div>

	<div class="calendar popup">
		<table style="width:200px;margin-left: 60px">
			<thead>
				<tr>
					<td class="title" colspan="7">March 2007</td>
				</tr>
				<tr>
					<td class="button">«</td>
					<td class="button">‹</td>
					<td class="button" colspan="3">Today</td>
					<td class="button">›</td>
					<td class="button">»</td>
				</tr>
				<tr>
					<th class="weekend">S</th>
					<th>M</th>
					<th>T</th>
					<th>W</th>
					<th>T</th>
					<th>F</th>
					<th class="weekend">S</th>
				</tr>
			</thead>
			<tbody>
				<tr class="days">
					<td class="otherDay weekend">25</td>
					<td class="otherDay">26</td>
					<td class="otherDay">27</td>
					<td class="otherDay">28</td>
					<td>1</td>
					<td>2</td>
					<td class=" weekend">3</td>
				</tr>
				<tr class="days">
					<td class="weekend">4</td>
					<td>5</td>
					<td class="selected">6</td>
					<td>7</td>
					<td>8</td>
					<td>9</td>
					<td class="weekend">10</td>
				</tr>
				<tr class="days">
					<td class="weekend">11</td>
					<td class="today">12</td>
					<td>13</td>
					<td>14</td>
					<td>15</td>
					<td>16</td>
					<td class="weekend">17</td>
				</tr>
				<tr class="days">
					<td class="weekend">18</td>
					<td>19</td>
					<td>20</td>
					<td>21</td>
					<td>22</td>
					<td>23</td>
					<td class="weekend">24</td>
				</tr>
				<tr class="days">
					<td class="weekend">25</td>
					<td>26</td>
					<td>27</td>
					<td>28</td>
					<td>29</td>
					<td>30</td>
					<td class="weekend">31</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>
</html>