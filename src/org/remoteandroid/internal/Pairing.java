package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.TIMEOUT_ACCEPT_ANONYMOUS;

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
	private static long sTimeoutAcceptAnonymous;
	public static void enableTemporaryAcceptAnonymous()
	{
		sTimeoutAcceptAnonymous=System.currentTimeMillis()+TIMEOUT_ACCEPT_ANONYMOUS;
	}
	public static boolean isTemporaryAcceptAnonymous()
	{
		return System.currentTimeMillis()<sTimeoutAcceptAnonymous;
	}
	public static void clearTemporaryAcceptAnonymous()
	{
		sTimeoutAcceptAnonymous=0;
	}
}
