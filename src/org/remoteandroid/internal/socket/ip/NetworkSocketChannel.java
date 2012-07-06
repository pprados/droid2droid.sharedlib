package org.remoteandroid.internal.socket.ip;

import static org.remoteandroid.internal.Constants.TAG_CLIENT_BIND;
import static org.remoteandroid.internal.Constants.V;

import java.io.IOException;
import java.net.Socket;

import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.socket.Channel;

import android.annotation.TargetApi;
import android.util.Log;
@TargetApi(14)
public final class NetworkSocketChannel extends Channel
{
	private final Socket mSocket;
	
	@TargetApi(14)
	public NetworkSocketChannel(Socket socket)
	{
		if (V) Log.v(TAG_CLIENT_BIND,"Open socket");
		mSocket=socket;
	}
	@TargetApi(14)
	@Override
	public void write(Msg msg) throws IOException
	{
		writeMsg(msg, mSocket.getOutputStream());
	}

	@Override
	public Msg read() throws IOException
	{
		return readMsg(mSocket.getInputStream());
	}

	@Override
	public void close() throws IOException
	{
		if (V) Log.v(TAG_CLIENT_BIND,"Close socket");
		if (mSocket.isConnected())
		{
			try
			{
				mSocket.shutdownInput(); // See http://code.google.com/p/android/issues/detail?id=7933
			}
			catch (IOException e)
			{
				// Ignore
			}
		}
		mSocket.close();
	}

	@Override
	public boolean isConnected()
	{
		return mSocket.isConnected();
	}
	@Override
	public boolean isBluetoothSecure()
	{
		return false;
	}
}
