package dev.xhyrom.portalgun;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DirectionHelper {
    private static final Long2ObjectMap<Direction> BY_NORMAL = Arrays.stream(Direction.values())
            .collect(Collectors
                    .toMap((p_122409_0_) -> (
                            new BlockPos(p_122409_0_.getNormal())
                    ).asLong(),
                            (p_122393_0_) -> p_122393_0_,
                            (p_122395_0_, p_122395_1_) -> {
                                throw new IllegalArgumentException("Duplicate keys");
                            }
    , Long2ObjectOpenHashMap::new));


    public static Direction fromNormal(BlockPos pos) {
        return BY_NORMAL.get(pos.asLong());
    }

    public static Direction fromNormal(int x, int y, int z) {
        return fromNormal(new BlockPos(x, y, z));
    }
}
