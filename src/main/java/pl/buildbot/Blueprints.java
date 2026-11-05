package pl.buildbot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Blueprints {

    // ---------------------------------------------------------------- DOM
    /** Dom 7x7, sciany 4 bloki wysokosci. center = srodek podlogi (poziom ziemi). */
    public static List<BuilderBotEntity.Step> house(BlockPos center, Direction facing) {
        List<BuilderBotEntity.Step> steps = new ArrayList<>();
        Direction doorSide = facing.getOpposite();
        int doorDx = doorSide.getOffsetX() * 3;
        int doorDz = doorSide.getOffsetZ() * 3;

        for (int y = 0; y <= 4; y++)
            for (int dx = -3; dx <= 3; dx++)
                for (int dz = -3; dz <= 3; dz++)
                    steps.add(new BuilderBotEntity.Step(center.add(dx, y, dz), Blocks.AIR.getDefaultState()));

        for (int y = -1; y <= 4; y++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                    boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                    boolean door = dx == doorDx && dz == doorDz && (y == 0 || y == 1);
                    BlockState state;

                    if (y == -1) state = Blocks.OAK_PLANKS.getDefaultState();
                    else if (y == 4) state = Blocks.SPRUCE_PLANKS.getDefaultState();
                    else if (!edge || door) continue;
                    else if (corner) state = Blocks.OAK_LOG.getDefaultState();
                    else if (y == 1 && (dx == 0 || dz == 0)) state = Blocks.GLASS.getDefaultState();
                    else state = Blocks.COBBLESTONE.getDefaultState();

                    steps.add(new BuilderBotEntity.Step(center.add(dx, y, dz), state));
                }
            }
        }
        return steps;
    }

    // -------------------------------------------------------------- PANDA
    /**
     * Wielka siedzaca panda (ok. 29 blokow wysokosci, 15 szerokosci).
     * base = srodek podstawy na poziomie ziemi. Panda patrzy w strone bota.
     */
    public static List<BuilderBotEntity.Step> panda(BlockPos base, Direction facing) {
        BlockState W = Blocks.WHITE_CONCRETE.getDefaultState();
        BlockState B = Blocks.BLACK_CONCRETE.getDefaultState();

        // wspolrzedne lokalne: x = na boki, y = w gore, z = do przodu (kierunek patrzenia)
        Map<BlockPos, BlockState> cells = new HashMap<>();
        Set<BlockPos> head = new HashSet<>();

        fill(cells, null, 0, 8.5, 0, 7.5, 8.5, 6.5, W);   // tulow
        fill(cells, head, 0, 21, 2, 6.5, 5.5, 5.5, W);    // glowa

        for (int s : new int[] { -1, 1 }) {
            paint(cells, head, s * 2.8, 21.8, 7, 2.3, 2.7, 3.0, B); // plamy wokol oczu
            fill(cells, null, s * 5, 26.5, 0.5, 2.5, 2.5, 2.5, B);  // uszy
            fill(cells, null, s * 7.5, 9.5, 2.5, 2.6, 5.5, 3.0, B); // lapy przednie
            fill(cells, null, s * 4.5, 2.5, 6.5, 3.2, 2.5, 3.5, B); // nogi
        }

        // oczy (biale) - na przedniej powierzchni glowy
        for (int sx : new int[] { -3, 3 })
            for (int y = 22; y <= 23; y++) {
                int z = frontZ(head, sx, y);
                if (z != Integer.MIN_VALUE) cells.put(new BlockPos(sx, y, z), W);
            }
        // nos (czarny)
        for (int x = -1; x <= 1; x++) {
            int z = frontZ(head, x, 19);
            if (z != Integer.MIN_VALUE) cells.put(new BlockPos(x, 19, z), B);
        }
        int nz = frontZ(head, 0, 20);
        if (nz != Integer.MIN_VALUE) cells.put(new BlockPos(0, 20, nz), B);

        // buduj od dolu, rzedami
        List<Map.Entry<BlockPos, BlockState>> list = new ArrayList<>(cells.entrySet());
        list.sort(Comparator.comparingInt((Map.Entry<BlockPos, BlockState> e) -> e.getKey().getY())
                .thenComparingInt(e -> e.getKey().getX())
                .thenComparingInt(e -> e.getKey().getZ()));

        Direction f = facing.getOpposite();      // panda patrzy na bota
        Direction r = f.rotateYClockwise();
        List<BuilderBotEntity.Step> steps = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> e : list) {
            BlockPos l = e.getKey();
            BlockPos w = base.add(
                    r.getOffsetX() * l.getX() + f.getOffsetX() * l.getZ(),
                    l.getY(),
                    r.getOffsetZ() * l.getX() + f.getOffsetZ() * l.getZ());
            steps.add(new BuilderBotEntity.Step(w, e.getValue()));
        }
        return steps;
    }

    private static boolean inside(int x, int y, int z, double cx, double cy, double cz,
                                  double rx, double ry, double rz) {
        double dx = (x - cx) / rx, dy = (y + 0.5 - cy) / ry, dz = (z - cz) / rz;
        return dx * dx + dy * dy + dz * dz <= 1.0;
    }

    private static void fill(Map<BlockPos, BlockState> cells, Set<BlockPos> track,
                             double cx, double cy, double cz, double rx, double ry, double rz,
                             BlockState state) {
        for (int x = (int) Math.floor(cx - rx); x <= (int) Math.ceil(cx + rx); x++)
            for (int y = (int) Math.floor(cy - ry); y <= (int) Math.ceil(cy + ry); y++)
                for (int z = (int) Math.floor(cz - rz); z <= (int) Math.ceil(cz + rz); z++)
                    if (inside(x, y, z, cx, cy, cz, rx, ry, rz)) {
                        BlockPos p = new BlockPos(x, y, z);
                        cells.put(p, state);
                        if (track != null) track.add(p);
                    }
    }

    /** Przemalowuje tylko juz istniejace bloki glowy. */
    private static void paint(Map<BlockPos, BlockState> cells, Set<BlockPos> head,
                              double cx, double cy, double cz, double rx, double ry, double rz,
                              BlockState state) {
        for (BlockPos p : head)
            if (inside(p.getX(), p.getY(), p.getZ(), cx, cy, cz, rx, ry, rz))
                cells.put(p, state);
    }

    private static int frontZ(Set<BlockPos> head, int x, int y) {
        int z = Integer.MIN_VALUE;
        for (BlockPos p : head)
            if (p.getX() == x && p.getY() == y && p.getZ() > z) z = p.getZ();
        return z;
    }
}
