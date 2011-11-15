package org.remoteandroid.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.net.Uri;

public class Tools
{
	public static String uriGetHostIPV6(Uri uri)
	{
		String host=uri.getHost();
		if (host.startsWith("[")) // Detect IPV6 format
		{
			String uriv6=uri.toString();
			int i=uriv6.indexOf('[');
			int j=uriv6.indexOf(']');
			return uriv6.substring(i+1,j);
		}
		else return host;
	}
	public static int uriGetPortIPV6(Uri uri)
	{
		String host=uri.getHost();
		if (host.startsWith("[")) // Detect IPV6 format
		{
			String uriv6=uri.toString();
			int j=uriv6.indexOf(']');
			if (j!=-1 && j<uriv6.length()-1 && uriv6.charAt(j+1)==':')
			{
				String port;
	    		port=uriv6.substring(j+2);
	    		int k=port.indexOf('/');
				if (k!=-1) port=port.substring(0,k);
				return Integer.parseInt(port);
			}
			else
				return -1;
		}
		return uri.getPort();
	}
	public static double getBogoMips()
	{
		BufferedReader in=null;
		try
		{
			ProcessBuilder pb = new ProcessBuilder("/system/bin/cat", "/proc/cpuinfo");
			Process process = pb.start();
			in = new BufferedReader(new InputStreamReader(process.getInputStream()),8096);
			String line;
			while ((line=in.readLine())!=null)
			{
				if (line.startsWith("BogoMIPS"))
				{
					return Double.parseDouble(line.substring(line.indexOf(':')+1));
				}
			}
		}
		catch (Exception e)
		{
			// Ignore
		}
		finally
		{
			if (in!=null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
			
		}
		return 0.0;
	}

	public static long getCPUFreq()
	{
		BufferedReader in=null;
		try
		{
			ProcessBuilder pb = new ProcessBuilder("/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
			Process process = pb.start();
			in = new BufferedReader(new InputStreamReader(process.getInputStream()),8096);
			String line;
			return Long.parseLong(in.readLine());
		}
		catch (Exception e)
		{
			// Ignore
		}
		finally
		{
			if (in!=null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
			
		}
		return 0;
	}


}
