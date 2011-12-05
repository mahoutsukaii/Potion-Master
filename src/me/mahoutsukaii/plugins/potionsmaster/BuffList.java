package me.mahoutsukaii.plugins.potionsmaster;

import java.util.ArrayList;

public class BuffList {
	
	private ArrayList<Integer> buffList;
	private int modifier;
	private int time;
	
	public BuffList(ArrayList<Integer> buffList, int modifier, int time)
	{
		this.buffList = buffList;
		this.modifier = modifier;
		this.time = time;
	}
	
	
	public ArrayList<Integer> getBuffs()
	{
		return buffList;
	}
	
	public int getModifier()
	{
		return modifier;
	}
	
	public int getTime()
	{
		return time;
	}

}
