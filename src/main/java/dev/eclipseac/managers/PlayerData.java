package dev.eclipseac.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String name;


    private Location lastLocation;
    private Location lastGroundLocation;
    private double lastDeltaY;
    private double lastHorizontalSpeed;
    private int airTicks;
    private int groundTicks;
    private boolean onGround;
    private boolean wasOnGround;
    private float fallDistance;
    private boolean exempt;
    private final Deque<Double> deltaYHistory = new ArrayDeque<>();


    private long lastMoveTime;
    private final Deque<Long> moveTimes = new ArrayDeque<>();


    private long lastAttackTime;
    private final Deque<Long> attackTimes = new ArrayDeque<>();


    private long lastPlaceTime;
    private final Deque<Long> placeTimes = new ArrayDeque<>();
    private int scaffoldStreak;
    private int scaffoldLookingDownStreak;


    private boolean inventoryOpen;
    private long lastInventoryOpenTime;
    private long lastInventoryCloseTime;
    private long lastInventoryAction;


    private float lastYaw;
    private float lastPitch;
    private final Deque<Float> yawHistory   = new ArrayDeque<>();
    private final Deque<Float> pitchHistory = new ArrayDeque<>();


    private int  teleportTicks;
    private int  vehicleExitTicks;
    private int  liquidExitTicks;
    private int  climbableExitTicks;
    private int  damageTicks;
    private int  respawnTicks;
    private long joinTime;


    private boolean inVehicle;
    private boolean wasInVehicle;
    private boolean inLiquid;
    private boolean wasInLiquid;
    private boolean onClimbable;
    private boolean hasSlowFalling;

    public PlayerData(Player player) {
        this.uuid        = player.getUniqueId();
        this.name        = player.getName();
        this.lastLocation = player.getLocation().clone();
        this.joinTime    = System.currentTimeMillis();
        this.onGround    = true;
        this.wasOnGround = true;
        this.lastYaw     = player.getLocation().getYaw();
        this.lastPitch   = player.getLocation().getPitch();
        this.respawnTicks = 40;
    }


    public UUID getUuid() { return uuid; }
    public String getName() { return name; }


    public Location getLastLocation() { return lastLocation; }
    public void setLastLocation(Location l) { this.lastLocation = l; }

    public Location getLastGroundLocation() { return lastGroundLocation; }
    public void setLastGroundLocation(Location l) { this.lastGroundLocation = l; }

    public double getLastDeltaY() { return lastDeltaY; }
    public void setLastDeltaY(double d) {
        this.lastDeltaY = d;
        deltaYHistory.addLast(d);
        if (deltaYHistory.size() > 20) deltaYHistory.pollFirst();
    }
    public Deque<Double> getDeltaYHistory() { return deltaYHistory; }

    public double getLastHorizontalSpeed() { return lastHorizontalSpeed; }
    public void setLastHorizontalSpeed(double s) { this.lastHorizontalSpeed = s; }

    public int getAirTicks() { return airTicks; }
    public void setAirTicks(int t) { this.airTicks = t; }
    public void incrementAirTicks() { airTicks++; }

    public int getGroundTicks() { return groundTicks; }
    public void setGroundTicks(int t) { this.groundTicks = t; }
    public void incrementGroundTicks() { groundTicks++; }

    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean b) { wasOnGround = onGround; onGround = b; }
    public boolean wasOnGround() { return wasOnGround; }

    public float getFallDistance() { return fallDistance; }
    public void setFallDistance(float f) { this.fallDistance = f; }

    public boolean isExempt() { return exempt; }
    public void setExempt(boolean e) { this.exempt = e; }


    public long getLastMoveTime() { return lastMoveTime; }
    public void setLastMoveTime(long t) { this.lastMoveTime = t; }
    public Deque<Long> getMoveTimes() { return moveTimes; }


    public long getLastAttackTime() { return lastAttackTime; }
    public Deque<Long> getAttackTimes() { return attackTimes; }

    public void recordAttack() {
        long now = System.currentTimeMillis();
        attackTimes.addLast(now);
        if (attackTimes.size() > 60) attackTimes.pollFirst();
        lastAttackTime = now;
    }

    public int getCurrentCps() {
        long now = System.currentTimeMillis();
        attackTimes.removeIf(t -> now - t > 1000);
        return attackTimes.size();
    }


    public long getLastPlaceTime() { return lastPlaceTime; }
    public void setLastPlaceTime(long t) { this.lastPlaceTime = t; }
    public Deque<Long> getPlaceTimes() { return placeTimes; }

    public void recordPlace() {
        long now = System.currentTimeMillis();
        placeTimes.addLast(now);
        if (placeTimes.size() > 60) placeTimes.pollFirst();
        lastPlaceTime = now;
    }

    public int getBlocksPerSecond() {
        long now = System.currentTimeMillis();
        placeTimes.removeIf(t -> now - t > 1000);
        return placeTimes.size();
    }

    public int getScaffoldStreak() { return scaffoldStreak; }
    public void incrementScaffoldStreak() { scaffoldStreak++; }
    public void resetScaffoldStreak() { scaffoldStreak = 0; }

    public int getScaffoldLookingDownStreak() { return scaffoldLookingDownStreak; }
    public void incrementScaffoldLookingDownStreak() { scaffoldLookingDownStreak++; }
    public void resetScaffoldLookingDownStreak() { scaffoldLookingDownStreak = 0; }


    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean b) {
        inventoryOpen = b;
        if (b) lastInventoryOpenTime  = System.currentTimeMillis();
        else   lastInventoryCloseTime = System.currentTimeMillis();
    }
    public long getLastInventoryOpenTime()  { return lastInventoryOpenTime; }
    public long getLastInventoryCloseTime() { return lastInventoryCloseTime; }
    public long getLastInventoryAction() { return lastInventoryAction; }
    public void setLastInventoryAction(long t) { this.lastInventoryAction = t; }


    public float getLastYaw()   { return lastYaw; }
    public float getLastPitch() { return lastPitch; }

    public void setLastYaw(float y) {
        yawHistory.addLast(lastYaw);
        if (yawHistory.size() > 20) yawHistory.pollFirst();
        lastYaw = y;
    }
    public void setLastPitch(float p) {
        pitchHistory.addLast(lastPitch);
        if (pitchHistory.size() > 20) pitchHistory.pollFirst();
        lastPitch = p;
    }
    public Deque<Float> getYawHistory()   { return yawHistory; }
    public Deque<Float> getPitchHistory() { return pitchHistory; }


    public int getTeleportTicks() { return teleportTicks; }
    public void setTeleportTicks(int t) { this.teleportTicks = t; }
    public void decrementTeleportTicks() { if (teleportTicks > 0) teleportTicks--; }

    public int getVehicleExitTicks() { return vehicleExitTicks; }
    public void setVehicleExitTicks(int t) { this.vehicleExitTicks = t; }
    public void decrementVehicleExitTicks() { if (vehicleExitTicks > 0) vehicleExitTicks--; }

    public int getLiquidExitTicks() { return liquidExitTicks; }
    public void setLiquidExitTicks(int t) { this.liquidExitTicks = t; }
    public void decrementLiquidExitTicks() { if (liquidExitTicks > 0) liquidExitTicks--; }

    public int getClimbableExitTicks() { return climbableExitTicks; }
    public void setClimbableExitTicks(int t) { this.climbableExitTicks = t; }
    public void decrementClimbableExitTicks() { if (climbableExitTicks > 0) climbableExitTicks--; }

    public int getDamageTicks() { return damageTicks; }
    public void setDamageTicks(int t) { this.damageTicks = t; }
    public void decrementDamageTicks() { if (damageTicks > 0) damageTicks--; }

    public int getRespawnTicks() { return respawnTicks; }
    public void setRespawnTicks(int t) { this.respawnTicks = t; }
    public void decrementRespawnTicks() { if (respawnTicks > 0) respawnTicks--; }

    public long getJoinTime() { return joinTime; }


    public boolean isInVehicle() { return inVehicle; }
    public void setInVehicle(boolean b) {
        wasInVehicle = inVehicle;
        inVehicle = b;
        if (!b && wasInVehicle) vehicleExitTicks = 20;
    }
    public boolean wasInVehicle() { return wasInVehicle; }

    public boolean isInLiquid() { return inLiquid; }
    public void setInLiquid(boolean b) {
        wasInLiquid = inLiquid;
        inLiquid = b;
        if (!b && wasInLiquid) liquidExitTicks = 12;
    }

    public boolean isOnClimbable() { return onClimbable; }
    public void setOnClimbable(boolean b) {
        boolean wasOnClimbable = onClimbable;
        onClimbable = b;
        if (!b && wasOnClimbable) climbableExitTicks = 8;
    }

    public boolean hasSlowFalling() { return hasSlowFalling; }
    public void setHasSlowFalling(boolean b) { this.hasSlowFalling = b; }

    /**
     */
    public boolean isInGracePeriod() {
        return teleportTicks > 0
            || vehicleExitTicks > 0
            || liquidExitTicks > 0
            || climbableExitTicks > 0
            || damageTicks > 0
            || respawnTicks > 0
            || (System.currentTimeMillis() - joinTime < 8000);
    }
}
