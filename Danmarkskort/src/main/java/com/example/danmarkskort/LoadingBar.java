package com.example.danmarkskort;


public class LoadingBar {
    private static LoadingBar loadingBarInstance;
    private double progress;
    private boolean isDone;

    LoadingBar() {
        this.progress = 0.0;
    }

    public static LoadingBar getInstance() {
        if (loadingBarInstance == null) {
            loadingBarInstance = new LoadingBar();
        }
        return loadingBarInstance;
    }

    public String getLoadingText() {
        if (progress == 0.0) {
            return "Loading nodes...";
        } else if (progress == 0.2) {
            return "Loading roads...";
        } else if (progress == 0.4) {
            return "Loading polygons...";
        } else if (progress == 0.6) {
            return "Loading tilegrid...";
        } else if (progress == 0.8) {
            return "drawing map...";
        } else if (progress == 1.0) {
            return "Done!";
        } return "Loading...";
    }
    public double getProgress() { return progress; }
    public void setProgress(double progress) {this.progress = progress; if (progress == 1.0) setDone();}
    public boolean isDone() { return isDone; }
    private void setDone() { isDone = true; }
}
