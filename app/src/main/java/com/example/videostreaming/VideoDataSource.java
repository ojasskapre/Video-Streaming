package com.example.videostreaming;

import android.media.MediaDataSource;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

@RequiresApi(api = Build.VERSION_CODES.M)
public class VideoDataSource extends MediaDataSource {
    final int SIZE = 20*1024000;
    private volatile static int pause_position = 0;
    private volatile byte[] videoBuffer = new byte[SIZE];
    private static final String TAG = "buffer";
    private volatile VideoDownloadListener listener;
    private volatile  boolean isDownloading;
    private volatile long curr_len = 0;
    private int threshold = 409600;
    static volatile long mediaplayer_position = -1;
    Runnable downloadVideoRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                Socket socket = SocketHandler.getSocket();
                InputStream inputStream = socket.getInputStream();
                //For appending incoming bytes
                int count = 0;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int read = 0;
                int x = 2048000;
                boolean flag = true;
                while (read != -1){ //While there is more data
                    //Read in bytes to data buffer
                    read = inputStream.read();
                    count ++;
                    //Write to output stream
                    byteArrayOutputStream.write(read);

                    if (count > x || read == -1){
                        x = 102400;
                        Log.e("FILE_READ", "READing from output stream");
                        byteArrayOutputStream.flush();
                        byte[] temp = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.reset();
                        System.arraycopy(temp, 0, videoBuffer, (int)curr_len % SIZE, temp.length);
                        curr_len += temp.length;
                        Log.d("buffer", "flushed data "+curr_len);
                        if (flag) {
                            flag = false;
                            listener.onVideoDownloaded();
                        }
                        count = 0;
                    }
                }

                Log.e(TAG, "Input stream read complete");
                inputStream.close();

                //Flush and set buffer.
                byteArrayOutputStream.flush();
                videoBuffer = byteArrayOutputStream.toByteArray();

                byteArrayOutputStream.close();
//                listener.onVideoDownloaded();
            }catch (Exception e){
                listener.onVideoDownloadError(e);
            }finally {
                isDownloading = false;
            }
        }
    };
    private volatile boolean pause_thread_start = true;

    public VideoDataSource(){
        isDownloading = false;
    }

    public void downloadVideo(VideoDownloadListener videoDownloadListener){
        if(isDownloading)
            return;
        listener = videoDownloadListener;
        Thread downloadThread = new Thread(downloadVideoRunnable);
        downloadThread.start();
        isDownloading = true;
    }

    Thread check_buffer = new Thread(new Runnable() {
        @Override
        public void run() {
            while(mediaplayer_position + threshold > curr_len && mediaplayer_position != -1){
                Log.e("buffer", mediaplayer_position + " " + curr_len);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            StreamingActivity.mp.seekTo(pause_position);
            StreamingActivity.mp.start();
            pause_thread_start = true;
            Log.e("buffer", "Starting player again");
        }
    });

    @Override
    public synchronized int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        synchronized (videoBuffer){
            int length = videoBuffer.length;
            Log.d("buffer", "position: "+position);
            Log.d("buffer", "size: "+size);

//            if(position > curr_len){
//                pause_position = StreamingActivity.mp.getCurrentPosition();
//                mediaplayer_position = position;
//                StreamingActivity.mp.pause();
//                Log.e("buffer", "Paused due to buffering");
//                if(pause_thread_start){
//                    pause_thread_start = false;
//                    check_buffer.start();
//                }
//
////                try {
////                    check_buffer.join();
//////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }

            if(position + size >SIZE){
                Log.e(TAG, "end reached,reset media player");
                StreamingActivity.mp.seekTo(0);
            }
            position %= SIZE;
            if (position >= length) {
                Log.d("buffer", "buffer end, position "+position+" len"+length);
                return -1; // -1 indicates EOF
            }
            if (position + size > length) {
                size -= (position + size) - length;
            }
            System.arraycopy(videoBuffer, (int)position, buffer, offset, size);
            return size;
        }
    }

    @Override
    public synchronized long getSize() throws IOException {
        return -1;
    }

    @Override
    public synchronized void close() throws IOException {

    }
}
