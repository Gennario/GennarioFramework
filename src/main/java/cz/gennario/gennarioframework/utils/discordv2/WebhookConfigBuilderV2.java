package cz.gennario.gennarioframework.utils.discordv2;

import cz.gennario.gennarioframework.utils.replacement.Replacement;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Configuration builder for DiscordWebhookV2New using BoostedYAML
 * Supports Discord Components V2 features:
 * - Containers with accent_color: null for no color sidebar
 * - TextDisplay, MediaGallery, Separator, Section components
 * - Legacy embeds
 * - Polls
 */
public class WebhookConfigBuilderV2 {

    private final Section section;
    private final Replacement replacement;
    private DiscordWebhookV2 webhook;

    public WebhookConfigBuilderV2(Section section, Replacement replacement) {
        this.section = section;
        this.replacement = replacement;
    }

    /**
     * Builds the webhook from configuration
     * @return this builder for chaining
     */
    public WebhookConfigBuilderV2 build() {
        if (!section.getBoolean("enabled", true)) return this;

        webhook = new DiscordWebhookV2(section.getString("url"));

        // Basic settings
        if (section.contains("username")) {
            webhook.setUsername(replacement.replace(null, section.getString("username")));
        }
        if (section.contains("avatar")) {
            webhook.setAvatarUrl(replacement.replace(null, section.getString("avatar")));
        }
        if (section.contains("tts")) {
            webhook.setTts(section.getBoolean("tts"));
        }

        // Content/Message - for legacy mode use content field
        // For Components V2, we'll add it as a TextDisplay in a container
        String messageContent = null;
        if (section.contains("message")) {
            messageContent = replacement.replace(null, section.getString("message"));
        } else if (section.contains("content")) {
            messageContent = replacement.replace(null, section.getString("content"));
        }

        // Message flags
        if (section.getBoolean("silent", false)) {
            webhook.setSilent(true);
        }
        if (section.getBoolean("suppress-embeds", false)) {
            webhook.setSuppressEmbeds(true);
        }

        // Allowed mentions
        if (section.contains("allowed-mentions") && section.isSection("allowed-mentions")) {
            parseAllowedMentions(section.getSection("allowed-mentions"));
        }

        // If we have a message and containers, add message as first TextDisplay component
        boolean hasContainers = section.contains("containers") && section.isSection("containers");
        if (messageContent != null && !messageContent.isEmpty() && hasContainers) {
            // Add TextDisplay (type 10) as first top-level component for the message
            webhook.addComponent(new DiscordWebhookV2.TextDisplay(messageContent));
        } else if (messageContent != null && !messageContent.isEmpty()) {
            // Legacy mode - set content directly
            webhook.setContent(messageContent);
        }

        // Containers (Components V2 - new system)
        if (hasContainers) {
            parseContainers(section.getSection("containers"));
        }

        // Action rows (buttons, etc.)
        if (section.contains("action-rows") && section.isSection("action-rows")) {
            parseActionRows(section.getSection("action-rows"));
        }

        // Legacy embeds
        if (section.contains("embeds") && section.isSection("embeds")) {
            parseEmbeds(section.getSection("embeds"));
        }

        // Poll
        if (section.contains("poll") && section.isSection("poll")) {
            parsePoll(section.getSection("poll"));
        }

        return this;
    }

    private void parseAllowedMentions(Section mentionsSection) {
        DiscordWebhookV2.AllowedMentions mentions = new DiscordWebhookV2.AllowedMentions();

        if (mentionsSection.contains("parse")) {
            for (String parse : mentionsSection.getStringList("parse")) {
                switch (parse.toLowerCase()) {
                    case "roles": mentions.allowRoles(); break;
                    case "users": mentions.allowUsers(); break;
                    case "everyone": mentions.allowEveryone(); break;
                }
            }
        }
        if (mentionsSection.contains("roles")) {
            for (String role : mentionsSection.getStringList("roles")) {
                mentions.addRole(role);
            }
        }
        if (mentionsSection.contains("users")) {
            for (String user : mentionsSection.getStringList("users")) {
                mentions.addUser(user);
            }
        }
        if (mentionsSection.contains("replied-user")) {
            mentions.setRepliedUser(mentionsSection.getBoolean("replied-user"));
        }

        webhook.setAllowedMentions(mentions);
    }

    private void parseContainers(Section containersSection) {
        for (String containerKey : containersSection.getRoutesAsStrings(false)) {
            Section containerSection = containersSection.getSection(containerKey);
            DiscordWebhookV2.Container container = new DiscordWebhookV2.Container();

            // Accent color (sidebar color)
            // - "none" or "transparent" = no color bar (accent_color: null)
            // - "#XXXXXX" = specific hex color
            // - not present = default (no accent_color in payload)
            if (containerSection.contains("accent-color")) {
                String colorStr = containerSection.getString("accent-color");
                if (colorStr != null) {
                    colorStr = colorStr.trim().toLowerCase();
                    if (colorStr.equals("none") || colorStr.equals("transparent") || colorStr.equals("null")) {
                        container.setNoAccentColor();
                    } else if (!colorStr.isEmpty()) {
                        try {
                            Color color = Color.decode(colorStr.startsWith("#") ? colorStr : "#" + colorStr);
                            container.setAccentColor(color);
                        } catch (NumberFormatException e) {
                            // Invalid color format, skip
                        }
                    }
                }
            }

            // Spoiler
            if (containerSection.getBoolean("spoiler", false)) {
                container.setSpoiler(true);
            }

            // Components inside container
            if (containerSection.contains("components") && containerSection.isSection("components")) {
                Section componentsSection = containerSection.getSection("components");
                for (String compKey : componentsSection.getRoutesAsStrings(false)) {
                    Section compSection = componentsSection.getSection(compKey);
                    String type = compSection.getString("type", "text").toLowerCase();

                    switch (type) {
                        case "text":
                        case "text-display":
                            String content = replacement.replace(null, compSection.getString("content", ""));
                            container.addComponent(new DiscordWebhookV2.TextDisplay(content));
                            break;

                        case "separator":
                            DiscordWebhookV2.Separator separator = new DiscordWebhookV2.Separator();
                            separator.setDivider(compSection.getBoolean("divider", true));
                            separator.setSpacing(compSection.getInt("spacing", 1));
                            container.addComponent(separator);
                            break;

                        case "media-gallery":
                        case "gallery":
                            DiscordWebhookV2.MediaGallery gallery = new DiscordWebhookV2.MediaGallery();
                            if (compSection.contains("items")) {
                                List<?> items = compSection.getList("items");
                                if (items != null) {
                                    for (Object itemObj : items) {
                                        if (itemObj instanceof Map) {
                                            @SuppressWarnings("unchecked")
                                            Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                                            String url = (String) itemMap.get("url");
                                            if (url != null) {
                                                DiscordWebhookV2.MediaItem mediaItem = new DiscordWebhookV2.MediaItem(url);
                                                if (itemMap.containsKey("description")) {
                                                    mediaItem.setDescription((String) itemMap.get("description"));
                                                }
                                                if (itemMap.containsKey("spoiler") && Boolean.TRUE.equals(itemMap.get("spoiler"))) {
                                                    mediaItem.setSpoiler(true);
                                                }
                                                gallery.addItem(mediaItem);
                                            }
                                        } else if (itemObj instanceof String) {
                                            gallery.addItem((String) itemObj);
                                        }
                                    }
                                }
                            }
                            container.addComponent(gallery);
                            break;

                        case "file":
                            String fileUrl = compSection.getString("url", "");
                            DiscordWebhookV2.FileDisplay file = new DiscordWebhookV2.FileDisplay(fileUrl);
                            if (compSection.getBoolean("spoiler", false)) {
                                file.setSpoiler(true);
                            }
                            container.addComponent(file);
                            break;
                    }
                }
            }

            webhook.addComponent(container);
        }
    }

    private void parseActionRows(Section actionRowsSection) {
        for (String rowKey : actionRowsSection.getRoutesAsStrings(false)) {
            Section rowSection = actionRowsSection.getSection(rowKey);
            DiscordWebhookV2.ActionRow actionRow = new DiscordWebhookV2.ActionRow();

            // Parse buttons in this row
            if (rowSection.contains("buttons") && rowSection.isSection("buttons")) {
                Section buttonsSection = rowSection.getSection("buttons");
                for (String btnKey : buttonsSection.getRoutesAsStrings(false)) {
                    Section btnSection = buttonsSection.getSection(btnKey);

                    String style = btnSection.getString("style", "link").toLowerCase();
                    String label = replacement.replace(null, btnSection.getString("label", ""));

                    DiscordWebhookV2.Button button;

                    switch (style) {
                        case "link":
                            String url = btnSection.getString("url", "");
                            button = DiscordWebhookV2.Button.link(label, url);
                            break;
                        case "primary":
                            button = DiscordWebhookV2.Button.primary(label, btnSection.getString("custom-id", "btn_" + btnKey));
                            break;
                        case "secondary":
                            button = DiscordWebhookV2.Button.secondary(label, btnSection.getString("custom-id", "btn_" + btnKey));
                            break;
                        case "success":
                            button = DiscordWebhookV2.Button.success(label, btnSection.getString("custom-id", "btn_" + btnKey));
                            break;
                        case "danger":
                            button = DiscordWebhookV2.Button.danger(label, btnSection.getString("custom-id", "btn_" + btnKey));
                            break;
                        default:
                            button = DiscordWebhookV2.Button.link(label, "");
                    }

                    if (btnSection.contains("emoji")) {
                        button.setEmoji(btnSection.getString("emoji"));
                    }
                    if (btnSection.getBoolean("disabled", false)) {
                        button.setDisabled(true);
                    }

                    actionRow.addComponent(button);
                }
            }

            // Parse select menu in this row
            if (rowSection.contains("select-menu") && rowSection.isSection("select-menu")) {
                Section selectSection = rowSection.getSection("select-menu");
                String customId = selectSection.getString("custom-id", "select_" + rowKey);

                DiscordWebhookV2.StringSelectMenu selectMenu = new DiscordWebhookV2.StringSelectMenu(customId);

                if (selectSection.contains("placeholder")) {
                    selectMenu.setPlaceholder(replacement.replace(null, selectSection.getString("placeholder")));
                }
                if (selectSection.contains("min-values")) {
                    selectMenu.setMinValues(selectSection.getInt("min-values"));
                }
                if (selectSection.contains("max-values")) {
                    selectMenu.setMaxValues(selectSection.getInt("max-values"));
                }
                if (selectSection.getBoolean("disabled", false)) {
                    selectMenu.setDisabled(true);
                }

                // Parse options
                if (selectSection.contains("options") && selectSection.isSection("options")) {
                    Section optionsSection = selectSection.getSection("options");
                    for (String optKey : optionsSection.getRoutesAsStrings(false)) {
                        Section optSection = optionsSection.getSection(optKey);

                        String label = replacement.replace(null, optSection.getString("label", optKey));
                        String value = optSection.getString("value", optKey);

                        DiscordWebhookV2.SelectOption option = new DiscordWebhookV2.SelectOption(label, value);

                        if (optSection.contains("description")) {
                            option.setDescription(replacement.replace(null, optSection.getString("description")));
                        }
                        if (optSection.contains("emoji")) {
                            option.setEmoji(optSection.getString("emoji"));
                        }
                        if (optSection.getBoolean("default", false)) {
                            option.setDefault(true);
                        }

                        selectMenu.addOption(option);
                    }
                }

                actionRow.addComponent(selectMenu);
            }

            webhook.addComponent(actionRow);
        }
    }

    private void parseEmbeds(Section embedsSection) {
        for (String embedKey : embedsSection.getRoutesAsStrings(false)) {
            Section embedSection = embedsSection.getSection(embedKey);
            DiscordWebhookV2.EmbedObject embed = new DiscordWebhookV2.EmbedObject();

            if (embedSection.contains("title")) {
                embed.setTitle(replacement.replace(null, embedSection.getString("title")));
            }
            if (embedSection.contains("description")) {
                embed.setDescription(replacement.replace(null, embedSection.getString("description")));
            }
            if (embedSection.contains("url")) {
                embed.setUrl(embedSection.getString("url"));
            }
            if (embedSection.contains("timestamp")) {
                embed.setTimestamp(embedSection.getString("timestamp"));
            }

            // Color
            if (embedSection.contains("color")) {
                String colorStr = embedSection.getString("color");
                if (colorStr != null && !colorStr.isEmpty()) {
                    colorStr = colorStr.trim();
                    if (!colorStr.equalsIgnoreCase("none") && !colorStr.equalsIgnoreCase("transparent")) {
                        try {
                            embed.setColor(Color.decode(colorStr.startsWith("#") ? colorStr : "#" + colorStr));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // Footer
            if (embedSection.contains("footer")) {
                embed.setFooter(
                    replacement.replace(null, embedSection.getString("footer.text")),
                    embedSection.getString("footer.icon")
                );
            }

            // Image
            if (embedSection.contains("image")) {
                if (embedSection.isSection("image")) {
                    embed.setImage(embedSection.getString("image.url"));
                } else {
                    embed.setImage(embedSection.getString("image"));
                }
            }

            // Thumbnail
            if (embedSection.contains("thumbnail")) {
                if (embedSection.isSection("thumbnail")) {
                    embed.setThumbnail(embedSection.getString("thumbnail.url"));
                } else {
                    embed.setThumbnail(embedSection.getString("thumbnail"));
                }
            }

            // Author
            if (embedSection.contains("author")) {
                embed.setAuthor(
                    replacement.replace(null, embedSection.getString("author.name")),
                    embedSection.getString("author.url"),
                    embedSection.getString("author.icon")
                );
            }

            // Fields
            if (embedSection.contains("fields") && embedSection.isSection("fields")) {
                Section fieldsSection = embedSection.getSection("fields");
                for (String fieldKey : fieldsSection.getRoutesAsStrings(false)) {
                    Section fieldSection = fieldsSection.getSection(fieldKey);
                    embed.addField(
                        replacement.replace(null, fieldSection.getString("name")),
                        replacement.replace(null, fieldSection.getString("value")),
                        fieldSection.getBoolean("inline", false)
                    );
                }
            }

            webhook.addEmbed(embed);
        }
    }

    private void parsePoll(Section pollSection) {
        String question = replacement.replace(null, pollSection.getString("question"));
        DiscordWebhookV2.Poll poll = new DiscordWebhookV2.Poll(question);

        if (pollSection.contains("duration-hours")) {
            poll.setDurationHours(pollSection.getInt("duration-hours"));
        }
        if (pollSection.contains("allow-multiselect")) {
            poll.setAllowMultiselect(pollSection.getBoolean("allow-multiselect"));
        }

        if (pollSection.contains("answers") && pollSection.isSection("answers")) {
            Section answersSection = pollSection.getSection("answers");
            for (String answerKey : answersSection.getRoutesAsStrings(false)) {
                if (answersSection.isSection(answerKey)) {
                    Section answerSection = answersSection.getSection(answerKey);
                    poll.addAnswer(
                        replacement.replace(null, answerSection.getString("text")),
                        answerSection.getString("emoji", null)
                    );
                } else {
                    poll.addAnswer(replacement.replace(null, answersSection.getString(answerKey)));
                }
            }
        }

        webhook.setPoll(poll);
    }

    /**
     * Sends the webhook to Discord
     */
    public void send() {
        if (webhook == null) return;
        try {
            webhook.execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send Discord webhook: " + e.getMessage(), e);
        }
    }

    /**
     * Get the built webhook instance
     */
    public DiscordWebhookV2 getWebhook() {
        return webhook;
    }

    /**
     * Check if the webhook is enabled in config
     */
    public boolean isEnabled() {
        return section.getBoolean("enabled", true);
    }
}
