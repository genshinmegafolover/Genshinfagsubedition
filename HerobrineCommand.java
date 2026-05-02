package dev.local.herobrine.command;

import dev.local.herobrine.HerobrinePlugin;
import dev.local.herobrine.haunt.HauntEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HerobrineCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ROOT = List.of("reload", "trigger", "stats");

    private final HerobrinePlugin plugin;

    public HerobrineCommand(HerobrinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("herobrine.admin")) {
            sender.sendMessage("\u00a7cNo permission.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage("\u00a7aHerobrine config reloaded.");
                return true;
            }
            case "stats" -> {
                sender.sendMessage("\u00a7eHerobrine session stats:");
                plugin.dispatcher().stats().forEach((id, count) ->
                        sender.sendMessage(" \u00a77- \u00a7f" + id + ": \u00a7b" + count.get()));
                sender.sendMessage(" \u00a77- \u00a7ftemp_block_active: \u00a7b" + plugin.tempBlockService().activeCount());
                return true;
            }
            case "trigger" -> {
                if (args.length < 2) {
                    sender.sendMessage("\u00a7cUsage: /herobrine trigger <player> [event]");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("\u00a7cPlayer not found: " + args[1]);
                    return true;
                }
                if (args.length >= 3) {
                    boolean ok = plugin.dispatcher().fireById(target, args[2]);
                    sender.sendMessage(ok
                            ? "\u00a7aFired event '" + args[2] + "' on " + target.getName()
                            : "\u00a7cEvent did not fire (unknown id, disabled, or no valid target spot).");
                } else {
                    boolean ok = plugin.dispatcher().tryFireRandom(target);
                    sender.sendMessage(ok
                            ? "\u00a7aFired a random event on " + target.getName()
                            : "\u00a7cNo event fired (cooldown or none eligible).");
                }
                return true;
            }
            default -> {
                sendUsage(sender);
                return true;
            }
        }
    }

    private void sendUsage(CommandSender s) {
        s.sendMessage("\u00a7eUsage: /herobrine <reload|trigger <player> [event]|stats>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("herobrine.admin")) return List.of();
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String lo = args[0].toLowerCase(Locale.ROOT);
            for (String s : ROOT) if (s.startsWith(lo)) out.add(s);
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trigger")) {
            String lo = args[1].toLowerCase(Locale.ROOT);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(lo)) out.add(p.getName());
            }
            return out;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("trigger")) {
            String lo = args[2].toLowerCase(Locale.ROOT);
            for (HauntEvent e : plugin.dispatcher().events()) {
                if (e.id().startsWith(lo)) out.add(e.id());
            }
            return out;
        }
        return out;
    }
}
