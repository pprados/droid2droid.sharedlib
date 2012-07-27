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

import static org.droid2droid.Droid2DroidManager.FLAG_PROPOSE_PAIRING;
import static org.droid2droid.internal.Constants.COOKIE_EXCEPTION;
import static org.droid2droid.internal.Constants.COOKIE_NO;
import static org.droid2droid.internal.Constants.COOKIE_SECURITY;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.SECURITY;
import static org.droid2droid.internal.Constants.TAG_INSTALL;
import static org.droid2droid.internal.Constants.TIMEOUT_PING_BINDER;
import static org.droid2droid.internal.Constants.V;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.internal.Messages.Msg;
import org.droid2droid.internal.Messages.Type;

import android.net.Uri;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.ByteString;

public abstract class AbstractProtoBufRemoteAndroid extends AbstractRemoteAndroidImpl
{
	
    protected Map<Long, ReadWait> mLocks=Collections.synchronizedMap(new HashMap<Long,ReadWait>());
    
	public abstract Msg sendRequestAndReadResponse(Msg msg,long timeout)
			throws RemoteException;

	protected AbstractProtoBufRemoteAndroid(Droid2DroidManager manager,Uri uri)
	{
		super(manager,uri);
	}
	@Override
	public boolean pingBinder()
	{
		try
		{
			final long threadid = Thread.currentThread().getId();
			Msg msg = Msg.newBuilder().setType(Type.PING).setThreadid(
				threadid).build();
			Msg resp = sendRequestAndReadResponse(msg,TIMEOUT_PING_BINDER);
			return (resp != null && resp.getType() == Type.PING);
		}
		catch (RemoteException e)
		{
			return false;
		}
	}

	public void checkStatus(Msg resp) throws SecurityException
	{
		int status = resp.getStatus();
		switch (status)
		{
			case AbstractRemoteAndroidImpl.STATUS_REFUSE_ANONYMOUS:
				close();
				throw new SecurityException("Refuse anonymous connexion. Pair the devices before using it.");
			case AbstractRemoteAndroidImpl.STATUS_REFUSE_CONNECTION_MODE:
				close();
				throw new SecurityException("Invalide connexion mode.");
			case AbstractRemoteAndroidImpl.STATUS_INVALIDE_COOKIE:
				close();
				throw new SecurityException("Invalide connexion mode.");
			default:
				// Ignore
				break;
		}
	}
	// Transaction version le context remote
	@Override
	final public boolean transactRemoteAndroid(int connid,int code, Parcel data, Parcel reply,
			int flags,long timeout) throws RemoteException
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"Transact...");
		if (!isBinderAlive())
			return false;
		final long threadid = Thread.currentThread().getId();
		Msg msg = Msg.newBuilder()
			.setType(Type.TRANSACT_RemoteAndroid)
			.setCmd(code)
			.setTimeout(timeout)
			.setThreadid(threadid)
			.setData(ByteString.copyFrom(data.marshall()))
			.setFlags(flags)
			.build();
		Msg resp = sendRequestAndReadResponse(msg,timeout);
		checkStatus(resp);
		ByteString bs = resp.getReply();
		byte[] rc = new byte[bs.size()];
		bs.copyTo(rc, 0);
		reply.unmarshall(rc, 0, rc.length);
		reply.setDataPosition(0);
		final boolean status = resp.getRc();
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"Transact done");
    	reply.readException();
		return status;
	}

	// Transaction vers un binder remote
	@Override
	final public boolean transactBinder(int connid,int oid, int code, Parcel data,
			Parcel reply, int flags,long timeout) throws RemoteException
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"TransactBinder...");
		final long threadid = Thread.currentThread().getId();
		updateData(data);
		Msg msg = Msg.newBuilder()
			.setType(Type.TRANSACT_Binder)
			.setThreadid(threadid)
			.setTimeout(timeout)
			.setOid(oid)
			.setCmd(code)
			.setData(ByteString.copyFrom(data.marshall()))
			.setFlags(flags)
			.build();
		Msg resp = sendRequestAndReadResponse(msg,timeout);
		checkStatus(resp);
		ByteString bs = resp.getReply();
		byte[] rc = new byte[bs.size()];
		bs.copyTo(rc, 0);
		reply.unmarshall(rc, 0, rc.length);
		reply.setDataPosition(0);
		updateReply(reply);
		final boolean status = resp.getRc();
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"Transact done");
		return status;
	}
	
	public static final int CMD_PROPOSE_APK=1;
	public static final int CMD_SEND_FILE=2;
	public static final int CMD_CANCEL_FILE=3;
	public static final int CMD_INSTALL_APK=4;
	
	@Override
	public int proposeApk(
			int connid,
			String label,
			String packageName,
			int version,
			byte[] sign,
			long len,
			int flags,
			long timeout
			) throws RemoteException
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"ProposeFile...");
		Parcel data=Parcel.obtain();
		Parcel reply=Parcel.obtain();
		try
		{
			final long threadid = Thread.currentThread().getId();
			data.writeString(label);
			data.writeString(packageName);
			data.writeInt(version);
			data.writeByteArray(sign);
			data.writeLong(len);
			Msg msg = Msg.newBuilder()
				.setType(Type.TRANSACT_Apk)
				.setCmd(CMD_PROPOSE_APK)
				.setTimeout(timeout)
				.setThreadid(threadid)
				.setData(ByteString.copyFrom(data.marshall()))
				.setFlags(flags)
				.build();
			Msg resp = sendRequestAndReadResponse(msg,timeout);
			checkStatus(resp);
			if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"ProposeFile done.");
			ByteString bs = resp.getReply();
			byte[] rc = new byte[bs.size()];
			bs.copyTo(rc, 0);
			reply.unmarshall(rc, 0, rc.length);
			reply.setDataPosition(0);
			reply.readException();
			return reply.readInt();
		}
		finally
		{
			data.recycle();
			reply.recycle();
		}
	}
	
	@Override
	public boolean sendFileData(int connid,int fd,byte[] datas,int len,long pos,long size,long timeout) throws RemoteException
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"SendFileData...");
		Parcel data=Parcel.obtain();
		Parcel reply=Parcel.obtain();
		try
		{
			final long threadid = Thread.currentThread().getId();
			data.writeInt(fd);
			data.writeByteArray(datas);
			data.writeInt(len);
			data.writeLong(pos);
			data.writeLong(size);
			Msg msg = Msg.newBuilder()
				.setType(Type.TRANSACT_Apk)
				.setCmd(CMD_SEND_FILE)
				.setTimeout(timeout)
				.setThreadid(threadid)
				.setData(ByteString.copyFrom(data.marshall()))
				.build();
			Msg resp = sendRequestAndReadResponse(msg,timeout);
			checkStatus(resp);
			if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"SendFileData done.");
			ByteString bs = resp.getReply();
			byte[] rc = new byte[bs.size()];
			bs.copyTo(rc, 0);
			reply.unmarshall(rc, 0, rc.length);
			reply.setDataPosition(0);
			reply.readException();
			return reply.readByte()==1;
		}
		finally
		{
			data.recycle();
			reply.recycle();
		}
	}
	@Override
	public void cancelFileData(int connid,int fd,long timeout) throws RemoteException
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"CancelFileData...");
		Parcel data=Parcel.obtain();
		Parcel reply=Parcel.obtain();
		try
		{
			final long threadid = Thread.currentThread().getId();
			data.writeInt(fd);
			Msg msg = Msg.newBuilder()
				.setType(Type.TRANSACT_Apk)
				.setCmd(CMD_CANCEL_FILE)
				.setTimeout(timeout)
				.setThreadid(threadid)
				.setData(ByteString.copyFrom(data.marshall()))
				.build();
			Msg resp=sendRequestAndReadResponse(msg,timeout);
			checkStatus(resp);
			reply.readException();
			if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"CancelFileData done.");
		}
		finally
		{
			data.recycle();
			reply.recycle();
		}
	}
	@Override
	public boolean installApk(int connid,String label,int fd,int flags,long timeout) throws RemoteException 
	{
		if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"InstallApk...");
		Parcel data=Parcel.obtain();
		Parcel reply=Parcel.obtain();
		try
		{
			final long threadid = Thread.currentThread().getId();
			data.writeString(label);
			data.writeInt(fd);
			Msg msg = Msg.newBuilder()
				.setType(Type.TRANSACT_Apk)
				.setCmd(CMD_INSTALL_APK)
				.setFlags(flags)
				.setTimeout(timeout)
				.setThreadid(threadid)
				.setData(ByteString.copyFrom(data.marshall()))
				.build();
			Msg resp = sendRequestAndReadResponse(msg,timeout);
			checkStatus(resp);
			if (V) Log.v(TAG_INSTALL, PREFIX_LOG+"InstallApk done.");
			ByteString bs = resp.getReply();
			byte[] rc = new byte[bs.size()];
			bs.copyTo(rc, 0);
			reply.unmarshall(rc, 0, rc.length);
			reply.setDataPosition(0);
			reply.readException();
			return reply.readByte()==1;
		}
		finally
		{
			data.recycle();
			reply.recycle();
		}
	}
	
	protected abstract void initBootstrap() throws UnknownHostException, IOException;
	
	private final long srvCookie(long cookie)
	{
		return cookie & 0xFFFF0000;
	}
	private final long cliCookie(long cookie)
	{
		return cookie & 0xFFFF;
	}
	// Connection avec exploitation d'un cookie
	@Override
	public boolean connect(Type conType,int flags,long cookie,long timeout) throws UnknownHostException, IOException, RemoteException, SecurityException
	{
		final long threadid = Thread.currentThread().getId();
		Msg resp;
		boolean cookieAlive;
		boolean refuse=false;
		do
		{
			do
			{
				// 1. Ask a cookie
				if (SECURITY)
				{
					if (conType==Type.CONNECT)
					{
						cookie=((Droid2DroidManagerImpl)mManager).getCookie(flags,mUri.toString());
						if (cookie==COOKIE_SECURITY)
						{
							if ((flags & FLAG_PROPOSE_PAIRING)==0)
								throw new SecurityException("Impossible to connect with "+mUri+". Add the flag FLAG_PROPOSE_PAIRING ?");
							else
								throw new SecurityException("Pairing refused with "+mUri+".");
						}
						if (cookie==COOKIE_EXCEPTION)
							throw new IOException("Impossible to connect with "+mUri+". Remote device is shared ?"); // FIXME: Avec Motorola Milestone et IPV6, java.net.SocketException: The socket level is invalid
	
						if (cookie==COOKIE_NO)
							throw new SecurityException("Connection impossible with "+mUri);
					}
				}
				initBootstrap();
				
				// 2. Use the cookie
				Msg msg = Msg.newBuilder()
					.setType(conType)
					.setThreadid(threadid)
					.setCookie(cliCookie(cookie))
					.setFlags(flags)
					.setIdentity(ProtobufConvs.toIdentity(mManager.getInfos())) // My identity
					.build();
				resp = sendRequestAndReadResponse(msg,timeout);
				if (resp.getStatus()==AbstractRemoteAndroidImpl.STATUS_REFUSE_ANONYMOUS)
				{
					close();
					((Droid2DroidManagerImpl)mManager).removeCookie(mUri.toString());
					throw new SecurityException("Refuse anonymous. Use FLAG_PROPOSE_PAIRING ?");
				}
				// Unpair
//FIXME				if (conType==Type.CONNECT_FOR_PAIRING && cookie==-1)
//					return true;
				// If invalide cookie, ask a new one and retry
				
				cookieAlive=resp.getStatus()!=AbstractRemoteAndroidImpl.STATUS_INVALIDE_COOKIE;
				if (!cookieAlive)
				{
					((Droid2DroidManagerImpl)mManager).removeCookie(mUri.toString());
					close();
				}
				if (srvCookie(cookie)!=srvCookie(resp.getCookie()))
				{
					((Droid2DroidManagerImpl)mManager).removeCookie(mUri.toString());
					close();
					throw new SecurityException("Unkwon remote server.");
				}
			} while (!cookieAlive);
			if (!refuse && resp.getStatus()==AbstractRemoteAndroidImpl.STATUS_REFUSE_ANONYMOUS)
			{
				refuse=true;
//FIXME				conType=org.droid2droid.internal.Messages.Type.CONNECT_FOR_PAIRING;
				((Droid2DroidManagerImpl)mManager).removeCookie(mUri.toString());
			}
			else
			{
				refuse=false;
			}
		} while (refuse);
			
		if (resp.getStatus()!=AbstractRemoteAndroidImpl.STATUS_OK)
		{
			((Droid2DroidManagerImpl)mManager).removeCookie(mUri.toString());
		}
		checkStatus(resp);
		if (resp.hasIdentity())
		{
			mInfo=ProtobufConvs.toRemoteAndroidInfo(mManager.getContext(),resp.getIdentity());
		}
		else
			mInfo=null;
		return true;
	}
	
    public static class ReadWait
    {
    	// TODO: Faire un recyclage des instances
    	public Lock lock=new ReentrantLock();
    	public Condition mCondition=lock.newCondition();
    	public Msg mResponse;
    	public Throwable mException;
    }
    
    public PublicKey getPeerPublicKey()
    {
    	return null;
    }
    public String  getPeerUUID()
    {
    	return null;
    }
}
