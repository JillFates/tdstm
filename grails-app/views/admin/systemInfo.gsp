<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>System Info</title>
	<style type="text/css">
		th {
			text-align: center
		}
	</style>
</head>
<body style="font-family:'helvetica','arial';">
<tds:subHeader title="System Info" crumbs="['Admin','System Info']"/>
<div class="body">
<div>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<h3>Memory Usage (Kb):</h3>
	<br>
	<pre style="width: 280px;">
       ---- System ----
 Physical Memory: ${String.format("%,10d", sysMemSize)}
     Used Memory: ${String.format("%,10d", sysMemSize - sysMemFree)}
     Free Memory: ${String.format("%,10d", sysMemFree)}
  Virtual Memory: ${String.format("%,10d", virtMemCommit)}
     Swap Memory: ${String.format("%,10d", swapSize)}
       Swap Used: ${String.format("%,10d", swapSize - swapFree)}
       Swap Free: ${String.format("%,10d", swapFree)}

       ---- Heap ----
             Max: ${String.format("%,10d", heapMax)}
       Committed: ${String.format("%,10d", heapCommitted)}
            Used: ${String.format("%,10d", heapUsed)}
     	    Free: ${String.format("%,10d", freeMemory)}

       ---- Non-Heap ----
             Max: ${String.format("%,10d", nonHeapMax)}
       Committed: ${String.format("%,10d", nonHeapCommitted)}
            Used: ${String.format("%,10d", nonHeapUsed)}
            Free: ${String.format("%,10d", nonHeapMax - nonHeapUsed)}
	</pre>

	<h3>Memory Pools:</h3>
	<table style="border-spacing: 5px; border-collapse: separate; border: 1px solid black;table-layout:fixed;width:auto">
		<tr>
			<th>Name</th>
			<th>Type</th>
			<th colspan="4">Usage (Mb)</th>
			<th colspan="4">Peak Usage (Mb)</th>
			<th colspan="4">Collection Usage (Mb)</th>
		</tr>
		<tr>
			<th></th>
			<th></th>
			<th>Init</th>
			<th>Used</th>
			<th>Commited</th>
			<th>Max</th>
			<th>Init</th>
			<th>Used</th>
			<th>Commited</th>
			<th>Max</th>
			<th>Init</th>
			<th>Used</th>
			<th>Commited</th>
			<th>Max</th>
		</tr>
		<g:each in="${java.lang.management.ManagementFactory.getMemoryPoolMXBeans()}" var="item">
		<tr>
			<td>${item.name}</td>
			<td>${item.type}</td>
			<td style="text-align: right">
				<g:if test="${item.usage}">${String.format("%,.2f",item.usage?.init / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.usage}">${String.format("%,.2f",item.usage?.used / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.usage}">${String.format("%,.2f",item.usage?.committed / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.usage}">${String.format("%,.2f",item.usage?.max / 1048576)}</g:if>
			</td>

			<td style="text-align: right">
				<g:if test="${item.peakUsage}">${String.format("%,.2f",item.peakUsage?.init / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.peakUsage}">${String.format("%,.2f",item.peakUsage?.used / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.peakUsage}">${String.format("%,.2f",item.peakUsage?.committed / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.peakUsage}">${String.format("%,.2f",item.peakUsage?.max / 1048576)}</g:if>
			</td>

			<td style="text-align: right">
				<g:if test="${item.collectionUsage}">${String.format("%,.2f",item.collectionUsage?.init / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.collectionUsage}">${String.format("%,.2f",item.collectionUsage?.used / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.collectionUsage}">${String.format("%,.2f",item.collectionUsage?.committed / 1048576)}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${item.collectionUsage}">${String.format("%,.2f",item.collectionUsage?.max / 1048576)}</g:if>
			</td>
		</tr>
		</g:each>
	</table>
	<br>
	<br>

	<h3>Garbage Collection:</h3>
	<table style="width: 40%">
		<tr>
			<th>Name</th>
			<th>Count</th>
			<th>Time (msec)</th>
			<th>Avg. (Time/Count)</th>
		</tr>
		<g:each in="${java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()}" var="gcitem">
		<tr>
			<td>${gcitem.name}</td>
			<td style="text-align: right">
				<g:if test="${gcitem.collectionCount}">${gcitem.collectionCount}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${gcitem.collectionTime}">${gcitem.collectionTime}</g:if>
			</td>
			<td style="text-align: right">
				<g:if test="${gcitem.collectionCount && gcitem.collectionTime}">${String.format("%,.2f",gcitem.collectionTime / gcitem.collectionCount)}</g:if>
			</td>
		</tr>
		</g:each>
	</table>

	<br>
	<b>Note:</b> <i>PS Scavenge is used on the young (eden, survivor) generation and PS MarkSweep is used on the old generation</i>
	<br>

	<h3>System Information:</h3>
	<table>
	<tr><td align=right>OS: </td><td>${osMxBean.getName()} (${osMxBean.getArch()})</td></tr>
	<tr><td align=right># of CPUs: </td><td>${rt.availableProcessors()}</td></tr>
	<tr><td align=right>Load Avg: </td><td>${String.format("%3.2f", osMxBean.getSystemLoadAverage() )}</td></tr>
	<tr><td align=right>VM Vendor: </td><td>${rtMXBean.getVmVendor()}</td></tr>
	<tr><td align=right>VM Name: </td><td>${rtMXBean.getVmName()}</td></tr>
	<tr><td align=right>VM Version: </td><td>${sysProps['java.runtime.version']}</td></tr>
	<tr><td align=right>Groovy Version: </td><td>${groovyVersion}</td></tr>


	<tr><td align=right valign=top>
		Input Args:
		</td>
		<td>
			<ul>
				<g:each in="${rtMXBean.getInputArguments()}" var="arg">
				<li>${arg}</li>
				</g:each>
			</ul>
		</td>
	</tr>

	<tr>
		<td align=right valign=top>System&nbsp;Properties: </td>
		<td>
			<ul>
				<g:each in="${sysProps}" var="prop">
				<li>${prop}</li>
				</g:each>
			</ul>
		</td>
	</tr>

	</table>
	<br>
	<br>

</div>
</div>
</body>

</html>
