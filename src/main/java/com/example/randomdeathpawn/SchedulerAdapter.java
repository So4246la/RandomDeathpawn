package com.example.randomdeathpawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to abstract scheduling between Folia and the traditional Bukkit scheduler.
 */
public class SchedulerAdapter {
    private final JavaPlugin plugin;
    private final boolean folia;
    private Object globalScheduler;
    private Method executeMethod;
    private Method runDelayedMethod;
    private Method runAtFixedRateMethod;

    public SchedulerAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
        boolean detected = false;
        try {
            Method getter = Bukkit.class.getMethod("getGlobalRegionScheduler");
            globalScheduler = getter.invoke(Bukkit.class);
            if (globalScheduler != null) {
                Class<?> schedulerClass = globalScheduler.getClass();
                executeMethod = schedulerClass.getMethod("execute", Plugin.class, Runnable.class);
                runDelayedMethod = schedulerClass.getMethod("runDelayed", Plugin.class, Runnable.class, long.class);
                runAtFixedRateMethod = schedulerClass.getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class);
                detected = true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        this.folia = detected;
    }

    public void runTask(Runnable task) {
        if (folia) {
            try {
                executeMethod.invoke(globalScheduler, plugin, task);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskLater(Runnable task, long delay) {
        if (folia) {
            try {
                runDelayedMethod.invoke(globalScheduler, plugin, task, delay);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public void runTaskTimer(Runnable task, long delay, long period) {
        if (folia) {
            try {
                runAtFixedRateMethod.invoke(globalScheduler, plugin, task, delay, period);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
}
