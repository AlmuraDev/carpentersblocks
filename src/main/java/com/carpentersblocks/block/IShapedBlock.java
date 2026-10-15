package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.AxisAlignedBB;

/**
 * A block made of boxes (fences, ladders, stairs...).
 * <p>
 * The same boxes are used for rendering, ray tracing and (by default) collision,
 * so what is drawn always matches what is hit. Boxes are in block coordinates (0 to 1).
 */
public interface IShapedBlock {

	/**
	 * Gets the boxes that make up the block.
	 *
	 * @param renderData the block data, as returned by {@link BlockCoverable#getRenderData}
	 * @return the boxes
	 */
	List<AxisAlignedBB> getShapeBoxes(int renderData);

	/**
	 * Gets the parts the block is drawn with. By default every shape box, taking the cover.
	 *
	 * @param renderData the block data, as returned by {@link BlockCoverable#getRenderData}
	 * @return the parts
	 */
	default List<ShapePart> getRenderParts(int renderData) {
		List<AxisAlignedBB> boxes = getShapeBoxes(renderData);
		List<ShapePart> parts = new ArrayList<ShapePart>(boxes.size());
		for (AxisAlignedBB box : boxes) {
			parts.add(ShapePart.covered(box));
		}
		return parts;
	}

	/**
	 * Gets the collision boxes, which can differ from the shape (fences are 1.5 blocks high).
	 *
	 * @param renderData the block data, as returned by {@link BlockCoverable#getRenderData}
	 * @return the boxes
	 */
	default List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return getShapeBoxes(renderData);
	}

	/**
	 * Gets the boxes that are hit when clicking and outlined, which can be simpler than
	 * the shape (a ladder is clicked as one panel, not rung by rung).
	 *
	 * @param renderData the block data, as returned by {@link BlockCoverable#getRenderData}
	 * @return the boxes
	 */
	default List<AxisAlignedBB> getSelectionBoxes(int renderData) {
		return getShapeBoxes(renderData);
	}

	/**
	 * Gets the data used to draw the item in inventory.
	 *
	 * @param itemDamage the item damage
	 * @return the render data
	 */
	default int getInventoryRenderData(int itemDamage) {
		return 0;
	}

	/**
	 * Whether covers can be added to each side as well as the whole block. Only the stairs allow this,
	 * like 1.7.10; the others only take one cover.
	 */
	default boolean supportsSideCovers() {
		return false;
	}

}
