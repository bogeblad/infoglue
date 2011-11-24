package org.infoglue.cms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This class is used to match incoming IP-addresses against a list of allowed ones.
 * Handles wildcards for ipv4 but only exact matches for ipv6.
 */
public class IPMatcher 
{
	//Need to find a IP regexp that handles wildcards 
	private static final Pattern IPV4_PATTERN = Pattern.compile("((?:\\d{1,3}\\.){3}\\d{1,3})(?:/(\\d{1,2}))?");
    private static final Pattern IPV6_PATTERN = Pattern.compile("((?:::)|(?:(?:(?:[0-9a-fA-F]{1,4}:)|:){1,7})[0-9a-fA-F]{1,4})(?:/(\\d{1,3}))?");

    /**
     * This method matches either of the two IP:s against a list of allowed IP-patterns.
     * 
     * @param allowedIP The list of allowed IP:s. Can be: "localhost", or a literal IP address in the form 999.999.999.999, or a literal IP address in the form *.*.*.* where any of the octets can be the wildcard character (*), or an IP range in the form 999.999.999.999-999.999.999.999
     * @param commonIP The primary user IP (IPv4 or IPv6-format)
     * @param alternateIP The primary user IP (IPv4 or IPv6-format)
     * @return true or false if the IP:s matches
     */
   	public static boolean isIpInList(List<String> allowedIP, String commonIP, String alternateIP) 
	{
   		boolean status = false;
   		
		for (String ip : allowedIP) 
		{
			ip = ip.trim();
			if(IPV6_PATTERN.matcher(ip).matches())
			{
				if (doesIPv6Match(ip, commonIP)||doesIPv6Match(ip, alternateIP))
				{
					status = true;
				}
			}
			if(doesIPv4Match(ip, commonIP)||doesIPv4Match(ip, alternateIP))
			{
				status = true;
			}
		}

		return status;
	}
	
	/**
	 * Determines if 2 given IP addresses matches. The first, ip1, can include wildcards (*). </br>
	 * ip1 can be: "localhost", or a literal IP address in the form 999.999.999.999, or a literal IP address in the form *.*.*.* where any of the octets can be the wildcard character (*), or an IP range in the form 999.999.999.999-999.999.999.999
	 * 
	 * @param ip1 the address to compare against
	 * @param ip2 the address to compare
	 * @return
	 */
	public static boolean doesIPv4Match(String ip1, String ip2) 
	{
		// Check if ip2 == null, if so return false
		// Check if ip2 is a IPv4 number.
		if (ip2 == null || !IPV4_PATTERN.matcher(ip2).matches()) 
		{
			return false;
		}

		// Scan the list of addresses configured and see if the ip2
		// matches any.
		boolean ret = false;

		// Match test 1: localhost.
		if (ip1.equalsIgnoreCase("localhost")) 
		{
			if (ip2.equalsIgnoreCase(ip1)) 
			{
				ret = true;
			}
		}

		// Match test 2: exact address match.
		if (ip1.indexOf("*") == -1 & ip1.indexOf("-") == -1) 
		{
			if (ip2.equalsIgnoreCase(ip1)) 
			{
				ret = true;
			}
		}

		// Match test 3: A single address with wildcards.
		if (ip1.indexOf("*") != -1) 
		{
			// Get all four octets from both the next address in the list and
			// the remote address so we can examine them individually.
			StringTokenizer nt = new StringTokenizer(ip1, ".");
			StringTokenizer rt = new StringTokenizer(ip2, ".");
			String ip1Octet1 = (String) nt.nextToken();
			String ip1Octet2 = (String) nt.nextToken();
			String ip1Octet3 = (String) nt.nextToken();
			String ip1Octet4 = (String) nt.nextToken();
			String ip2Octet1 = (String) rt.nextToken();
			String ip2Octet2 = (String) rt.nextToken();
			String ip2Octet3 = (String) rt.nextToken();
			String ip2Octet4 = (String) rt.nextToken();
			
			// Now, for each octet, see if we have either an exact match or a
			// wildcard match, and if so set the appropriate octet flag.
			boolean octet1Ok = false;
			boolean octet2Ok = false;
			boolean octet3Ok = false;
			boolean octet4Ok = false;
			
			if (ip2Octet1.equalsIgnoreCase(ip1Octet1) || ip1Octet1.equalsIgnoreCase("*")) 
			{
				octet1Ok = true;
			}
			if (ip2Octet2.equalsIgnoreCase(ip1Octet2) || ip1Octet2.equalsIgnoreCase("*")) 
			{
				octet2Ok = true;
			}
			if (ip2Octet3.equalsIgnoreCase(ip1Octet3) || ip1Octet3.equalsIgnoreCase("*")) 
			{
				octet3Ok = true;
			}
			if (ip2Octet4.equalsIgnoreCase(ip1Octet4) || ip1Octet4.equalsIgnoreCase("*")) 
			{
				octet4Ok = true;
			}
			// Finally, if all four flags are true, the address is OK.
			if (octet1Ok & octet2Ok & octet3Ok & octet4Ok) 
			{
				ret = true;
			}
		}

		// Match test 4: IP range.
		if (ip1.indexOf("-") != -1) 
		{
			StringTokenizer st = new StringTokenizer(ip1, "-");
			String rangeStart = st.nextToken();
			String rangeEnd = st.nextToken();
			long rangeStartLong = ipToLong(rangeStart);
			long rangeEndLong = ipToLong(rangeEnd);
			long remoteAddrLong = ipToLong(ip2);
			if (remoteAddrLong >= rangeStartLong && remoteAddrLong <= rangeEndLong) 
			{
				ret = true;
			}
		}

		return ret;
	} // End addressInList().
	 
	 
	/**
	 * Determines if the second IP number matches the first.
	 * 
	 * @param ip1 the IP to match against
	 * @param ip2 the IP to match
	 * @return true if ip1 and ip2 matches
	 */
	public static boolean doesIPv6Match(String ip1, String ip2)
	{
		if(ip2==null || !IPV6_PATTERN.matcher(ip2).matches())
		{
			return false;
		}
		
		if(ip2.equalsIgnoreCase(ip1))
		{
			return true;
		}
		
		return false;
	}

	
	/**
	 * Method that converts an IP address to a long.
	 * 
	 * @param ip
	 *            The IP address to convert.
	 * @return The IP address as a long.
	 */
	private static long ipToLong(String ip) 
	{

		StringTokenizer st = new StringTokenizer(ip, ".");
		int o1 = Integer.parseInt((String) st.nextToken());
		int o2 = Integer.parseInt((String) st.nextToken());
		int o3 = Integer.parseInt((String) st.nextToken());
		int o4 = Integer.parseInt((String) st.nextToken());
		String o1S = Integer.toBinaryString(o1).trim();
		String o2S = Integer.toBinaryString(o2).trim();
		String o3S = Integer.toBinaryString(o3).trim();
		String o4S = Integer.toBinaryString(o4).trim();
		o1S = padBinByteStr(o1S);
		o2S = padBinByteStr(o2S);
		o3S = padBinByteStr(o3S);
		o4S = padBinByteStr(o4S);
		String bin = o1S + o2S + o3S + o4S;
		long res = 0;
		long j = 2147483648L;
		for (int i = 0; i < 32; i++) 
		{
			char c = bin.charAt(i);
			if (c == '1') 
			{
				res = res + j;
			}
			j = j / 2;
		}
		return res;

	} // End ipToLong().
	  
	/**
	 * Method that pads (prefixes) a string representation of a byte with 0's.
	 * 
	 * @param binByte String of the byte (maybe less than 8 bits) to pad.
	 * @return String of the byte guaranteed to have 8 bits.
	 */
	private static String padBinByteStr(String binByte) 
	{
		if (binByte.length() == 8) 
		{
			return binByte;
		}
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < (8 - binByte.length()); i++) 
		{
			sb.append("0");
		}
		
		sb.append(binByte);
		return sb.toString();

	} // End padBinByteStr().
	  
	  
	public static void main(String[] args) 
	{
		List<String> ipList = new ArrayList<String>();
		ipList.add("123.123.123.123");
		ipList.add("124.124.124.*");
		ipList.add("124.124.125.0-124.124.125.125");
		ipList.add("124.124.124.*");
		ipList.add("1080:0:0:0:8:800:200C:417A");
		ipList.add("1080::8:800:200C:417B");

		// Should be true
		boolean test1 = isIpInList(ipList, "123.123.123.123", null);
		System.out.println("Test 1 (true):" + test1);

		// Should be false
		boolean test2 = isIpInList(ipList, "123.123.123.124", null);
		System.out.println("Test 2 (false):" + test2);

		// Should be true
		boolean test3 = isIpInList(ipList, "123.123.123.124", "123.123.123.123");
		System.out.println("Test 3 (true):" + test3);

		// Should be true
		boolean test4 = isIpInList(ipList, "124.124.124.123", null);
		System.out.println("Test 4 (true):" + test4);

		// Should be true
		boolean test5 = isIpInList(ipList, "124.124.125.123", null);
		System.out.println("Test 5 (true):" + test5);

		// Should be false
		boolean test6 = isIpInList(ipList, "124.124.125.126", null);
		System.out.println("Test 6 (false):" + test6);

		// Should be true
		boolean test7 = isIpInList(ipList, "1080:0:0:0:8:800:200C:417B", null);
		System.out.println("Test 7 (true):" + test7);

		// Should be true
		boolean test8 = isIpInList(ipList, "1080:0:0:0:8:800:200C:417A", null);
		System.out.println("Test 8 (true):" + test8);
		// Should be false
		boolean test9 = isIpInList(ipList, "1080:0:0:0:8:800:200C:417C", null);
		System.out.println("Test 9 (false):" + test9);

		System.out.println(IPV6_PATTERN.matcher("123.123.123.123").matches());
		System.out.println(IPV6_PATTERN.matcher("1080:0:0:0:8:800:200C:417A").matches());
		System.out.println(IPV6_PATTERN.matcher("fe80::89c4:9411:9a7f:c513").matches());
		// System.out.println(IPV4_PATTERN_v2.matcher("123.123.123.*").matches());
	}
}
