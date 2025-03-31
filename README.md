# AlloySmelter

This mod adds a multi-block structure for smelting ores. In its basic configuration, it serves as a simple way to increase the speed of smelting ores, but you can add recipes as you see fit. These features are primarily intended for mod and modpack authors.

Example of json recipe:
```
{
  "type": "alloy_smelter:smelting",
  "ingredients": [
    {
      "ingredient": "minecraft:raw_copper",
      "count": 1
    },
    {
      "ingredient": "minecraft:raw_iron",
      "count": 2
    }
  ],
  "result": {
    "id": "minecraft:diamond",
    "count": 2
  },
  "smeltingTime": 200,
  "fuelPerTick": 1,
  "requiredTier": 2
}
```

https://modrinth.com/user/TheFogIOF
