package org.remoteandroid.internal;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.UUID;

import static org.remoteandroid.internal.Constants.*;

import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.ByteString;

public class ProtobufConvs
{

	public static Messages.Identity toIdentity(RemoteAndroidInfo i)
	{
		RemoteAndroidInfoImpl info=(RemoteAndroidInfoImpl)i;
		Messages.Identity.Builder identityBuilder=Messages.Identity.newBuilder();
		identityBuilder
			.setUuid(info.uuid.toString())
			.setName(info.name)
			.setPublicKey(ByteString.copyFrom(info.publicKey.getEncoded()))
			.setVersion(info.version)
			.setOs(info.os)
			.setCapability(info.capability);

		identityBuilder.setCandidates(toCandidates(info));
		return identityBuilder.build();
	}

	public static Messages.Candidates toCandidates(RemoteAndroidInfoImpl info)
	{
		org.remoteandroid.internal.Messages.Candidates.Builder candidateBuilder=Messages.Candidates.newBuilder();
		boolean setport=false;
		boolean setbtanonymous=false;
		for (int j=0;j<info.uris.size();++j)
		{
			String uri=info.uris.get(j);
			Uri uuri=Uri.parse(uri);
			String sheme=uuri.getScheme();
			if (sheme.equals(SCHEME_BT) || sheme.equals(SCHEME_BTS))
			{
				if (sheme.equals(SCHEME_BT) && !setbtanonymous)
				{
					candidateBuilder.setBluetoothAnonmymous(true);
					setbtanonymous=true;
				}
				try
				{
					candidateBuilder.setBluetoothMac(Long.parseLong(uuri.getHost(),16));
				}
				catch (NumberFormatException e)
				{
					if (W) Log.w(TAG_CANDIDATE,PREFIX_LOG+" Error when parse uri "+uri);
				}
			}
			else if (sheme.equals(SCHEME_TCP))
			{
				try
				{
					int port=Tools.uriGetPortIPV6(uuri);
					if (!setport)
					{
						setport=true;
						candidateBuilder.setPort(port);
					}
					InetAddress add = InetAddress.getByName(Tools.uriGetHostIPV6(uuri));
					if (add instanceof Inet4Address)
					{
						candidateBuilder.addInternetIpv4(Tools.byteArrayToInt(add.getAddress()));
					}
					else
					{
						candidateBuilder.addInternetIpv6(ByteString.copyFrom(add.getAddress()));
					}
				}
				catch (UnknownHostException e)
				{
					if (W) Log.w(TAG_CANDIDATE,PREFIX_LOG+" Error when parse uri "+uri);
				}
			}
		}
		return candidateBuilder.build();
	}

	public static RemoteAndroidInfoImpl toRemoteAndroidInfo(Messages.Identity identity)
	{
		try
		{
			RemoteAndroidInfoImpl info=new RemoteAndroidInfoImpl();
			if (D) Log.d("Conv",PREFIX_LOG+"UUID=\""+identity.getUuid()+'"');
			info.uuid=UUID.fromString(identity.getUuid());
			info.name=identity.getName();
			byte[] pubBytes=identity.getPublicKey().toByteArray();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
			info.publicKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);
			info.version=identity.getVersion();
			info.os=identity.getOs();
			info.capability=identity.getCapability();
			info.uris=toUris(identity.getCandidates());
			info.isBonded=identity.getBounded();
			return info;
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new Error(e);
		}
		catch (InvalidKeySpecException e)
		{
			throw new Error(e);
		}
	}
	public static ArrayList<String> toUris(Messages.Candidates candidates) 
	{
		//FIXME: Classer en trois groupes. Prio, normal, low
		ArrayList<String> results=new ArrayList<String>();
		boolean localNetwork=true; // FIXME
		final int port=candidates.hasPort() ? candidates.getPort() : RemoteAndroidManager.DEFAULT_PORT;
		// FIXME
//		switch (conn.getActiveNetworkInfo().getType())
//		{
//			case ConnectivityManager.TYPE_MOBILE:
//			case ConnectivityManager.TYPE_MOBILE_DUN:
//			case ConnectivityManager.TYPE_MOBILE_HIPRI:
//				localNetwork=false;
//				break;
//			default:
//				localNetwork=true;
//				break;
//		}
		int[] priority; // odd: IPV6, even: IPV4;
		
		if (ETHERNET)
		{
			if (ETHERNET_IPV4_FIRST)
			{
				priority=new int[]{1,0};
			}
			else
				priority=new int[]{0,1};
			
			for (int prio:priority)
			{
				switch (prio)
				{
					case 0:
						if (!ETHERNET_ONLY_IPV4)
							tryIpv6(candidates, results, localNetwork,port);
						break;
					case 1:
						tryIpv4(candidates, results, localNetwork,port);
						break;
				}
			}
		}
		
		if (BLUETOOTH)
		{
			
			if (candidates.hasBluetoothMac() && candidates.getBluetoothMac()!=0)
			{
				final int BT_MAC_SIZE=12;
				String btmac=("00000000000"+Long.toHexString(candidates.getBluetoothMac()).toUpperCase());
				btmac=btmac.substring(btmac.length()-BT_MAC_SIZE,btmac.length());
				boolean acceptAnonymous=(Compatibility.VERSION_SDK_INT>=Compatibility.VERSION_GINGERBREAD_MR1);
				if (BLUETOOTH_FIRST)
				{
					
					if (acceptAnonymous && candidates.hasBluetoothAnonmymous())
						results.add(0,SCHEME_BT+"://"+btmac+'/');
					else
						results.add(0,SCHEME_BTS+"://"+btmac+'/');
				}
				else
				{
					if (acceptAnonymous && candidates.hasBluetoothAnonmymous())
						results.add(SCHEME_BT+"://"+btmac+'/');
					else
						results.add(SCHEME_BTS+"://"+btmac+'/');
				}
			}
		}
// FIXME: gérer le cas du results vide !
		results.trimToSize();
		return results;
	}
	private static void tryIpv4(Messages.Candidates candidates, ArrayList<String> results,
			boolean localNetwork,int port)
	{
		for (int i=candidates.getInternetIpv4Count()-1;i>=0;--i)
		{
			try
			{
				InetAddress add=Inet4Address.getByAddress(Tools.intToByteArray(candidates.getInternetIpv4(i)));
				if (add.isLoopbackAddress())
					continue;
				if (add.isLinkLocalAddress() && !localNetwork) 
					continue;
				results.add(SCHEME_TCP+"://"+add.getHostAddress()+':'+port+'/');
			}
			catch (UnknownHostException e)
			{
				if (V) Log.v(TAG_CANDIDATE,PREFIX_LOG+"Invalide ipv4. Ignore.");
			}
		}
	}
	private static void tryIpv6(Messages.Candidates candidates, ArrayList<String> results,
			boolean localNetwork,int port)
	{
		for (int i=candidates.getInternetIpv6Count()-1;i>=0;--i)
		{
			try
			{
				InetAddress add=Inet6Address.getByAddress(candidates.getInternetIpv6(i).toByteArray());
				if (add.isLoopbackAddress())
					continue;
				if (add.isLinkLocalAddress() && !localNetwork) 
					continue;
				results.add(SCHEME_TCP+"://["+add.getHostAddress()+"]:"+port+"/");
			}
			catch (UnknownHostException e)
			{
				if (V) Log.v(TAG_CANDIDATE,PREFIX_LOG+"Invalide ipv6. Ignore.");
			}
		}
	}
	


}
