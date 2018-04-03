package com.hti.graduationproject.wificalling.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import com.hti.graduationproject.wificalling.activities.CallActivity;
import com.hti.graduationproject.wificalling.utils.Constants;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


//Todo (1) register receiver to receive commands from other app parts - done
//Todo (2) send signals to other mobiles - done
//Todo (3) send broadcast to other parts in application -done
//Todo (4) open signaling server -done
//Todo (5) Handle incoming signals -done
//Todo (6) send phone number broadcast - done

//Todo close service

/**
 * This call responsible for signaling server
 * (1) open server when start server
 * (2) receive network signaling messages and send broadcast to other app parts
 * (3) open CallActivity when there are incoming call
 * (4) receive broadcast from other app parts to send signaling it's action is Constants.Signaling.SIGNALING_SERVICE_ACTION
 */
public class SignalingService extends Service {

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private String mTag;
    SharedPreferences prefs;
    String devicePhoneNumber;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_MESSAGE);
            Log.i(mTag, String.format("There are new broadcast its message is: %s", msg));
            try {
                if (intent.getStringExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_IP_ADDRESS) != null) {
                    sendNetworkMessage(intent.getStringExtra(Constants.Signaling.SIGNALING_SERVICE_ACTION_IP_ADDRESS), msg);
                } else if (Constants.getDeviceIP() != null) {
                    String networkIP = Constants.getDeviceIP().substring(0, Constants.getDeviceIP().lastIndexOf("."));
                    networkIP += ".255";
                    sendNetworkMessage(networkIP, msg);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mTag = this.getClass().getSimpleName();
        Log.i(mTag, "Signaling Service Started");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Signaling.SIGNALING_SERVICE_ACTION);
        registerReceiver(receiver, filter);
        createBroadCastTimer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1,new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Create Signaling observable
        Observable<String> signalingServerObservable = Observable.create(observer -> {
            try {
                DatagramSocket serverSocket = new DatagramSocket(Constants.Signaling.SIGNALING_SERVER_PORT);
                Log.i(mTag, "Signaling server started!");
                while (true) {
                    byte[] receiveData = new byte[Constants.Signaling.MIN_SIGNALING_BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData,
                            receiveData.length);
                    serverSocket.receive(receivePacket);

                    String signal = new String(receivePacket.getData());
                    signal += "##";
                    signal += receivePacket.getAddress().getHostAddress();
                    observer.onNext(signal);
                }
            } catch (Exception ex) {
                observer.onError(ex);
            }
        });

        //Subscribe to observer at new thread and take results at main thread
        signalingServerObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    public void onNext(String incomeMessage) {
                        //Here we must send app broadcast with specific action like Constants.Signaling.CALL_INVITATION_SIGNAL_PARAM to notify any app component with the new request
                        Log.i(mTag, "New network message: " + incomeMessage.substring(0, 40));

                        String action;
                        if (incomeMessage.startsWith("_invite_")) {
                            //Incoming call -> open incoming call activity,
                            //send broadcast to other app components with USER_REGISTER_REQUEST_SIGNAL_PARAM action

                            String phoneNumber = incomeMessage.split("##")[1];//Signaling must be _invite_##phoneNumber##IP Address
                            Intent openCallActivity = new Intent(getBaseContext(), CallActivity.class);
                            openCallActivity.putExtra("PHONE_NUM", phoneNumber);
                            openCallActivity.putExtra("CALL_TYPE", "INCOMING");
                            openCallActivity.putExtra("CALL_TECH", "D2D");
                            openCallActivity.putExtra("PHONE_IP", incomeMessage.split("##")[2]);
                            openCallActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(openCallActivity);
                            action = Constants.Signaling.CALL_INVITATION_SIGNAL_PARAM;

                        } else if (incomeMessage.startsWith(Constants.Signaling.USER_REGISTER_REQUEST_SIGNAL_PARAM)) {
                            //Registration request -> send broadcast to other app components with USER_REGISTER_REQUEST_SIGNAL_PARAM action
                            action = Constants.Signaling.USER_REGISTER_REQUEST_SIGNAL_PARAM;
                            //Add user number to current users in cell
                            String[] msg = incomeMessage.split("##");
                            if (!msg[2].equals(Constants.getDeviceIP())) {
                                Constants.addNumber(msg[1].trim(), msg[2].trim());
                                prefs = getSharedPreferences(Constants.SharedPref.SHARED_PREF, MODE_PRIVATE);
                                devicePhoneNumber = prefs.getString(Constants.SharedPref.SHARED_PREF_PHONE_NUM, "SHARED_PREF_PHONE_NUM");
                                if (!devicePhoneNumber.equals(msg[1].trim()))
                                    Constants.addNearbyDevice(msg[1].trim());
                                Log.i(mTag, String.format("New number added: %s", msg[1]));
                            }


                        } else if (incomeMessage.startsWith(Constants.Signaling.END_CALL_SIGNAL_PARAM)) {
                            //Todo Implement end call callback
                            //End call-> send broadcast to other app components with END_CALL_SIGNAL_PARAM action
                            action = Constants.Signaling.END_CALL_SIGNAL_PARAM;
                        } else if (incomeMessage.startsWith("_accept_")) {
                            Intent i = new Intent();
                            i.setAction("CALL_ACCEPTED");
                            sendBroadcast(i);
                            return;
                        } else if (incomeMessage.startsWith("_end_")) {
                            Intent i = new Intent();
                            i.setAction("CALL_ENDED");
                            sendBroadcast(i);
                            return;
                        } else {
                            return;
                        }
                        sendAppBroadcast(action, incomeMessage);//Send application broadcast with the number
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void createBroadCastTimer() {
        Observable<Long> broadcastTimeObservable = Observable.interval(2, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        broadcastTimeObservable.subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(mTag, "Network broadcast timer started");
                mDisposable.add(d);
            }

            @Override
            public void onNext(Object o) {
                String ip = Constants.getDeviceIP();
                if (ip != null) {
                    String networkIP = ip.substring(0, ip.lastIndexOf("."));
                    networkIP += ".255";
                    Log.i(mTag, "New Broadcast to networkIP: " + networkIP);
                    prefs = getSharedPreferences(Constants.SharedPref.SHARED_PREF, MODE_PRIVATE);
                    devicePhoneNumber = prefs.getString(Constants.SharedPref.SHARED_PREF_PHONE_NUM, "SHARED_PREF_PHONE_NUM");
                    String msg = "_register_##" + devicePhoneNumber + "##" + ip;
                    try {
                        sendNetworkMessage(networkIP, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    void sendNetworkMessage(String ip, String msg) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.i(mTag, "Sending message to: " + ip);
        final InetAddress destination = InetAddress.getByName(ip);
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = new byte[1024];
        sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destination, Constants.Signaling.SIGNALING_SERVER_PORT);
        clientSocket.send(sendPacket);
        Log.i(mTag, "Message sent to recipient");
    }

    private void sendAppBroadcast(String action, String msg) {
        Intent i = new Intent();
        i.setAction(action);
        i.putExtra(Constants.Signaling.SIGNALING_MESSAGE, msg);
        sendBroadcast(i);
    }

}
