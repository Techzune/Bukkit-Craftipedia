package me.operon.craftipedia;
import java.io.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Craftipedia extends JavaPlugin {
	public static String configVersion = "2.0"; //Version of Config in Use
	Logger log = Logger.getLogger("Minecraft"); //Console
	protected FileConfiguration config;
	public void onEnable() { //On Plugin Enable
		config = getConfig();
		if (config.getString("config.configversion") == null) { //Checks if the "configversion" config is not there.
			
			boolean generated;
			try {
				genConfig(); //Generate a new config if it isn't there.
				generated = true;
			} catch (IOException e) {
				log.info("[Craftipedia] ERROR: COULD NOT GENERATE NEW CONFIG!"); //Error
				log.info("[Craftipedia] If you reloaded, please restart your server.");
				generated = false;
			}
			if (generated == true) { //If the config was generated, tell that it was generated.
				log.info("[Craftipedia] Config Generated");
			}
		} else {
			if (config.getString("config.configversion").equals(configVersion) == false) { //Checks if the config is up to date or behind.
				log.info("[Craftipedia] Config is not compatible with this version of Craftipedia.");
				log.info("[Craftipedia] Generating new config...");
				log.info("[Craftipedia] Backing Up Config...");
				boolean backedup;
				try {
					backupConfig();
					backedup = true;
				} catch (IOException e) {
					log.info("[Craftipedia] ERROR: COULD NOT BACKUP!");
					backedup = false;
				}
				log.info("[Craftipedia] Generating New Config;");
				if (backedup) {
					try {
						genConfig();
					} catch (IOException e) {
						log.info("[Craftipedia] ERORR: COULD NOT GENERATE NEW CONFIG!");
					}
				}
			}
		}
		log.info("[Craftipedia] Config Loaded");
	}
	public void onDisable() {}
	public void backupConfig() throws IOException { //Backs up the configuration.
		File infile = new File("plugins" + File.separator + getDescription().getName() + File.separator + "config.yml"); //Destination of Config File
		File outfile = new File("plugins" + File.separator + getDescription().getName() + File.separator +  "BACKUP" + config.getString("config.configversion") + "config.yml"); //Input of Config File
		FileWriter out = new FileWriter(outfile); //Opens a writer to write to the config file.
		FileReader in = new FileReader(infile); //Opens a reader to read the input config file.
		int c;
		while ((c = in.read()) != -1)
			out.write(c);
		in.close();
		out.close();
	}
	public void genConfig() throws IOException { //Generates the configuration.
		File dir = new File("plugins" + File.separator + getDescription().getName());
		if (dir.exists() == false) {
			dir.mkdir();
		}
		File coutfile = new File("plugins" + File.separator + getDescription().getName() + File.separator + "config.yml"); //Destination of Config File
		FileWriter cout = new FileWriter(coutfile); //Opens a writer to write to the config file.
		InputStreamReader cin = new InputStreamReader(this.getClass().getResourceAsStream("customrecipes.yml") ); //
		int cc;
		while ((cc = cin.read()) != -1)
			cout.write(cc);
		cin.close();
		cout.close();
	}
	//Handles when a player says a command\\
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("craftipedia")) { //Player says /craftipedia
			if (sender.hasPermission("craftipedia.ver")) { //Checks for permission.
				PluginDescriptionFile pdfile = getDescription();
				sender.sendMessage("Craftipedia is running version " + pdfile.getVersion()); //Sends the player the version of Craftipedia.
			} else {
				sender.sendMessage(ChatColor.RED + "Sorry, You do not have permission to use Craftipedia."); //Player does not have permission.
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("recipe") || cmd.getName().equalsIgnoreCase("rc")) { //Player says /recipe
			if (sender.hasPermission("craftipedia.use")) {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Please enter an item or block."); //No Item/Block Requested
				} else { //Checks if the player gave an item name.
					if (args.length > 1) { //If there are more than 1 arguments, they will be combined.
						for (int i=1; i!=args.length; i++) {
							args[0] = (args[0] + args[i]);
						}
					}
					if (sender.getName().equals("CONSOLE")) {
						log.info("[Craftipedia] Craftipedia is not compatible with the console. Sorry!");
					} else {
						sendRecipe(sender, cmd, commandLabel, args); //Sends the recipe (See Below)
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Sorry, You do not have permission to use Craftipedia.");//Player does not have permission.
			}
			return true;
		}
		return false;
	}
	//Recipe Sending - Sends the player the recipe\\
	public void sendRecipe(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender; //Gets the player of the sender.
		/* Recipe Template
		if (args[0].equalsIgnoreCase("")) {
    		sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
    		sender.sendMessage(ChatColor.GREEN + "You will need ");
    		sender.sendMessage(ChatColor.GOLD + "- - -");
    		sender.sendMessage(ChatColor.GOLD + "- - -");
    		sender.sendMessage(ChatColor.GOLD + "- - -");
    		sender.sendMessage(ChatColor.RED + "Where ");
    		sender.sendMessage(ChatColor.WHITE + "Result: ");
    		sender.sendMessage(ChatColor.AQUA + "-----------------------------");
    		r = 1;
    	}
		 */
		int r = 0;
		if (args[0].equalsIgnoreCase("reload")) { //Reloads the config.
			reloadConfig();
			getConfig();
			sender.sendMessage(ChatColor.AQUA + "[Craftipedia]:" + ChatColor.WHITE + " Reloaded");
			log.info(sender.getName() + ": [Craftipedia]: Reloaded");
			r = 1;
		}
		if (args[0].equalsIgnoreCase("crafting") || args[0].equalsIgnoreCase("grid") || args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("g")) {
			if (sender.hasPermission("craftipedia.portablecrafting")) { //Checks if the player has permission.
				player.openWorkbench(null, true); //Opens the workbench GUI for the player.
			}
			r = 1;
		}
		//Define Recipes:
		//Custom Recipes\\
		if (config.getString("recipes." + args[0].toLowerCase() + ".need") != null && config.getString("config.customcraftingrecipes").equals("true")) {
			sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
			sender.sendMessage(ChatColor.GREEN + "You will need " + config.getString("recipes." + args[0] + ".need"));
			sender.sendMessage(ChatColor.GOLD + config.getString("recipes." + args[0] + ".row0"));
			sender.sendMessage(ChatColor.GOLD + config.getString("recipes." + args[0] + ".row1"));
			sender.sendMessage(ChatColor.GOLD + config.getString("recipes." + args[0] + ".row2"));
			if (config.getString("recipes." + args[0] + ".define").length() != 0) { sender.sendMessage(ChatColor.RED + "Where " + config.getString("recipes." + args[0] + ".define")); }
			if (config.getString("recipes." + args[0] + ".define1").length() != 0) { sender.sendMessage(ChatColor.RED + "Where " + config.getString("recipes." + args[0] + ".define1")); }
			if (config.getString("recipes." + args[0] + ".define2").length() != 0) { sender.sendMessage(ChatColor.RED + "Where " + config.getString("recipes." + args[0] + ".define2")); }
			if (config.getString("recipes." + args[0] + ".define3").length() != 0) { sender.sendMessage(ChatColor.RED + "Where " + config.getString("recipes." + args[0] + ".define3")); }
			if (config.getString("recipes." + args[0] + ".define4").length() != 0) { sender.sendMessage(ChatColor.RED + "Where " + config.getString("recipes." + args[0] + ".define4")); }
			sender.sendMessage(ChatColor.WHITE + "Result: " + config.getString("recipes." + args[0] + ".result"));
			sender.sendMessage(ChatColor.AQUA + "-----------------------------");
			r = 1;
		}
		if (config.getString("frecipes." + args[0].toLowerCase()) != null && config.getString("config.customfurnacerecipes").equals("true")) {
			sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
			sender.sendMessage(ChatColor.GOLD + config.getString("frecipes." + args[0]));
			sender.sendMessage(ChatColor.AQUA + "-----------------------------");
			r = 1;
		}
		//Tools\\
		if (r != 1) {
			if (args[0].equalsIgnoreCase("woodenpickaxe") || args[0].equalsIgnoreCase("woodenpick") || args[0].equalsIgnoreCase("woodpickaxe") || args[0].equalsIgnoreCase("woodpick") || args[0].equalsIgnoreCase("stonepickaxe") || args[0].equalsIgnoreCase("stonepick") || args[0].equalsIgnoreCase("ironpickaxe") || args[0].equalsIgnoreCase("ironpick") ||args[0].equalsIgnoreCase("goldpickaxe") || args[0].equalsIgnoreCase("goldpick") || args[0].equalsIgnoreCase("diamondpickaxe") || args[0].equalsIgnoreCase("diamondpick") || args[0].equalsIgnoreCase("pickaxe") || args[0].equalsIgnoreCase("pick")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 planks/cobblestone/iron ingot/gold ingot/diamond and 2 sticks.");
				sender.sendMessage(ChatColor.GOLD + "C C C");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where C is planks/cobblestone/iron ingot/gold ingot/diamond");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Pickaxe");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("woodensword") || args[0].equalsIgnoreCase("woodsword") || args[0].equalsIgnoreCase("stonesword") || args[0].equalsIgnoreCase("ironsword")  ||args[0].equalsIgnoreCase("goldsword") || args[0].equalsIgnoreCase("diamondsword") || args[0].equalsIgnoreCase("sword")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 planks/cobblestone/iron ingot/gold ingot/diamond and 1 stick.");
				sender.sendMessage(ChatColor.GOLD + "- C -");
				sender.sendMessage(ChatColor.GOLD + "- C -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where C is planks/cobblestone/iron ingot/gold ingot/diamond");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Sword");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("woodenaxe") || args[0].equalsIgnoreCase("woodaxe") || args[0].equalsIgnoreCase("stoneaxe") || args[0].equalsIgnoreCase("ironaxe")  ||args[0].equalsIgnoreCase("goldaxe") || args[0].equalsIgnoreCase("diamondaxe") || args[0].equalsIgnoreCase("axe")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 planks/cobblestone/iron ingot/gold ingot/diamond and 2 stick.");
				sender.sendMessage(ChatColor.GOLD + "C C -");
				sender.sendMessage(ChatColor.GOLD + "C I -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where C is planks/cobblestone/iron ingot/gold ingot/diamond");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Axe");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("woodenshovel") || args[0].equalsIgnoreCase("woodshovel") || args[0].equalsIgnoreCase("stoneshovel") || args[0].equalsIgnoreCase("ironshovel")  ||args[0].equalsIgnoreCase("goldshovel") || args[0].equalsIgnoreCase("diamondshovel") || args[0].equalsIgnoreCase("shovel")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 planks/cobblestone/iron ingot/gold ingot/diamond and 2 stick.");
				sender.sendMessage(ChatColor.GOLD + "- C -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where C is planks/cobblestone/iron ingot/gold ingot/diamond");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Shovel");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("woodenhoe") || args[0].equalsIgnoreCase("woodhoe") || args[0].equalsIgnoreCase("stonehoe") || args[0].equalsIgnoreCase("ironhoe")  ||args[0].equalsIgnoreCase("goldhoe") || args[0].equalsIgnoreCase("diamondhoe") || args[0].equalsIgnoreCase("hoe")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 planks/cobblestone/iron ingot/gold ingot/diamond and 2 stick.");
				sender.sendMessage(ChatColor.GOLD + "C C -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where C is planks/cobblestone/iron ingot/gold ingot/diamond");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Hoe");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Basic Recipes\\
			if (args[0].equalsIgnoreCase("woodenplank") || args[0].equalsIgnoreCase("plank") || args[0].equalsIgnoreCase("woodenplanks") | args[0].equalsIgnoreCase("planks")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 log.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- L -");
				sender.sendMessage(ChatColor.RED + "Where L is log.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Wooden Planks");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stick") || args[0].equalsIgnoreCase("sticks")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- P -");
				sender.sendMessage(ChatColor.GOLD + "- P -");
				sender.sendMessage(ChatColor.RED + "Where P is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Sticks");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("torch")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 coal and 1 stick.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where X is coal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Torches");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("craftbench") || args[0].equalsIgnoreCase("workbench")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "P P -");
				sender.sendMessage(ChatColor.GOLD + "P P -");
				sender.sendMessage(ChatColor.RED + "Where P is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Workbench");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("furnace")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 cobblestone.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is cobblestone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Furnace");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("chest")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 planks.");
				sender.sendMessage(ChatColor.GOLD + "P P P");
				sender.sendMessage(ChatColor.GOLD + "P - P");
				sender.sendMessage(ChatColor.GOLD + "P P P");
				sender.sendMessage(ChatColor.RED + "Where P is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Chest");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Blocks\\
			if (args[0].equalsIgnoreCase("ironblock") || args[0].equalsIgnoreCase("goldblock") || args[0].equalsIgnoreCase("diamondblock") || args[0].equalsIgnoreCase("lapislazuliblock") || args[0].equalsIgnoreCase("oreblock")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 9 iron ingot/gold ingot/diamond gem/lapis lazuli.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is iron ingot/gold ingot/diamond gem/lapis lazuli.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Ore Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("glowstone")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 glowstone dust.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "G G -");
				sender.sendMessage(ChatColor.GOLD + "G G -");
				sender.sendMessage(ChatColor.RED + "Where G is glowstone dust.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Glowstone");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("wool") || args[0].equalsIgnoreCase("whitewool")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 string.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.RED + "Where S is string.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 White Wool");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("tnt")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 sand and 5 gunpowder.");
				sender.sendMessage(ChatColor.GOLD + "X S X");
				sender.sendMessage(ChatColor.GOLD + "S X S");
				sender.sendMessage(ChatColor.GOLD + "X S X");
				sender.sendMessage(ChatColor.RED + "Where X is gunpowder.");
				sender.sendMessage(ChatColor.RED + "Where S is sand.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 TNT");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("slab") || args[0].equalsIgnoreCase("stoneslab") || args[0].equalsIgnoreCase("cobblestoneslab") || args[0].equalsIgnoreCase("sandstoneslab") || args[0].equalsIgnoreCase("woodslab") || args[0].equalsIgnoreCase("woodenslab") || args[0].equalsIgnoreCase("brickslab") || args[0].equalsIgnoreCase("bricksslab")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 stone, sandstone, planks, cobblestone, or bricks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is stone, sandstone, planks, cobblestone, or bricks.");
				sender.sendMessage(ChatColor.WHITE + "Result: 6 Slabs");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stair") || args[0].equalsIgnoreCase("woodstair") || args[0].equalsIgnoreCase("woodenstair") || args[0].equalsIgnoreCase("cobblestair") || args[0].equalsIgnoreCase("cobblestonestair") || args[0].equalsIgnoreCase("brickstair") || args[0].equalsIgnoreCase("Stonebrickstair") || args[0].equalsIgnoreCase("Netherbrickstair") || args[0].equalsIgnoreCase("netherstair")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 planks, cobblestone, bricks, stone bricks, or nether bricks.");
				sender.sendMessage(ChatColor.GOLD + "X - -");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is planks, cobblestone, bricks, stone bricks, or nether bricks.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Stairs");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("snowblock") || args[0].equalsIgnoreCase("snow")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 snowballs.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.RED + "Where S is snowball.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Snow Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("clayblock")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 clay balls.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "C C -");
				sender.sendMessage(ChatColor.GOLD + "C C -");
				sender.sendMessage(ChatColor.RED + "Where C is clay ball.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Clay Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("brickblock") || args[0].equalsIgnoreCase("brick")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 clay bricks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "B B -");
				sender.sendMessage(ChatColor.GOLD + "B B -");
				sender.sendMessage(ChatColor.RED + "Where B is clay brick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Brick Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stonebrick")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 stone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.RED + "Where X is stone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Stone Brick");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("bookshelf") || args[0].equalsIgnoreCase("bookcase")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 planks and 3 book.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "B B B");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where B is book.");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Bookshelf");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("sandstone")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need sand");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.GOLD + "S S -");
				sender.sendMessage(ChatColor.RED + "Where S is sand.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Sandstone");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("jackolantern")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 pumpkin and 1 torch.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- P -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where P is pumpkin.");
				sender.sendMessage(ChatColor.RED + "Where I is torch.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Jack-O-Lantern");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Tools\\
			if (args[0].equalsIgnoreCase("flintandsteel")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 iron ingot and 1 flint.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "I - -");
				sender.sendMessage(ChatColor.GOLD + "- F -");
				sender.sendMessage(ChatColor.RED + "Where I is iron ingot.");
				sender.sendMessage(ChatColor.RED + "Where F is flint.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Flint and Steel");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("bucket")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 iron ingot.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "V - V");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where V is iron ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Bucket.");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("compass")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 iron ingot and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "X R X");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is iron ingot.");
				sender.sendMessage(ChatColor.RED + "Where T is redstone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Compass.");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("map")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 paper and 1 compass.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X C X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is paper.");
				sender.sendMessage(ChatColor.RED + "Where C is compass.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Map");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("clock") || args[0].equalsIgnoreCase("watch")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 gold ingots and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X R X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is gold ingot.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Clock");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("fishingrod") || args[0].equalsIgnoreCase("rod")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 sticks and 2 string.");
				sender.sendMessage(ChatColor.GOLD + "- - X");
				sender.sendMessage(ChatColor.GOLD + ". I S");
				sender.sendMessage(ChatColor.GOLD + "I - S");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where S is string.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Fishing Rod");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("shears")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 iron ingot.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- N -");
				sender.sendMessage(ChatColor.GOLD + "N - -");
				sender.sendMessage(ChatColor.RED + "Where N is iron ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Shears");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Bow and Arrow\\
			if (args[0].equalsIgnoreCase("bow")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 sticks and 3 string.");
				sender.sendMessage(ChatColor.GOLD + "- I S");
				sender.sendMessage(ChatColor.GOLD + "I - S");
				sender.sendMessage(ChatColor.GOLD + "- I S");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where S is string.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Bow");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("arrows") || args[0].equalsIgnoreCase("arrow")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 flint, 1 stick, and 1 feather.");
				sender.sendMessage(ChatColor.GOLD + "- F -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- R -");
				sender.sendMessage(ChatColor.RED + "Where F is flint.");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where R is feather");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Arrows");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Armor\\
			if (args[0].equalsIgnoreCase("leatherhelmet") || args[0].equalsIgnoreCase("ironhelmet") || args[0].equalsIgnoreCase("goldhelmet") || args[0].equalsIgnoreCase("diamondhelmet")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 5 leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.RED + "Where X is leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Helmet");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("leatherchestplate") || args[0].equalsIgnoreCase("ironchestplate") || args[0].equalsIgnoreCase("goldchestplate") || args[0].equalsIgnoreCase("diamondchestplate")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Chestplate");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("leatherleggings") || args[0].equalsIgnoreCase("ironleggings") || args[0].equalsIgnoreCase("goldleggings") || args[0].equalsIgnoreCase("diamondleggings")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 7 leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.RED + "Where X is leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Leggings");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("leatherboots") || args[0].equalsIgnoreCase("ironboots") || args[0].equalsIgnoreCase("goldboots") || args[0].equalsIgnoreCase("diamondboots")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.RED + "Where X is leather, gold ingot, iron ingot, or diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Boots");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Transportation\\
			if (args[0].equalsIgnoreCase("minecart")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 iron ingots.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "V - V");
				sender.sendMessage(ChatColor.GOLD + "V V V");
				sender.sendMessage(ChatColor.RED + "Where V is iron ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Minecart");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("poweredminecart")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 furnace and 1 minecart.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- F -");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where F is furnace.");
				sender.sendMessage(ChatColor.RED + "Where V is minecart.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Powered Minecart");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("storageminecart")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 chest and 1 minecart.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where X is chest.");
				sender.sendMessage(ChatColor.RED + "Where V is minecart.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Storage Minecart");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("rail")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 iron ingots and 1 stick.");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.GOLD + "I X I");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.RED + "Where I is iron ingot.");
				sender.sendMessage(ChatColor.RED + "Where X is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 16 Rails");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("poweredrail")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 gold ingot, 1 stick, and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.GOLD + "I X I");
				sender.sendMessage(ChatColor.GOLD + "I R I");
				sender.sendMessage(ChatColor.RED + "Where I is gold ingot.");
				sender.sendMessage(ChatColor.RED + "Where X is stick.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone");
				sender.sendMessage(ChatColor.WHITE + "Result: 6 Powered Rail");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("detectorrail")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 iron ingot, 1 stone pressure plate, and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.GOLD + "I O I");
				sender.sendMessage(ChatColor.GOLD + "I R I");
				sender.sendMessage(ChatColor.RED + "Where I is iron ingot.");
				sender.sendMessage(ChatColor.RED + "Where O is stone pressure plate.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone");
				sender.sendMessage(ChatColor.WHITE + "Result: 6 Detector Rail");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("boat")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 5 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Boat");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Mechanisms\\
			if (args[0].equalsIgnoreCase("woodendoor") || args[0].equalsIgnoreCase("wooddoor") || args[0].equalsIgnoreCase("irondoor") || args[0].equalsIgnoreCase("door")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 planks or iron ingots.");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.GOLD + "X X -");
				sender.sendMessage(ChatColor.RED + "Where X is plank or iron ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Door");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("trapdoor")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Trap Doors");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stonepressureplate") || args[0].equalsIgnoreCase("woodenpressureplate") || args[0].equalsIgnoreCase("woodpressureplate") || args[0].equalsIgnoreCase("pressureplate")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 stone or planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is stone or plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Pressure Plate");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("button") || args[0].equalsIgnoreCase("stonebutton")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 stone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is stone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Stone Button");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("redstonetorch")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 redstone and 1 stick.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- R -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Redstone Torch");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("lever")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 stick and 1 cobblestone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where X is cobblestone");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Lever");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("noteblock")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 planks and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X R X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Note Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("jukebox")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 planks and 1 diamond.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X D X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.RED + "Where D is diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Jukebox");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("dispenser")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 7 cobblestone, 1 bow, and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X B X");
				sender.sendMessage(ChatColor.GOLD + "X R X");
				sender.sendMessage(ChatColor.RED + "Where X is cobblestone.");
				sender.sendMessage(ChatColor.RED + "Where B is bow.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Dispenser");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("redstonerepeater") || args[0].equalsIgnoreCase("diode")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 redstone torches, 1 redstone, and 3 stone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "T R T");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where T is redstone torch.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.RED + "Where X is stone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Redstone Repeater");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("piston")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 planks, 4 cobblestone, 1 iron ingot, and 1 redstone.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "C I C");
				sender.sendMessage(ChatColor.GOLD + "C R C");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.RED + "Where C is cobblestone.");
				sender.sendMessage(ChatColor.RED + "Where I is iron ingot.");
				sender.sendMessage(ChatColor.RED + "Where R is redstone");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Piston");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stickypiston")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 slime ball and 1 piston.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- S -");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where S is slime ball.");
				sender.sendMessage(ChatColor.RED + "Where V is piston");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Sticky Piston");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Food\\
			if (args[0].equalsIgnoreCase("bowl")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "V - V");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where V is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Bowls");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("mushroomstew") || args[0].equalsIgnoreCase("stew")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 red mushroom, 1 brown mushroom, and 1 bowl.");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.GOLD + "- V -");
				sender.sendMessage(ChatColor.RED + "Where X is T is red or brown mushroom.");
				sender.sendMessage(ChatColor.RED + "Where V is bowl.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Mushroom Stew");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("bread")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 wheat.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "W W W");
				sender.sendMessage(ChatColor.RED + "Where W is wheat.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Bread");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("sugar")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 sugar cane.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- S -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where S is sugar cane.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Sugar");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cake")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 milk buckets, 2 sugar, 1 egg, and 3 wheat.");
				sender.sendMessage(ChatColor.GOLD + "V V V");
				sender.sendMessage(ChatColor.GOLD + "S E S");
				sender.sendMessage(ChatColor.GOLD + "W W W");
				sender.sendMessage(ChatColor.RED + "Where V is milk bucket.");
				sender.sendMessage(ChatColor.RED + "Where S is sugar.");
				sender.sendMessage(ChatColor.RED + "Where E is egg.");
				sender.sendMessage(ChatColor.RED + "Where W is wheat.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Cake");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cookie")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 2 wheat and 1 cocoa bean.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "W C W");
				sender.sendMessage(ChatColor.RED + "Where W is wheat.");
				sender.sendMessage(ChatColor.RED + "Where C is cocoa bean");
				sender.sendMessage(ChatColor.WHITE + "Result: 8 Cookies");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("goldenapple")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 gold blocks and 1 apple.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X A X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is gold block.");
				sender.sendMessage(ChatColor.RED + "Where A is apple.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Golden Apple");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("melonblock") || args[0].equalsIgnoreCase("melon")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 9 melon slices.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is melon slice.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Melon Block");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("melonseeds")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 melon slice.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is melon slice.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Melon Seed");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("pumpkinseeds")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 pumpkin.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is pumpkin.");
				sender.sendMessage(ChatColor.WHITE + "Result: 4 Pumkin Seeds");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Msc\\
			if (args[0].equalsIgnoreCase("painting")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 8 sticks and 1 wool.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X S X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is stick.");
				sender.sendMessage(ChatColor.RED + "Where S is wool.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Painting");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("sign")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 planks and 1 stick.");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Sign");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("ladder")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 7 sticks.");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.GOLD + "I I I");
				sender.sendMessage(ChatColor.GOLD + "I - I");
				sender.sendMessage(ChatColor.RED + "Where X is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 3 Ladders");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("glasspane")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 glass.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is glass.");
				sender.sendMessage(ChatColor.WHITE + "Result: 16 Glass Panes.");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("ironbars") || args[0].equalsIgnoreCase("bars")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 iron ingots.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is iron ingots.");
				sender.sendMessage(ChatColor.WHITE + "Result: 16 Iron Bars");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("paper")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 sugar cane.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "S S S");
				sender.sendMessage(ChatColor.RED + "Where S is sugar cane.");
				sender.sendMessage(ChatColor.WHITE + "Result: 3 Paper");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("book")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 paper.");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is paper.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Book");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("fence")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 sticks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is stick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Fences");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("netherfence") || args[0].equalsIgnoreCase("netherbrickfence")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 6 nether bricks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is nether brick.");
				sender.sendMessage(ChatColor.WHITE + "Result: 6 Nether Brick Fences");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("gate") || args[0].equalsIgnoreCase("fencegate")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 sticks and 2 planks.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "I X I");
				sender.sendMessage(ChatColor.GOLD + "I X I");
				sender.sendMessage(ChatColor.RED + "Where I is stick.");
				sender.sendMessage(ChatColor.RED + "Where X is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Fence Gate");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("bed")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need ");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "W W W");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where W is wool.");
				sender.sendMessage(ChatColor.RED  + "Where X is plank.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Bed");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("eyeofender") || args[0].equalsIgnoreCase("endereeye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 ender pearl and 1 blaze powder.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.RED + "Where X is ender pearl.");
				sender.sendMessage(ChatColor.RED + "Where T is blaze powder.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Eye of Ender");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Dye\\
			if (args[0].equalsIgnoreCase("bonemeal")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 bone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- I -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where I is bone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 3 Bone Meal");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("lightgraydye") || args[0].equalsIgnoreCase("lightgreydye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 ink sac and 2 bone meal.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X T T");
				sender.sendMessage(ChatColor.RED + "Where X is ink sac.");
				sender.sendMessage(ChatColor.RED + "Where T is bone meal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 3 Light Gray Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("graydye") || args[0].equalsIgnoreCase("greydye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 ink sac and 1 bone meal.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is ink sac.");
				sender.sendMessage(ChatColor.RED + "Where T is bone meal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Gray Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("reddye") || args[0].equalsIgnoreCase("rosered")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 rose.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is rose.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Rose Red");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("orangedye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 rose red and 1 dandelion yellow.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is rose red.");
				sender.sendMessage(ChatColor.RED + "Where T is dendelion yellow");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Orange Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("dandelionyellow") || args[0].equalsIgnoreCase("yellowdye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 dandelion.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is dandelion.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Dandelion Yellow");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("limedye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 cactus green and 1 bone meal.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is cactus green.");
				sender.sendMessage(ChatColor.RED + "Where T is bone meal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Lime Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("lightbluedye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 lapis lazuli dye and 1 bone meal.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is lapis lazuli dye.");
				sender.sendMessage(ChatColor.RED + "Where T is bone meal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Light Blue Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cyandye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 lapis lazuli dye and 1 cactus green.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is lapis lazuli dye.");
				sender.sendMessage(ChatColor.RED + "Where T is cactus green.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Cyan Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("purpledye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 lapis lazuli dye and 1 rose red.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is lapis lazuli dye.");
				sender.sendMessage(ChatColor.RED + "Where T is rose red.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Purple Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("magentadye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 purple dye and 1 pink dye.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is purple dye.");
				sender.sendMessage(ChatColor.RED + "Where T is pink dye.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Magenta Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("pinkdye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 rose red and 1 bone meal.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - T");
				sender.sendMessage(ChatColor.RED + "Where X is rose red.");
				sender.sendMessage(ChatColor.RED + "Where T is bone meal.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Pink Dye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Enchantment and Brewing\\
			if (args[0].equalsIgnoreCase("glassbottle") || args[0].equalsIgnoreCase("bottle")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 3 glass.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.RED + "Where X is glass.");
				sender.sendMessage(ChatColor.WHITE + "Result: 3 Glass Bottles");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cauldron")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 7 iron ingots.");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X - X");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where X is iron ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Cauldron");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("brewingstand")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 blaze rod and 3 cobblestone.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where T is blaze rod.");
				sender.sendMessage(ChatColor.RED + "Where X is cobblestone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Brewing Stand");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("blazepowder")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 blaze rod.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where X is blaze rod.");
				sender.sendMessage(ChatColor.WHITE + "Result: 2 Blaze Powder");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("magmacream")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 slime ball and 1 blaze powder.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.RED + "Where X is slimeball.");
				sender.sendMessage(ChatColor.RED + "Where T is blaze powder.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Magma Cream");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("fermentedspidereye")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 spider eye, 1 brown mushroom, and 1 sugar.");
				sender.sendMessage(ChatColor.GOLD + "X - -");
				sender.sendMessage(ChatColor.GOLD + "T S -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where X is spider eye.");
				sender.sendMessage(ChatColor.RED + "Where T is brown mushroom.");
				sender.sendMessage(ChatColor.RED + "Where S is sugar.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Fermented Spider Eye");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("glisteringmelon")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 melon slice and 1 gold nugget.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "X G -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where X is melon slice.");
				sender.sendMessage(ChatColor.RED + "Where G is gold nugget.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Glistering Melon");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("goldnugget")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 gold ingot.");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- - -");
				sender.sendMessage(ChatColor.RED + "Where X is gold ingot.");
				sender.sendMessage(ChatColor.WHITE + "Result: 9 Gold Nugget");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("enchantmenttable")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 book, 4 obsidian, and 2 diamonds.");
				sender.sendMessage(ChatColor.GOLD + "- B -");
				sender.sendMessage(ChatColor.GOLD + "T X T");
				sender.sendMessage(ChatColor.GOLD + "X X X");
				sender.sendMessage(ChatColor.RED + "Where B is book.");
				sender.sendMessage(ChatColor.RED + "Where X is obsidian.");
				sender.sendMessage(ChatColor.RED + "Where T is diamond.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Enchantment Table");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//1.2 Update\\
			if (args[0].equalsIgnoreCase("lamp") || args[0].equalsIgnoreCase("redstonelamp")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 4 redstone and 1 glowstone.");
				sender.sendMessage(ChatColor.GOLD + "- R -");
				sender.sendMessage(ChatColor.GOLD + "R X R");
				sender.sendMessage(ChatColor.GOLD + "- R -");
				sender.sendMessage(ChatColor.RED + "Where R is redstone.");
				sender.sendMessage(ChatColor.RED + "Where X is glowstone.");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Redstone Lamp");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("firecharge") || args[0].equalsIgnoreCase("fireball")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GREEN + "You will need 1 blaze powder, 1 coal/charcoal, and 1 gunpowder.");
				sender.sendMessage(ChatColor.GOLD + "- R -");
				sender.sendMessage(ChatColor.GOLD + "- X -");
				sender.sendMessage(ChatColor.GOLD + "- T -");
				sender.sendMessage(ChatColor.RED + "Where R is blaze powder.");
				sender.sendMessage(ChatColor.RED + "Where X is coal.");
				sender.sendMessage(ChatColor.RED + "Where T is gunpowder");
				sender.sendMessage(ChatColor.WHITE + "Result: 1 Fire Charge");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Furnace Recipes\\
			/* Furnace Recipe Template
			if (args[0].equalsIgnoreCase("")) {
    			sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
    			sender.sendMessage(ChatColor.GOLD + " >> ");
    			sender.sendMessage(ChatColor.AQUA + "-----------------------------");
    			r = 1;
    		}
			 */
			//Food\\
			if (args[0].equalsIgnoreCase("cookedporkchop")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Raw Porkchop >> Cooked Porkchop");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("steak") || args[0].equalsIgnoreCase("cookedbeef")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Raw Beef >> Steak");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cookedchicken")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Raw Chicken >> Cooked Chicken");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cookedfish")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Raw Fish >> Cooked Fish");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Ores\\
			if (args[0].equalsIgnoreCase("ironingot")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Iron Ore >> Iron Ingot");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("goldingot")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Gold Ore >> Gold Ingot");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("diamond")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Diamond Ore >> Diamond");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("lapislazuli")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Lapis Lazuli Ore >> Lapis Lazuli");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("redstonedust") || args[0].equalsIgnoreCase("redstone")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Redstone Ore >> Redstone Dust");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("coal")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Coal Ore >> Coal");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			//Other\\
			if (args[0].equalsIgnoreCase("glass")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Sand >> Glass");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("stone")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Cobblestone >> Stone");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("claybrick")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Clay >> Clay Brick");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("charcoal")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Log >> Charcoal");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
			if (args[0].equalsIgnoreCase("cactusgreen")) {
				sender.sendMessage(ChatColor.AQUA + "----------Craftipedia----------");
				sender.sendMessage(ChatColor.GOLD + "Cactus >> Cactus Green");
				sender.sendMessage(ChatColor.AQUA + "-----------------------------");
				r = 1;
			}
		}
		if (r == 0) {
			sender.sendMessage(ChatColor.RED + "A recipe for: " + args[0] + " was not found.");
		} else {
			r = 0;
		}
	}
}