package org.remoteandroid.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import static org.remoteandroid.RemoteAndroidInfo.*;

public final class Tools
{
	public static String uriGetHostIPV6(Uri uri)
	{
		String host=uri.getHost();
		if (host.startsWith("[")) // Detect IPV6 format
		{
			String uriv6=uri.toString();
			int i=uriv6.indexOf('[');
			int j=uriv6.indexOf(']');
			host=uriv6.substring(i,j+1);
		}
		return host;
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
	
	// Check if local IP is only for local network
	public static boolean isLocalNetwork(Context context) throws SocketException
	{
		boolean local=false;
		for (Enumeration<NetworkInterface> networks=NetworkInterface.getNetworkInterfaces();networks.hasMoreElements();)
		{
			NetworkInterface network=networks.nextElement();
			for (Enumeration<InetAddress> addrs=network.getInetAddresses();addrs.hasMoreElements();)
			{
				InetAddress add=(InetAddress)addrs.nextElement();
				if (network.getName().startsWith("sit")) // vpn ?
					continue;
				if (network.getName().startsWith("dummy")) // ipv6 in ipv4
					continue;
				if (add.isLoopbackAddress())
					continue;
				if (add.isSiteLocalAddress())
					local=true;
			}
		}
		return local;
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

	public static final byte[] intToByteArray(int value) 
	{
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	public static final int byteArrayToInt(byte [] b) 
	{
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
	}
	
	public static final long byteArrayToLong(byte [] b)
	{
		return byteArrayToLong(b,0);
	}
	public static final long byteArrayToLong(byte [] b,int start) 
	{
		long value = 0;
		for (int i = start; i < start+8; i++)
		{
		   value = (value << 8) + (b[i] & 0xff);
		}
		return value;
	}
	
	public static byte[] longToByteArray(long data) 
	{
		return new byte[] {
			(byte)((data >> 56) & 0xff),
			(byte)((data >> 48) & 0xff),
			(byte)((data >> 40) & 0xff),
			(byte)((data >> 32) & 0xff),
			(byte)((data >> 24) & 0xff),
			(byte)((data >> 16) & 0xff),
			(byte)((data >> 8 ) & 0xff),
			(byte)((data >> 0) & 0xff),
			};
	}

}
