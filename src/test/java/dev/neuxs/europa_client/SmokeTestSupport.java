package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandManager;
import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.modules.cheats.Fly;
import dev.neuxs.europa_client.modules.cheats.JetpackHeight;
import dev.neuxs.europa_client.modules.cheats.LiquidWalk;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.cheats.Xray;
import dev.neuxs.europa_client.modules.utils.Freecam;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.NoFog;
import dev.neuxs.europa_client.modules.utils.PacketInspector;
import dev.neuxs.europa_client.modules.utils.Zoom;
import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.variant.ClientVariant;
import dev.neuxs.europa_client.variant.CheatVariant;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.savelib.IByteArray;
import finalforeach.cosmicreach.savelib.utils.DynamicArrays;
import finalforeach.cosmicreach.savelib.utils.IDynamicArray;
import finalforeach.cosmicreach.savelib.utils.IDynamicArrayInstantiator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SmokeTestSupport {
    private SmokeTestSupport() {
    }

    public static FakeChat resetForVariant(ClientVariant variant) {
        SettingsManager.setAutoSaveEnabled(false);
        DynamicArrays.instantiator = new TestDynamicArrayInstantiator();
        ClientSettings.resetAll();
        ClientSettings.COMMAND_PREFIX.setValue(".");
        FakeChat chat = new FakeChat();
        Client.clientChat = chat;

        setStatic(Client.class, "variant", variant);
        setStatic(Client.class, "initialized", true);
        setStatic(Client.class, "attemptedLazyVariantInit", true);

        clearCommandState();
        setStatic(Modules.class, "initialized", false);
        setStatic(Modules.class, "initializedVariantType", null);
        registerBehaviorModules(variant);
        ClientCommandRegistry.registerClientCommands();
        return chat;
    }

    private static void registerBehaviorModules(ClientVariant variant) {
        Modules.moduleList.clear();
        Modules.utilModuleList.clear();
        Modules.uiModuleList.clear();
        CheatModules.cheatModuleList.clear();

        int unbound = -1;
        Modules.registerModule(Modules.fullbright = new Fullbright(unbound, false), Modules.utilModuleList);
        Modules.registerModule(Modules.noFog = new NoFog(unbound, false), Modules.utilModuleList);
        Modules.registerModule(Modules.packetInspector = new PacketInspector(unbound, false), Modules.utilModuleList);
        Modules.registerModule(Modules.zoom = new Zoom(unbound, false), Modules.utilModuleList);
        Modules.registerModule(Modules.freecam = new Freecam(unbound, false), Modules.utilModuleList);

        if (variant instanceof CheatVariant) {
            Modules.registerModule(CheatModules.noClip = new NoClip(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.fly = new Fly(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.speed = new Speed(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.reach = new Reach(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.xray = new Xray(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.liquidWalk = new LiquidWalk(unbound, false), CheatModules.cheatModuleList);
            Modules.registerModule(CheatModules.jetpackHeight = new JetpackHeight(unbound, false), CheatModules.cheatModuleList);
        } else {
            CheatModules.noClip = null;
            CheatModules.fly = null;
            CheatModules.speed = null;
            CheatModules.reach = null;
            CheatModules.xray = null;
            CheatModules.liquidWalk = null;
            CheatModules.jetpackHeight = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static void clearCommandState() {
        ((Map<String, ?>) getStatic(ClientCommandManager.class, "COMMANDS")).clear();
        ((Map<String, ?>) getStatic(ClientCommandManager.class, "ALIASES")).clear();
        setStatic(ClientCommandRegistry.class, "registered", false);
    }

    private static Object getStatic(Class<?> owner, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to read " + owner.getName() + "." + fieldName, e);
        }
    }

    private static void setStatic(Class<?> owner, String fieldName, Object value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to set " + owner.getName() + "." + fieldName, e);
        }
    }

    public static final class FakeChat implements IChat {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void addMessage(Account account, String message) {
            messages.add(message);
        }

        public List<String> messages() {
            return messages;
        }

        public String lastMessage() {
            return messages.isEmpty() ? "" : messages.get(messages.size() - 1);
        }
    }

    private static final class TestDynamicArrayInstantiator implements IDynamicArrayInstantiator {
        @Override
        public <E> IDynamicArray<E> create(Class<E> itemClass) {
            return new TestDynamicArray<>(itemClass);
        }

        @Override
        public <E> IDynamicArray<E> create(Class<E> itemClass, int capacity) {
            return new TestDynamicArray<>(itemClass);
        }

        @Override
        public IByteArray createByteArray() {
            return new TestByteArray();
        }
    }

    private static final class TestDynamicArray<E> implements IDynamicArray<E> {
        private final Class<E> itemClass;
        private final List<E> values = new ArrayList<>();

        private TestDynamicArray(Class<E> itemClass) {
            this.itemClass = itemClass;
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public void add(E value) {
            values.add(value);
        }

        @Override
        public E get(int index) {
            return values.get(index);
        }

        @Override
        public boolean contains(E value, boolean identity) {
            return indexOf(value, identity) >= 0;
        }

        @Override
        public int indexOf(E value, boolean identity) {
            for (int i = 0; i < values.size(); i++) {
                E current = values.get(i);
                if (identity ? current == value : java.util.Objects.equals(current, value)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E[] items() {
            return values.toArray((E[]) Array.newInstance(itemClass, values.size()));
        }

        @Override
        public void clear() {
            values.clear();
        }

        @Override
        public E removeIndex(int index) {
            return values.remove(index);
        }

        @Override
        public void truncate(int newSize) {
            while (values.size() > newSize) {
                values.remove(values.size() - 1);
            }
        }

        @Override
        public Iterator<E> iterator() {
            return values.iterator();
        }
    }

    private static final class TestByteArray implements IByteArray {
        private final List<Byte> values = new ArrayList<>();

        @Override
        public byte[] toArray() {
            return items();
        }

        @Override
        public void addAll(byte... bytes) {
            for (byte value : bytes) {
                values.add(value);
            }
        }

        @Override
        public void set(int index, byte value) {
            values.set(index, value);
        }

        @Override
        public void add(byte value) {
            values.add(value);
        }

        @Override
        public void addAll(IByteArray other) {
            addAll(other.toArray());
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public byte[] items() {
            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < values.size(); i++) {
                bytes[i] = values.get(i);
            }
            return bytes;
        }
    }
}
