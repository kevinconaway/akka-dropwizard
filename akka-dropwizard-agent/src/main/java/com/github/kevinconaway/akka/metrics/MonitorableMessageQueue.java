package com.github.kevinconaway.akka.metrics;

import com.codahale.metrics.Timer;

public interface MonitorableMessageQueue {

    String getMetricPrefix();

    void setMetricPrefix(String prefix);

    Timer getWaitTimer();

    void setWaitTimer(Timer timer);

}
