package tk.meowmc.portalgun.entities;

import me.Thelnfamous1.portalgun.ColoredPortal;
import me.Thelnfamous1.portalgun.IntBoxHelper;
import me.Thelnfamous1.portalgun.PortalHelper;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.IntBox;

public class CustomPortal extends Portal implements ColoredPortal {
    private static final Logger LOGGER = LogManager.getLogger();

    public PortalGunRecord.PortalDescriptor descriptor;

    public IntBox wallBox;
    public IntBox airBox;

    public int thisSideUpdateCounter = 0;
    public int otherSideUpdateCounter = 0;
    private boolean hasCustomPortalColor = false;
    private int customPortalColor = 0;

    public CustomPortal(@NotNull EntityType<?> entityType, net.minecraft.world.level.Level world) {
        super(entityType, world);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        descriptor = PortalGunRecord.PortalDescriptor.fromTag(compoundTag.getCompound("descriptor"));
        wallBox = IntBoxHelper.fromTag(compoundTag.getCompound("wallBox"));
        airBox = IntBoxHelper.fromTag(compoundTag.getCompound("airBox"));
        thisSideUpdateCounter = compoundTag.getInt("thisSideUpdateCounter");
        otherSideUpdateCounter = compoundTag.getInt("otherSideUpdateCounter");
        this.readCustomPortalColor(compoundTag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("descriptor", descriptor.toTag());
        compoundTag.put("wallBox", IntBoxHelper.toTag(wallBox));
        compoundTag.put("airBox", IntBoxHelper.toTag(airBox));
        compoundTag.putInt("thisSideUpdateCounter", thisSideUpdateCounter);
        compoundTag.putInt("otherSideUpdateCounter", otherSideUpdateCounter);
        this.writeCustomPortalColor(compoundTag);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            updateState();
        }
    }

    // disable the interpolation between last tick pos and this tick pos
    // because the portal should change abruptly
    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        setOldPosAndRot();
    }

    void updateState() {
        if (descriptor == null || wallBox == null) {
            LOGGER.error("Portal info abnormal {}", this);
            kill();
            return;
        }

        PortalGunRecord record = PortalGunRecord.get();
        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(descriptor.getTheOtherSide());
        if (thisSideInfo == null) {
            // info is missing
            kill();
            return;
        }
        if (thisSideUpdateCounter != thisSideInfo.updateCounter() || !thisSideInfo.portalId().equals(getUUID())) {
            // replaced by new portal
            kill();
            return;
        }
        // check block status
        if (!PortalGunMod.isWallValid(level(), wallBox) || !PortalGunMod.isAreaClear(level(), airBox)) {
            kill();
            record.data.remove(descriptor);
            record.setDirty();
            level().playSound(
                    null,
                    getX(), getY(), getZ(),
                    PortalGunMod.PORTAL_CLOSE_EVENT.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
            );
            return;
        }
        if (otherSideInfo == null) {
            // other side is missing, make this side inactive
            if (otherSideUpdateCounter != -1) {
                otherSideUpdateCounter = -1;
                teleportable = false;
                setIsVisible(false);
                setDestination(getOriginPos().add(0, 10, 0));
                reloadAndSyncToClient();
            }
            return;
        }
        if (otherSideInfo.updateCounter() != otherSideUpdateCounter) {
            // other side is replaced by new portal, update linking
            if (!isVisible()) {
                level().playSound(
                        null,
                        getX(), getY(), getZ(),
                        PortalGunMod.PORTAL_OPEN_EVENT.get(),
                        SoundSource.PLAYERS,
                        1.0F, 1.0F
                );
            }
            otherSideUpdateCounter = otherSideInfo.updateCounter();
            teleportable = true;
            setIsVisible(true);
            setDestination(otherSideInfo.portalPos());
            setDestinationDimension(otherSideInfo.portalDim());
            PortalHelper.setOtherSideOrientation(this, otherSideInfo.portalOrientation());//setOtherSideOrientation(otherSideInfo.portalOrientation());
            reloadAndSyncToClient();
        }
    }

    @Override
    public boolean hasCustomPortalColor(){
        return this.hasCustomPortalColor;
    }

    @Override
    public int getPortalColor(){
        return this.hasCustomPortalColor ? this.customPortalColor : this.descriptor.side().getColorInt();
    }

    @Override
    public void setCustomPortalColor(int portalColor){
        this.hasCustomPortalColor = true;
        this.customPortalColor = portalColor;
    }

    @Override
    public void clearCustomPortalColor(){
        this.hasCustomPortalColor = false;
        this.customPortalColor = 0;
    }
}