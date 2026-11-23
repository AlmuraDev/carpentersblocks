# 1.12.2 sources (port reference)

`legacy/1.12.2` is the 1.12.2 branch as it was when the 26.3 port started (branch `1.12.2` @ 01157de). It is not
compiled. Each block is rewritten for 26.3 under `src/main` and checked against these files.

## Ported to 26.3

| 1.12.2 | 26.3 |
|---|---|
| `tileentity/CbTileEntity` | `blockentity/CbBlockEntity` (same `cbAttrList` / `cbMetadata` / `cbOwner` tags; items in the 26.3 item format, covers also keep their block state) |
| `util/attribute/*` | `util/attribute/*` |
| `block/BlockCoverable` | `block/BlockCoverable` (left clicks arrive through `CommonEvents`, which knows the face) |
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

## Still to port

Blocks: barrier, bed, button, collapsible block, daylight sensor, door, flower pot, garage door, gate, hatch, ladder,
lever, pressure plate, safe, slope, stairs, torch. Items: door, bed, tile (and the tile entity). Also: the slope
selection packet, routable fluids, rail slope fill, bed design textures (`CarpentersBlocksCachedResources`),
`canSustainPlant` for covers, and converting 3.5 (1.12.2) Carpenter's blocks in the Almura world converter.

Each new block needs its blockstate (`{"type": "carpentersblocks:coverable", "shape": ...}`), a shape in
`BlockShapes`, an item model, a loot table and its recipe, and the 1.12.2 hardness in `CbBlocks`.
