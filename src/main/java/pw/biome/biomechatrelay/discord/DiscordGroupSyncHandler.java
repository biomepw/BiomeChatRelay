package pw.biome.biomechatrelay.discord;

import co.aikar.commands.lib.expiringmap.ExpiringMap;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.spec.RoleCreateSpec;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import org.bukkit.Bukkit;
import pw.biome.biomechat.obj.Corp;
import pw.biome.biomechatrelay.BiomeChatRelay;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordGroupSyncHandler {

    private static final Snowflake BIOME_GUILD_ID = Snowflake.of("618600961153433611");
    private static final Snowflake BIOME_PLAYER_ROLE_ID = Snowflake.of("619028333308411925");

    private final DiscordClient discordClient;
    private RestGuild guild;
    private PermissionSet playerPermissionSet;

    private final ExpiringMap<String, MemberData> discordUsernameCache;

    /**
     * Constructor
     *
     * @param discordClient connection to discord
     */
    public DiscordGroupSyncHandler(DiscordClient discordClient) {
        this.discordClient = discordClient;
        this.discordUsernameCache = ExpiringMap.builder().expiration(10000, TimeUnit.SECONDS).build();
    }

    /**
     * Method to handle the updating of a users discord roles based on their in game rank
     *
     * @param name of the player
     * @param corp of the player being adjusted
     */
    public void handleUser(String name, Corp corp) {
        boolean debug = BiomeChatRelay.getInstance().getDiscordThread().isDebugMode();

        if (debug) BiomeChatRelay.info("Debug: handling user " + name + " in corp " + corp.getName());

        if (discordUsernameCache.size() != 0) {
            MemberData memberData = discordUsernameCache.get(name);
            if (memberData == null) {
                if (debug) BiomeChatRelay.info("Debug: memberdata is null for " + name + " likely just have a mismatch in username!");
                return;
            }
            Snowflake userId = Snowflake.of(memberData.user().id());

            List<String> roles = memberData.roles();

            String sanitised = capitaliseFirst(corp.getName().toLowerCase().replaceAll("_", ""));

            // If their roles contains none of the following, it's time to add it!
            if (!roles.contains(corp.getName())
                    && !roles.contains(corp.getName().toLowerCase())
                    && !roles.contains(sanitised)) {
                AtomicBoolean roleExists = new AtomicBoolean(false);

                // Try search through the current roles and add the user to it
                loadOrGetGuild().getRoles().filter(roleData ->
                        roleData.name().equalsIgnoreCase(corp.getName())
                                || roleData.name().equalsIgnoreCase(sanitised))
                        .subscribe(roleData -> {
                            roleExists.set(true);
                            Snowflake roleId = Snowflake.of(roleData.id());
                            loadOrGetGuild().addMemberRole(userId, roleId, "DiscordGroupSyncHandler");
                            if (debug)
                                BiomeChatRelay.info("Debug: Role exists for " + corp.getName() + " adding user " + name);
                        });

                // Otherwise if the role doesn't exist
                if (!roleExists.get()) {
                    if (debug)
                        BiomeChatRelay.info("Debug: Role doesnt exist for " + corp.getName() + " sanitised: " + sanitised);
                    Color color = Color.of(corp.getPrefix().getColor().getRGB());

                    // Create role and then assign it to the user
                    createRole(sanitised, color).subscribe(roleData -> {
                        Snowflake roleId = Snowflake.of(roleData.id());
                        loadOrGetGuild().addMemberRole(userId, roleId, "DiscordGroupSyncHandler");
                        if (debug)
                            BiomeChatRelay.info("Debug:  creating role and assigning user: " + name + " for corp " + corp.getName());
                    });
                }
            }
        } else {
            if (debug) BiomeChatRelay.info("Debug: have to reload username cache and try again!");
            // Load username cache and then try again!
            loadUsernameCache();
            Bukkit.getScheduler().runTaskLaterAsynchronously(BiomeChatRelay.getInstance(), () -> handleUser(name, corp), 100);
        }
    }

    private void loadUsernameCache() {
        loadOrGetGuild().getMembers().toIterable().forEach(memberData -> {
            String username = memberData.user().username();
            discordUsernameCache.put(username, memberData);

            // If they have a nickname, cache that too!
            if (!memberData.nick().isAbsent() && memberData.nick().get().isPresent()) {
                String nickname = memberData.nick().get().get();
                discordUsernameCache.put(nickname, memberData);
            }
        });
    }

    private static String capitaliseFirst(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Create a role in the Biome guild
     *
     * @param name   of the role
     * @param colour of the role
     */
    public Mono<RoleData> createRole(String name, Color colour) {
        RoleCreateSpec roleCreateSpec = new RoleCreateSpec();

        roleCreateSpec
                .setName(name)
                .setColor(colour)
                .setMentionable(false)
                .setHoist(true)
                .setPermissions(this.playerPermissionSet);

        return loadOrGetGuild()
                .createRole(roleCreateSpec.asRequest(), "DiscordGroupSyncHandler");
    }

    /**
     * Deletes a role in the Biome guild
     *
     * @param name of the role to delete
     */
    public void deleteRole(String name) {
        loadOrGetGuild().getRoles().collectList().subscribe(listOfRoles -> listOfRoles.forEach(role -> {
            if (role.name().equalsIgnoreCase(name)) {
                loadOrGetGuild().deleteRole(Snowflake.of(role.id()), "DiscordGroupSyncHandler");
            }
        }));
    }

    public RestGuild loadOrGetGuild() {
        if (this.guild == null) {
            this.guild = discordClient.getGuildById(BIOME_GUILD_ID);
        }
        return this.guild;
    }

    public void loadPermissionSet() {
        discordClient
                .getRoleById(BIOME_GUILD_ID, BIOME_PLAYER_ROLE_ID)
                .getData()
                .subscribe(roleData -> this.playerPermissionSet = PermissionSet.of(roleData.permissions()));
    }
}
