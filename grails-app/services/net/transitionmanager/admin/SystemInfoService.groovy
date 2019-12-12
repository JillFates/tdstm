package net.transitionmanager.admin

import com.tdssrc.grails.NumberUtil
import grails.util.Metadata
import groovy.sql.GroovyResultSet
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.io.FileUtils

import javax.sql.DataSource
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage
import java.lang.management.OperatingSystemMXBean
import java.lang.management.RuntimeMXBean
import java.nio.file.FileSystems

/**
 * A service for gathering system info for debugging/troubleshooting.
 */
class SystemInfoService implements ServiceMethods {
	DataSource dataSource

	/**
	 * Gathers various system info about memory, filesystem, cpu, and creates a map that it renders to the getInfo.gsp
	 *
	 * @return a map of system info
	 */
	Map getInfo() {

		Map MySQLData = getMySqlData()
		Metadata metadata = grails.util.Metadata.current
		Runtime rt = Runtime.getRuntime()

		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean()
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean()
		OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean()
		RuntimeMXBean rtMXBean = ManagementFactory.getRuntimeMXBean()
		MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage()
		MemoryUsage memNonHeap = memoryMXBean.getNonHeapMemoryUsage()

		Map sysProps = rtMXBean.getSystemProperties()

		long freeMemory = NumberUtil.toLong(rt.freeMemory())
		long totalMemory = NumberUtil.toLong(rt.totalMemory())

		[
			fqdn               : InetAddress.getLocalHost().getCanonicalHostName(),
			freeMemory         : FileUtils.byteCountToDisplaySize(freeMemory),
			totalMemory        : FileUtils.byteCountToDisplaySize(totalMemory),
			maxMemory          : FileUtils.byteCountToDisplaySize(rt.maxMemory()),
			usedMemory         : FileUtils.byteCountToDisplaySize(totalMemory - freeMemory),
			osName             : metadata['os.name'],
			osVersion          : metadata['os.version'],
			osArchitecture     : metadata['os.arch'],

			inputArguments     : rtMXBean.getInputArguments(),
			heapUsed           : FileUtils.byteCountToDisplaySize(memHeap.getUsed()),
			heapCommitted      : FileUtils.byteCountToDisplaySize(memHeap.getCommitted()),
			heapMax            : FileUtils.byteCountToDisplaySize(memHeap.getMax()),
			nonHeapUsed        : FileUtils.byteCountToDisplaySize(memNonHeap.getUsed()),
			nonHeapCommitted   : FileUtils.byteCountToDisplaySize(memNonHeap.getCommitted()),
			nonHeapMax         : FileUtils.byteCountToDisplaySize(memNonHeap.getMax()),
			nonHeapFree        : FileUtils.byteCountToDisplaySize(memNonHeap.getMax() - memNonHeap.getUsed()),

			sysMemSize         : FileUtils.byteCountToDisplaySize(osMxBean.getTotalPhysicalMemorySize()),
			sysMemFree         : FileUtils.byteCountToDisplaySize(osMxBean.getFreePhysicalMemorySize()),
			sysMemUsed         : FileUtils.byteCountToDisplaySize(osMxBean.getTotalPhysicalMemorySize() - osMxBean.getTotalPhysicalMemorySize()),
			swapSize           : FileUtils.byteCountToDisplaySize(osMxBean.getTotalSwapSpaceSize()),
			swapFree           : FileUtils.byteCountToDisplaySize(osMxBean.getFreeSwapSpaceSize()),
			swapUsed           : FileUtils.byteCountToDisplaySize(osMxBean.getTotalSwapSpaceSize() - osMxBean.getFreeSwapSpaceSize()),
			virtMemCommit      : FileUtils.byteCountToDisplaySize(osMxBean.getCommittedVirtualMemorySize()),
			sysProps           : sysProps,
			systemLoadAverage  : String.format("%3.2f", osMxBean.getSystemLoadAverage()),
			availableProcessors: rt.availableProcessors(),
			groovyVersion      : GroovySystem.getVersion(),
			javaVersion        : metadata['java.runtime.version'],
			javaVendor         : metadata['java.vm.vendor'],
			vmName             : metadata['java.vm.name'],
			grailsVersion      : metadata['info.app.grailsVersion'],
			appVersion         : metadata['info.app.version'],
			upTimeApplication  : "${NumberUtil.toLong(rb.getUptime() / 1000 / 60)} Minutes",
			mysqlName          : MySQLData["version_comment"],
			myslqVersion       : MySQLData["version"],
			mysqlInnodbVersion : MySQLData["innodb_version"],
			mySQlTlsVersion    : MySQLData["tls_version"],
			fileSystems        : getFileSystemInfo(),
			cpuProcesses       : getCpuProcesses(),
			machineUptime      : getMachineUptime(),
			seLinuxStatus      : getSELinuxStatus()


		]
	}

	/**
	 * Gets version data from MySql as a map
	 *
	 * @return A map of MySql version info.
	 */
	Map getMySqlData() {
		Map mySQLData = [:]
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		sql.eachRow('SHOW VARIABLES LIKE "%version%";') { GroovyResultSet row ->
			mySQLData[row['variable_name']] = row['Value']
		}

		return mySQLData
	}

	/**
	 * Gets Filesystem info, as a List of Maps containing the name, free space and total space.
	 *
	 * @return A list of maps containing filesystem info: name, free space and total space
	 */
	List<Map> getFileSystemInfo() {
		return FileSystems.getDefault().getFileStores().collect {
			[
				name      : it.name(),
				freeSpace : FileUtils.byteCountToDisplaySize(it.getUsableSpace()),
				totalSpace: FileUtils.byteCountToDisplaySize(it.getTotalSpace())
			]
		}
	}

	/**
	 * Calls 'ps -eo pid,ppid,pcpu,pmem,comm --sort=-pcpu | head -11' for the top ten processes consuming CPU, or returns an unsupported
	 * message if there is an error, or nothing returned, because of the environment it is run.
	 *
	 * @return A string of the top ten cpu consuming processes or and unsupported error string.
	 */
	String getCpuProcesses() {
		try {
			return 'ps -eo pid,ppid,pcpu,pmem,comm --sort=-pcpu'.execute().text.split('\n')[0..11].join('\n')
		} catch (Exception e) {
            log.info(e.message)
			return i18nMessage('tdstm.admin.getCpuProcesses.not.supported')
		}
	}

	/**
	 * Calls 'uptime' for the machine uptime and load averages, or returns an unsupported
	 * message if there is an error or nothing returned, because of the environment it is run.
	 *
	 * @return A string of the machine uptime with the load averages, or and unsupported error string.
	 */
	String getMachineUptime() {
		try {
			return 'uptime'.execute().text.trim() ?: i18nMessage('tdstm.admin.getMachineUptime.not.supported')
		} catch (Exception e) {
			return i18nMessage('tdstm.admin.getMachineUptime.not.supported')
		}
	}

	/**
	 * Calls 'getenforce' for the machine SE Linux enforcement status, or returns an unsupported
	 * message if there is an error or nothing returned, because of the environment it is run.
	 *
	 * @return A string of the SE Linux enabled status, or and unsupported error string.
	 */
	String getSELinuxStatus() {
		try {
			return 'getenforce'.execute().text.trim() ?: i18nMessage('tdstm.admin.getSELinuxStatus.not.supported')
		} catch (Exception e) {
			return i18nMessage('tdstm.admin.getSELinuxStatus.not.supported')
		}
	}

}
