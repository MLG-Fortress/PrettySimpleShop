Project page + downloads: https://dev.bukkit.org/projects/prettysimpleshop

# PrettySimpleShop
A pretty simple shop plugin. No signs required. Supports DoubleChests and claim protections!

[**Having problems or issues?❓ Click here for help!💡**](../../issues) | [Support the developer c:](https://r.robomwm.com/patreon) | [Source code](../../)

## Getting started
- To make a shop:
  1. Place a chest
  2. Put some items (of the same type) in it
  3. /setprice
  4. Tada! You now have a shop!
- To view item info or buy from a shop:
  1. Left-click a shop to view item details (hover over text in chat)
  2. Click the item or the shop again, and you will be asked how much you'd like to buy. Simply input the quantity you wish to buy.
  - Or if you wish to use a command, /buy as much as you want.
  
  
- Revenue from sales are stored in the shop, and are transferred to the player when they open or destroy the shop.
- Integrates nicely with area protection plugins.
  - Only players who can open the shop can change its price, collect revenue, and create new shops in that area.
  - All players are able to buy from a shop regardless of area protections.

> `<EmpathyHeals>` no sign formats to type, no commands to buy, respects protection plugins, can sell custom items, nice holographic displays, keeps a history of sales, it's nearly perfect

![](https://i.imgur.com/j15bGIw.png)
![](https://i.imgur.com/Y2M8sZO.png)
![](https://i.imgur.com/UCcBvE5.png)
![](https://i.imgur.com/blcPnT0.png)

## Commands
- /shop
  - Prints a configurable help message that informs players how to create a shop and how to buy from other shops.
- /setprice
  - Sets the price per item of a shop.
- /buy
  - Buys items from the selected shop. 
  - If configured, the player is asked to confirm their transaction, in case they made a mistake and to review the total transaction cost.

## Features
- Supports DoubleChests! (Wasn't a simple task.)
- Supports any block that is a Nameable Container - e.g. trapped chests, shulker boxes, even dispensers and furnaces if you so desire.
- Records shop sales, so you can see how popular a shop is.
- Supports custom items. Displays custom item name and lore.
- Players don't even _need_ to know any commands to buy items. Clicking a shop twice will prompt the player to input the quantity they wish to buy from the shop.
- **No sign formats to memorize!**
- All shop data (prices, items, and revenue) is stored in the world! No messy, potentially-corruptible storage files!
  - Shops are "future-proof" since the items for sale are stored in the actual chest and are converted by Mojang whenever they update blocks/items.
- It's easy to change the item you're selling in a shop. Just take out the old items and put the new ones in.
- Confirmation system helps players avoid mistakes and review the transaction before confirming the purchase.
- Uses Minecraft item names if you're using [Paper](https://papermc.io)
- Supports all region protection plugins! No need to "allow use" or anything silly like that just to allow players to buy.
- Uses your economy's format and symbol.
- Nearly every message is configurable.
- Chests named "shop" (or whatever you have set in the config) will help prompt players to create a shop with that chest. Useful if you "sell" shops to players.

[**Having problems or issues?❓ Click here for help!💡**](../../issues) | [Support the developer c:](https://r.robomwm.com/patreon) | [Source code](../../)
