package de.simonsator.partyandfriends.friends.subcommands;

import de.simonsator.partyandfriends.api.friends.ServerConnector;
import de.simonsator.partyandfriends.api.friends.abstractcommands.FriendSubCommand;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.utilities.PatterCollection;
import de.simonsator.partyandfriends.utilities.StandardConnector;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.regex.Matcher;

import static de.simonsator.partyandfriends.main.Main.getInstance;
import static de.simonsator.partyandfriends.main.Main.getPlayerManager;

/***
 * The command jump
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class Jump extends FriendSubCommand {
	private static ServerConnector connector = new StandardConnector();

	public Jump(String[] pCommands, int pPriority, String pHelp) {
		super(pCommands, pPriority, pHelp);
	}

	/**
	 * Sets the server connector, which will be used to join a server.
	 *
	 * @param pConnector The connector
	 */
	public static void setServerConnector(ServerConnector pConnector) {
		connector = pConnector;
	}

	@Override
	public void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		if (!isPlayerGiven(pPlayer, args))
			return;
		PAFPlayer playerQuery = getPlayerManager().getPlayer(args[1]);
		if (!isPlayerOnline(pPlayer, playerQuery))
			return;
		OnlinePAFPlayer friend = (OnlinePAFPlayer) playerQuery;
		if (!isAFriendOf(pPlayer, friend))
			return;
		ServerInfo toJoin = friend.getServer();
		if (!serverExists(pPlayer, toJoin))
			return;
		if (isAlreadyOnServer(pPlayer, toJoin))
			return;
		if (!allowsJumps(pPlayer, friend))
			return;
		if (isDisabled(pPlayer, toJoin))
			return;
		connector.connect(pPlayer.getPlayer(), toJoin);
		pPlayer.sendMessage(
				new TextComponent(
						getInstance().getFriendsPrefix() + PatterCollection.PLAYER_PATTERN
								.matcher(getInstance().getMessagesYml()
										.getString("Friends.Command.Jump.JoinedTheServer"))
								.replaceAll(Matcher.quoteReplacement(friend.getDisplayName()))));
	}

	private boolean serverExists(OnlinePAFPlayer pPlayer, ServerInfo toJoin) {
		if (toJoin != null)
			return true;
		sendError(pPlayer, "Friends.Command.Jump.CanNotJump");
		return false;
	}

	private boolean allowsJumps(OnlinePAFPlayer pPlayer, OnlinePAFPlayer pQueryPlayer) {
		if (pQueryPlayer.getSettingsWorth(4) == 1) {
			sendError(pPlayer, "Friends.Command.Jump.CanNotJump");
			return false;
		}
		return true;
	}

	private boolean isAlreadyOnServer(OnlinePAFPlayer pPlayer, ServerInfo pToJoin) {
		if (pToJoin.equals(pPlayer.getServer())) {
			sendError(pPlayer, "Friends.Command.Jump.AlreadyOnTheServer");
			return true;
		}
		return false;
	}

	private boolean isPlayerOnline(OnlinePAFPlayer pSender, PAFPlayer pQueryPlayer) {
		if (!pQueryPlayer.isOnline()) {
			sendError(pSender, "Friends.General.PlayerIsOffline");
			return false;
		}
		return true;
	}

	private boolean isDisabled(OnlinePAFPlayer pPlayer, ServerInfo pToJoin) {
		if (getInstance().getConfig().getStringList("Commands.Friends.SubCommands.Jump.DisabledServers").contains(pToJoin.getName())) {
			sendError(pPlayer, "Friends.Command.Jump.CanNotJump");
			return true;
		}
		return false;
	}
}
