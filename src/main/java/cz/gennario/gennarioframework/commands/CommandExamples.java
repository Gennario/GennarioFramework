package cz.gennario.gennarioframework.commands;

import cz.gennario.gennarioframework.commands.argument.Argument;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Příklady použití nové GennarioCommand knihovny.
 * Tato třída obsahuje ukázky jak vytvářet příkazy - od jednoduchých po pokročilé.
 *
 * <p>Stačí zavolat metody z onEnable() pluginu:</p>
 * <pre>
 *     CommandExamples.registerAll(this);
 * </pre>
 */
public class CommandExamples {

    /**
     * Zaregistruje všechny ukázkové příkazy.
     */
    public static void registerAll(JavaPlugin plugin) {
        simpleCommand(plugin);
        commandWithSubCommands(plugin);
        commandWithArguments(plugin);
        advancedCommand(plugin);
        nestedSubCommands(plugin);
        dynamicTabComplete(plugin);
        customMessages(plugin);
    }

    // ================================================================
    // 1) JEDNODUCHÝ PŘÍKAZ - žádné argumenty, jen executes
    // ================================================================

    /**
     * /ping → odpoví "Pong!"
     */
    public static void simpleCommand(JavaPlugin plugin) {
        GennarioCommand.create("ping", plugin)
                .description("Responds with Pong!")
                .aliases("pong")
                .executes(ctx -> {
                    ctx.reply("&aPong! &7Latency: &f" + (ctx.isPlayer() ? ctx.player().getPing() + "ms" : "N/A"));
                })
                .register();
    }

    // ================================================================
    // 2) PŘÍKAZ S SUB-COMMANDS
    // ================================================================

    /**
     * /myplugin reload     → Reload konfigurace
     * /myplugin info       → Info o pluginu
     * /myplugin help       → Auto-generovaný help
     */
    public static void commandWithSubCommands(JavaPlugin plugin) {
        GennarioCommand.create("myplugin", plugin)
                .description("Main plugin command")
                .aliases("mp", "mpl")
                .permission("myplugin.admin")
                .autoHelp() // automaticky generuje /myplugin help

                .subCommand("reload")
                    .description("Reload the plugin configuration")
                    .permission("myplugin.reload")
                    .aliases("rl")
                    .executes(ctx -> {
                        // ... reload logika ...
                        ctx.reply("&aConfiguration reloaded successfully!");
                    })
                    .done()

                .subCommand("info")
                    .description("Show plugin information")
                    .executes(ctx -> {
                        ctx.reply("&6MyPlugin &7v1.0.0");
                        ctx.reply("&7Author: &fGennario");
                    })
                    .done()

                .register();
    }

    // ================================================================
    // 3) PŘÍKAZ S TYPOVANÝMI ARGUMENTY
    // ================================================================

    /**
     * /give <player> <material> [amount]
     * - player: automatický tab-complete online hráčů
     * - material: automatický tab-complete všech materiálů
     * - amount: volitelný int (1-64), default 1
     */
    public static void commandWithArguments(JavaPlugin plugin) {
        GennarioCommand.create("give", plugin)
                .description("Give items to a player")
                .permission("myplugin.give")

                .argument(Argument.player("player"))
                .argument(Argument.material("material"))
                .argument(Argument.integer("amount").min(1).max(64).optional(1))

                .executes(ctx -> {
                    Player target = ctx.arg("player");
                    Material material = ctx.arg("material");
                    int amount = ctx.argInt("amount", 1);

                    target.getInventory().addItem(new ItemStack(material, amount));
                    ctx.reply("&aGave &f" + amount + "x " + material.name() + " &ato &f" + target.getName());
                })
                .register();
    }

    // ================================================================
    // 4) POKROČILÝ PŘÍKAZ - cooldown, podmínky, error handling, player only
    // ================================================================

    /**
     * /heal [player]
     * - Pouze pro hráče
     * - 30s cooldown
     * - Volitelně může healovat jiného hráče
     */
    public static void advancedCommand(JavaPlugin plugin) {
        GennarioCommand.create("heal", plugin)
                .description("Heal yourself or another player")
                .permission("myplugin.heal")
                .playerOnly()
                .cooldown(30_000) // 30 sekund cooldown

                // Volitelný argument - pokud nezadáno, healne sebe
                .argument(Argument.player("target").optional())

                // Custom handler na cooldown
                .onCooldown((sender, label) -> {
                    sender.sendMessage("§cMusíš počkat než se znovu uzdravíš!");
                })

                // Custom error handler
                .onError((sender, exception) -> {
                    sender.sendMessage("§cNěco se pokazilo: " + exception.getMessage());
                })

                .executes(ctx -> {
                    Player player = ctx.player();
                    Player target = ctx.hasArg("target") ? ctx.arg("target") : player;

                    target.setHealth(target.getMaxHealth());
                    target.setFoodLevel(20);
                    target.setSaturation(20f);

                    if (target == player) {
                        ctx.reply("&a❤ You have been healed!");
                    } else {
                        ctx.reply("&a❤ Healed &f" + target.getName());
                        target.sendMessage("§a❤ You have been healed by " + player.getName());
                    }
                })
                .register();
    }

    // ================================================================
    // 5) ZANOŘENÉ SUB-COMMANDS (neomezená hloubka)
    // ================================================================

    /**
     * /world create <name>
     * /world tp <name>
     * /world settings difficulty <name> <difficulty>
     * /world settings gamemode <name> <gamemode>
     * /world settings time set <name> <ticks>
     * /world settings time cycle <name> <true/false>
     */
    public static void nestedSubCommands(JavaPlugin plugin) {
        GennarioCommand cmd = GennarioCommand.create("world", plugin)
                .description("World management")
                .permission("myplugin.world")
                .autoHelp();

        // /world create <name>
        cmd.subCommand("create")
                .description("Create a new world")
                .argument(Argument.string("name"))
                .executes(ctx -> {
                    String name = ctx.arg("name");
                    ctx.reply("&aCreating world: &f" + name + "...");
                });

        // /world tp <world>
        cmd.subCommand("tp")
                .description("Teleport to a world")
                .playerOnly()
                .argument(Argument.world("world"))
                .executes(ctx -> {
                    World world = ctx.arg("world");
                    ctx.player().teleport(world.getSpawnLocation());
                    ctx.reply("&aTeleported to &f" + world.getName());
                });

        // /world settings ...
        GennarioSubCommand settings = cmd.subCommand("settings")
                .description("World settings");

        // /world settings difficulty <world> <difficulty>
        settings.subCommand("difficulty")
                .description("Set world difficulty")
                .argument(Argument.world("world"))
                .argument(Argument.enumArg("difficulty", org.bukkit.Difficulty.class))
                .executes(ctx -> {
                    World world = ctx.arg("world");
                    org.bukkit.Difficulty diff = ctx.arg("difficulty");
                    world.setDifficulty(diff);
                    ctx.reply("&aDifficulty set to &f" + diff.name() + " &ain &f" + world.getName());
                });

        // /world settings gamemode <world> <gamemode>
        settings.subCommand("gamemode")
                .description("Set default gamemode")
                .argument(Argument.world("world"))
                .argument(Argument.enumArg("gamemode", GameMode.class))
                .executes(ctx -> {
                    ctx.reply("&aGamemode set!");
                });

        // /world settings time ...
        GennarioSubCommand time = settings.subCommand("time")
                .description("Time settings");

        // /world settings time set <world> <ticks>
        time.subCommand("set")
                .description("Set world time")
                .argument(Argument.world("world"))
                .argument(Argument.longArg("ticks").min(0).max(24000))
                .executes(ctx -> {
                    World world = ctx.arg("world");
                    long ticks = ctx.arg("ticks");
                    world.setTime(ticks);
                    ctx.reply("&aTime set to &f" + ticks + " &ain &f" + world.getName());
                });

        // /world settings time cycle <world> <true/false>
        time.subCommand("cycle")
                .description("Toggle day/night cycle")
                .argument(Argument.world("world"))
                .argument(Argument.bool("enabled"))
                .executes(ctx -> {
                    World world = ctx.arg("world");
                    boolean enabled = ctx.argBool("enabled");
                    world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, enabled);
                    ctx.reply("&aDay/night cycle: &f" + (enabled ? "ON" : "OFF"));
                });

        cmd.register();
    }

    // ================================================================
    // 6) DYNAMICKÉ TAB COMPLETIONS - závisí na kontextu
    // ================================================================

    /**
     * /kit <kit_name> [player]
     * - kit_name: dynamicky načítá z konfigurace
     * - player: online hráči (default behavior)
     *
     * /warp <warp_name>
     * - Warp names s kontextovým filtrováním dle permission
     */
    public static void dynamicTabComplete(JavaPlugin plugin) {
        GennarioCommand.create("kit", plugin)
                .description("Give a kit to a player")
                .permission("myplugin.kit")
                .playerOnly()

                .argument(Argument.string("kit")
                        // Dynamický tab-complete - načítá aktuální data
                        .tabComplete(() -> {
                            // Tady bys normálně četl z configu/databáze
                            return java.util.List.of("starter", "warrior", "archer", "mage", "vip");
                        }))

                .argument(Argument.player("target").optional())

                .executes(ctx -> {
                    String kit = ctx.arg("kit");
                    Player target = ctx.hasArg("target") ? ctx.arg("target") : ctx.player();
                    ctx.reply("&aGiving kit &f" + kit + " &ato &f" + target.getName());
                })
                .register();

        // Pokročilý tab-complete s kontextem - závisí na předchozích argumentech
        GennarioCommand.create("shop", plugin)
                .description("Shop system")
                .permission("myplugin.shop")

                .argument(Argument.literal("action", "buy", "sell", "info"))

                .argument(Argument.string("item")
                        // Tab-complete závisí na tom, jestli uživatel zadal "buy" nebo "sell"
                        .tabComplete(tabCtx -> {
                            String action = tabCtx.previousArg("action");
                            if ("buy".equals(action)) {
                                return java.util.List.of("diamond_sword", "diamond_pickaxe", "golden_apple");
                            } else if ("sell".equals(action)) {
                                return java.util.List.of("cobblestone", "dirt", "sand", "gravel");
                            } else {
                                return java.util.List.of("diamond_sword", "cobblestone");
                            }
                        }))

                .argument(Argument.integer("amount").min(1).max(64).optional(1))

                .executes(ctx -> {
                    String action = ctx.arg("action");
                    String item = ctx.arg("item");
                    int amount = ctx.argInt("amount", 1);
                    ctx.reply("&7Action: &f" + action + " &7Item: &f" + item + " &7x" + amount);
                })
                .register();
    }

    // ================================================================
    // 7) CUSTOM MESSAGES + CUSTOM ARGUMENT TYPE
    // ================================================================

    /**
     * Ukázka custom zpráv a vlastního argument typu.
     */
    public static void customMessages(JavaPlugin plugin) {
        // Vlastní zprávy
        CommandMessages messages = new CommandMessages();
        messages.setNoPermission("&4&l✘ &cNemáš oprávnění!");
        messages.setPlayerOnly("&4&l✘ &cTento příkaz mohou použít pouze hráči!");
        messages.setOnCooldown("&6⏳ &ePočkej ještě &f%time%s&e!");
        messages.setUsage("&e⚡ &7Použití: &f/%label% %usage%");
        messages.setHelpHeader("&8&m━━━━━━━━━━━&8 &6⚡ %label% &8&m━━━━━━━━━━━");
        messages.setHelpEntry("  &8▸ &e/%label% %usage% &8- &7%description%");
        messages.setHelpFooter("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        GennarioCommand.create("economy", plugin)
                .description("Economy commands")
                .aliases("eco", "money")
                .permission("myplugin.economy")
                .messages(messages) // aplikuj custom zprávy
                .autoHelp()

                .subCommand("balance")
                    .aliases("bal")
                    .description("Check your balance")
                    .playerOnly()
                    .argument(Argument.player("player").optional())
                    .executes(ctx -> {
                        Player target = ctx.hasArg("player") ? ctx.arg("player") : ctx.player();
                        // double balance = economyAPI.getBalance(target);
                        ctx.reply("&6💰 &f" + target.getName() + "&7's balance: &a$1000.00");
                    })
                    .done()

                .subCommand("pay")
                    .description("Pay another player")
                    .playerOnly()
                    .cooldown(5000)
                    .argument(Argument.player("target"))
                    .argument(Argument.doubleArg("amount").min(0.01))
                    .condition(ctx -> {
                        // Custom podmínka - nemůžeš platit sám sobě
                        if (!ctx.hasArg("target")) return true;
                        Player target = ctx.arg("target");
                        return !target.equals(ctx.player());
                    })
                    .executes(ctx -> {
                        Player target = ctx.arg("target");
                        double amount = ctx.argDouble("amount");
                        ctx.reply("&a💸 Sent &f$" + String.format("%.2f", amount) + " &ato &f" + target.getName());
                    })
                    .done()

                .subCommand("set")
                    .description("Set a player's balance")
                    .permission("myplugin.economy.admin")
                    .argument(Argument.player("player"))
                    .argument(Argument.doubleArg("amount").min(0))
                    .executes(ctx -> {
                        Player target = ctx.arg("player");
                        double amount = ctx.argDouble("amount");
                        ctx.reply("&aSet &f" + target.getName() + "&a's balance to &f$" + String.format("%.2f", amount));
                    })
                    .done()

                // Custom argument type - vlastní parser
                .subCommand("convert")
                    .description("Convert currency")
                    .argument(Argument.custom("currency", String.class, input -> {
                        // Custom parser - přijímá jen specifické hodnoty
                        return switch (input.toLowerCase()) {
                            case "usd", "eur", "czk", "gbp" -> input.toUpperCase();
                            default -> null; // vrátí null = vyhodí ArgumentParseException
                        };
                    }).typeName("currency (usd/eur/czk/gbp)")
                      .tabComplete("usd", "eur", "czk", "gbp"))
                    .argument(Argument.doubleArg("amount").min(0.01))
                    .executes(ctx -> {
                        String currency = ctx.arg("currency");
                        double amount = ctx.argDouble("amount");
                        ctx.reply("&7Converting &f$" + amount + " &7to &f" + currency);
                    })
                    .done()

                .register();
    }
}
