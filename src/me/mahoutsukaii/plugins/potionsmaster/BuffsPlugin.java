package me.mahoutsukaii.plugins.potionsmaster;


import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;
//import net.minecraft.server.Packet41MobEffect;
//import net.minecraft.server.Packet42RemoveMobEffect;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;


public class BuffsPlugin extends JavaPlugin {

	private final int ticks = 20;
	ArrayList<Integer> healthyBuffs = new ArrayList<Integer>();
	ArrayList<Integer> unhealthyBuffs = new ArrayList<Integer>();
	BuffConfig config = new BuffConfig(this);
	HashMap<Integer, MobEffectList> buffIds = new HashMap<Integer, MobEffectList>();
	HashMap<String, Integer> buffAliases = new HashMap<String, Integer>();


	@Override
	public void onDisable() {
		System.out.print(this + " is disabled.");

	}

	@Override
	public void onEnable() {
		System.out.print(this + " is enabled.");

		loadLists();
		config.load();


	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(command.getLabel().equalsIgnoreCase("buff"))
			return buff(sender, args);
		if(command.getLabel().equalsIgnoreCase("debuff"))
			return debuff(sender, args);
		if(command.getLabel().equalsIgnoreCase("custombuff"))
			return customBuff(sender, args);
		if(command.getLabel().equalsIgnoreCase("reloadbuffs"))
			return reload(sender);
		return false;

	}
	public boolean reload(CommandSender sender)
	{
		if(!getPermissions(sender, "potions.reload"))
			return false;

		config.reload();

		return true;
	}

	public void getHelp(CommandSender sender)
	{

		sender.sendMessage(ChatColor.RED + "Aliases include:");
		String aliases = "";
		for(String str : config.buffs.keySet())
		{
			aliases = aliases + str + ", ";
		}
		sender.sendMessage(ChatColor.GOLD + aliases);
		String customBuffs = "";

		for(String str : buffAliases.keySet())
		{
			customBuffs = customBuffs + str + ", ";
		}
		sender.sendMessage(ChatColor.YELLOW + "For custom buffs:");
		sender.sendMessage(ChatColor.RED + customBuffs);

	}
	public boolean buff(CommandSender sender, String[] args)
	{
		if(!getPermissions(sender, "potions.buff")) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission.");
			return true;
		}
		if(args.length < 1 )
		{
			sender.sendMessage(ChatColor.RED + "Use: " + ChatColor.YELLOW + "/buff [alias] {player}");
			getHelp(sender);
			return true;
		}

		if(!config.buffs.keySet().contains(args[0].toString()))
		{
			sender.sendMessage(ChatColor.RED + "Alias " + ChatColor.YELLOW + "\""+ args[0] + "\"" + ChatColor.RED + " was not found in the config.");

			return true;
		}
		String key = args[0];
		if(!(sender instanceof Player))
		{
			if(args.length < 2)
			{
				sender.sendMessage("You need to specify a player!");
				return true;
			}
		}

		Player player;
		if(args.length < 2)
			player = (Player)sender;
		else
		{
			String victim = expandName(args[1]);
			player = null;

			for(Player str : getServer().getOnlinePlayers())
			{
				if(str.getName().equalsIgnoreCase( victim))
					player = str;
			}
			if(player == null)
			{
				sender.sendMessage(ChatColor.RED + victim + " is not online.");
				return true;
			}
		}


		int time = config.buffs.get(key).getTime() * ticks;
		int modifier = config.buffs.get(key).getModifier();

		for(Integer i : config.buffs.get(key).getBuffs())
		{

			((CraftPlayer)player).getHandle().addEffect(new MobEffect(i, time, modifier));
		}
		sender.sendMessage(ChatColor.GREEN + "buffs sent!");
		player.sendMessage(ChatColor.GREEN + "You've been buffed!");
		return true;
	}


	public boolean customBuff(CommandSender sender, String[] args)
	{
		if(args.length < 3)
		{
			getHelp(sender);
			return true;
		}

		if(!sender.hasPermission("potions.custombuff"))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission.");
			return true;
		}
		Player victim = null;
		if(args.length == 3)
			victim = (Player)sender;
		else
		{
			if(args.length < 4)
				return false;
			String vic = expandName(args[0]);
			for(Player str : getServer().getOnlinePlayers())
			{
				if(str.getName().equalsIgnoreCase( vic))
					victim = str;
			}
			if(victim == null)
			{
				sender.sendMessage(ChatColor.RED + vic + " is not online.");
				return true;
			}
		}

		String typeAlias;
		int type;
		int modifier;
		int time;
		try{
			if(args.length < 4)
			{
				typeAlias =args[0];
				modifier = Integer.parseInt(args[1]);
				time = Integer.parseInt(args[2]) * ticks;
			}
			else
			{
				typeAlias = args[1];
				modifier = Integer.parseInt(args[2]);
				time = Integer.parseInt(args[3]) * ticks;
			}
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(ChatColor.RED + "Bad numbers.");
			return false;
		}

		if(!buffAliases.containsKey(typeAlias))
		{
			String customBuffs = "";

			for(String str : buffAliases.keySet())
			{
				customBuffs = customBuffs + str + ", ";
			}
			sender.sendMessage(ChatColor.RED + "Invalid type.");
			sender.sendMessage(ChatColor.RED  + customBuffs);
			return true;
		}
		type = buffAliases.get(typeAlias);

		((CraftPlayer)victim).getHandle().addEffect(new MobEffect(type, time, modifier));
		sender.sendMessage(ChatColor.GREEN + "buffs sent!");
		victim.sendMessage(ChatColor.GREEN + "You've been buffed!");

		return true;
	}
	public boolean debuff(CommandSender sender, String[] args)
	{
		if(!sender.hasPermission("potions.debuff"))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission.");
			return true;
		}

		if(!(sender instanceof Player))
		{
			if(args.length < 1)
			{
				sender.sendMessage("Can't debuff yourself!");
				return true;
			}
		}
		Player victim = null;
		if(args.length < 1)
			victim = (Player)sender;
		else
		{
			String vic = expandName(args[0]);
			for(Player str : getServer().getOnlinePlayers())
			{
				if(str.getName().equalsIgnoreCase( vic))
					victim = str;
			}
			if(victim == null)
			{
				sender.sendMessage(ChatColor.RED + vic + " is not online.");
				return true;
			}
		}

		for(int i = 1; i < 19; i++)
		{
			/*
			int id = ((CraftPlayer)victim).getHandle().id;

			Packet42RemoveMobEffect packet = new Packet42RemoveMobEffect();
			packet.a = id;
			packet.b = (byte) i;
		((CraftPlayer)victim).getHandle().netServerHandler.sendPacket(packet);
			 */
			if(((CraftPlayer)victim).getHandle().hasEffect(idToList(i)))
			{
				int modifier = ((CraftPlayer)victim).getHandle().getEffect(idToList(i)).getAmplifier();
				((CraftPlayer)victim).getHandle().addEffect(new MobEffect(i,-1,modifier + 1));

			}
		}
		victim.sendMessage(ChatColor.GREEN + "You have lost all your effects!");
		return true;

	}

	public boolean getPermissions(CommandSender sender, String node)
	{
		if(sender.hasPermission(node)) return true;
		if(sender.isOp()) return true;
		return false;
	}

	public MobEffectList idToList(int id)
	{
		return buffIds.get(id);

	}
	public String expandName(String Name) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
			String str = getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + Name + ".*")) {
				m++;
				Result = str;
				if(m==2) {
					return null;
				}
			}
			if (str.equalsIgnoreCase(Name))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1) {
			return null;
		}
		if (m < 1) {
			return Name;
		}
		return Name;
	}

	public void loadLists()
	{
		buffIds.put(1, MobEffectList.FASTER_MOVEMENT);
		buffIds.put(2, MobEffectList.SLOWER_MOVEMENT);
		buffIds.put(3, MobEffectList.FASTER_DIG);
		buffIds.put(4, MobEffectList.SLOWER_DIG);
		buffIds.put(5, MobEffectList.INCREASE_DAMAGE);
		buffIds.put(6, MobEffectList.HEAL);
		buffIds.put(7, MobEffectList.HARM);
		buffIds.put(8, MobEffectList.JUMP);
		buffIds.put(9, MobEffectList.CONFUSION);
		buffIds.put(10, MobEffectList.REGENERATION);
		buffIds.put(11, MobEffectList.RESISTANCE);
		buffIds.put(12, MobEffectList.FIRE_RESISTANCE);
		buffIds.put(13, MobEffectList.WATER_BREATHING);
		buffIds.put(14, MobEffectList.INVISIBILITY);
		buffIds.put(15, MobEffectList.BLINDNESS);
		buffIds.put(16, MobEffectList.NIGHT_VISION);
		buffIds.put(17, MobEffectList.HUNGER);
		buffIds.put(18, MobEffectList.WEAKNESS);
		buffIds.put(19, MobEffectList.POISON);

		buffAliases.put("fast", 1);
		buffAliases.put("slow", 2);
		buffAliases.put("fastdig", 3);
		buffAliases.put("slowdig", 4);
		buffAliases.put("strength", 5);
		buffAliases.put("heal", 6);
		buffAliases.put("harm", 7);
		buffAliases.put("jump", 8);
		buffAliases.put("confusion", 9);
		buffAliases.put("regen", 10);
		buffAliases.put("resist", 11);
		buffAliases.put("fireresist", 12);
		buffAliases.put("breathe", 13);
		buffAliases.put("invis", 14);
		buffAliases.put("blindness", 15);
		buffAliases.put("night", 16);
		buffAliases.put("hunger", 17);
		buffAliases.put("weakness", 18);
		buffAliases.put("poison", 19);
		
	}
	
	  public String getKeyFromValue(HashMap<String, Integer> hm, Integer value) {
		    for (String o : hm.keySet()) {
		      if (hm.get(o).equals(value)) {
		        return o;
		      }
		    }
		    return null;
		  }
}
