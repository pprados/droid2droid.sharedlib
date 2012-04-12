package org.remoteandroid.internal.socket.ip;

import java.io.IOException;
import java.net.Socket;

import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.socket.Channel;

public final class NetworkSocketChannel extends Channel
{
	private Socket mSocket;
	
	public NetworkSocketChannel(Socket socket)
	{
		mSocket=socket;
	}
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
		mSocket.shutdownInput(); // See http://code.google.com/p/android/issues/detail?id=7933
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
