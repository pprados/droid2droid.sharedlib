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
package org.droid2droid.internal.socket.ip;

import static org.droid2droid.internal.Constants.TAG_CLIENT_BIND;
import static org.droid2droid.internal.Constants.V;

import java.io.IOException;
import java.net.Socket;

import org.droid2droid.internal.Messages.Msg;
import org.droid2droid.internal.socket.Channel;

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
