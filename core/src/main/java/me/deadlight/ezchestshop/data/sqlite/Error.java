package me.deadlight.ezchestshop.data.sqlite;

import me.deadlight.ezchestshop.EzChestShop;

import java.util.logging.Level;

public class Error {
        public static void close(EzChestShop plugin, Exception ex){
            plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
        }

}
