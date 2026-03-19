package com.jef.justenoughfakepixel.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class JefEventRegistrar {

    private static final String BASE_PACKAGE = "com.jef.justenoughfakepixel";

    private static final Reflections REFS = new Reflections(new ConfigurationBuilder()
            .forPackages(BASE_PACKAGE)
            .addScanners(new TypeAnnotationsScanner(), new FieldAnnotationsScanner()));

    public static void registerAll() {
        registerEvents();
        registerCommands();
        registerKeybinds();
    }

    private static void registerEvents() {
        for (Class<?> clazz : REFS.getTypesAnnotatedWith(RegisterEvents.class)) {
            try {
                MinecraftForge.EVENT_BUS.register(newInstance(clazz));
            } catch (Throwable t) {
                System.err.println("[JEF] Failed to register events for " + clazz.getName());
                t.printStackTrace();
            }
        }

        Set<Field> fields = REFS.getFieldsAnnotatedWith(RegisterInstance.class);
        for (Field field : fields) {
            try {
                if (!Modifier.isStatic(field.getModifiers())) {
                    System.err.println("[JEF] @RegisterInstance field must be static: " + field.getDeclaringClass().getName() + "." + field.getName());
                    continue;
                }
                field.setAccessible(true);
                Object instance = field.get(null);
                if (instance == null) {
                    System.err.println("[JEF] @RegisterInstance field is null (not yet initialized): " + field.getName());
                    continue;
                }
                if (instance instanceof ICommand) {
                    ClientCommandHandler.instance.registerCommand((ICommand) instance);
                } else {
                    MinecraftForge.EVENT_BUS.register(instance);
                }
            } catch (Throwable t) {
                System.err.println("[JEF] Failed to register instance field: " + field.getName());
                t.printStackTrace();
            }
        }
    }

    private static void registerCommands() {
        for (Class<?> clazz : REFS.getTypesAnnotatedWith(RegisterCommand.class)) {
            try {
                if (!ICommand.class.isAssignableFrom(clazz)) {
                    System.err.println("[JEF] @RegisterEvents class does not implement ICommand: " + clazz.getName());
                    continue;
                }
                ClientCommandHandler.instance.registerCommand((ICommand) newInstance(clazz));
            } catch (Throwable t) {
                System.err.println("[JEF] Failed to register command: " + clazz.getName());
                t.printStackTrace();
            }
        }
    }

    private static void registerKeybinds() {
        for (Field field : REFS.getFieldsAnnotatedWith(RegisterKeybind.class)) {
            try {
                if (!Modifier.isStatic(field.getModifiers())) {
                    System.err.println("[JEF] @RegisterKeybind field must be static: " + field.getDeclaringClass().getName() + "." + field.getName());
                    continue;
                }
                if (!KeyBinding.class.isAssignableFrom(field.getType())) {
                    System.err.println("[JEF] @RegisterKeybind field is not a KeyBinding: " + field.getName());
                    continue;
                }
                field.setAccessible(true);
                KeyBinding key = (KeyBinding) field.get(null);
                if (key == null) {
                    System.err.println("[JEF] @RegisterKeybind field is null: " + field.getName());
                    continue;
                }
                ClientRegistry.registerKeyBinding(key);
            } catch (Throwable t) {
                System.err.println("[JEF] Failed to register keybind: " + field.getName());
                t.printStackTrace();
            }
        }
    }

    private static Object newInstance(Class<?> clazz) throws Exception {
        Constructor<?> c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }
}