package org.remoteandroid.internal;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.Messages.Type;

import android.net.Uri;
import android.os.RemoteException;

public abstract class Pairing
{
	public static Pairing sPairing;
	public static Pairing getPairing()
	{
		return sPairing;
	}
	public abstract Pair<RemoteAndroidInfoImpl,Long> client(
		AbstractProtoBufRemoteAndroid android,
		Uri uri,
		Type type,
		int flags,
		long timeout) throws UnknownHostException, IOException, RemoteException;
	public abstract Msg server(Object conContext,Msg req,long cookie);
}