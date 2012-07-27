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
package org.droid2droid.internal.socket;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.droid2droid.internal.Messages.Msg;

import com.google.protobuf.MessageLite;

public abstract class Channel implements Closeable
{
    public static final MessageLite sPrototype=Msg.getDefaultInstance();
    public abstract void write(Msg msg) throws IOException;
    public abstract Msg read() throws IOException;
	@Override
	public abstract void close() throws IOException;
	public abstract boolean isConnected();
	public abstract boolean isBluetoothSecure();

	public static final int INITIAL_BUFSIZE=1024;
	private WeakReference<byte[]> mBuf=new WeakReference<byte[]>(new byte[INITIAL_BUFSIZE]);
	
	public static void writeMsg(MessageLite msg, OutputStream out) throws IOException
	{
		byte[] data=msg.toByteArray();
		final int l=data.length;
		byte[] len=new byte[] 
		        {
	                (byte)(l >>> 24),
	                (byte)(l >>> 16),
	                (byte)(l >>> 8),
	                (byte)l
	            };
		out.write(len);
		out.write(data);
		out.flush();
	}
	
	public Msg readMsg(InputStream in) throws IOException, EOFException
	{
		byte[] buf=mBuf.get();
		if (buf==null)
		{
			mBuf=new WeakReference<byte[]>(buf=new byte[INITIAL_BUFSIZE]);
		}
		int ss=in.read(buf,0,4);
		if (ss!=4)
		{
			EOFException e=new EOFException("Communication closed");
			throw e;
		}
		int length=(buf[0] << 24)
        	+ ((buf[1] & 0xFF) << 16)
        	+ ((buf[2] & 0xFF) << 8)
        	+ (buf[3] & 0xFF);
		if (length+4>buf.length)
		{
			mBuf=new WeakReference<byte[]>(buf=new byte[length+4]);
		}
		int pos=4;
		int l=length;
		do
		{
			int s=in.read(buf,pos,l);
			if (s==-1)
				throw new EOFException();
			pos+=s;
			l-=s;
		} while (l>0);
		// Read protobuf message
	    Msg msg=(Msg)Channel.sPrototype.newBuilderForType().mergeFrom(buf, 4, length).build();
	    return msg;
	}
	
}

