package pw.biome.biomechatrelay.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.spec.RoleCreateSpec;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.util.Color;
import discord4j.rest.util.PermissionSet;
import pw.biome.biomechat.obj.Corp;
import pw.biome.biomechatrelay.util.StringFormatUtility;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordGroupSyncHandler {

    private static final Snowflake BIOME_GUILD_ID = Snowflake.of("618600961153433611");
    private static final Snowflake BIOME_PLAYER_ROLE_ID = Snowflake.of("619028333308411925");

    private final DiscordClient discordClient;
    private RestGuild guild;
    private PermissionSet playerPermissionSet;

    /**
     * Constructor
     *
     * @param discordClient connection to discord
     */
    public DiscordGroupSyncHandler(DiscordClient discordClient) {
        this.discordClient = discordClient;
    }

    /**
     * Method to handle the updating of a users discord roles based on their in game rank
     *
     * @param corp of the player being adjusted
     */
    public void handleUser(Corp corp) {
        guild.getMembers().filter(memberData -> memberData.nick().get().isPresent())
                .collectList().subscribe(matched -> matched.forEach(matchedMember -> {
            if (!matchedMember.roles().contains(corp.getName())) {
                Snowflake userId = Snowflake.of(matchedMember.user().id());
                AtomicBoolean roleExists = new AtomicBoolean(false);

                // Try search through the current roles and add the user to it
                guild.getRoles().filter(roleData ->
                        roleData.name().equalsIgnoreCase(corp.getName()))
                        .subscribe(roleData -> {
                            roleExists.set(true);
                            Snowflake roleId = Snowflake.of(roleData.id());
                            guild.addMemberRole(userId, roleId, "DiscordGroupSyncHandler");
                        });

                // Otherwise if the role doesn't exist
                if (!roleExists.get()) {
                    String rankName = corp.getName();
                    String formattedName = StringFormatUtility.convertFromMCRoleNameToDiscordRoleName(rankName);
                    Color color = Color.of(corp.getPrefix().getColor().getRGB());

                    // Create role and then assign it to the user
                    createRole(formattedName, color).subscribe(roleData -> {
                        Snowflake roleId = Snowflake.of(roleData.id());
                        guild.addMemberRole(userId, roleId, "DiscordGroupSyncHandler");
                    });
                }
            }
        }));
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
                .setPermissions(loadOrGetPermissionSet());

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

    public PermissionSet loadOrGetPermissionSet() {
        if (this.playerPermissionSet == null) {
            discordClient
                    .getRoleById(BIOME_GUILD_ID, BIOME_PLAYER_ROLE_ID)
                    .getData()
                    .subscribe(roleData -> this.playerPermissionSet = PermissionSet.of(roleData.permissions()));
        }
        return this.playerPermissionSet;
    }
}
