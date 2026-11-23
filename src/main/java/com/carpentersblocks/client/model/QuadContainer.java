package com.carpentersblocks.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.carpentersblocks.util.RotationUtil.Rotation;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The quads of a block shape, at a location (the whole block, or a side cover laid on one side).
 */
public class QuadContainer {

    private final List<Quad> _quads;
    private final EnumAttributeLocation _location;
    private static final double MAX_UP_SLOPE_GRASS = Math.sin(35 * Math.PI / 180);
    private static final double MAX_SIDE_SLOPE_GRASS = -Math.sin(55 * Math.PI / 180);
    private static final double MAX_UP_SLOPE = Math.sin(46 * Math.PI / 180);
    private static final double MAX_SIDE_SLOPE = -Math.sin(44 * Math.PI / 180);

    public QuadContainer(EnumAttributeLocation location) {
        _quads = new ArrayList<>();
        _location = location;
    }

    // Finished quads drawn as they are, without covers (plants in flower pots)
    private final List<BakedQuad> _bakedQuads = new ArrayList<>();

    public void addBaked(BakedQuad quad) {
        _bakedQuads.add(quad);
    }

    public List<BakedQuad> getBakedQuads() {
        return _bakedQuads;
    }

    public EnumAttributeLocation getLocation() {
        return _location;
    }

    public List<BakedQuad> bakeQuads() {
        List<BakedQuad> quads = bakeQuads(_quads);
        quads.addAll(_bakedQuads);
        return quads;
    }

    /**
     * Gets copies of the quads on some sides (all quads if none are given).
     *
     * @param blockState the cover, which decides which side sloped quads count as, or null
     */
    public List<Quad> getQuads(BlockState blockState, Direction... facings) {
        transformForBlockState(false, blockState);
        List<Quad> quads = new ArrayList<>();
        for (Quad quad : _quads) {
            if (facings.length == 0) {
                quads.add(new Quad(quad));
            } else {
                for (Direction facing : facings) {
                    if (facing.equals(quad.getFacing())) {
                        quads.add(new Quad(quad));
                    }
                }
            }
        }
        return quads;
    }

    public void addAll(Collection<Quad> collection) {
        for (Quad quad : collection) {
            add(quad);
        }
    }

    public void add(Quad quad) {
        if (quad != null && QuadUtil.isValid(quad)) {
            _quads.add(quad);
        }
    }

    /**
     * Gets the quads of a side cover: the block's faces on that side, pushed out by the cover's depth, with the
     * edges that join them to the block.
     */
    public QuadContainer toSideLocation(EnumAttributeLocation location, double depth) {
        QuadContainer quadContainer = new QuadContainer(location);
        Direction facing = location.getDirection();
        for (Quad quad : _quads) {
            Direction offsetFacing = quad.getSideCoverAltFacing(); // Sloped faces should be UP/DOWN
            if (facing.equals(offsetFacing)) {
                Quad offsetQuad = new Quad(quad);
                offsetQuad.applyFacing(true, offsetFacing);
                quadContainer.addAll(QuadUtil.getPerpendicularQuads(offsetQuad, depth));
                quadContainer.add(quad.offset(offsetFacing.getStepX() * depth, offsetFacing.getStepY() * depth, offsetFacing.getStepZ() * depth));
            }
        }

        // Remove duplicate quads with matching vector coordinates
        if (quadContainer._quads.size() > 6) {
            List<Quad> list = new ArrayList<>(quadContainer._quads);
            Iterator<Quad> iter = quadContainer._quads.iterator();
            while (iter.hasNext()) {
                Quad q1 = iter.next();
                for (Quad q2 : list) {
                    if (q1 != q2 && sameCorners(q1, q2)) {
                        iter.remove();
                        break;
                    }
                }
            }
        }
        return quadContainer;
    }

    public List<BakedQuad> bakeQuads(List<Quad> quads) {
        List<BakedQuad> list = new ArrayList<>();
        for (Quad quad : quads) {
            list.add(new Quad(quad).bake(_location));
        }
        return list;
    }

    public boolean hasCoverOverride() {
        for (Quad quad : _quads) {
            if (!quad.canCover()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether every corner of the first quad is a corner of the second.
     */
    private static boolean sameCorners(Quad q1, Quad q2) {
        List<Vec3> tempList = new ArrayList<>(Arrays.asList(q1.getVecs()));
        Iterator<Vec3> iter = tempList.iterator();
        while (iter.hasNext()) {
            Vec3 v1 = iter.next();
            for (Vec3 v2 : q2.getVecs()) {
                if (Mth.equal((float) v1.x, (float) v2.x)
                        && Mth.equal((float) v1.y, (float) v2.y)
                        && Mth.equal((float) v1.z, (float) v2.z)) {
                    iter.remove();
                    break;
                }
            }
        }
        return tempList.isEmpty();
    }

    /**
     * Rotates quads.
     */
    public void rotate(Rotation rotation) {
        for (Quad quad : _quads) {
            quad.rotate(rotation);
        }
    }

    /**
     * For sloped quads, maps correct facing for quad based on UP, DOWN, or side facing orientation and block type.
     *
     * @param blockState cover blockstate
     */
    public void transformForBlockState(boolean isSideCover, BlockState blockState) {
        if (!isSideCover && blockState == null) {
            return;
        }
        List<Quad> list = new ArrayList<>();
        for (Quad quad : _quads) {
            if (!quad.isSloped(Axis.Y)) {
                list.add(new Quad(quad));
                continue;
            }
            if (isSideCover) {
                if (!Axis.Y.equals(quad.getFacing().getAxis())) {
                    Quad newQuad = new Quad(quad);
                    newQuad.applyFacing(quad.getNormal().y > 0.0D ? Direction.UP : Direction.DOWN);
                    list.add(newQuad);
                } else {
                    list.add(new Quad(quad));
                }
                continue;
            }
            double maxUpSlope = MAX_UP_SLOPE;
            double maxSideSlope = MAX_SIDE_SLOPE;
            if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
                maxUpSlope = MAX_UP_SLOPE_GRASS;
                maxSideSlope = MAX_SIDE_SLOPE_GRASS;
            }
            Quad newQuad = new Quad(quad);
            if (QuadUtil.compare(quad.getNormal().y, maxUpSlope) >= 0) {
                if (!Direction.UP.equals(quad.getFacing())) {
                    newQuad.applyFacing(Direction.UP);
                }
            } else if (QuadUtil.compare(quad.getNormal().y, maxSideSlope) >= 0) {
                if (!quad.getCardinalFacing().equals(quad.getFacing())) {
                    newQuad.applyFacing(quad.getCardinalFacing());
                }
            } else if (!Direction.DOWN.equals(quad.getFacing())) {
                newQuad.applyFacing(Direction.DOWN);
            }
            list.add(newQuad);
        }
        _quads.clear();
        _quads.addAll(list);
    }

}
