package com.sps.iCheker;

import java.io.IOException;

public class LoadTimer extends java.util.TimerTask {
    private Main main;

    public LoadTimer(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        try {
            main.pullComments();

        } catch (IOException e) {
            System.out.println("some Log: some error");
            e.printStackTrace();
        }
    }

}
