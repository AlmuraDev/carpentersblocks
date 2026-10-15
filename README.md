Carpenter's Blocks
================

Carpenter's Blocks is a Minecraft Forge mod that adds slopes and a range of vanilla-inspired blocks that take on the look of any block you cover them with. Cover a slope in stone bricks, a door in birch planks or a ladder in iron, then add side covers, overlays, dyes and chiseled patterns to build shapes vanilla Minecraft can't.

This branch is built against **Forge 14.23.5.2838** for **Minecraft 1.12.2** and is a work-in-progress. Some features may still behave differently from the 1.7.10 version, and worlds from 1.7.10 cannot be converted.

This build was created and provided by **Almura Development Studios**.

Blocks
================

Every block starts out blank and takes the texture of the block used to cover it.

| Block | What it does |
|---|---|
| Carpenter's Block | A full block. Takes a cover on every side. The base ingredient for the other blocks. |
| Carpenter's Slope | Wedge, oblique interior, oblique exterior, prism and prism wedge slopes, plus more shapes with the hammer. Can be walked up like stairs. |
| Carpenter's Stairs | Stairs in many shapes and directions, with covers on every side. |
| Carpenter's Collapsible Block | A block whose corners can be raised and lowered with the hammer to make uneven ground and roofs. |
| Carpenter's Barrier | Fences and walls, including vanilla-style variants. |
| Carpenter's Gate | A fence gate in several styles. |
| Carpenter's Ladder | Ladders in several styles. |
| Carpenter's Door | Doors in several styles, with handles on both faces. Can act like a wooden or an iron door. |
| Carpenter's Garage Door | A door that rolls up as one piece and fills down to the floor when placed. |
| Carpenter's Hatch | A trapdoor in several styles, including windows and screens. |
| Carpenter's Button, Lever and Pressure Plate | Redstone controls, with options for inverted output and which entities trigger them. |
| Carpenter's Daylight Sensor | A daylight sensor that can be covered. |
| Carpenter's Torch | A torch that can be put out by rain and snow. |
| Carpenter's Bed | A bed whose blanket can be dyed or given a design. |
| Carpenter's Flower Pot | Holds soil and a plant. Bone meal tints the plant, and the pot can be given a design. |
| Carpenter's Safe | A lockable storage block, with panel upgrades of gold, diamond or emerald. |
| Carpenter's Tile | A decorative tile placed on walls, floors and ceilings, with many designs. |

Tools
================

* **Carpenter's Hammer:** changes a block's shape, type or direction with left and right click, and cycles bed and flower pot designs. Sneak and left click to take back the last thing added to a block.
* **Carpenter's Chisel:** cycles through chiseled patterns on a covered block with left and right click. Sneak and left click to remove the pattern.

Decorating
================

* **Covers:** right click a block with almost any full block to cover it.
* **Side covers:** right click a covered face again to cover just that side (on blocks that support it).
* **Overlays:** right click with seeds (grass), a snowball (snow), string (cobweb), vines, wheat (hay) or a mushroom (mycelium).
* **Dyes:** right click with a dye to color a block.
* **Illumination:** right click with glowstone dust to make a block give off light.
* **Ownership:** only the player who placed a block (and server operators) can change it.
* **Rail slopes:** a sloping rail on top of a carpenter's block gets a matching slope underneath.
* **Routable fluids** (off by default): water or lava next to a block shows inside its open space.
* **Connected textures:** Chisel and CTM connected textures connect to covered blocks.

Configuration
================

Settings are in `config/carpentersblocks.cfg`. Every block, the hammer, the chisel and the tile can be turned off, along with each feature above. You can also set the hammer's durability, how smooth slope steps are, how many garage door pieces can link together, and which items act as overlays.

Reporting issues
================

If you run into a crash, a bug, or something that doesn't work like it did in 1.7.10, please open an issue at https://github.com/AlmuraDev/carpentersblocks/issues. Every report will be investigated. Including your `logs/latest.log` or crash report, and the steps to reproduce the problem, helps a lot.
