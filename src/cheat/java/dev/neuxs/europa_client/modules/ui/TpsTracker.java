package dev.neuxs.europa_client.modules.ui;

import finalforeach.cosmicreach.singletons.GameSingletons;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TpsTracker {
    private static final int MAX_SAMPLES = 100;
    private static final long STALE_AFTER_NANOS = 3_000_000_000L;
    private static final double NANOS_PER_MILLISECOND = 1_000_000.0;
    private static final double MILLIS_PER_SECOND = 1000.0;

    private static final Deque<TickSample> localSamples = new ArrayDeque<>();
    private static final Deque<TickSample> remoteSamples = new ArrayDeque<>();

    private static long localTickStartNanos = -1L;
    private static long lastLocalTickStartNanos = -1L;
    private static long lastLocalSampleNanos = -1L;
    private static long lastRemoteTickNanos = -1L;
    private static long lastRemoteWorldTick = Long.MIN_VALUE;
    private static long lastRemoteSampleNanos = -1L;

    private TpsTracker() {
    }

    public static synchronized void recordLocalTickStart() {
        localTickStartNanos = System.nanoTime();
    }

    public static synchronized void recordLocalTickEnd() {
        if (localTickStartNanos < 0L) {
            return;
        }

        long now = System.nanoTime();
        double tickDurationMillis = (now - localTickStartNanos) / NANOS_PER_MILLISECOND;
        if (lastLocalTickStartNanos >= 0L) {
            double tickIntervalMillis = (localTickStartNanos - lastLocalTickStartNanos) / NANOS_PER_MILLISECOND;
            addSample(localSamples, tickIntervalMillis, tickDurationMillis);
            lastLocalSampleNanos = now;
        }

        lastLocalTickStartNanos = localTickStartNanos;
        localTickStartNanos = -1L;
    }

    public static synchronized void recordRemoteTick(long worldTick) {
        long now = System.nanoTime();
        if (lastRemoteTickNanos >= 0L && worldTick > lastRemoteWorldTick) {
            long tickDelta = Math.max(1L, worldTick - lastRemoteWorldTick);
            double tickIntervalMillis = ((now - lastRemoteTickNanos) / NANOS_PER_MILLISECOND) / tickDelta;
            addSample(remoteSamples, tickIntervalMillis, tickIntervalMillis);
            lastRemoteSampleNanos = now;
        }

        lastRemoteTickNanos = now;
        lastRemoteWorldTick = worldTick;
    }

    public static synchronized Snapshot getSnapshot() {
        long now = System.nanoTime();
        boolean remoteFresh = lastRemoteSampleNanos >= 0L && now - lastRemoteSampleNanos <= STALE_AFTER_NANOS;
        boolean localFresh = lastLocalSampleNanos >= 0L && now - lastLocalSampleNanos <= STALE_AFTER_NANOS;

        if (remoteFresh && !GameSingletons.isHost()) {
            return buildSnapshot(remoteSamples, true);
        }
        if (localFresh) {
            return buildSnapshot(localSamples, false);
        }
        if (remoteFresh) {
            return buildSnapshot(remoteSamples, true);
        }

        return Snapshot.unavailable();
    }

    private static Snapshot buildSnapshot(Deque<TickSample> samples, boolean remote) {
        if (samples.isEmpty()) {
            return Snapshot.unavailable();
        }

        double totalIntervalMillis = 0.0;
        double totalDurationMillis = 0.0;
        for (TickSample sample : samples) {
            totalIntervalMillis += sample.intervalMillis;
            totalDurationMillis += sample.durationMillis;
        }

        double averageIntervalMillis = totalIntervalMillis / samples.size();
        double averageDurationMillis = totalDurationMillis / samples.size();
        double tps = averageIntervalMillis <= 0.0 ? 0.0 : MILLIS_PER_SECOND / averageIntervalMillis;
        return new Snapshot(tps, averageDurationMillis, remote, true);
    }

    private static void addSample(Deque<TickSample> samples, double intervalMillis, double durationMillis) {
        if (!Double.isFinite(intervalMillis) || intervalMillis <= 0.0
                || !Double.isFinite(durationMillis) || durationMillis < 0.0) {
            return;
        }

        samples.addLast(new TickSample(intervalMillis, durationMillis));
        while (samples.size() > MAX_SAMPLES) {
            samples.removeFirst();
        }
    }

    private record TickSample(double intervalMillis, double durationMillis) {
    }

    public record Snapshot(double tps, double mspt, boolean remoteEstimate, boolean available) {
        private static Snapshot unavailable() {
            return new Snapshot(0.0, 0.0, false, false);
        }
    }
}
