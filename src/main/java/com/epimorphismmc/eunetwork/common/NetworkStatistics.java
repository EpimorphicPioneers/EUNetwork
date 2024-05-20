package com.epimorphismmc.eunetwork.common;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.nbt.CompoundTag;

public class NetworkStatistics {
    public static final int CHANGE_COUNT = 6;

    private final EUNetworkBase network;

    private int timer;

    public long energyInput;
    public long energyOutput;

    public final LongList energyChange = new LongArrayList(CHANGE_COUNT);

    public long totalBuffer;
    public long totalEnergy;

    private long energyChange5;
    private long energyInput4;
    private long energyOutput4;

    public int averageTickMicro;
    private long runningTotalNano;

    private long startNanoTime;

    public NetworkStatistics(EUNetworkBase network) {
        this.network = network;
        energyChange.size(CHANGE_COUNT);
    }

    public void startProfiling() {
        startNanoTime = System.nanoTime();
    }

    public void stopProfiling() {
        if (timer == 0) {
            weakestTick();
        }

        runningTotalNano += System.nanoTime() - startNanoTime;

        timer = ++timer % 100;
    }



    /**
     * Called every 100 ticks
     */
    private void weakestTick() {
        for (int i = 1; i < CHANGE_COUNT; i++) {
            energyChange.set(i - 1, energyChange.getLong(i));
        }
        energyChange.set(CHANGE_COUNT - 1, energyChange5 / 5);
        energyChange5 = 0;
    }

    public void writeNBT(CompoundTag tag) {
        tag.putLong("5", energyInput);
        tag.putLong("6", energyOutput);
        tag.putLong("7", totalBuffer);
        tag.putLong("8", totalEnergy);
        tag.putInt("9", averageTickMicro);
        tag.putLongArray("a", energyChange);
    }

    public void readNBT(CompoundTag tag) {
        energyInput = tag.getLong("5");
        energyOutput = tag.getLong("6");
        totalBuffer = tag.getLong("7");
        totalEnergy = tag.getLong("8");
        averageTickMicro = tag.getInt("9");
        long[] a = tag.getLongArray("a");
        for (int i = 0; i < a.length; i++) {
            energyChange.set(i, a[i]);
        }
    }
}
