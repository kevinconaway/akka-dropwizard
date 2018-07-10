package com.github.kevinconaway.akka.metrics;

public interface MonitorableEnvelope {

    /**
     * @return The time that the envelope was enqueued in nanoseconds
     * @see System#nanoTime()
     */
    Long getEnqueueTime();

    /**
     * Set the enqueued time on this envelope
     *
     * @param enqueueTime Time in nanoseconds
     */
    void setEnqueueTime(Long enqueueTime);

}
