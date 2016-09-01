package me.Cutiemango.MangoQuest.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.TextComponentFactory;
import me.Cutiemango.MangoQuest.data.QuestFinishData;
import me.Cutiemango.MangoQuest.data.QuestObjectProgress;
import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.data.QuestProgress;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.questobjects.NumerableObject;
import me.Cutiemango.MangoQuest.questobjects.SimpleQuestObject;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestGUIManager {

	public static void openGUI(Player p, QuestProgress q){
		TextComponent p1 = new TextComponent(ChatColor.BOLD + "任務名稱： ");
		p1.addExtra(q.getQuest().getQuestName() + "\n");
		if (!q.getQuest().isCommandQuest()){
			p1.addExtra(ChatColor.BOLD + "任務NPC： ");
			NPC npc = q.getQuest().getQuestNPC();
			p1.addExtra(TextComponentFactory.convertLocationtoHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
			p1.addExtra("\n\n");
		}
		
		p1.addExtra(ChatColor.BOLD + "任務內容： \n");
		for (int i = 0; i < q.getQuest().getStages().size(); i++){
			if (q.getCurrentStage() > i){
				for (SimpleQuestObject obj : q.getQuest().getStage(i).getObjects()){
					p1.addExtra(obj.toTextComponent(true));
					p1.addExtra("\n");
				}
			}
			else if (q.getCurrentStage() == i){
				for (int k = 0; k < q.getCurrentObjects().size(); k++){
					SimpleQuestObject obj = q.getQuest().getStage(i).getObjects().get(k);
					QuestObjectProgress ob = q.getCurrentObjects().get(k);
					if (ob.getObject().equals(obj) && ob.isFinished()){
						p1.addExtra(obj.toTextComponent(true));
						p1.addExtra("\n");
					}
					else{
						p1.addExtra(obj.toTextComponent(false));
						if (obj instanceof NumerableObject)
							p1.addExtra(new TextComponent(QuestUtil.translateColor(
									" &8(" + ob.getProgress() + "/" + ((NumerableObject)obj).getAmount() + ")")));
						p1.addExtra("\n");
					}
				}
			}
			else{
				for (int j = 0; j < q.getQuest().getStage(i).getObjects().size(); j++){
					p1.addExtra(new TextComponent(QuestUtil.translateColor("&8&l？？？")));
					p1.addExtra("\n");
				}
			}
		}

		TextComponent p2 = new TextComponent(ChatColor.BOLD + "任務提要： \n");
		p2.addExtra(q.getQuest().getQuestOutline());

		TextComponent p3 = new TextComponent(ChatColor.BOLD + "任務獎勵：\n");

		if (q.getQuest().getQuestReward().hasItem()){
			for (ItemStack is : q.getQuest().getQuestReward().getItems()){
				p3.addExtra("\n");
				p3.addExtra(TextComponentFactory.convertItemStacktoHoverEvent(is, false));
				TextComponent suffix = new TextComponent(ChatColor.translateAlternateColorCodes('&' , " &l" + is.getAmount() + " &0個"));
				p3.addExtra(suffix);
				p3.addExtra("\n");
			}
		}
		
		if (q.getQuest().getQuestReward().hasMoney()){
			p3.addExtra(ChatColor.GOLD + "金錢 " + ChatColor.BLACK + q.getQuest().getQuestReward().getMoney() + ChatColor.GOLD + " 元");
			p3.addExtra("\n");
		}
		
		if (q.getQuest().getQuestReward().hasExp()){
			p3.addExtra(ChatColor.GREEN + "經驗值 " + ChatColor.BLACK + q.getQuest().getQuestReward().getExp() + ChatColor.GREEN + " 點");
			p3.addExtra("\n");
		}
		
		if (q.getQuest().getQuestReward().hasFriendPoint()){
			for (Integer id : q.getQuest().getQuestReward().getFp().keySet()){
				NPC npc = CitizensAPI.getNPCRegistry().getById(id);
				p3.addExtra(TextComponentFactory.convertLocationtoHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
				p3.addExtra(QuestUtil.translateColor(" &c將會感激你"));
				p3.addExtra("\n");
			}
		}

		openBook(p, p1, p2, p3);
	}
	
	public static void openJourney(Player p){
		QuestPlayerData qd = QuestUtil.getData(p);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&0&l[進行中的任務]"));
		p1.addExtra("\n");
		for (QuestProgress qp : qd.getProgresses()){
			p1.addExtra("\n");
			p1.addExtra(TextComponentFactory.convertViewQuest(qp.getQuest()));
			p1.addExtra("：");
			p1.addExtra(TextComponentFactory.registerClickCommandEvent("&c&l【放棄】", "/mq quit " + qp.getQuest().getInternalID()));
			p1.addExtra("\n");
			for (QuestObjectProgress qop : qp.getCurrentObjects()){
				p1.addExtra("- ");
				if (qop.isFinished()){
					p1.addExtra(qop.getObject().toTextComponent(true));
					p1.addExtra("\n");
				}
				else{
					p1.addExtra(qop.getObject().toTextComponent(false));
					if (qop.getObject() instanceof NumerableObject)
						p1.addExtra(new TextComponent(QuestUtil.translateColor(
								" &8(" + qop.getProgress() + "/" + ((NumerableObject)qop.getObject()).getAmount() + ")")));
					p1.addExtra("\n");
				}
			}
		}

		TextComponent p2 = new TextComponent(QuestUtil.translateColor("&0&l[可進行的任務]"));
		p2.addExtra("\n");
		for (Quest q : QuestStorage.Quests.values()){
			if (!qd.canTake(q, false))
				continue;
			else{
				p2.addExtra("- ");
				p2.addExtra(TextComponentFactory.convertViewQuest(q));
				if (q.isCommandQuest())
					p2.addExtra(TextComponentFactory.registerClickCommandEvent("&2&l【接受】", "/mq take " + q.getInternalID()));
				p2.addExtra("\n");
			}
		}

		TextComponent p3 = new TextComponent(QuestUtil.translateColor("&0&l[已完成的任務]"));
		p3.addExtra("\n");
		for (QuestFinishData qfd : qd.getFinishQuests()){
			p3.addExtra("- ");
			p3.addExtra(TextComponentFactory.convertViewQuest(qfd.getQuest()));
			p3.addExtra("： 已完成 " + qfd.getFinishedTimes() + " 次\n");
		}

		openBook(p, p1, p2, p3);
	}
	
	public static void openInfo(Player p, String msg){
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&c請關閉書本視窗，\n"));
		p1.addExtra(QuestUtil.translateColor(msg));
		p1.addExtra(ChatColor.GRAY + "(取消請輸入cancel。)");
		openBook(p, p1);
	}

	public static void openBook(Player p, TextComponent... texts){
		Main.instance.handler.openBook(p, texts);
	}
	
	public static void openNPCInfo(Player p, NPC npc){
		QuestPlayerData qd = QuestUtil.getData(p);
		TextComponent p1 = new TextComponent(QuestUtil.translateColor("&5&lNPC介面 &0&l| "));
		p1.addExtra(TextComponentFactory.convertLocationtoHoverEvent(npc.getName(), npc.getEntity().getLocation(), false));
		p1.addExtra("\n\n");
		p1.addExtra(QuestUtil.translateColor("&0&l[" + npc.getName() + "&0&l]："));
		p1.addExtra("\n");
		p1.addExtra(QuestUtil.getNPCMessage(npc.getId(), qd.getNPCfp(npc.getId())));
		p1.addExtra("\n\n");
		p1.addExtra(QuestUtil.translateColor("&0&l[任務列表]"));
		p1.addExtra("\n");
		for (Quest q : QuestUtil.getGivenNPCQuests(npc)){
			if (qd.canTake(q, false)){
				if (qd.hasFinished(q))
					p1.addExtra(QuestUtil.translateColor("&0- &8&l！ &0"));
				else
					p1.addExtra(QuestUtil.translateColor("&0- &6&l！ &0"));
				p1.addExtra(TextComponentFactory.convertViewQuest(q));
				p1.addExtra(TextComponentFactory.registerClickCommandEvent("&2&l【接受】", "/mq take " + q.getInternalID()));
				p1.addExtra("\n");
				continue;
			}
			else{
				p1.addExtra(QuestUtil.translateColor("&0- &8&l？ &0"));
				p1.addExtra(TextComponentFactory.convertViewQuest(q));
				p1.addExtra(TextComponentFactory.registerClickCommandEvent("&c&l【放棄】", "/mq quit " + q.getInternalID()));
				p1.addExtra("\n");
				continue;
			}
		}
		openBook(p, p1);
	}

}