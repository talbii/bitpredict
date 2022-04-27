package com.talbii.bitpredict;

import java.util.Timer;
import java.util.TimerTask;

public class RecallableTimer {
    Timer t;
    boolean isCancelled;

    public RecallableTimer() {
        t = new Timer();
        isCancelled = false;
    }

    public void schedule(TimerTask task, long delay, long period) {
        if(isCancelled) t = new Timer();
        t.schedule(task, delay, period);
    }

    public void cancel() {
        t.cancel();
        isCancelled = true;
    }
}
