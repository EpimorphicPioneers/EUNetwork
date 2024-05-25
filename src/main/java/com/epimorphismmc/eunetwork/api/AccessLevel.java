package com.epimorphismmc.eunetwork.api;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;

import javax.annotation.Nonnull;

@Getter
public enum AccessLevel {
    OWNER("eunetwork.owner", ChatFormatting.GOLD, "Owner", "所有者"),
    USER("eunetwork.user", ChatFormatting.BLUE, "User", "用户"),
    BLOCKED("eunetwork.block", ChatFormatting.GRAY, "Blocked", "屏蔽");

    private static final AccessLevel[] VALUES = values();

    private final String translationKey;
    private final ChatFormatting formatting;
    private final String enString;
    private final String cnString;

    AccessLevel(String translationKey, ChatFormatting formatting, String enString, String cnString) {
        this.translationKey = translationKey;
        this.formatting = formatting;
        this.enString = enString;
        this.cnString = cnString;
    }

    @Nonnull
    public static AccessLevel fromKey(byte key) {
        return VALUES[key];
    }

    public byte getKey() {
        return (byte) ordinal();
    }

    @Nonnull
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
}
