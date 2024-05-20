//package com.epimorphismmc.eunetwork.common;
//
//import com.epimorphismmc.eunetwork.api.machine.feature.IEUNetMachine;
//import net.minecraft.world.entity.player.Player;
//
//import javax.annotation.Nonnull;
//import java.util.*;
//
//public class ServerEUNetwork extends EUNetworkBase {
//    private static final Comparator<IEUNetMachine> sDescendingOrder =
//            (lhs, rhs) -> Integer.compare(rhs.getTransferHandler().getPriority(),
//                    lhs.getTransferHandler().getPriority());
//
//    // LinkedList doesn't create large arrays, should be better
//    private final LinkedList<IEUNetMachine> mToAdd = new LinkedList<>();
//    private final LinkedList<IEUNetMachine> mToRemove = new LinkedList<>();
//
//    private boolean mSortConnections = true;
//
//    private final TransferIterator mPlugTransferIterator = new TransferIterator(false);
//    private final TransferIterator mPointTransferIterator = new TransferIterator(true);
//
//    private long mBufferLimiter = 0;
//
//    ServerEUNetwork() {
//    }
//
//    ServerEUNetwork(int id, String name,  @Nonnull Player owner) {
//        super(id, name, owner);
//    }
//
//    private void handleConnectionQueue() {
//        IEUNetMachine device;
//        while ((device = mToAdd.poll()) != null) {
//            for (int type = 0; type < sLogicalTypes.length; type++) {
//                if (sLogicalTypes[type].isInstance(device)) {
//                    var list = getLogicalDevices(type);
//                    assert !list.contains(device);
//                    mSortConnections |= list.add(device);
//                }
//            }
//        }
//        while ((device = mToRemove.poll()) != null) {
//            for (int type = 0; type < sLogicalTypes.length; type++) {
//                if (sLogicalTypes[type].isInstance(device)) {
//                    var list = getLogicalDevices(type);
//                    assert list.contains(device);
//                    mSortConnections |= list.remove(device);
//                }
//            }
//        }
//        if (mSortConnections) {
//            getLogicalDevices(PLUG).sort(sDescendingOrder);
//            getLogicalDevices(POINT).sort(sDescendingOrder);
//            mSortConnections = false;
//        }
//    }
//
//    @Override
//    public void onEndServerTick() {
//        mStatistics.startProfiling();
//
//        handleConnectionQueue();
//
//        mBufferLimiter = 0;
//
//        List<IEUNetMachine> devices = getLogicalDevices(ANY);
//        for (var d : devices) {
//            d.getTransferHandler().onCycleStart();
//        }
//
//        List<IEUNetMachine> plugs = getLogicalDevices(PLUG);
//        List<IEUNetMachine> points = getLogicalDevices(POINT);
//        if (!points.isEmpty() && !plugs.isEmpty()) {
//            // 推入堆栈，因为他们在下面调用了太多次
//            final TransferIterator plugIterator = mPlugTransferIterator.reset(plugs);
//            final TransferIterator pointIterator = mPointTransferIterator.reset(points);
//            CYCLE:
//            while (pointIterator.hasNext()) {
//                while (plugIterator.hasNext()) {
//                    IEUNetMachine plug = plugIterator.next();
//                    IEUNetMachine point = pointIterator.next();
//                    if (plug.getDeviceType() == point.getDeviceType()) {
//                        break CYCLE; // 存储始终具有最低优先级，此处可以打破循环。
//                    }
//                    // 我们不需要模拟这个动作
//                    long actual = plug.getTransferHandler().removeFromBuffer(point.getTransferHandler().getRequest());
//                    if (actual > 0) {
//                        point.getTransferHandler().addToBuffer(actual);
//                        continue CYCLE;
//                    } else {
//                        // although the plug still need transfer (buffer > 0) 虽然插头仍然需要传输（缓冲> 0）
//                        // but it reached max transfer limit, so we use next plug 但它达到了最大传输限制，所以我们使用 NEXT PLUG
//                        plugIterator.increment();
//                    }
//                }
//                break; // all plugs have been used 所有插头均已使用
//            }
//        }
//
//        long limiter = 0;
//        for (var d : devices) {
//            TransferHandler h = d.getTransferHandler();
//            h.onCycleEnd();
//            limiter += h.getRequest();
//            if (h.getChange() != 0) {
//                d.markChunkUnsaved();
//            }
//        }
//        mBufferLimiter = limiter;
//
//        mStatistics.stopProfiling();
//    }
//
//    @Override
//    public long getBufferLimiter() {
//        return mBufferLimiter;
//    }
//
//    @Override
//    public void onDelete() {
//        super.onDelete();
//        Arrays.fill(mDevices, null);
//        mToAdd.clear();
//        mToRemove.clear();
//    }
//
//    @Override
//    public boolean isValid() {
//        return true;
//    }
//
//    public void markSortConnections() {
//        mSortConnections = true;
//    }
//
//    @Override
//    public int changeMembership(@Nonnull Player player, @Nonnull UUID targetUUID, byte type) {
//        final AccessLevel access = getPlayerAccess(player);
//        boolean editPermission = access.canEdit();
//        boolean ownerPermission = access.canDelete();
//        // 检查权限
//        if (!editPermission) {
//            return EUNetConstants.RESPONSE_NO_ADMIN;
//        }
//
//        // 编辑自己
//        final boolean self = player.getUUID().equals(targetUUID);
//        // 网络中的当前成员
//        final NetworkMember current = getMemberByUUID(targetUUID);
//
//        // 创建新成员
//        if (type == EUNetConstants.MEMBERSHIP_SET_USER && current == null) {
//            final Player target = EPLevelUtil.getCurrentServer()
//                    .getPlayerList().getPlayer(targetUUID);
//            if (target != null) {
//                NetworkMember m = NetworkMember.create(target, AccessLevel.USER);
//                mMemberMap.put(m.getPlayerUUID(), m);
//                return EUNetConstants.RESPONSE_SUCCESS;
//            } else {
//                // 播放器现在处于离线状态
//                return EUNetConstants.RESPONSE_INVALID_USER;
//            }
//        } else if (current != null) {
//            // 超级用户仍然可以将所有权转让给自己
//            if (self && current.getAccessLevel() == AccessLevel.OWNER) {
//                return EUNetConstants.RESPONSE_INVALID_USER;
//            }
//            boolean changed = false;
//            if (type == EUNetConstants.MEMBERSHIP_SET_ADMIN) {
//                // we are not owner or super admin
//                if (!ownerPermission) {
//                    return EUNetConstants.RESPONSE_NO_OWNER;
//                }
//            } else if (type == EUNetConstants.MEMBERSHIP_SET_USER) {
//                changed = current.setAccessLevel(AccessLevel.USER);
//            } else if (type == EUNetConstants.MEMBERSHIP_CANCEL_MEMBERSHIP) {
//                changed = mMemberMap.remove(targetUUID) != null;
//            } else if (type == EUNetConstants.MEMBERSHIP_TRANSFER_OWNERSHIP) {
//                if (!ownerPermission) {
//                    return EUNetConstants.RESPONSE_NO_OWNER;
//                }
//                getAllMembers().forEach(f -> {
//                    if (f.getAccessLevel().canDelete()) {
//                        f.setAccessLevel(AccessLevel.USER);
//                    }
//                });
//                mOwnerUUID = targetUUID;
//                current.setAccessLevel(AccessLevel.OWNER);
//                changed = true;
//            }
//            return changed ? EUNetConstants.RESPONSE_SUCCESS : EUNetConstants.RESPONSE_INVALID_USER;
//        } else if (type == EUNetConstants.MEMBERSHIP_TRANSFER_OWNERSHIP) {
//            if (!ownerPermission) {
//                return EUNetConstants.RESPONSE_NO_OWNER;
//            }
//            // 超级用户仍然可以将所有权转让给自己
//            if (self && access == AccessLevel.OWNER) {
//                return EUNetConstants.RESPONSE_INVALID_USER;
//            }
//            Player target = EPLevelUtil.getCurrentServer().getPlayerList().getPlayer(targetUUID);
//            // is online
//            if (target != null) {
//                getAllMembers().forEach(f -> {
//                    if (f.getAccessLevel().canDelete()) {
//                        f.setAccessLevel(AccessLevel.USER);
//                    }
//                });
//                NetworkMember m = NetworkMember.create(target, AccessLevel.OWNER);
//                mMemberMap.put(m.getPlayerUUID(), m);
//                mOwnerUUID = targetUUID;
//                return EUNetConstants.RESPONSE_SUCCESS;
//            } else {
//                return EUNetConstants.RESPONSE_INVALID_USER;
//            }
//        } else {
//            return EUNetConstants.RESPONSE_INVALID_USER;
//        }
//    }
//
//    /*private void addToLite(IFluxDevice flux) {
//        Optional<IFluxDevice> c = all_connectors.getValue().stream().filter(f -> f.getCoords().equals(flux.getCoords
//        ())).findFirst();
//        if (c.isPresent()) {
//            changeChunkLoaded(flux, true);
//        } else {
//            SimpleFluxDevice lite = new SimpleFluxDevice(flux);
//            all_connectors.getValue().add(lite);
//        }
//    }
//
//    private void removeFromLite(IFluxDevice flux) {
//        all_connectors.getValue().removeIf(f -> f.getCoords().equals(flux.getCoords()));
//    }
//
//    private void changeChunkLoaded(IFluxDevice flux, boolean chunkLoaded) {
//        Optional<IFluxDevice> c = all_connectors.getValue().stream().filter(f -> f.getCoords().equals(flux.getCoords
//        ())).findFirst();
//        c.ifPresent(fluxConnector -> fluxConnector.setChunkLoaded(chunkLoaded));
//    }
//
//    @Override
//    public void addNewMember(String name) {
//        NetworkMember a = NetworkMember.createMemberByUsername(name);
//        if (network_players.getValue().stream().noneMatch(f -> f.getPlayerUUID().equals(a.getPlayerUUID()))) {
//            network_players.getValue().add(a);
//        }
//    }
//
//    @Override
//    public void removeMember(UUID uuid) {
//        network_players.getValue().removeIf(p -> p.getPlayerUUID().equals(uuid) && !p.getAccessPermission().canDelete
//        ());
//    }*/
//}
