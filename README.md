# AlloySmelter

This mod adds a multi-block structure for smelting ores. In its basic configuration, it serves as a simple way to increase the speed of smelting ores, but you can add recipes as you see fit. These features are primarily intended for mod and modpack authors.

Example of json recipe:
```
{
  "type": "alloy_smelter:smelting",
  "ingredients": [
    {
      "ingredient": { "item": "minecraft:raw_copper" },
      "count": 1
    }
  ],
  "result": {
    "id": "minecraft:copper_ingot",
    "count": 1
  },
  "smeltingTime": 150,
  "fuelPerTick": 1
}
```
