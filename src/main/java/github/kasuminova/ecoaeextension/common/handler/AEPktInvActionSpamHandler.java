package github.kasuminova.ecoaeextension.common.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AEPktInvActionSpamHandler {

    protected static final int LIMIT_PER_SECOND = 8;
    protected static final Cache<EntityPlayer, PacketCounter> PLAYER_PKT_COUNTER = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.SECONDS)
            .build();

    public static boolean receivePacketAndCheckSpam(final EntityPlayer player) {
        try {
            PacketCounter counter = PLAYER_PKT_COUNTER.get(player, PacketCounter::new);
            return counter.addPacket((int) TaskExecutor.tickExisted) > LIMIT_PER_SECOND * 3;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public static class PacketCounter {
        protected final LinkedList<Element> data = new LinkedList<>();
        protected int counter = 0;

        public int addPacket(int currentTick) {
            // 空列表时直接添加。
            if (data.isEmpty()) {
                data.addLast(new Element(currentTick));
                return counter = 1;
            }

            // 新网络包接收时调用。
            Element last = data.getLast();
            if (last.currentTick == currentTick) {
                last.incCount();
            } else {
                data.addLast(new Element(currentTick));
            }
            counter++;

            // 移除旧数据。
            Element first;
            while ((first = data.peekFirst()) != null && first.currentTick < currentTick - 60) {
                data.removeFirst();
                counter -= first.count;
            }

            return counter;
        }

        public static class Element {

            protected final int currentTick;
            @Getter
            protected int count = 0;

            public Element(int currentTick) {
                this.currentTick = currentTick;
            }

            public void incCount() {
                ++count;
            }

        }

    }

}
