package com.hti.graduationproject.wificalling.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.hti.graduationproject.wificalling.utils.Constants;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//Todo (1) register broadcast receiver to receive commands from other app parts - done
//Todo (2) Initiate call server or call instance as required
//Todo (3) At end of call send report to server

public class CallService extends Service {

    private boolean mRunCallingServer = false;
    private boolean mRunCallingInstance = false;
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int minBufSize = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private static String mTag;
    private short x = 0, y = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mTag = this.getClass().getSimpleName();

        Log.i(mTag, "CAll Service Created");
        IntentFilter callServiceIntentFilter = new IntentFilter();
        callServiceIntentFilter.addAction(Constants.Calling.CALL_SERVICE_ACTION);
        registerReceiver(mCallServiceReceiver, callServiceIntentFilter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1,new Notification());
        }
    }

    private void startSpeaker() {
        Thread speakerThread = new Thread(() -> {
            //Instance for speaker
            AudioTrack atrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, Constants.Calling.SAMPLING_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
            //start speaker
            atrack.play();
            try {
                x = 1;
                Log.i(mTag, "Speaker Thread is Started");
                //Servers to receive from client
                DatagramSocket serverSocket = new DatagramSocket(Constants.Calling.CALLING_SERVER_PORT);
                //buffer to hold incoming sampled sound
                byte[] receiveData = new byte[minBufSize];
                //While call is running
                while (mRunCallingServer) {
                    //new instance for datagram recived packet
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, minBufSize);
                    //receive data from server socket and hold it in receivePacket
                    serverSocket.receive(receivePacket);
                    Log.i("Speaker", "Recived packet & its size is: " + receivePacket.getData().length);
                    //write recived data to speaker
                    atrack.write(receivePacket.getData(), 0, minBufSize);
                }
                serverSocket.disconnect();
                serverSocket.close();
                atrack.stop();
                atrack.flush();
                atrack.release();
                Log.w("Speaker", " is closed");
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        speakerThread.start();
    }

    private void startMic(String serverIP) {
        Thread micThread = new Thread(() -> {
            //Instance for microphone
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.Calling.SAMPLING_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecord.getMinBufferSize(Constants.Calling.SAMPLING_RATE,
                            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
            try {
                y = 1;
                Log.i(mTag, "Mic Thread is Started");
                //Address of destination call server
                final InetAddress destination = InetAddress.getByName(serverIP);
                //Servers to receive from client
                DatagramSocket socket = new DatagramSocket();
                //Let microphone start recording sound
                recorder.startRecording();
                //buffer to receive from microphone
                byte[] buffer = new byte[minBufSize];
                int bytes_read;
                //While call is running
                while (mRunCallingInstance) {
                    bytes_read = recorder.read(buffer, 0, minBufSize);
                    //putting buffer in the packet
                    DatagramPacket sendPacket = new DatagramPacket(buffer, bytes_read, destination, Constants.Calling.CALLING_SERVER_PORT);
                    //Send voice packet to destination
                    socket.send(sendPacket);
                    Log.i("Mic", "Sent data & its length is: " + sendPacket.getData().length);
                }
                socket.disconnect();
                socket.close();
                recorder.stop();
                recorder.release();
                Log.w("Mic", " is closed");
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        micThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mCallServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Todo Implement open call server action
            String ip = intent.getStringExtra(Constants.Calling.MAKE_CALL_ACTION_PARAM);
            Log.i(mTag, String.format("broadcast recieved and ip is: %s", ip));
            if (intent.getStringExtra("END") != null && intent.getStringExtra("END").equals("END")) {
                mRunCallingInstance = false;
                mRunCallingServer = false;
                Constants.callState = false;
                x = 0;
                y = 0;
                return;
            }
            if (ip == null && x == 0) {
                mRunCallingServer = true;
                mRunCallingInstance = true;
                startSpeaker();
            } else if (y == 0) {
                mRunCallingInstance = true;
                startMic(ip);
            }
            //Todo Implement open call instance action
        }
    };

}