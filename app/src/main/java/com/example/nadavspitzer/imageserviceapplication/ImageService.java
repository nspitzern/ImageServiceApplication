package com.example.nadavspitzer.imageserviceapplication;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ImageService extends Service {

    // members
    private boolean isRunning;
    private boolean isTransfering;
    private Communication communication;
    private BroadcastReceiver broadcastReceiver;
    private byte[] imgByte;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*****
     * the function creates the service, and initialize the running state to true
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.isRunning = false;
        this.isTransfering = false;
        this.communication = new Communication();
    }

    /*********
     * the function is linked to the start button, and starts the service
     * @param intent the intent of start service
     * @param flag a flag
     * @param startId a startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        if (!this.isRunning) {
            // view toast message
            Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();
            // the service is nw running
            this.isRunning = true;
            establishWIFI();
        }
        return START_STICKY;
    }

    /*******
     * the function is registered to the wifi state. if connected starts the send of the pictures.
     */
    private void establishWIFI() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(wifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        // get the different network state
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            startTransfer();
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.broadcastReceiver, intentFilter);
    }

    /********
     * the function is lined to the stop button. it stops the activity of the communication and
     * the service
     */
    public void onDestroy() {
        // the service is not running now
        this.isRunning = false;
        unregisterReceiver(this.broadcastReceiver);
        // stop the communication
        this.communication.Stop();
        // view a toast message
        Toast.makeText(this, "Service ended", Toast.LENGTH_SHORT).show();
    }

    /******
     * the function starts the communication and sends the pictures
     */
    private void startTransfer() {
        // start connection
        new Thread(new Runnable() {
            @Override
            public void run() {
                communication.Start();
                if (!isTransfering) {
                    isTransfering = true;
                    // send the pictures
                    SendPictures();
                }
            }
        }).start();
    }

    /*********
     * the function goes over the items in the given directory and returns a list of the files.
     * if an item is a directory it gets the files inside in recursion.
     * @param currentDir the current directory
     * @return a list of files.
     */
    private List<File> GetPicturesFromPhone(File currentDir) {
        List<File> list = new ArrayList<>();
        try {
            for (File current : currentDir.listFiles()) {
                if (current.isDirectory()) {
                    list.addAll(GetPicturesFromPhone(current));
                } else {
                    list.add(current);
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /********
     * the function gets all the pictures in the dcim directory and send them to the server
     */
    private void SendPictures() {
        // getting the camera folder
        File dcim = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        // get the files in the dcim folders
        List<File> itemsList = GetPicturesFromPhone(dcim);
        // convert to array
        final File[] pics = new File[itemsList.size()];
        itemsList.toArray(pics);
        // convert each picture to bytes array and send it.
        if (pics != null) {
            new Thread(new NotificationDisp(pics, communication, this)).start();
        }
    }

    /********
     * the function converts a Bitmap to an array of bytes
     * @param bitmap a bitmap
     * @return an array of bytes
     */
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        // open byte output stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // compress the bitmape
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }


    /********
     * the main class that shows the notification bar and sends each picture by length, name and
     * picture
     */
    // runnable class
    class NotificationDisp implements Runnable {
        // members
        File[] pics;
        Communication communication;
        Context context;

        //constructor
        NotificationDisp(File[] p, Communication com, Context context) {
            this.pics = p;
            this.communication = com;
            this.context = context;
        }

        public void run() {
            final int notification_id = 1;
            final NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                    .from(this.context);
            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this.context, "default");
            // set icon of the notification
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            // set title of the notification
            builder.setContentTitle("Pictures Transfer");
            builder.setContentText("Transfer in Progress...")
                    .setPriority(NotificationCompat.PRIORITY_LOW);
            // get number of pics
            int picsCount = this.pics.length, counter = 0;
            for (File pic : pics) {
                try {
                    // update progress bar
                    builder.setProgress(100, ((counter * 100) / picsCount), false);
                    builder.setContentText("Photo " + counter + "/" + picsCount + " Downloaded.");
                    notificationManager.notify(notification_id, builder.build());
                    // start file input stream
                    FileInputStream fileInputStream = new FileInputStream(pic);
                    Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                    // convert pic to array of bytes
                    imgByte = getBytesFromBitmap(bitmap);

                    // get the pic size
                    String toSend = imgByte.length + "\n";
                    communication.Send(toSend.getBytes());
                    //Thread.sleep(100);

                    // send name of file
                    toSend = pic.getName() + "\n";
                    communication.Send(toSend.getBytes());
                    //Thread.sleep(100);

                    //send the picture
                    communication.Send(imgByte);
                    //Thread.sleep(300);
                    counter++;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } /*catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
            // at the end
            builder.setContentText("Download Completed").setProgress(0, 0, false);
            notificationManager.notify(notification_id, builder.build());
            try {
                String toSend = "End\n";
                communication.Send(toSend.getBytes());
            } catch (Exception e) {
                Log.e("TCP", "S: Error: ", e);
            }
            isTransfering = false;
        }
    }
}