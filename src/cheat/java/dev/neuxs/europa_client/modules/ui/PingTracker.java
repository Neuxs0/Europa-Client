package dev.neuxs.europa_client.modules.ui;

import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.singletons.GameSingletons;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PingTracker {
    private static final int DEFAULT_PORT = 47137;
    private static final int REMOTE_PROBE_TIMEOUT_MILLIS = 3_000;
    private static final long REMOTE_PROBE_INTERVAL_NANOS = 10_000_000_000L;
    private static final long REMOTE_STALE_AFTER_NANOS = 30_000_000_000L;
    private static final long LOCAL_STALE_AFTER_NANOS = 2_000_000_000L;
    private static final double NANOS_PER_MILLISECOND = 1_000_000.0;
    private static final ExecutorService REMOTE_PROBE_EXECUTOR = Executors.newSingleThreadExecutor(new PingThreadFactory());
    private static final AtomicBoolean remoteProbeInFlight = new AtomicBoolean(false);

    private static volatile String host;
    private static volatile int port = DEFAULT_PORT;
    private static volatile String textDisplayAddress;
    private static volatile String numericDisplayAddress;
    private static volatile long lastRemotePingSampleNanos = Long.MIN_VALUE;
    private static volatile long lastRemoteProbeNanos = Long.MIN_VALUE;
    private static volatile long localTickStartNanos = Long.MIN_VALUE;
    private static volatile long lastLocalPingSampleNanos = Long.MIN_VALUE;
    private static volatile long lastLocalPingMillis = -1L;
    private static volatile long lastPingMillis = -1L;

    private PingTracker() {
    }

    public static void setTarget(String address) {
        Target target = parseTarget(address);
        if (target.host == null) {
            clearTarget();
            return;
        }
        textDisplayAddress = formatTypedDisplayAddress(address, target);
        setTarget(target.host, target.port);
    }

    public static void setTarget(SocketAddress address) {
        if (address instanceof InetSocketAddress inetSocketAddress) {
            numericDisplayAddress = formatSocketDisplayAddress(inetSocketAddress);
            setTarget(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        }
    }

    public static void clearTarget() {
        host = null;
        port = DEFAULT_PORT;
        textDisplayAddress = null;
        numericDisplayAddress = null;
        lastRemotePingSampleNanos = Long.MIN_VALUE;
        lastRemoteProbeNanos = Long.MIN_VALUE;
        localTickStartNanos = Long.MIN_VALUE;
        lastLocalPingSampleNanos = Long.MIN_VALUE;
        lastLocalPingMillis = -1L;
        lastPingMillis = -1L;
    }

    public static String getConnectedServerDisplayAddress() {
        if (isLocalHostMode()) {
            return "Singleplayer";
        }

        refreshTargetFromConnectedClient();

        String typedAddress = textDisplayAddress;
        if (typedAddress != null && !typedAddress.isBlank()) {
            return typedAddress;
        }

        String numericAddress = numericDisplayAddress;
        if (numericAddress != null && !numericAddress.isBlank()) {
            return numericAddress;
        }

        String currentHost = host;
        if (currentHost == null || currentHost.isBlank()) {
            return null;
        }

        return formatHostAndPort(currentHost, port, true);
    }

    public static Snapshot getSnapshot() {
        Snapshot localSnapshot = getLocalSnapshot();
        if (localSnapshot.available()) {
            return localSnapshot;
        }

        refreshTargetFromConnectedClient();

        String currentHost = host;
        if (currentHost == null || currentHost.isBlank()) {
            return Snapshot.unavailable();
        }

        scheduleRemoteProbe(currentHost, port);

        long now = System.nanoTime();
        boolean available = lastPingMillis >= 0L
                && lastRemotePingSampleNanos != Long.MIN_VALUE
                && now - lastRemotePingSampleNanos <= REMOTE_STALE_AFTER_NANOS;
        return new Snapshot(lastPingMillis, currentHost, port, available, false);
    }

    public static void recordOutboundPacket() {
    }

    public static void recordOutboundPacket(SocketAddress address) {
        setTarget(address);
    }

    public static void recordInboundPacket(SocketAddress address) {
        setTarget(address);
    }

    public static void recordLocalServerTickStart() {
        if (!isLocalHostMode()) {
            localTickStartNanos = Long.MIN_VALUE;
            return;
        }

        localTickStartNanos = System.nanoTime();
    }

    public static void recordLocalServerTickEnd() {
        if (!isLocalHostMode()) {
            localTickStartNanos = Long.MIN_VALUE;
            return;
        }

        long tickStartNanos = localTickStartNanos;
        if (tickStartNanos == Long.MIN_VALUE) {
            return;
        }

        long now = System.nanoTime();
        long elapsedNanos = now - tickStartNanos;
        if (elapsedNanos < 0L || elapsedNanos > LOCAL_STALE_AFTER_NANOS) {
            localTickStartNanos = Long.MIN_VALUE;
            return;
        }

        lastLocalPingMillis = Math.max(0L, Math.round(elapsedNanos / NANOS_PER_MILLISECOND));
        lastLocalPingSampleNanos = now;
        localTickStartNanos = Long.MIN_VALUE;
    }

    private static void setTarget(String targetHost, int targetPort) {
        if (targetHost == null || targetHost.isBlank()) {
            return;
        }

        String normalizedHost = targetHost.toLowerCase(Locale.ROOT);
        int normalizedPort = targetPort > 0 ? targetPort : DEFAULT_PORT;
        if (normalizedHost.equals(host) && normalizedPort == port) {
            return;
        }

        host = normalizedHost;
        port = normalizedPort;
        lastRemotePingSampleNanos = Long.MIN_VALUE;
        lastRemoteProbeNanos = Long.MIN_VALUE;
        lastPingMillis = -1L;
    }

    private static void scheduleRemoteProbe(String targetHost, int targetPort) {
        long now = System.nanoTime();
        long lastProbeNanos = lastRemoteProbeNanos;
        if (lastProbeNanos != Long.MIN_VALUE && now - lastProbeNanos < REMOTE_PROBE_INTERVAL_NANOS) {
            return;
        }

        if (!remoteProbeInFlight.compareAndSet(false, true)) {
            return;
        }

        lastRemoteProbeNanos = now;
        REMOTE_PROBE_EXECUTOR.execute(() -> probeRemoteServer(targetHost, targetPort));
    }

    private static void probeRemoteServer(String targetHost, int targetPort) {
        long startNanos = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, targetPort), REMOTE_PROBE_TIMEOUT_MILLIS);
            long elapsedNanos = System.nanoTime() - startNanos;
            if (elapsedNanos >= 0L && targetHost.equals(host) && targetPort == port) {
                lastPingMillis = Math.max(0L, Math.round(elapsedNanos / NANOS_PER_MILLISECOND));
                lastRemotePingSampleNanos = System.nanoTime();
            }
        } catch (RuntimeException | java.io.IOException ignored) {
        } finally {
            remoteProbeInFlight.set(false);
        }
    }

    private static Snapshot getLocalSnapshot() {
        if (!isLocalHostMode()) {
            return Snapshot.unavailable();
        }

        long localPingMillis = lastLocalPingMillis;
        long localSampleNanos = lastLocalPingSampleNanos;
        if (localPingMillis < 0L || localSampleNanos == Long.MIN_VALUE) {
            return Snapshot.unavailable();
        }

        long now = System.nanoTime();
        long elapsedNanos = now - localSampleNanos;
        if (elapsedNanos < 0L || elapsedNanos > LOCAL_STALE_AFTER_NANOS) {
            return Snapshot.unavailable();
        }

        return new Snapshot(localPingMillis, "internal", 0, true, true);
    }

    private static boolean isLocalHostMode() {
        try {
            return GameSingletons.isHost() && !ClientNetworkManager.isConnected();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static void refreshTargetFromConnectedClient() {
        try {
            if (!ClientNetworkManager.isConnected()
                    || ClientNetworkManager.CLIENT.ctx == null
                    || ClientNetworkManager.CLIENT.ctx.channel() == null) {
                return;
            }

            setTarget(ClientNetworkManager.CLIENT.ctx.channel().remoteAddress());
        } catch (RuntimeException ignored) {
        }
    }

    private static Target parseTarget(String address) {
        if (address == null || address.isBlank()) {
            return new Target(null, DEFAULT_PORT);
        }

        String trimmed = address.trim();
        int parsedPort = DEFAULT_PORT;
        String parsedHost = trimmed;

        int separatorIndex = trimmed.lastIndexOf(':');
        boolean bracketedPort = trimmed.lastIndexOf(']') >= 0 && trimmed.lastIndexOf(']') < separatorIndex;
        boolean singleColonPort = trimmed.indexOf(':') == separatorIndex;
        if (separatorIndex > 0 && separatorIndex < trimmed.length() - 1 && (bracketedPort || singleColonPort)) {
            String rawPort = trimmed.substring(separatorIndex + 1);
            try {
                parsedPort = Integer.parseInt(rawPort);
                parsedHost = trimmed.substring(0, separatorIndex);
            } catch (NumberFormatException ignored) {
                parsedHost = trimmed;
            }
        }

        if (parsedHost.startsWith("[") && parsedHost.endsWith("]")) {
            parsedHost = parsedHost.substring(1, parsedHost.length() - 1);
        }

        return new Target(parsedHost, parsedPort);
    }

    private static String formatTypedDisplayAddress(String address, Target target) {
        if (address == null) {
            return null;
        }

        String trimmed = address.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        boolean hasExplicitPort = target.port != DEFAULT_PORT || typedAddressHasPort(trimmed);
        return formatHostAndPort(target.host, target.port, hasExplicitPort);
    }

    private static boolean typedAddressHasPort(String address) {
        int closingBracketIndex = address.lastIndexOf(']');
        int separatorIndex = address.lastIndexOf(':');
        if (separatorIndex <= 0 || separatorIndex >= address.length() - 1) {
            return false;
        }
        if (closingBracketIndex >= 0) {
            return closingBracketIndex < separatorIndex;
        }

        return address.indexOf(':') == separatorIndex;
    }

    private static String formatSocketDisplayAddress(InetSocketAddress address) {
        InetAddress inetAddress = address.getAddress();
        String addressText = inetAddress == null ? address.getHostString() : inetAddress.getHostAddress();
        return formatHostAndPort(addressText, address.getPort(), true);
    }

    private static String formatHostAndPort(String targetHost, int targetPort, boolean includePort) {
        if (targetHost == null || targetHost.isBlank()) {
            return null;
        }

        String displayHost = targetHost.trim();
        if (!includePort || targetPort <= 0) {
            return displayHost;
        }

        if (displayHost.indexOf(':') >= 0 && !(displayHost.startsWith("[") && displayHost.endsWith("]"))) {
            displayHost = "[" + displayHost + "]";
        }
        return displayHost + ":" + targetPort;
    }

    private record Target(String host, int port) {
    }

    public record Snapshot(long pingMillis, String host, int port, boolean available, boolean local) {
        private static Snapshot unavailable() {
            return new Snapshot(-1L, null, DEFAULT_PORT, false, false);
        }
    }

    private static final class PingThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Europa Client Ping Probe");
            thread.setDaemon(true);
            return thread;
        }
    }
}
