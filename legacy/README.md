# 1.12.2 sources (port reference)

`legacy/1.12.2` is the 1.12.2 branch as it was when the 26.3 port started (branch `1.12.2` @ 01157de). It is not
compiled. Each block is rewritten for 26.3 under `src/main` and checked against these files.

## Ported to 26.3

| 1.12.2 | 26.3 |
|---|---|
| `tileentity/CbTileEntity` | `blockentity/CbBlockEntity` (same `cbAttrList` / `cbMetadata` / `cbOwner` tags; items in the 26.3 item format, covers also keep their block state) |
| `util/attribute/*` | `util/attribute/*` |
| `block/BlockCoverable` | `block/ICoverableBlock` (the behavior, as default methods), `CoverableSupport` (helpers) and `BlockCoverable` (a Block forwarding to them); left clicks arrive through `CommonEvents`, which knows the face |
| `block/BlockCarpentersBlock` | `block/BlockCarpentersBlock` |
| `item/ItemCarpentersHammer`, `ItemCarpentersChisel` | `item/*` (`canDestroyBlock` keeps them from breaking Carpenter's blocks) |
| `util/registry/ConfigRegistry` | `CbConfig` (startup config; overlays and cover exceptions by item id) |
| `util/handler/DyeHandler`, `OverlayHandler`, `DesignHandler` | `util/handler/*` (dyes and glowstone by tag; the build lists the designs in `designs.json`) |
| `util/protection/*` | `util/protection/*` |
| `renderer/Quad`, `QuadContainer`, `QuadUtil`, `UV`, `util/RotationUtil` | `client/model/*`, `util/RotationUtil` |
| `renderer/RenderPkg` | `client/model/CoverQuadBuilder` |
| `renderer/AbstractBakedModel`, `bakedblock/BakedBlock`, `ModelLoader` | `client/model/CoverableModel` + `client/model/shape/BlockShapes` ("block") |
| `renderer/CoverColorHandler` | `client/model/CoverTints` |
| `util/registry/SpriteRegistry` | `client/model/CbSprites` |
| `block/IShapedBlock`, `ShapePart`, `ShapeTextures`, `BlockSidedShaped`, `block/data/SidedUtil`, `RedstoneData` | same names (shapes become cached VoxelShapes in `BlockCoverable`) |
| `renderer/bakedblock/BakedShapedBlock`, `model/ModelShapedBlock`, `helper/RenderHelper` | `client/model/shape/BlockShapes` ("shaped") + `client/model/CoverableItemModel` |
| `block/BlockCarpentersBarrier`, `Gate`, `Ladder`, `Button`, `Lever`, `Torch` and their `block/data/*Data` | same names |
| `block/BlockCarpentersDoor`, `Hatch`, `GarageDoor`, `block/data/DoorData`, `item/ItemCarpentersDoor` | same names (`getRenderAttributeSource` now returns a position) |
| `block/BlockCarpentersStairs`, `block/data/StairsData`, `block/BlockCarpentersDaylightSensor` | same names |
| `block/BlockCarpentersSlope`, `block/data/SlopeData`, `RotatableData`, `util/block/SlopeShape`, `item/ItemBlockCarpentersSlope` | same names (the slope type is the `carpentersblocks:slope_type` item component, not the item damage) |
| `renderer/bakedblock/BakedSlope`, `helper/RenderHelperSlope` | `client/model/shape/SlopeShapes`, `RenderHelperSlope` ("slope") |
| `network/PacketSlopeSelect`, `util/handler/PacketHandler` | `network/SlopeSelectPayload` (a NeoForge payload) |
| `block/BlockCarpentersCollapsibleBlock`, `util/block/CollapsibleUtil`, `renderer/bakedblock/BakedCollapsibleBlock`, `helper/RenderHelperCollapsible` | same names; the renderer is the "collapsible" shape (`BlockFacing` folded into the block) |
| `block/BlockCarpentersPressurePlate`, `util/block/PressurePlateUtil`, `util/states/*`, `renderer/bakedblock/BakedPressurePlate` | `block/BlockCarpentersPressurePlate` (an IShapedBlock: the state file only held its two boxes) |
| `block/BlockCarpentersFlowerPot`, `util/block/FlowerPotUtil`, `renderer/bakedblock/BakedFlowerPot` | same names; the renderer is the "flower_pot" shape (`client/model/shape/FlowerPotShape`) |
| `block/BlockCarpentersBed`, `item/ItemCarpentersBed` | same names; the bed is a vanilla `AbstractBedBlock` (players only sleep in those) with `ICoverableBlock`, its facing/part/occupied in the blockstate. Bed designs are drawn piece by piece from the whole design texture (`client/model/shape/BedDesignPieces`), not cut into a generated resource pack (`CarpentersBlocksCachedResources`) |
| `block/BlockCarpentersSafe`, `tileentity/TECarpentersSafe` | `block/BlockCarpentersSafe`, `blockentity/SafeBlockEntity` (block entity `carpentersblocks:safe`; a vanilla chest menu; pipes through NeoForge's WorldlyContainerWrapper) |
| `entity/EntityCarpentersTile`, `item/ItemCarpentersTile`, `renderer/entity/RenderCarpentersTile` | `entity/EntityCarpentersTile` (entity `carpentersblocks:carpenters_tile`, same dir/rot/dye/tile/owner tags; old ore dictionary dye names are read), `item/ItemCarpentersTile`, `client/renderer/CarpentersTileRenderer` |
| `BlockCoverable.canSustainPlant` | `ICoverableBlock.coverableCanSustainPlant` (a soil cover or grass/mycelium overlay holds the plants that soil holds, by the 26.3 support tags) |
| Rail slope fill, routable fluids (`BlockCoverable.getRailSlope`, `getRoutedFluidPos`, `RenderPkg`) | `client/model/CoverExtras` (read from the meshing snapshot; config `enableRailSlopeFill`, `enableRoutableFluids`) |

## Still to port

Nothing. 3.5 (1.12.2) worlds convert through Almura-4.0's `CarpentersBlocksConversion` (test legacy package; needs this jar in
`runs/convert-mods`): same block names and block data, attribute items upgraded to the 26.3 item format, the bed's facing and
half into its blockstate, slope items' damage into `carpentersblocks:slope_type`, and Carpenter's Tiles kept as they were.

Each new block needs its blockstate and item definition (`{"type": "carpentersblocks:coverable", "shape": ...}`; boxed
blocks use "shaped"), a loot table and its recipe (with a `neoforge:registered` condition), and its 1.12.2 hardness in
`CbBlocks`. Block data that depends on neighbors goes in `getRenderData(level, pos, cbMetadata)`, which also runs while
chunks mesh: read neighbor data only through `BlockCoverable.getCbMetadataAt`.

Not ported with their blocks: Forge's `rotateBlock` (wrench rotation), and pathfinding through open gates.
