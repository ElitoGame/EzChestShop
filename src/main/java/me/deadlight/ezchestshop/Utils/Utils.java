package me.deadlight.ezchestshop.Utils;
import me.deadlight.ezchestshop.Commands.EcsAdmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Utils.Objects.TransactionLogObject;
import org.bukkit.*;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static List<Object> onlinePackets = new ArrayList<>();

    public static String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void storeItem(ItemStack item, PersistentDataContainer data) throws IOException {

        String encodedItem = encodeItem(item);
        if (encodedItem != null) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING, encodedItem);
        }


    }

    public static String encodeItem(ItemStack item) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeObject(item);

            os.flush();
            byte[] rawData = io.toByteArray();

            String encodedData = Base64.getEncoder().encodeToString(rawData);

            os.close();
           return encodedData;

        } catch (IOException ex) {
            System.out.println(ex);
            return null;
        }
    }

    public static ItemStack getItem(String encodedItem) {

        byte[] rawData = Base64.getDecoder().decode(encodedItem);

        try {

            ByteArrayInputStream io = new ByteArrayInputStream(rawData);
            BukkitObjectInputStream in = new BukkitObjectInputStream(io);

            ItemStack thatItem = (ItemStack) in.readObject();

            in.close();

            return thatItem;

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
            return null;
        }

    }

    /**
     * Get the Inventory of the given Block if it is a Chest, Barrel or any Shulker
     * @param block
     * @return
     */
    public static Inventory getBlockInventory(Block block) {
        if (block.getType() == Material.CHEST) {
            return  ((Chest) block.getState()).getInventory();
        } else if (block.getType() == Material.BARREL) {
            return  ((Barrel) block.getState()).getInventory();
        }
        else if (isShulkerBox(block)) {
            return  ((ShulkerBox) block.getState()).getInventory();
        }
        else return null;
    }
    /**
     * Check if the given Block is a Shulker box (dye check)
     * @param block
     * @return
     */
    public static boolean isShulkerBox(Block block) {
        return isShulkerBox(block.getType());
    }

    /**
     * Check if the given Material is a Shulker box (dye check)
     * @param type
     * @return
     */
    public static boolean isShulkerBox(Material type) {
        return Arrays.asList(Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
                Material.BLACK_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
                Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX).contains(type);
    }

    /**
     * Check if the given Block is a applicable Shop.
     * @param block
     * @return
     */
    public static boolean isApplicableContainer(Block block) {
        return isApplicableContainer(block.getType());
    }

    /**
     * Check if the given Material is a applicable Shop.
     * @param type
     * @return
     */
    public static boolean isApplicableContainer(Material type) {
        return (type == Material.CHEST && Config.container_chests)
                || (type == Material.TRAPPED_CHEST && Config.container_trapped_chests)
                || (type == Material.BARREL && Config.container_barrels)
                || (isShulkerBox(type) && Config.container_shulkers);
    }


    public static void reloadLanguages() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
        EzChestShop.setLanguages(fc);
        LanguageManager newLanguage = new LanguageManager();
        MainCommands.updateLM(newLanguage);
        ChatListener.updateLM(newLanguage);
        EcsAdmin.updateLM(newLanguage);
    }

    //this one checks for the config.yml ima make one for language.yml
    public static void checkForConfigYMLupdate() throws IOException {

        //update 1.3.3 new config file model update constructed by ElitoGame
        boolean isOldConfigModel = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).isBoolean("show-holograms");
        //if true, then we have to implement the new config model and delete old ones
        if (isOldConfigModel) {
            //getting current values of configs
            //show-holograms
            boolean show_holograms = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getBoolean("show-holograms");
            String hologram_first_line = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getString("hologram-first-line");
            String hologram_second_line = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getString("hologram-second-line");
            int hologram_disappearance_delay = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getInt("hologram-disappearance-delay");

            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));

            fc.set("show-holograms", null);
            fc.set("hologram-first-line", null);
            fc.set("hologram-second-line", null);
            fc.set("hologram-disappearance-delay", null);

            fc.set("shops.hologram.show-holograms", show_holograms);
            fc.set("shops.hologram.hologram-first-line", hologram_first_line);
            fc.set("shops.hologram.hologram-second-line", hologram_second_line);
            fc.set("shops.hologram.hologram-disappearance-delay", hologram_disappearance_delay);
            //new hologram settings:
            fc.set("shops.hologram.distance.toggled", true);
            fc.set("shops.hologram.distance.range", 10.0);

            //new containers:
            fc.set("shops.container.chests", true);
            fc.set("shops.container.trapped-chests", true);
            fc.set("shops.container.barrels", true);
            fc.set("shops.container.shulkers", true);

            //new commands section:
            fc.set("commands.alias.ecs-shop", true);
            fc.set("commands.alias.ecsadmin-adminshop", true);

            //new permissions section:
            fc.set("permissions.create-shops", false);

            //new economy config section
            fc.set("economy.server-currency", "$");

            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();

        }

        //well then its already an updated config, no need to change

    }

    public static void checkForLanguagesYMLupdate() throws IOException {

        //update 1.2.8 Languages
        boolean result = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml")).isString("commandmsg-negativeprice");
        boolean update1_3_0 = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml")).isString("settingsButton");
        boolean update1_4_0 = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml")).isString("copiedShopSettings");
        if (!result) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            //new values that were added in update 1.2.8
            fc.set("commandmsg-negativeprice", "&cNegative price? but you have to use positive price...");
            fc.set("commandmsg-notenoughargs", "&cYou haven't provided enough arguments! \\n &cCorrect usage: /ecs create (Buy price) (Sell price)");
            fc.set("commandmsg-consolenotallowed", "&cYou are not allowed to execute any command from console.");
            fc.set("commandmsg-help", "&7- &c/ecs create (Buy Price) (Sell Price) &7| Create a chest shop by looking at a chest and having the item that you want to sell in your hand. \n &7- &c/ecs remove &7| Removes the chest shop that you are looking at \n &7Eazy right? :)");
            fc.set("commandmsg-alreadyashop", "&cThis chest is already a shop!");
            fc.set("commandmsg-shopcreated", "&aYou have successfully created a chest shop!");
            fc.set("commandmsg-holdsomething", "&cPlease hold something in your main hand!");
            fc.set("commandmsg-notallowdtocreate", "&cYou are not allowed to create/remove a chest shop in this location.");
            fc.set("commandmsg-notchest", "&cThe block that you are looking at is not supported type of chest/is not a chest.");
            fc.set("commandmsg-lookatchest", "&cPlease look at a chest.");
            fc.set("commandmsg-csremoved", "&eThis chest shop successfully removed.");
            fc.set("commandmsg-notowner", "&aYou are not the owner of this chest shop!");
            fc.set("commandmsg-notachestorcs", "&cThe block that you are looking at is not a chest/or this is not a chest shop.");
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));

            reloadLanguages();
            EzChestShop.getPlugin().logConsole("&c[&eEzChestShop&c]&r &bNew languages.yml generated...");
        }

        if (!update1_3_0) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            //for update 1.3.0
            fc.set("settingsButton", "&b&lSettings");
            fc.set("disabledButtonTitle", "&cDisabled");
            fc.set("disabledButtonLore", "&7This option is disabled by \n &7the shop owner.");
            fc.set("transactionButtonTitle", "&aTransaction logs");
            fc.set("backToSettingsButton", "&eBack to settings");
            fc.set("transactionPaperTitleBuy", "&aBuy | %player%");
            fc.set("transactionPaperTitleSell", "&cSell | %player%");
            fc.set("transactionPaperLoreBuy", "&7Total Price: %price% \n &7Quantity: %count% \n &7Transaction Type: &aBought from you \n &e%time%");
            fc.set("transactionPaperLoreSell", "&7Total Price: %price% \n &7Quantity: %count% \n &7Transaction Type: &cSold to you \n &e%time%");
            fc.set("lessthanminute", "&eless than a minute ago");
            fc.set("minutesago", "&e%minutes% minute(s) ago");
            fc.set("hoursago", "&e%hours% hour(s) ago");
            fc.set("daysago", "&e%days% days ago");
            fc.set("adminshopguititle", "&cAdmin shop");
            fc.set("settingsGuiTitle", "&b&lSettings");
            fc.set("latestTransactionsButton", "&aLatest Transactions");
            fc.set("toggleTransactionMessageButton", "&eToggle Transaction Message");
            fc.set("statusOn", "&aOn");
            fc.set("statusOff", "&cOff");
            fc.set("toggleTransactionMessageButtonLore", "&7Current status: %status% \n &7If you keep this option on, \n &7you will recieve transaction \n &7messages in chat whenever someone \n &7buy/sell something from this shop");
            fc.set("toggleTransactionMessageOnInChat", "&7Toggle Transaction Messages: &aON");
            fc.set("toggleTransactionMessageOffInChat", "&7Toggle Transaction Messages: &cOFF");
            fc.set("disableBuyingButtonTitle", "&eDisable Buying");
            fc.set("disableBuyingButtonLore", "&7Current status: %status% \n &7If you keep this option on, \n &7the shop won't let anyone buy \n &7from your chest shop.");
            fc.set("disableBuyingOnInChat", "&7Disable Buying: &aON");
            fc.set("disableBuyingOffInChat", "&7Disable Buying: &cOFF");
            fc.set("disableSellingButtonTitle", "&eDisable Selling");
            fc.set("disableSellingButtonLore", "&7Current status: %status% \n &7If you keep this option on, \n &7the shop won't let anyone sell \n &7anything to the shop.");
            fc.set("disableSellingOnInChat", "&7Disable Selling: &aON");
            fc.set("disableSellingOffInChat", "&7Disable Selling: &cOFF");
            fc.set("shopAdminsButtonTitle", "&eShop admins");
            fc.set("nobodyStatusAdmins", "&aNobody");
            fc.set("shopAdminsButtonLore", "&7You can add/remove admins to \n &7your chest shop. Admins are able to \n &7access the shop storage & access certain \n &7settings (everything except share income \n &7and add/remove-ing admins). \n &aLeft Click &7to add an admin \n &cRight Click &7to remove an admin \n &7Current admins: %admins%");
            fc.set("addingAdminWaiting", "&ePlease enter the name of the person you want to add to the list of admins.");
            fc.set("removingAdminWaiting", "&ePlease enter the name of the person you want to remove from the list of admins.");
            fc.set("shareIncomeButtonTitle", "&eShared income");
            fc.set("shareIncomeButtonLore", "&7Current status: %status% \n &7If you keep this option on, \n &7the profit of ONLY sales, will be \n &7shared with admins as well.");
            fc.set("sharedIncomeOnInChat", "&7Shared income: &aON");
            fc.set("sharedIncomeOffInChat", "&7Shared income: &cOFF");
            fc.set("backToShopGuiButton", "&eBack to shop");
            fc.set("selfAdmin", "&cYou can't add or remove yourself in the admins list!");
            fc.set("noPlayer", "&cThis player doesn't exist or haven't played here before.");
            fc.set("sucAdminAdded", "&e%player% &asuccessfully added to the admins list.");
            fc.set("alreadyAdmin", "&cThis player is already in the admins list!");
            fc.set("sucAdminRemoved", "&e%player% &asuccessfully removed from the admins list.");
            fc.set("notInAdminList", "&cThis player is not in the admins list!");
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            reloadLanguages();
            EzChestShop.getPlugin().logConsole("&c[&eEzChestShop&c]&r &bNew languages.yml generated... (1.3.0V)");
        }
        if (!update1_4_0) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            //for update 1.4.0
            fc.set("copiedShopSettings", "&6Copied &7shop settings!");
            fc.set("pastedShopSettings", "&ePasted &7shop settings!");
            fc.set("clearedAdmins", "&cRemoved all admins from this shop.");
            fc.set("maxShopLimitReached", "&cMaximum shop limit reached: %shoplimit%!");
            fc.set("buyingIsDisabled", "&cBuying is disabled in this shop.");
            fc.set("sellingIsDisabled", "&cSelling is disabled in this shop.");
            fc.set("gui-customAmountSign-title", "&eCustom Buy/Sell");
            fc.set("gui-customAmountSign-lore", "&7Buy or Sell in custom amount \n &d \n &aLeft click for custom Buy \n &cRight click for custom Sell \n &d \n &7You can totally &abuy &e%buycount%&7 of this item. \n &7You can totally &csell &e%sellcount%&7 of this item.");
            fc.set("signEditorGui-buy", "&a^^ Max: %max% ^^ \n &bInsert your \n &bdesired amount");
            fc.set("signEditorGui-sell", "&c^^ Max: %max% ^^ \n &bInsert your \n &bdesired amount");
            fc.set("wrongInput", "&cWrong input(Or probably a very large number), please insert a number!");
            fc.set("enterTheAmount", "&ePlease write your desired amount in the sign");
            fc.set("unsupportedInteger", "&cHey!, the amount cannot be zero or negative obviously.");
            fc.set("openingShopProblem", "&cThere is a problem in opening this chest shop, Please contact administrator and check the console.");

            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            reloadLanguages();
            EzChestShop.getPlugin().logConsole("&c[&eEzChestShop&c]&r &bNew languages.yml generated... (1.4.0V)");
        }
    }

    public static HashMap<String, Block> blockBreakMap = new HashMap<>();

    public static LanguageManager lm;

    public static List<UUID> getAdminsList(PersistentDataContainer data) {

        String adminsString = data.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        //UUID@UUID@UUID
        if (adminsString.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        } else {
            String[] stringUUIDS = adminsString.split("@");
            List<UUID> finalList = new ArrayList<>();
            for (String uuidInString : stringUUIDS) {
                finalList.add(UUID.fromString(uuidInString));
            }
            return finalList;
        }
    }


    public static List<TransactionLogObject> getListOfTransactions(Block containerBlock) {
        TileState state = ((TileState)containerBlock.getState());
        PersistentDataContainer data = state.getPersistentDataContainer();
        String wholeString = data.get(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING);
        if (wholeString == null || wholeString.equalsIgnoreCase("none")) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
            state.update();
            return new ArrayList<>();
        } else {
            List<TransactionLogObject> logObjectList = new ArrayList<>();
            String[] logs = wholeString.split("#");
            for (String log : logs) {
                String[] datas = log.split("@");
                String pname = datas[0];
                String type = datas[1];
                String price = datas[2];
                String time = datas[3];
                int count = Integer.parseInt(datas[4]);
                logObjectList.add(new TransactionLogObject(type, pname, price, time, count));

            }
            return logObjectList;

        }
    }

    public static String getFinalItemName(ItemStack item) {
        String itemname = "Error";
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                itemname = Utils.color(item.getItemMeta().getDisplayName());
            } else if (item.getItemMeta().hasLocalizedName()) {
                itemname = item.getItemMeta().getLocalizedName();
            } else {
                itemname = Utils.capitalizeFirstSplit(item.getType().toString());
            }
        } else {
            itemname = Utils.capitalizeFirstSplit(item.getType().toString());
        }
        return Utils.color(itemname);
    }

    public static Location getSpawnLocation(Chest chest) {
        return chest.getLocation().clone().add(0.5, 1, 0.5);
    }


    public static boolean is1_17 = false;
    public static boolean is1_17_1 = false;
    public static boolean family1_17 = false;

    /**
     * Convert a Location to a String
     *
     * @param loc
     * @return
     */
    public static String LocationtoString(Location loc) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        sloc += ("X:" + loc.getX() + ",");
        sloc += ("Y:" + loc.getY() + ",");
        sloc += ("Z:" + loc.getZ());
        return sloc;
    }

    /**
     * Convert a Location to a String with the Location rounded as defined via the decimal argument
     *
     * @param loc
     * @param decimals
     * @return
     */
    public static String LocationRoundedtoString(Location loc, int decimals) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        sloc += ("X:" + round(loc.getX(), decimals) + ",");
        sloc += ("Y:" + round(loc.getY(), decimals) + ",");
        sloc += ("Z:" + round(loc.getZ(), decimals));
        return sloc;
    }

    /**
     * Convert a String to a Location
     *
     * @param sloc
     * @return
     */
    public static Location StringtoLocation(String sloc) {
        if (sloc == null)
            return null;
        String[] slocs = sloc.split(",");
        World w = Bukkit.getWorld(slocs[0].split(":")[1]);
        Double x = Double.valueOf(slocs[1].split(":")[1]);
        Double y = Double.valueOf(slocs[2].split(":")[1]);
        Double z = Double.valueOf(slocs[3].split(":")[1]);
        Location loc = new Location(w, x, y, z);

        if (sloc.contains("Yaw:") && sloc.contains("Pitch:")) {
            loc.setYaw(Float.valueOf(slocs[4].split(":")[1]));
            loc.setPitch(Float.valueOf(slocs[5].split(":")[1]));
        }
        return loc;
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Check if a String can be safely converted into a numeric value.
     *
     * @param strNum
     * @return
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Get the max permission level of a permission object (e.g. player)
     *
     * @param permissible a object using the Permissible System e.g. a Player.
     * @param permission a Permission String to check e.g. ecs.shops.limit.
     * @return the maximum int found, unless user is an Operator or has the ecs.admin permission.
     * Then the returned result will be -1
     */
    public static int getMaxPermission(Permissible permissible, String permission) {
        if (permissible.isOp() || permissible.hasPermission("ecs.admin"))
            return -1;

        final AtomicInteger max = new AtomicInteger();

        permissible.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission)
                .map(String::toLowerCase).filter(value -> value.startsWith(permission))
                .map(value -> value.replace(permission, "")).forEach(value -> {
            if (value.equalsIgnoreCase("*")) {
                max.set(-1);
                return;
            }

            if (max.get() == -1)
                return;

            try {
                int amount = Integer.parseInt(value);

                if (amount > max.get())
                    max.set(amount);
            } catch (NumberFormatException ignored) {
            }
        });

        return max.get();
    }


    //


    /**
     * Split a String by "_" and capitalize each First word, then join them together
     * via " "
     *
     * @param string
     * @return
     */
    public static String capitalizeFirstSplit(String string) {
        string = string.toLowerCase();
        String n_string = "";
        for (String s : string.split("_")) {
            n_string += s.subSequence(0, 1).toString().toUpperCase()
                    + s.subSequence(1, s.length()).toString().toLowerCase() + " ";
        }
        return n_string;
    }


    public static boolean hasEnoughSpace(Player player, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (content.isSimilar(item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }

    public static int playerEmptyCount(ItemStack[] storageContents, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : storageContents) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (content.isSimilar(item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }
        return emptySlots;
    }

    public static int containerEmptyCount(ItemStack[] storageContents, ItemStack item) {

        if (storageContents == null) {
            return Integer.MAX_VALUE;
        }

        int emptySlots = 0;
        for (ItemStack content : storageContents) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (content.isSimilar(item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }
        return emptySlots;
    }

    public static int howManyOfItemExists(ItemStack[] itemStacks, ItemStack mainItem) {

        if (itemStacks == null) {
            return Integer.MAX_VALUE;
        }


        int amount = 0;
        for (ItemStack item : itemStacks) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (item.isSimilar(mainItem)) {
                amount += item.getAmount();
            }

        }
        return amount;

    }


    public static boolean containerHasEnoughSpace(Inventory container, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : container.getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (content.isSimilar(item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }

    public static boolean amountCheck(int amount) {
        if (amount == 0) {
            return false;
        }

        if (amount < 0) {
            return false;
        }
        return true;
    }

    public static List<String> calculatePossibleAmount(OfflinePlayer offlineCustomer, OfflinePlayer offlineSeller, ItemStack[] playerInventory, ItemStack[] storageInventory, double eachBuyPrice, double eachSellPrice, ItemStack itemStack) {

        List<String> results = new ArrayList<>();

        String buyCount = calculateBuyPossibleAmount(offlineCustomer, playerInventory, storageInventory, eachBuyPrice, itemStack);
        String sellCount = calculateSellPossibleAmount(offlineSeller, playerInventory, storageInventory, eachSellPrice, itemStack);

        results.add(buyCount);
        results.add(sellCount);
        return results;
    }


    public static String calculateBuyPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory, ItemStack[] storageInventory, double eachBuyPrice, ItemStack itemStack) {
        //I was going to run this in async but maybe later...
                int possibleCount = 0;
                double buyerBalance = EzChestShop.getEconomy().getBalance(offlinePlayer);
                int emptyCount = playerEmptyCount(playerInventory, itemStack);
                int howManyExists = howManyOfItemExists(storageInventory, itemStack);

                for (int num = 0; num < emptyCount; num++) {
                    if (possibleCount + 1 > howManyExists) {
                        break;
                    }
                    possibleCount += 1;
                }

                int result = 0;
                for (int num = 0; num < possibleCount; num++) {
                    result += 1;
                    if ((num + 1) * eachBuyPrice > buyerBalance) {
                        return String.valueOf(num);
                    }
                }

        return String.valueOf(result);
    }
    public static String calculateSellPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory, ItemStack[] storageInventory, double eachSellPrice, ItemStack itemStack) {

                int possibleCount = 0;
                double buyerBalance;
                if (offlinePlayer == null) {
                    buyerBalance = Double.MAX_VALUE;
                } else {
                    if (offlinePlayer.hasPlayedBefore()) {
                        buyerBalance = EzChestShop.getEconomy().getBalance(offlinePlayer);
                    } else {
                        buyerBalance = 0;
                    }
                }
                int emptyCount = containerEmptyCount(storageInventory, itemStack);
                int howManyExists = howManyOfItemExists(playerInventory, itemStack);

                for (int num = 0; num < emptyCount; num++) {
                    if (possibleCount + 1 > howManyExists) {
                        break;
                    }
                    possibleCount += 1;
                }

                int result = 0;
                for (int num = 0; num < possibleCount; num++) {
                    result += 1;
                    if ((num + 1) * eachSellPrice > buyerBalance) {
                        return String.valueOf(num);
                    }
                }

        return String.valueOf(result);
    }

    public static boolean isInteger(String str) {
        try {
            int num = Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }




}
