package com.example.danmarkskort;


import com.example.danmarkskort.MVC.Model;

public class LoadingBar {
    private static LoadingBar loadingBarInstance;
    private int progress;

    LoadingBar() {
        this.progress = 0;
    }

    public static LoadingBar getInstance() {
        if (loadingBarInstance == null) {
            loadingBarInstance = new LoadingBar();
        }
        return loadingBarInstance;
    }

    public int getProgress() { return progress; }
    public void setProgress(int progress) {this.progress = progress; }

}
