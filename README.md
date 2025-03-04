# AlloySmelter

The Alloy Smelter adds a multi-block alloy smelter, the Alloy Forge. In its basic configuration, it serves as a simple way to increase the speed of ore smelting, but everything is data-driven, allowing you to add recipes as you see fit. These data-driven features are primarily intended for mod and modpack authors.

It is also possible to change the blocks required for the forge, the block tag for the forge `alloy_smelter_blocks`

Example of json recipe:
```
{
  "type": "alloy_smelter:smelting",
  "ingredients": [
    {
      "item": "minecraft:raw_copper",
      "count": 1
    },
    {
      "item": "minecraft:raw_iron",
      "count": 2
    }
  ],
  "result": {
    "item": "minecraft:diamond",
    "count": 2
  },
  "smeltingTime": 200,
  "fuelPerTick": 1
}
```
