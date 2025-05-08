package com.example.danmarkskort;


public class LoadingBar {
    private static LoadingBar loadingBarInstance;
    private double progress;

    LoadingBar() {
        this.progress = 0.0;
    }

    /**
     * Method to get the singleton LoadingBar
     * @return LoadingBar (Singleton)
     */
    public static LoadingBar getInstance() {
        if (loadingBarInstance == null) {
            loadingBarInstance = new LoadingBar();
        }
        return loadingBarInstance;
    }

    /**
     * Returns a status message based on the current progress of the loading process.
     * The progress value is checked against specific predefined milestones to determine
     * the appropriate message to return.
     *
     * @return A String representing the loading status.
     */
    public String getLoadingText() {
        if (progress <= 0.0) {
            return "Loading nodes";
        } else if (progress <= 0.2) {
            return "Loading roads";
        } else if (progress <= 0.4) {
            return "Loading polygons";
        } else if (progress <= 0.6) {
            return "Loading tilegrid";
        } else if (progress <= 0.8) {
            return "Drawing map";
        } else if (progress <= 1.0) {
            return "Done!";
        } return "Loading";
    }
    public double getProgress() { return progress; }
    public void setProgress(double progress) {this.progress = progress;}
}
