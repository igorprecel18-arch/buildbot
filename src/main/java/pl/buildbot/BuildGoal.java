package pl.buildbot;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BuildGoal extends Goal {
    private static final double REACH_SQ = 49.0;   // zasieg poziomy: 7 blokow (w pionie bot dosiega "magicznie")
    private static final int PER_TICK = 2;         // bloki na tick (40/s)
    private static final int STUCK_LIMIT = 30;     // po tylu tickach bez dojscia stawia z miejsca

    private final BuilderBotEntity bot;
    private int repath = 0;
    private int stuck = 0;
    private int soundTick = 0;

    public BuildGoal(BuilderBotEntity bot) {
        this.bot = bot;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override public boolean canStart() { return bot.hasWork(); }
    @Override public boolean shouldContinue() { return bot.hasWork(); }
    @Override public boolean shouldRunEveryTick() { return true; }
    @Override public void stop() { bot.getNavigation().stop(); }

    private boolean inReach(BlockPos p) {
        double dx = bot.getX() - (p.getX() + 0.5);
        double dz = bot.getZ() - (p.getZ() + 0.5);
        return dx * dx + dz * dz <= REACH_SQ;
    }

    @Override
    public void tick() {
        if (!(bot.getWorld() instanceof ServerWorld world)) return;

        // Czyszczenie terenu (powietrze) - natychmiast
        BuilderBotEntity.Step s;
        while ((s = bot.peekStep()) != null && s.state().isAir()) {
            bot.pollStep();
            world.setBlockState(s.pos(), s.state(), Block.NOTIFY_LISTENERS);
        }
        if (s == null) return;

        Vec3d c = Vec3d.ofCenter(s.pos());
        bot.getLookControl().lookAt(c);

        boolean forced = false;
        if (!inReach(s.pos())) {
            if (--repath <= 0) {
                repath = 10;
                bot.getNavigation().startMovingTo(c.x, bot.getY(), c.z, 1.0);
            }
            if (++stuck < STUCK_LIMIT) return;
            forced = true;
        } else {
            bot.getNavigation().stop();
        }

        int placed = 0;
        BlockState last = null;
        while (placed < PER_TICK
                && (s = bot.peekStep()) != null
                && !s.state().isAir()
                && (forced || inReach(s.pos()))) {
            // nie zamurowuj samego siebie
            if (s.state().isSolid() && new Box(s.pos()).intersects(bot.getBoundingBox())) {
                Vec3d safe = bot.getSafeSpot();
                bot.requestTeleport(safe.x, safe.y, safe.z);
                break;
            }
            bot.pollStep();
            world.setBlockState(s.pos(), s.state(), Block.NOTIFY_LISTENERS);
            last = s.state();
            placed++;
        }

        if (placed > 0) {
            stuck = 0;
            // bot ma wszystkie bloki: trzyma w rece ten, ktory aktualnie stawia
            bot.equipStack(EquipmentSlot.MAINHAND, new ItemStack(last.getBlock().asItem()));
            bot.swingHand(Hand.MAIN_HAND);
            if (++soundTick % 3 == 0)
                world.playSound(null, bot.getBlockPos(), last.getSoundGroup().getPlaceSound(),
                        SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }
}
