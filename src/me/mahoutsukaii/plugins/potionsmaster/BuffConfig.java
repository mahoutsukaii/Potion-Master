package me.mahoutsukaii.plugins.potionsmaster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BuffConfig {

	FileConfiguration config  = new YamlConfiguration();
	
	public HashMap<String, BuffList> buffs = new HashMap<String, BuffList>();
	private BuffsPlugin plugin;
	
	public BuffConfig(BuffsPlugin instance)
	{
		plugin = instance;
	}
	
	public void reload()
	{
		buffs.clear();
		load();
	}
	@SuppressWarnings("unchecked")
	public void load()
	{
		new File("plugins/Buffs/").mkdirs();
		try {
			config.load(new File("plugins/Buffs/config.yml"));
		} catch (FileNotFoundException e) {

			loadDefaults();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		for(String str : config.getKeys(false))
		{
			List<String> buffAliases = new ArrayList<String>();
			buffAliases = config.getList(str + ".bufftypes");
			int modifier = config.getInt( str + ".modifier");
			int duration = config.getInt( str + ".duration");
			
			ArrayList<Integer> buffTypes = new ArrayList<Integer>();
			for(String alias : buffAliases)
			{
				if(plugin.buffAliases.containsKey(alias))
				buffTypes.add(plugin.buffAliases.get(alias));
				else
				{
					System.out.print("[Buffs] Could not load \"" + alias + "\" from the config file. Ignoring.");
				}
			}
			
			buffs.put(str, new BuffList(buffTypes, modifier, duration));
		}
		
	}
	
	
	public void loadDefaults()
	{	
		ArrayList<Integer> healthyBuffs = new ArrayList<Integer>();
	ArrayList<Integer> unhealthyBuffs = new ArrayList<Integer>();
	ArrayList<Integer> upsideDown = new ArrayList<Integer>();
		healthyBuffs.add(1);
		healthyBuffs.add(3);
		healthyBuffs.add(8);
		healthyBuffs.add(10);
		healthyBuffs.add(12);
		unhealthyBuffs.add(2);
		unhealthyBuffs.add(4);
		unhealthyBuffs.add(9);
		unhealthyBuffs.add(15);
		upsideDown.add(1);
		upsideDown.add(2);
		buffs.put("admin", new BuffList(healthyBuffs, 3, 60));
		buffs.put("abuse", new BuffList(unhealthyBuffs, 3, 60));
		buffs.put("upside", new BuffList(upsideDown, -10, 60));
		
		save();
	}
	
	public void save()
	{
		//config.set("buffs", buffs.keySet());
		for(String str : buffs.keySet())
		{

			config.set( str + ".modifier", buffs.get(str).getModifier());
			config.set( str + ".duration", buffs.get(str).getTime());
			//config.set( str + ".bufftypes", buffs.get(str).getBuffs());
			ArrayList<String> buffAliases = new ArrayList<String>();
			for(int i : buffs.get(str).getBuffs())
			{
				buffAliases.add(plugin.getKeyFromValue(plugin.buffAliases, i));
			}
			config.set(str + ".bufftypes", buffAliases);
		}
		
		try {
			config.save(new File("plugins/Buffs/config.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
