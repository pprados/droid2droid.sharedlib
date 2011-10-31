package org.remoteandroid.internal;

import java.io.IOException;
import java.net.UnknownHostException;

import org.remoteandroid.internal.Messages.Msg;

import android.os.RemoteException;

public abstract class Login
{
	public static Login sLogin;
	public static Login getLogin()
	{
		return sLogin;
	}
	public abstract long client(AbstractProtoBufRemoteAndroid android,long timeout) throws UnknownHostException, IOException, RemoteException;
	public abstract Msg server(Object conContext,Msg req,long cookie);
}
