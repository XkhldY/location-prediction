/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\workspace\\Location Prediction\\libs\\com\\google\\android\\lib\\services\\IRouteRecordingService.aidl
 */
package com.google.android.lib.services;
public interface IRouteRecordingService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.google.android.lib.services.IRouteRecordingService
{
private static final java.lang.String DESCRIPTOR = "com.google.android.lib.services.IRouteRecordingService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.google.android.lib.services.IRouteRecordingService interface,
 * generating a proxy if needed.
 */
public static com.google.android.lib.services.IRouteRecordingService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.google.android.lib.services.IRouteRecordingService))) {
return ((com.google.android.lib.services.IRouteRecordingService)iin);
}
return new com.google.android.lib.services.IRouteRecordingService.Stub.Proxy(obj);
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
case TRANSACTION_isRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_insertEndPointWithId:
{
data.enforceInterface(DESCRIPTOR);
com.google.android.lib.content.CreateTrack _arg0;
if ((0!=data.readInt())) {
_arg0 = com.google.android.lib.content.CreateTrack.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
long _result = this.insertEndPointWithId(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_startNewRouteId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.startNewRouteId();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getRecordingRouteId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getRecordingRouteId();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_recordLocation:
{
data.enforceInterface(DESCRIPTOR);
this.recordLocation();
reply.writeNoException();
return true;
}
case TRANSACTION_calculateStatistics:
{
data.enforceInterface(DESCRIPTOR);
this.calculateStatistics();
reply.writeNoException();
return true;
}
case TRANSACTION_existEndPointAtId:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.existEndPointAtId();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_endCurrentRoute:
{
data.enforceInterface(DESCRIPTOR);
this.endCurrentRoute();
reply.writeNoException();
return true;
}
case TRANSACTION_getSensorData:
{
data.enforceInterface(DESCRIPTOR);
byte[] _result = this.getSensorData();
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
case TRANSACTION_getSensorState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSensorState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.google.android.lib.services.IRouteRecordingService
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
/**
 *check if it's recording or not.
 */
public boolean isRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
  * insert an endpoint and returns 
  *@params id of the new endpoint
  */
public long insertEndPointWithId(com.google.android.lib.content.CreateTrack track) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((track!=null)) {
_data.writeInt(1);
track.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_insertEndPointWithId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
  * start a new route returning
  *@prams id of the route
  *
  */
public long startNewRouteId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startNewRouteId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
  * returns the id of the route
  *being recorded
  *@params id of the current recording route
  */
public long getRecordingRouteId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRecordingRouteId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
  *while recording records a new locating
  *
  */
public void recordLocation() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_recordLocation, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/** calculate statistics
  * for every point and route
  */
public void calculateStatistics() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_calculateStatistics, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/** check fi a current point already exists in the database
  *
  */
public boolean existEndPointAtId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_existEndPointAtId, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**stop recording
  *
  */
public void endCurrentRoute() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_endCurrentRoute, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public byte[] getSensorData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSensorData, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getSensorState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSensorState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_isRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_insertEndPointWithId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_startNewRouteId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getRecordingRouteId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_recordLocation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_calculateStatistics = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_existEndPointAtId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_endCurrentRoute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getSensorData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getSensorState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
/**
 *check if it's recording or not.
 */
public boolean isRecording() throws android.os.RemoteException;
/**
  * insert an endpoint and returns 
  *@params id of the new endpoint
  */
public long insertEndPointWithId(com.google.android.lib.content.CreateTrack track) throws android.os.RemoteException;
/**
  * start a new route returning
  *@prams id of the route
  *
  */
public long startNewRouteId() throws android.os.RemoteException;
/**
  * returns the id of the route
  *being recorded
  *@params id of the current recording route
  */
public long getRecordingRouteId() throws android.os.RemoteException;
/**
  *while recording records a new locating
  *
  */
public void recordLocation() throws android.os.RemoteException;
/** calculate statistics
  * for every point and route
  */
public void calculateStatistics() throws android.os.RemoteException;
/** check fi a current point already exists in the database
  *
  */
public boolean existEndPointAtId() throws android.os.RemoteException;
/**stop recording
  *
  */
public void endCurrentRoute() throws android.os.RemoteException;
public byte[] getSensorData() throws android.os.RemoteException;
public int getSensorState() throws android.os.RemoteException;
}
