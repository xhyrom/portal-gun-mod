package dev.xhyrom.portalgun.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public abstract class RotationUtil {
    private static final Direction[][] DIR_WORLD_TO_PLAYER = new Direction[6][];
    static {
        for(Direction gravityDirection : Direction.values()) {
            DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vec3 directionVector = Vec3.atLowerCornerOf(direction.getNormal());
                directionVector = RotationUtil.vecWorldToPlayer(directionVector, gravityDirection);
                DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()][direction.get3DDataValue()] = DirectionPolyfill.fromNormal(new BlockPos(new Vec3i((int) Math.floor(directionVector.x), (int) Math.floor(directionVector.y), (int) Math.floor(directionVector.z))));
            }
        }
    }

    public static Direction dirWorldToPlayer(Direction direction, Direction gravityDirection) {
        return DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()][direction.get3DDataValue()];
    }

    public static Vec3 vecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        return switch(gravityDirection) {
            case DOWN  -> new Vec3( x,  y,  z);
            case UP    -> new Vec3(-x, -y,  z);
            case NORTH -> new Vec3( x,  z, -y);
            case SOUTH -> new Vec3(-x, -z, -y);
            case WEST  -> new Vec3(-z,  x, -y);
            case EAST  -> new Vec3( z, -x, -y);
        };
    }

    public static Vec3 vecWorldToPlayer(Vec3 v, Direction gravityDirection) {
        return vecWorldToPlayer(v.x, v.y, v.z, gravityDirection);
    }
}