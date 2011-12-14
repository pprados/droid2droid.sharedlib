/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/pprados/workspace.remoteandroid/remote-android-sharedlib/src/org/remoteandroid/internal/IRemoteAndroidManager.aidl
 */
package org.remoteandroid.internal;
public interface IRemoteAndroidManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.remoteandroid.internal.IRemoteAndroidManager
{
private static final java.lang.String DESCRIPTOR = "org.remoteandroid.internal.IRemoteAndroidManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.remoteandroid.internal.IRemoteAndroidManager interface,
 * generating a proxy if needed.
 */
public static org.remoteandroid.internal.IRemoteAndroidManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.remoteandroid.internal.IRemoteAndroidManager))) {
return ((org.remoteandroid.internal.IRemoteAndroidManager)iin);
}
return new org.remoteandroid.internal.IRemoteAndroidManager.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getInfo:
{
data.enforceInterface(DESCRIPTOR);
org.remoteandroid.internal.RemoteAndroidInfoImpl _result = this.getInfo();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_startDiscover:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
this.startDiscover(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelDiscover:
{
data.enforceInterface(DESCRIPTOR);
this.cancelDiscover();
reply.writeNoException();
return true;
}
case TRANSACTION_isDiscovering:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isDiscovering();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getBoundedDevices:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<org.remoteandroid.internal.RemoteAndroidInfoImpl> _result = this.getBoundedDevices();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getCookie:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _result = this.getCookie(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_removeCookie:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.removeCookie(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setLog:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.setLog(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.remoteandroid.internal.IRemoteAndroidManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public org.remoteandroid.internal.RemoteAndroidInfoImpl getInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.remoteandroid.internal.RemoteAndroidInfoImpl _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.remoteandroid.internal.RemoteAndroidInfoImpl.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void startDiscover(int flags, long timeToDiscover) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(flags);
_data.writeLong(timeToDiscover);
mRemote.transact(Stub.TRANSACTION_startDiscover, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void cancelDiscover() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelDiscover, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isDiscovering() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isDiscovering, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.util.List<org.remoteandroid.internal.RemoteAndroidInfoImpl> getBoundedDevices() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<org.remoteandroid.internal.RemoteAndroidInfoImpl> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBoundedDevices, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(org.remoteandroid.internal.RemoteAndroidInfoImpl.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long getCookie(java.lang.String uri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(uri);
mRemote.transact(Stub.TRANSACTION_getCookie, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void removeCookie(java.lang.String uri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(uri);
mRemote.transact(Stub.TRANSACTION_removeCookie, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setLog(int type, boolean state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeInt(((state)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startDiscover = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_cancelDiscover = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isDiscovering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getBoundedDevices = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getCookie = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_removeCookie = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
}
public org.remoteandroid.internal.RemoteAndroidInfoImpl getInfo() throws android.os.RemoteException;
public void startDiscover(int flags, long timeToDiscover) throws android.os.RemoteException;
public void cancelDiscover() throws android.os.RemoteException;
public boolean isDiscovering() throws android.os.RemoteException;
public java.util.List<org.remoteandroid.internal.RemoteAndroidInfoImpl> getBoundedDevices() throws android.os.RemoteException;
public long getCookie(java.lang.String uri) throws android.os.RemoteException;
public void removeCookie(java.lang.String uri) throws android.os.RemoteException;
public void setLog(int type, boolean state) throws android.os.RemoteException;
}
