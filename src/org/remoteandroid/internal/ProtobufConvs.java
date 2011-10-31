package org.remoteandroid.internal;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import static org.remoteandroid.internal.Constants.*;

import org.remoteandroid.RemoteAndroidInfo;

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
		if (info.bluetoothid!=null)
		{
			identityBuilder.setBluetoothId(info.bluetoothid);
		}
		if (info.ethernetMac!=null)
		{
			identityBuilder.setEthernetMac(info.ethernetMac);
		}
		return identityBuilder.build();
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
			info.bluetoothid=identity.getBluetoothId();
			info.ethernetMac=identity.getEthernetMac();
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

}
