package me.Thelnfamous1.portalgun;

import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionExcluder {
    VoxelShape getThisSideCollisionExclusion();
}
