package dev.xhyrom.portalgun.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import qouteall.q_misc_util.my_util.IntBox;

public class IntBoxPolyfill {
    public static IntBox getBoxByPosAndSignedSize(
            BlockPos basePos,
            BlockPos signedSize
    ) {
        return new IntBox(
                basePos,
                new BlockPos(
                        getEndCoordWithSignedSize(basePos.getX(), signedSize.getX()),
                        getEndCoordWithSignedSize(basePos.getY(), signedSize.getY()),
                        getEndCoordWithSignedSize(basePos.getZ(), signedSize.getZ())
                )
        );
    }

    private static int getEndCoordWithSignedSize(int base, int signedSize) {
        if (signedSize > 0) {
            return base + signedSize - 1;
        }
        else if (signedSize < 0) {
            return base + signedSize + 1;
        }
        else if (signedSize == 0) {
            return base;
        }
        else {
            throw new IllegalArgumentException("Signed size cannot be zero");
        }
    }

    public static IntBox fromTag(CompoundTag tag) {
        return new IntBox(
                new BlockPos(
                        tag.getInt("lX"),
                        tag.getInt("lY"),
                        tag.getInt("lZ")
                ),
                new BlockPos(
                        tag.getInt("hX"),
                        tag.getInt("hY"),
                        tag.getInt("hZ")
                )
        );
    }

    public static CompoundTag toTag(IntBox box) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("lX", box.l.getX());
        tag.putInt("lY", box.l.getY());
        tag.putInt("lZ", box.l.getZ());

        tag.putInt("hX", box.h.getX());
        tag.putInt("hY", box.h.getY());
        tag.putInt("hZ", box.h.getZ());

        return tag;
    }
}
