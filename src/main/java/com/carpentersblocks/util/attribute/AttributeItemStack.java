package com.carpentersblocks.util.attribute;

import org.apache.logging.log4j.Level;

import com.carpentersblocks.util.ModLogger;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Stores attribute and unique identifier for validation purposes.
 */
public class AttributeItemStack extends AbstractAttribute<ItemStack> {

	private static final String TAG_RESOURCE_LOC_ID = "cbResourceLoc";
    private ResourceLocation _resourceLoc;

    public AttributeItemStack() {
    	super(null, null, null);
    }

    public AttributeItemStack(EnumAttributeLocation location, EnumAttributeType type, ItemStack itemStack) {
    	super(location, type, itemStack);
        if (itemStack != null && !itemStack.isEmpty()) {
            _resourceLoc = itemStack.getItem().getRegistryName();
        }
	}

    public ResourceLocation getResourceLocation() {
        return _resourceLoc;
    }

    /**
     * Whether the item could not be loaded (for example its mod was removed).
     */
    public boolean isMissing() {
        return getModel() == null || getModel().isEmpty();
    }

	@Override
	public void writeModelToNBT(NBTTagCompound nbt) {
		getModel().writeToNBT(nbt);
		if (_resourceLoc != null) {
			nbt.setString(TAG_RESOURCE_LOC_ID, _resourceLoc.toString());
		}
	}

	@Override
	public void readModelFromNBT(NBTTagCompound nbt) {
		// Unknown items load as an empty stack in 1.12
        ItemStack itemStack = new ItemStack(nbt);
        setModel(itemStack);
        _resourceLoc = new ResourceLocation(nbt.getString(TAG_RESOURCE_LOC_ID));
        if (itemStack.isEmpty()) {
            ModLogger.log(Level.WARN, "Block attribute '" + _resourceLoc + "' could not be loaded. Was a mod removed?");
        }
	}

	@Override
	public String getModelIdentifier() {
		return IDENT_ATTR_ITEMSTACK;
	}

	@Override
	public AbstractAttribute copy() {
		return new AttributeItemStack(_location, _type, _model.copy());
	}

}
