package org.remoteandroid.internal.socket.bluetooth;

import static org.remoteandroid.internal.Constants.*;

import java.io.IOException;
import java.util.UUID;

import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.socket.Channel;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


// The channel en charge de l'écriture des messages sur le socket.
public class BluetoothSocketChannel extends Channel
{
	private UUID mUUID;
	private BluetoothSocket mSocket;
	private boolean mSecure;

	public BluetoothSocketChannel(UUID uuid,BluetoothSocket socket,boolean secure)
	{
		mUUID=uuid;
		mSocket=socket;
		mSecure=secure;
		BluetoothSocketBossSender.sUUIDUseds.add(uuid);
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
		BluetoothSocketBossSender.sUUIDUseds.remove(mUUID);
		mSocket.close();
	}

	@Override
	public boolean isConnected()
	{
		try
		{
			mSocket.getInputStream();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	@Override
	public void finalize()
	{
		try
		{
			close();
		}
		catch (IOException e)
		{
			if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Error in finalize",e);
		}
	}

	@Override
	public boolean isBluetoothSecure()
	{
		return mSecure;
	}
}
