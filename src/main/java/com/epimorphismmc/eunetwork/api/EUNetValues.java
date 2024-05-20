package com.epimorphismmc.eunetwork.api;

public class EUNetValues {

    /**
     * NBT access type, save data to disk (R/W server only).
     */
    public static final byte NBT_SAVE_ALL = 1;

    /**
     * NBT access type, network data-sync or operation.
     * Write - server, Read - client/server.
     * <ul>
     *     <li>(Server to client) Basic: network ID, network name, network color</li>
     *     <li>(Client requests) General: ownerUUID, securityLevel, wirelessMode</li>
     *     <li>(Client requests) Members: All network members</li>
     *     <li>(Client requests) Connections: All network connections (i.e. loaded and unloaded)</li>
     *     <li>(Client requests) Statistics: Latest network statistics</li>
     * </ul>
     * Note that password is always opaque to clients (even if you are super admin).
     */
    //TODO update relevant message handling
    public static final byte
            NBT_NET_BASIC = 21,
            NBT_NET_MEMBERS = 22,
            NBT_NET_ALL_CONNECTIONS = 23,
            NBT_NET_STATISTICS = 24;

    /**
     * Response codes. Positive - Have Toast, Negative - Action Only.
     */
    public static final int
            RESPONSE_SUCCESS = -1,
            RESPONSE_REQUIRE_PASSWORD = -2;
    public static final int
            RESPONSE_REJECT = 1,
            RESPONSE_NO_OWNER = 2,
            RESPONSE_NO_ADMIN = 3,
            RESPONSE_NO_SPACE = 4,
            RESPONSE_HAS_CONTROLLER = 5,
            RESPONSE_INVALID_USER = 6,
            RESPONSE_INVALID_PASSWORD = 7,
            RESPONSE_BANNED_LOADING = 8;

    /**
     * Request keys.
     */
    public static final int
            REQUEST_CREATE_NETWORK = 1,
            REQUEST_DELETE_NETWORK = 2,
            REQUEST_EDIT_TILE = 3,
            REQUEST_TILE_NETWORK = 4,
            REQUEST_EDIT_MEMBER = 5,
            REQUEST_EDIT_NETWORK = 6,
            REQUEST_EDIT_CONNECTION = 7,
            REQUEST_UPDATE_NETWORK = 8,
            REQUEST_UPDATE_CONNECTION = 9,
            REQUEST_DISCONNECT = 10;

    // Network members editing type
    public static final byte MEMBERSHIP_SET_USER = 1;
    public static final byte MEMBERSHIP_CANCEL_MEMBERSHIP = 2;
    public static final byte MEMBERSHIP_TRANSFER_OWNERSHIP = 3;

    // NBT key
    public static final String NETWORK_ID = "networkID";
    public static final String CUSTOM_NAME = "customName";
    public static final String PRIORITY = "priority";
    public static final String LIMIT = "limit";
    public static final String SURGE_MODE = "surgeMode";
    public static final String DISABLE_LIMIT = "disableLimit";
    public static final String PLAYER_UUID = "playerUUID";

    public static final String CLIENT_COLOR = "clientColor";
    public static final String FLAGS = "flags";

    public static final String BUFFER = "buffer";
    public static final String ENERGY = "energy"; // equals to buffer, but with different display text
    public static final String CHANGE = "change";

}
