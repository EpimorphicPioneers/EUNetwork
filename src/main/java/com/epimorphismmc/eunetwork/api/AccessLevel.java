package com.epimorphismmc.eunetwork.api;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
public enum AccessLevel implements StringRepresentable {
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

    @NotNull
    public static AccessLevel fromKey(byte key) {
        return VALUES[key];
    }

    public byte getKey() {
        return (byte) ordinal();
    }

    @NotNull
    public MutableComponent getComponent() {
        return Component.translatable(translationKey).withStyle(formatting);
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
