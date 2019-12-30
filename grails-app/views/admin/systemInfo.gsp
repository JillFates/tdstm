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

	<h3>FQDN: </h3>${fqdn}

	<h3>Memory Usage (Kb):</h3>
	<br>
	<pre style="width: 380px;">
       ---- System ----
 Physical Memory: ${sysMemSize}
     Used Memory: ${sysMemUsed}
     Free Memory: ${sysMemFree}
  Virtual Memory: ${virtMemCommit}
     Swap Memory: ${swapSize}
       Swap Used: ${swapUsed}
       Swap Free: ${swapFree}

       ---- Heap ----
             Max: ${heapMax}
       Committed: ${heapCommitted}
            Used: ${heapUsed}
     	    Free: ${freeMemory}

       ---- Non-Heap ----
             Max: ${nonHeapMax}
       Committed: ${nonHeapCommitted}
            Used: ${nonHeapUsed}
            Free: ${nonHeapFree}
	</pre>

	<h3>Memory Pools:</h3>
	<table style="border-spacing: 5px; border-collapse: separate; border: 1px solid black;table-layout:fixed;width:auto">
		<tr>
			<th style="text-align: left">Name</th>
			<th style="text-align: left">Type</th>
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
			<th style="text-align: left">Name</th>
			<th style="text-align: right">Count</th>
			<th style="text-align: right">Time (msec)</th>
			<th style="text-align: right">Avg. (Time/Count)</th>
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

	<h3>File System:</h3>
	<table style="width: 40%">
		<tr>
			<th style="text-align: left">Name</th>
			<th style="text-align: right">Free Space</th>
			<th style="text-align: right">Total Space</th>
		</tr>
		<g:each in="${fileSystems}" var="fileSystem">
		<tr>
			<td>${fileSystem.name}</td>
			<td style="text-align: right">
				${fileSystem.freeSpace}
			</td>
			<td style="text-align: right">
				${fileSystem.totalSpace}
			</td>
		</tr>
		</g:each>
	</table>

	<br>
	<b>Note:</b> <i>PS Scavenge is used on the young (eden, survivor) generation and PS MarkSweep is used on the old generation</i>
	<br>

	<h3>System Information:</h3>
	<table>
	<tr><td align=right>OS: </td><td>${osName} (${osVersion} ${osArchitecture})</td></tr>
	<tr><td align=right># of CPUs: </td><td>${availableProcessors}</td></tr>
	<tr><td align=right>Load Avg: </td><td>${systemLoadAverage}</td></tr>
	<tr><td align=right>VM Vendor: </td><td>${javaVendor}</td></tr>
	<tr><td align=right>VM Name: </td><td>${vmName}</td></tr>
	<tr><td align=right>VM Version: </td><td>${javaVersion}</td></tr>
	<tr><td align=right>Groovy Version: </td><td>${groovyVersion}</td></tr>
	<tr><td align=right>Grails Version: </td><td>${grailsVersion}</td></tr>
	<tr><td align=right>MySQL version: </td><td>${mysqlName} ${myslqVersion}</td></tr>
	<tr><td align=right>MySQL Innodb Version: </td><td>${mysqlInnodbVersion}</td></tr>
	<tr><td align=right>MySQL TLS Version: </td><td>${mySQlTlsVersion}</td></tr>
	<tr><td align=right>Application Version: </td><td>${appVersion}</td></tr>
	<tr><td align=right>Application Up Time: </td><td>${upTimeApplication}</td></tr>


	<tr><td align=right valign=top>
		Input Args:
		</td>
		<td>
			<ul>
				<g:each in="${inputArguments}" var="arg">
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

	<h3>Top 10 CPU-Consuming Processes:</h3>
	<br>
	<pre style="width: 100%;">
${cpuProcesses}
	</pre>

	<h3>Up Time Load:</h3>
	<br>
	<pre style="width: 100%;">
${machineUptime}
	</pre>

	<h3>SELinux Status:</h3>
	<br>
	<pre style="width: 100%;">
${seLinuxStatus}
	</pre>

</div>
</div>
</body>

</html>
