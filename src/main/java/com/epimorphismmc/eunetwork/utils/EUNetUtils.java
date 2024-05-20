package com.epimorphismmc.eunetwork.utils;

import com.epimorphismmc.eunetwork.common.EUNetworkBase;

import javax.annotation.Nonnull;

public class EUNetUtils {
    public static boolean isBadNetworkName(@Nonnull String s) {
        return s.isEmpty() || s.length() > EUNetworkBase.MAX_NETWORK_NAME_LENGTH;
    }
}
