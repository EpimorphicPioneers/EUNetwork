package com.epimorphismmc.eunetwork.api;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum AccessLevel implements StringRepresentable {
    OWNER("eunetwork.owner", ChatFormatting.GOLD),
    USER("eunetwork.user", ChatFormatting.BLUE),
    BLOCKED("eunetwork.block", ChatFormatting.GRAY);

    private static final AccessLevel[] VALUES = values();

    private final String translationKey;
    private final ChatFormatting formatting;

    AccessLevel(String translationKey, ChatFormatting formatting) {
        this.translationKey = translationKey;
        this.formatting = formatting;
    }

    @NotNull
    public static AccessLevel fromKey(byte key) {
        return VALUES[key];
    }

    public byte getKey() {
        return (byte) ordinal();
    }

    @NotNull
    public String getFormattedName() {
        return formatting + LocalizationUtils.format(translationKey);
    }

    public boolean canUse() {
        return this != BLOCKED;
    }

    public boolean canEdit() {
        return canUse() && this != USER;
    }

    public boolean canDelete() {
        return this == OWNER;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
