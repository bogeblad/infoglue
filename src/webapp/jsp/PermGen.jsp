<%@ page import="java.lang.management.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
  <title>JVM Memory Monitor</title>
</head>

<%
Iterator iter1 = ManagementFactory.getMemoryPoolMXBeans().iterator();
while (iter1.hasNext()) 
{
    MemoryPoolMXBean item = (MemoryPoolMXBean) iter1.next();
	%>

	<table border="0" width="100%">
	<tr><td colspan="2" align="center"><h3>Memory MXBean</h3></td></tr>
	<tr><td width="200">Heap Memory Usage</td><td><%= ManagementFactory.getMemoryMXBean().getHeapMemoryUsage() %></td></tr>
	<tr><td>Non-Heap Memory Usage</td><td><%= ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage() %></td></tr></body>
	<tr><td colspan="2">&nbsp;</td></tr>
	<tr><td colspan="2" align="center"><h3>Memory Pool MXBeans</h3></td></tr>
	<%
    Iterator iter2 = ManagementFactory.getMemoryPoolMXBeans().iterator();
    while (iter2.hasNext()) {
        MemoryPoolMXBean item2 = (MemoryPoolMXBean) iter2.next();
		%>
		<tr><td colspan="2">
		<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
		<tr><td colspan="2" align="center"><b><%= item2.getName() %></b></td></tr>
		<tr><td width="200">Type</td><td><%= item2.getType() %></td></tr>
		<tr><td>Usage</td><td><%= item2.getUsage() %></td></tr>
		<tr><td>Peak Usage</td><td><%= item2.getPeakUsage() %></td></tr>
		<tr><td>Collection Usage</td><td><%= item2.getCollectionUsage() %></td></tr>
		</table>
		</td></tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<%
	}
}
%>

</table>