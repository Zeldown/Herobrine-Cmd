package be.zeldown.herobrinecmd.lib.entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import be.zeldown.herobrinecmd.lib.utils.FastUUID;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@Getter
public class OfflinePlayer {

	private final String name;
	private final UUID uuid;
	private final String uuidString;

	private final Optional<EntityPlayer> player;

	private OfflinePlayer(final @NonNull String name, final @NonNull UUID uuid, final EntityPlayer player) {
		this.name = name;
		this.uuid = uuid;
		this.uuidString = FastUUID.toString(uuid);
		this.player = Optional.ofNullable(player);
	}

	public static @NonNull OfflinePlayer of(final @NonNull UUID uuid) {
		return new OfflinePlayer(FastUUID.toString(uuid), uuid, null);
	}

	public static @NonNull OfflinePlayer of(final @NonNull String name, final @NonNull UUID uuid) {
		return new OfflinePlayer(name, uuid, null);
	}

	public static @NonNull OfflinePlayer of(final @NonNull GameProfile profile) {
		return new OfflinePlayer(profile.getName(), profile.getId(), null);
	}

	public static @NonNull OfflinePlayer of(final @NonNull EntityPlayer player) {
		return new OfflinePlayer(player.getCommandSenderName(), player.getUniqueID(), player);
	}

	public static CompletableFuture<OfflinePlayer> load(final @NonNull String name) {
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
			final EntityPlayer player = net.minecraft.server.MinecraftServer.getServer().getConfigurationManager().func_152612_a(name);
			if (player != null) {
				return CompletableFuture.completedFuture(OfflinePlayer.of(player));
			}
		}

		final CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
		new Thread(() -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
				final JsonObject object = (JsonObject) new JsonParser().parse(reader);
				final String uuidString = object.get("id").toString().replace("\"", "");
				final String parsedName = object.get("name").toString().replace("\"", "");
				future.complete(OfflinePlayer.of(parsedName, FastUUID.parseWithoutDashes(uuidString)));
				reader.close();
			} catch (final Exception e) {
				future.complete(null);
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (final Exception e) {}
			}
		}, "OfflinePlayerLoader/Name").start();
		return future;
	}

	public static CompletableFuture<OfflinePlayer> load(final @NonNull UUID uuid) {
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
			for (final World world : net.minecraft.server.MinecraftServer.getServer().worldServers) {
				final EntityPlayer player = world.func_152378_a(uuid);
				if (player != null) {
					return CompletableFuture.completedFuture(OfflinePlayer.of(player));
				}
			}
		}

		final CompletableFuture<OfflinePlayer> future = new CompletableFuture<>();
		new Thread(() -> {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + FastUUID.toString(uuid).replace("-", "")).openStream()));
				final String name = ((JsonObject) new JsonParser().parse(reader)).get("name").toString().replace("\"", "");
				future.complete(OfflinePlayer.of(name, uuid));
			} catch (final Exception e) {
				future.complete(null);
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (final Exception e) {}
			}
		}, "OfflinePlayerLoader/UUID").start();
		return future;
	}

	public boolean isOnline() {
		return this.player.isPresent();
	}

}