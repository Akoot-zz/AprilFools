package com.akoot.plugins.aprilfools;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class AprilFools extends JavaPlugin implements CommandExecutor, TabExecutor, Listener {

    private Random random;
    private boolean enabled;
    private double chance = 0.3;

    public static String scrambleName(String name) {
        char[] chars = name.toCharArray();
        List<Integer> indexes = new ArrayList<>();
        indexes.add(0);
        for (int i = 1; i < chars.length; i++) {
            if (Character.isUpperCase(chars[i]) || Character.isDigit(chars[i])) {
                if (i < chars.length - 1) {
                    char nextChar = chars[i + 1];
                    if (!Character.isUpperCase(nextChar)) {
                        indexes.add(i);
                    }
                }
            }
        }

        List<String> words = new ArrayList<>();
        for (int i = 0; i < indexes.size(); i++) {
            int begin = indexes.get(i);
            int end = i + 1;
            if (end < indexes.size()) {
                end = indexes.get(end);
            } else {
                end = name.length();
            }
            String word = name.substring(begin, end);
            words.add(word);
        }

        Collections.shuffle(words);
        return String.join("", words);
    }

    @Override
    public void onEnable() {

        random = new Random();
        enabled = true;

        getCommand("fooled").setExecutor(this);
        getCommand("fool").setExecutor(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fooled")) {
            if (sender instanceof Player) {
                List<String> fooled = getConfig().getStringList("fooled");
                String uuid = ((Player) sender).getUniqueId().toString();
                if (!fooled.contains(uuid)) {
                    fooled.add(uuid);
                    getConfig().set("fooled", fooled);
                    sender.sendMessage("You are no longer fooled!");
                } else {
                    sender.sendMessage("You are already un-fooled!");
                }
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("fool")) {
            if (args.length == 1) {
                enabled = (Boolean.parseBoolean(args[0].toLowerCase()));
                sender.sendMessage(ChatColor.LIGHT_PURPLE + (enabled ? "En" : "Dis") + "abled fool mode");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> suggestions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("fool")) {
            if (args.length == 1) {
                suggestions.add("true");
                suggestions.add("false");
            }
            return suggestions;
        } else if (command.getName().equalsIgnoreCase("fooled")) {
            return suggestions;
        }
        return null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (enabled) {
            Player player = event.getPlayer();
            if (!getConfig().getStringList("fooled").contains(player.getUniqueId().toString())) {
                String message = event.getMessage();
                String format = event.getFormat();
                String name = player.getName();

                if (message.contains("annoying") || message.contains("stop")) {
                    player.sendMessage(ChatColor.DARK_GREEN + "April Fools! Too annoying? Type " + ChatColor.GRAY + "/fooled");
                }

                if (Math.random() <= chance) {
                    name = scrambleName(name);
                }

                if (Math.random() <= chance) {
                    name = ChatColor.values()[random.nextInt(ChatColor.values().length)] + name + ChatColor.RESET;
                }

                String newMessage = "";

                if (Math.random() <= chance) {
                    chance = chance / 4.0;
                    for (String s : message.split(" ")) {
                        if (Math.random() <= chance) {
                            newMessage += "\"" + s + "\"";
                        } else if (Math.random() <= chance) {
                            newMessage += "*" + s + "* ";
                        } else if (Math.random() <= chance) {
                            newMessage += s.toUpperCase();
                        } else if (Math.random() <= chance) {
                            newMessage += ChatColor.UNDERLINE + s + ChatColor.RESET;
                        } else if (Math.random() <= chance) {
                            newMessage += ChatColor.STRIKETHROUGH + s + ChatColor.RESET;
                        } else if (Math.random() <= chance) {
                            newMessage += ChatColor.BOLD + s + ChatColor.RESET;
                        } else if (Math.random() <= chance) {
                            newMessage += ChatColor.ITALIC + s + ChatColor.RESET;
                        } else if (Math.random() <= chance) {
                            newMessage += s + "!";
                        } else if (Math.random() <= chance) {
                            newMessage += s + "...";
                        } else if (Math.random() <= chance) {
                            newMessage += s + "?";
                        } else {
                            newMessage += s;
                        }
                        newMessage += " ";
                    }
                    chance = chance * 4.0;
                } else if (Math.random() <= chance) {
                    for (String s : message.split(" ")) {
                        newMessage += s.substring(0, 1).toUpperCase() + s.substring(1) + " ";
                    }
                } else if (Math.random() <= chance) {
                    newMessage = message.toUpperCase();
                } else {
                    newMessage = message;
                }

                newMessage = newMessage.trim();

                if (Math.random() <= chance) {
                    newMessage = newMessage + "!!!!".substring(0, random.nextInt(4));
                }

                event.setFormat(format.replace("%1$s", name));
                event.setMessage(newMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerHurt(EntityDamageEvent event) {
        if (enabled) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (!isFooled(player)) {
                    chance = chance * 2.0;
                    if (Math.random() <= chance) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 0.55F, 0.55F);
                    }
                    chance = chance / 2.0;
                }
            }
        }
    }

    public boolean isFooled(Player player) {
        return getConfig().getStringList("fooled").contains(player.getUniqueId().toString());
    }
}
