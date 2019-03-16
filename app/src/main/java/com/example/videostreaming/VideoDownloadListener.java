package com.example.videostreaming;

public interface VideoDownloadListener {
    public void onVideoDownloaded();
    public void onVideoDownloadError(Exception e);
}
