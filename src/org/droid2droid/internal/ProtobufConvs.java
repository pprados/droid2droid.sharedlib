/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.internal;

import static org.droid2droid.Droid2DroidManager.DEFAULT_PORT;
import static org.droid2droid.internal.Constants.ETHERNET;
import static org.droid2droid.internal.Constants.ETHERNET_IPV4_FIRST;
import static org.droid2droid.internal.Constants.ETHERNET_ONLY_IPV4;
import static org.droid2droid.internal.Constants.KEYPAIR_ALGORITHM;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.SCHEME_TCP;
import static org.droid2droid.internal.Constants.TAG_CANDIDATE;
import static org.droid2droid.internal.Constants.V;
import static org.droid2droid.internal.Constants.W;

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

import org.droid2droid.RemoteAndroidInfo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.ByteString;

public final class ProtobufConvs
{

	private static byte[] fromUUID(UUID uuid)
	{
		long longOne = uuid.getMostSignificantBits();
		long longTwo = uuid.getLeastSignificantBits();

		return new byte[]
		{ (byte) (longOne >>> 56), (byte) (longOne >>> 48), (byte) (longOne >>> 40), (byte) (longOne >>> 32),
				(byte) (longOne >>> 24), (byte) (longOne >>> 16), (byte) (longOne >>> 8), (byte) longOne,
				(byte) (longTwo >>> 56), (byte) (longTwo >>> 48), (byte) (longTwo >>> 40), (byte) (longTwo >>> 32),
				(byte) (longTwo >>> 24), (byte) (longTwo >>> 16), (byte) (longTwo >>> 8), (byte) longTwo };
	}
	private static UUID toUUID(byte[] data)
	{
		return new UUID(Tools.byteArrayToLong(data,0),Tools.byteArrayToLong(data,8));
	}
	public static Messages.Identity toIdentity(RemoteAndroidInfo i)
	{
		
		RemoteAndroidInfoImpl info = (RemoteAndroidInfoImpl) i;
		
		Messages.Identity.Builder identityBuilder = Messages.Identity.newBuilder();
		identityBuilder.setUuid(ByteString.copyFrom(fromUUID(info.uuid)))
			.setName(info.name)
			.setPublicKey(ByteString.copyFrom(info.publicKey.getEncoded()))
			.setVersion(info.version)
			.setOs(info.os)
			.setCapability(info.feature);

		identityBuilder.setCandidates(toCandidates(info));
		return identityBuilder.build();
	}

	public static Messages.Candidates toCandidates(RemoteAndroidInfoImpl info)
	{
		org.droid2droid.internal.Messages.Candidates.Builder candidateBuilder = Messages.Candidates.newBuilder();
		boolean setport = false;
		for (int j = 0; j < info.uris.size(); ++j)
		{
			String uri = info.uris.get(j);
			Uri uuri = Uri.parse(uri);
			String sheme = uuri.getScheme();
			if (sheme.equals(SCHEME_TCP))
			{
				try
				{
					int port = Tools.uriGetPortIPV6(uuri);
					if (!setport)
					{
						setport = true;
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
					if (W)
						Log.w(
							TAG_CANDIDATE, PREFIX_LOG + " Error when parse uri " + uri);
				}
			}
		}
		return candidateBuilder.build();
	}

	public static RemoteAndroidInfoImpl toRemoteAndroidInfo(Context context,Messages.Identity identity)
	{
		try
		{
			RemoteAndroidInfoImpl info = new RemoteAndroidInfoImpl();
			info.uuid = toUUID(identity.getUuid().toByteArray());
			info.name = identity.getName();
			byte[] pubBytes = identity.getPublicKey().toByteArray();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
			info.publicKey = KeyFactory.getInstance(KEYPAIR_ALGORITHM).generatePublic(pubKeySpec);
			info.version = identity.getVersion();
			info.os = identity.getOs();
			info.feature = identity.getCapability();
			info.uris = toUris(context,identity.getCandidates());
			info.isBonded = identity.getBonded();
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

	public static ArrayList<String> toUris(Context context,Messages.Candidates candidates) 
	{
		//FIXME: Classer en trois groupes. Prio, normal, low
		ArrayList<String> results=new ArrayList<String>();
		final int port=candidates.hasPort() ? candidates.getPort() : DEFAULT_PORT;
		int[] priority; // odd: IPV6, even: IPV4;
		boolean localNetwork=(NetworkTools.getActiveNetwork(context) & NetworkTools.ACTIVE_LOCAL_NETWORK)!=0;
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
		
// FIXME: gérer le cas du results vide !
		results.trimToSize();
		return results;
	}

	private static void tryIpv4(Messages.Candidates candidates, ArrayList<String> results, boolean localNetwork,
			int port)
	{
		for (int i = candidates.getInternetIpv4Count() - 1; i >= 0; --i)
		{
			try
			{
				InetAddress add = Inet4Address.getByAddress(Tools.intToByteArray(candidates.getInternetIpv4(i)));
				if (add.isLoopbackAddress())
					continue;
				if (add.isLinkLocalAddress() && !localNetwork)
					continue;
				results.add(SCHEME_TCP + "://" + add.getHostAddress() + ':' + port + '/');
			}
			catch (UnknownHostException e)
			{
				if (V)
					Log.v(
						TAG_CANDIDATE, PREFIX_LOG + "Invalide ipv4. Ignore.");
			}
		}
	}

	private static void tryIpv6(Messages.Candidates candidates, ArrayList<String> results, boolean localNetwork,
			int port)
	{
		for (int i = candidates.getInternetIpv6Count() - 1; i >= 0; --i)
		{
			try
			{
				InetAddress add = Inet6Address.getByAddress(candidates.getInternetIpv6(
					i).toByteArray());
				if (add.isLoopbackAddress())
					continue;
				if (add.isLinkLocalAddress() && !localNetwork)
					continue;
				results.add(SCHEME_TCP + "://[" + add.getHostAddress() + "]:" + port + "/");
			}
			catch (UnknownHostException e)
			{
				if (V)
					Log.v(
						TAG_CANDIDATE, PREFIX_LOG + "Invalide ipv6. Ignore.");
			}
		}
	}

}
