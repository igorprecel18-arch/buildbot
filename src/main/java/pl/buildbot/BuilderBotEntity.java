package pl.buildbot;

import java.util.ArrayDeque;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BuilderBotEntity extends PathAwareEntity {
    public record Step(BlockPos pos, BlockState state) {}

    private final ArrayDeque<Step> queue = new ArrayDeque<>();
    private Vec3d safeSpot = Vec3d.ZERO;

    public BuilderBotEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    public static DefaultAttributeContainer.Builder createBuilderAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new BuildGoal(this));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
    }

    /** Klik = wielka panda, Shift + klik = dom. */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!this.getWorld().isClient) {
            if (hasWork()) {
                player.sendMessage(Text.literal("Budowa w toku, zostalo blokow: " + queue.size()), true);
            } else if (player.isSneaking()) {
                safeSpot = this.getPos();
                Direction facing = this.getHorizontalFacing();
                queue.addAll(Blueprints.house(this.getBlockPos().offset(facing, 7), facing));
                player.sendMessage(Text.literal("Zaczynam budowac dom!"), true);
            } else {
                safeSpot = this.getPos();
                Direction facing = this.getHorizontalFacing();
                queue.addAll(Blueprints.panda(this.getBlockPos().offset(facing, 16), facing));
                player.sendMessage(Text.literal("Buduje wielka pande!"), true);
            }
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    public boolean hasWork() { return !queue.isEmpty(); }
    public Step peekStep() { return queue.peek(); }
    public Step pollStep() { return queue.poll(); }
    public Vec3d getSafeSpot() { return safeSpot; }
}
