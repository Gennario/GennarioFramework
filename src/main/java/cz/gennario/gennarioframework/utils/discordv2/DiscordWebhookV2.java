package cz.gennario.gennarioframework.utils.discordv2;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

/**
 * Discord Webhook V2 with full support for Discord Components V2 features:
 * - Container-based messages (type 17) with accent_color for colored/colorless sidebar
 * - TextDisplay (type 10), MediaGallery (type 12), Separator (type 14)
 * - Section (type 9), File (type 13)
 * - Legacy embeds support
 * - Polls
 *
 * For Components V2 messages:
 * - Add ?with_components_v2=true to webhook URL (done automatically)
 * - Use Container with accent_color: null for no color sidebar
 */
@Setter
@Getter
public class DiscordWebhookV2 {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();
    private final List<TopLevelComponent> components = new ArrayList<>();
    private AllowedMentions allowedMentions;
    private Poll poll;
    private String threadName;
    private Integer flags;

    // Message flags
    public static final int FLAG_SUPPRESS_EMBEDS = 1 << 2;
    public static final int FLAG_SUPPRESS_NOTIFICATIONS = 1 << 12;
    public static final int FLAG_IS_COMPONENTS_V2 = 1 << 15; // Required for Components V2

    public DiscordWebhookV2(String url) {
        this.url = url;
    }

    public DiscordWebhookV2 addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    public DiscordWebhookV2 addComponent(TopLevelComponent component) {
        this.components.add(component);
        return this;
    }

    public DiscordWebhookV2 setSilent(boolean silent) {
        if (silent) {
            this.flags = (this.flags == null ? 0 : this.flags) | FLAG_SUPPRESS_NOTIFICATIONS;
        }
        return this;
    }

    public DiscordWebhookV2 setSuppressEmbeds(boolean suppress) {
        if (suppress) {
            this.flags = (this.flags == null ? 0 : this.flags) | FLAG_SUPPRESS_EMBEDS;
        }
        return this;
    }

    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty() && this.poll == null && this.components.isEmpty()) {
            throw new IllegalArgumentException("Set content, add at least one EmbedObject, Component, or Poll");
        }

        // Check if using Components V2 (has containers or other V2 components)
        boolean useComponentsV2 = !this.components.isEmpty();

        // Note: IS_COMPONENTS_V2 flag is handled via query parameter, not in body
        // Some Discord endpoints may also need it in flags, but webhook uses query param

        JSONObject json = new JSONObject();

        // Username and avatar - works with Components V2 too
        if (this.username != null) {
            json.put("username", this.username);
        }
        if (this.avatarUrl != null) {
            json.put("avatar_url", this.avatarUrl);
        }

        // Content field - for legacy mode only
        // In Components V2 mode, content should be inside a TextDisplay component
        if (!useComponentsV2 && this.content != null && !this.content.isEmpty()) {
            json.put("content", this.content);
        }

        if (this.tts && !useComponentsV2) {
            json.put("tts", true);
        }

        // Flags - Components V2 needs the flag
        if (useComponentsV2) {
            int flagsValue = (this.flags == null ? 0 : this.flags) | FLAG_IS_COMPONENTS_V2;
            json.put("flags", flagsValue);
        } else if (this.flags != null && this.flags != 0) {
            json.put("flags", this.flags);
        }

        // Components V2
        if (useComponentsV2) {
            List<JSONObject> componentsList = new ArrayList<>();
            for (TopLevelComponent comp : this.components) {
                componentsList.add(comp.toJson());
            }
            json.put("components", componentsList.toArray());
        }

        // Legacy embeds (only if not using Components V2)
        if (!useComponentsV2 && !this.embeds.isEmpty()) {
            List<JSONObject> embedsList = new ArrayList<>();
            for (EmbedObject embed : this.embeds) {
                embedsList.add(serializeEmbed(embed));
            }
            json.put("embeds", embedsList.toArray());
        }

        // Allowed mentions
        if (this.allowedMentions != null) {
            json.put("allowed_mentions", serializeAllowedMentions(this.allowedMentions));
        }

        // Poll
        if (this.poll != null) {
            json.put("poll", serializePoll(this.poll));
        }

        // Build URL with query param for Components V2
        // Must use /api/v10/webhooks/ and with_components=true
        String webhookUrl = this.url;

        // Convert /api/webhooks/ to /api/v10/webhooks/ if needed
        if (webhookUrl.contains("/api/webhooks/") && !webhookUrl.contains("/api/v10/")) {
            webhookUrl = webhookUrl.replace("/api/webhooks/", "/api/v10/webhooks/");
        }

        if (useComponentsV2) {
            webhookUrl += (webhookUrl.contains("?") ? "&" : "?") + "with_components=true";
        }

        String jsonString = json.toString();

        // Send JSON request
        URL urlObj = new URL(webhookUrl);
        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "GennarioFramework-DiscordWebhook");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");


        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            stream.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            String errorResponse = "";
            try (java.io.InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(errorStream, java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    errorResponse = sb.toString();
                }
            }
            throw new IOException("Discord API error (HTTP " + responseCode + "): " + errorResponse + " | URL: " + webhookUrl + " | Payload: " + jsonString);
        }

        connection.disconnect();
    }

    private JSONObject serializeEmbed(EmbedObject embed) {
        JSONObject json = new JSONObject();

        if (embed.getTitle() != null) json.put("title", embed.getTitle());
        if (embed.getDescription() != null) json.put("description", embed.getDescription());
        if (embed.getUrl() != null) json.put("url", embed.getUrl());
        if (embed.getTimestamp() != null) json.put("timestamp", embed.getTimestamp());

        // Color - only include if explicitly set
        if (embed.getColor() != null) {
            Color c = embed.getColor();
            int rgb = (c.getRed() << 16) + (c.getGreen() << 8) + c.getBlue();
            json.put("color", rgb);
        }

        if (embed.getFooter() != null) {
            JSONObject footer = new JSONObject();
            footer.put("text", embed.getFooter().getText());
            if (embed.getFooter().getIconUrl() != null) {
                footer.put("icon_url", embed.getFooter().getIconUrl());
            }
            json.put("footer", footer);
        }

        if (embed.getImage() != null) {
            JSONObject image = new JSONObject();
            image.put("url", embed.getImage().getUrl());
            json.put("image", image);
        }

        if (embed.getThumbnail() != null) {
            JSONObject thumbnail = new JSONObject();
            thumbnail.put("url", embed.getThumbnail().getUrl());
            json.put("thumbnail", thumbnail);
        }

        if (embed.getAuthor() != null) {
            JSONObject author = new JSONObject();
            author.put("name", embed.getAuthor().getName());
            if (embed.getAuthor().getUrl() != null) author.put("url", embed.getAuthor().getUrl());
            if (embed.getAuthor().getIconUrl() != null) author.put("icon_url", embed.getAuthor().getIconUrl());
            json.put("author", author);
        }

        if (!embed.getFields().isEmpty()) {
            List<JSONObject> fields = new ArrayList<>();
            for (EmbedObject.Field field : embed.getFields()) {
                JSONObject f = new JSONObject();
                f.put("name", field.getName());
                f.put("value", field.getValue());
                f.put("inline", field.isInline());
                fields.add(f);
            }
            json.put("fields", fields.toArray());
        }

        return json;
    }

    private JSONObject serializeAllowedMentions(AllowedMentions mentions) {
        JSONObject json = new JSONObject();
        if (mentions.getParse() != null && !mentions.getParse().isEmpty()) {
            json.put("parse", mentions.getParse().toArray());
        }
        if (mentions.getRoles() != null && !mentions.getRoles().isEmpty()) {
            json.put("roles", mentions.getRoles().toArray());
        }
        if (mentions.getUsers() != null && !mentions.getUsers().isEmpty()) {
            json.put("users", mentions.getUsers().toArray());
        }
        if (mentions.isRepliedUser()) {
            json.put("replied_user", true);
        }
        return json;
    }

    private JSONObject serializePoll(Poll poll) {
        JSONObject json = new JSONObject();

        JSONObject question = new JSONObject();
        question.put("text", poll.getQuestion());
        json.put("question", question);

        List<JSONObject> answers = new ArrayList<>();
        for (Poll.Answer answer : poll.getAnswers()) {
            JSONObject a = new JSONObject();
            JSONObject pollMedia = new JSONObject();
            pollMedia.put("text", answer.getText());
            if (answer.getEmoji() != null) {
                JSONObject emoji = new JSONObject();
                emoji.put("name", answer.getEmoji());
                pollMedia.put("emoji", emoji);
            }
            a.put("poll_media", pollMedia);
            answers.add(a);
        }
        json.put("answers", answers.toArray());

        if (poll.getDurationHours() != null) {
            json.put("duration", poll.getDurationHours());
        }
        if (poll.isAllowMultiselect()) {
            json.put("allow_multiselect", true);
        }

        return json;
    }

    // ==========================================
    // TOP-LEVEL COMPONENTS
    // ==========================================

    public interface TopLevelComponent {
        JSONObject toJson();
    }

    // ==========================================
    // ACTION ROW (type 1) - Contains buttons or select menus
    // ==========================================

    @Getter
    public static class ActionRow implements TopLevelComponent {
        private final List<ActionRowComponent> components = new ArrayList<>();

        public ActionRow addComponent(ActionRowComponent component) {
            this.components.add(component);
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 1);

            List<JSONObject> compList = new ArrayList<>();
            for (ActionRowComponent comp : components) {
                compList.add(comp.toJson());
            }
            json.put("components", compList.toArray());

            return json;
        }
    }

    public interface ActionRowComponent {
        JSONObject toJson();
    }

    // ==========================================
    // BUTTON (type 2)
    // ==========================================

    @Getter
    public static class Button implements ActionRowComponent {
        private int style; // 1=Primary, 2=Secondary, 3=Success, 4=Danger, 5=Link
        private String label;
        private String customId;
        private String url;
        private String emojiName;
        private String emojiId;
        private boolean disabled;

        public Button(int style, String label) {
            this.style = style;
            this.label = label;
        }

        public static Button link(String label, String url) {
            Button btn = new Button(5, label);
            btn.url = url;
            return btn;
        }

        public static Button primary(String label, String customId) {
            Button btn = new Button(1, label);
            btn.customId = customId;
            return btn;
        }

        public static Button secondary(String label, String customId) {
            Button btn = new Button(2, label);
            btn.customId = customId;
            return btn;
        }

        public static Button success(String label, String customId) {
            Button btn = new Button(3, label);
            btn.customId = customId;
            return btn;
        }

        public static Button danger(String label, String customId) {
            Button btn = new Button(4, label);
            btn.customId = customId;
            return btn;
        }

        public Button setEmoji(String name) {
            this.emojiName = name;
            return this;
        }

        public Button setEmoji(String name, String id) {
            this.emojiName = name;
            this.emojiId = id;
            return this;
        }

        public Button setDisabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 2);
            json.put("style", style);
            json.put("label", label);

            if (style == 5 && url != null) {
                json.put("url", url);
            } else if (customId != null) {
                json.put("custom_id", customId);
            }

            if (emojiName != null) {
                JSONObject emoji = new JSONObject();
                emoji.put("name", emojiName);
                emoji.putNullable("id", emojiId);
                json.put("emoji", emoji);
            } else {
                json.putNullable("emoji", null);
            }

            json.put("disabled", disabled);

            return json;
        }
    }

    // ==========================================
    // STRING SELECT MENU (type 3)
    // ==========================================

    @Getter
    public static class StringSelectMenu implements ActionRowComponent {
        private final String customId;
        private String placeholder;
        private int minValues = 1;
        private int maxValues = 1;
        private boolean disabled;
        private final List<SelectOption> options = new ArrayList<>();

        public StringSelectMenu(String customId) {
            this.customId = customId;
        }

        public StringSelectMenu setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public StringSelectMenu setMinValues(int minValues) {
            this.minValues = minValues;
            return this;
        }

        public StringSelectMenu setMaxValues(int maxValues) {
            this.maxValues = maxValues;
            return this;
        }

        public StringSelectMenu setDisabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public StringSelectMenu addOption(SelectOption option) {
            this.options.add(option);
            return this;
        }

        public StringSelectMenu addOption(String label, String value) {
            this.options.add(new SelectOption(label, value));
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 3);
            json.put("custom_id", customId);

            if (placeholder != null) {
                json.put("placeholder", placeholder);
            }

            json.put("min_values", minValues);
            json.put("max_values", maxValues);
            json.put("disabled", disabled);

            List<JSONObject> optList = new ArrayList<>();
            for (SelectOption opt : options) {
                optList.add(opt.toJson());
            }
            json.put("options", optList.toArray());

            return json;
        }
    }

    @Getter
    public static class SelectOption {
        private final String label;
        private final String value;
        private String description;
        private String emojiName;
        private String emojiId;
        private boolean isDefault;

        public SelectOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public SelectOption setDescription(String description) {
            this.description = description;
            return this;
        }

        public SelectOption setEmoji(String name) {
            this.emojiName = name;
            return this;
        }

        public SelectOption setEmoji(String name, String id) {
            this.emojiName = name;
            this.emojiId = id;
            return this;
        }

        public SelectOption setDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("label", label);
            json.put("value", value);

            if (description != null) {
                json.put("description", description);
            }

            if (emojiName != null) {
                JSONObject emoji = new JSONObject();
                emoji.put("name", emojiName);
                emoji.putNullable("id", emojiId);
                json.put("emoji", emoji);
            }

            if (isDefault) {
                json.put("default", true);
            }

            return json;
        }
    }

    // ==========================================
    // CONTAINER (type 17) - Main V2 component
    // ==========================================

    @Getter
    public static class Container implements TopLevelComponent {
        private Integer accentColor; // null = no color bar, Integer = specific color
        private boolean hasAccentColor = false;
        private boolean spoiler = false;
        private final List<ContainerComponent> components = new ArrayList<>();

        public Container addComponent(ContainerComponent component) {
            this.components.add(component);
            return this;
        }

        /**
         * Set accent color (sidebar color)
         * @param color The color for the sidebar
         */
        public Container setAccentColor(Color color) {
            if (color != null) {
                this.accentColor = (color.getRed() << 16) + (color.getGreen() << 8) + color.getBlue();
                this.hasAccentColor = true;
            }
            return this;
        }

        /**
         * Set accent color from integer RGB value
         */
        public Container setAccentColor(int rgb) {
            this.accentColor = rgb;
            this.hasAccentColor = true;
            return this;
        }

        /**
         * Remove accent color - makes sidebar invisible (transparent)
         * This is the Discord Messaging V2 feature for "no color bar"
         */
        public Container setNoAccentColor() {
            this.accentColor = null;
            this.hasAccentColor = true; // We explicitly want null
            return this;
        }

        public Container setSpoiler(boolean spoiler) {
            this.spoiler = spoiler;
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 17);

            // accent_color: null = no sidebar, number = colored sidebar
            // Must always be present according to Discord API
            json.putNullable("accent_color", accentColor);

            // spoiler must always be present
            json.put("spoiler", spoiler);

            List<JSONObject> compList = new ArrayList<>();
            for (ContainerComponent comp : components) {
                compList.add(comp.toJson());
            }
            json.put("components", compList.toArray());

            return json;
        }
    }

    // ==========================================
    // CONTAINER COMPONENTS
    // ==========================================

    public interface ContainerComponent {
        JSONObject toJson();
    }

    /**
     * TextDisplay (type 10) - Displays markdown text
     * Can be used as top-level component OR inside a Container
     */
    @Getter
    public static class TextDisplay implements ContainerComponent, TopLevelComponent {
        private final String content;

        public TextDisplay(String content) {
            this.content = content;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 10);
            json.put("content", content);
            return json;
        }
    }

    /**
     * Section (type 9) - Contains text and optional accessory
     */
    @Getter
    public static class Section implements ContainerComponent {
        private final List<ContainerComponent> components = new ArrayList<>();
        private ContainerComponent accessory;

        public Section addComponent(ContainerComponent component) {
            this.components.add(component);
            return this;
        }

        public Section setAccessory(ContainerComponent accessory) {
            this.accessory = accessory;
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 9);

            List<JSONObject> compList = new ArrayList<>();
            for (ContainerComponent comp : components) {
                compList.add(comp.toJson());
            }
            json.put("components", compList.toArray());

            if (accessory != null) {
                json.put("accessory", accessory.toJson());
            }

            return json;
        }
    }

    /**
     * MediaGallery (type 12) - Displays images/videos
     */
    @Getter
    public static class MediaGallery implements ContainerComponent {
        private final List<MediaItem> items = new ArrayList<>();

        public MediaGallery addItem(MediaItem item) {
            this.items.add(item);
            return this;
        }

        public MediaGallery addItem(String url) {
            this.items.add(new MediaItem(url));
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 12);

            List<JSONObject> itemList = new ArrayList<>();
            for (MediaItem item : items) {
                itemList.add(item.toJson());
            }
            json.put("items", itemList.toArray());

            return json;
        }
    }

    @Getter
    public static class MediaItem {
        private final String url;
        private String description;
        private boolean spoiler;

        public MediaItem(String url) {
            this.url = url;
        }

        public MediaItem setDescription(String description) {
            this.description = description;
            return this;
        }

        public MediaItem setSpoiler(boolean spoiler) {
            this.spoiler = spoiler;
            return this;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            JSONObject media = new JSONObject();
            media.put("url", url);
            json.put("media", media);
            // Always include description (null if not set) and spoiler (false if not set)
            json.putNullable("description", description);
            json.put("spoiler", spoiler);
            return json;
        }
    }

    /**
     * Separator (type 14) - Visual divider
     */
    public static class Separator implements ContainerComponent {
        private boolean divider = true;
        private int spacing = 1; // 1 = small, 2 = large

        public Separator setDivider(boolean divider) {
            this.divider = divider;
            return this;
        }

        public Separator setSpacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 14);
            json.put("divider", divider);
            json.put("spacing", spacing);
            return json;
        }
    }

    /**
     * File (type 13) - File attachment display
     */
    @Getter
    public static class FileDisplay implements ContainerComponent {
        private final String fileUrl;
        private boolean spoiler;

        public FileDisplay(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public FileDisplay setSpoiler(boolean spoiler) {
            this.spoiler = spoiler;
            return this;
        }

        @Override
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", 13);
            JSONObject file = new JSONObject();
            file.put("url", fileUrl);
            json.put("file", file);
            // Always include spoiler field
            json.put("spoiler", spoiler);
            return json;
        }
    }

    // ==========================================
    // EMBED OBJECT (Legacy)
    // ==========================================

    @Getter
    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private String timestamp;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public EmbedObject setTitle(String title) { this.title = title; return this; }
        public EmbedObject setDescription(String description) { this.description = description; return this; }
        public EmbedObject setUrl(String url) { this.url = url; return this; }
        public EmbedObject setTimestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public EmbedObject setColor(Color color) { this.color = color; return this; }

        public EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        public EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public EmbedObject setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        @Getter
        public static class Footer {
            private final String text;
            private final String iconUrl;
            public Footer(String text, String iconUrl) { this.text = text; this.iconUrl = iconUrl; }
        }

        @Getter
        public static class Thumbnail {
            private final String url;
            public Thumbnail(String url) { this.url = url; }
        }

        @Getter
        public static class Image {
            private final String url;
            public Image(String url) { this.url = url; }
        }

        @Getter
        public static class Author {
            private final String name;
            private final String url;
            private final String iconUrl;
            public Author(String name, String url, String iconUrl) { this.name = name; this.url = url; this.iconUrl = iconUrl; }
        }

        @Getter
        public static class Field {
            private final String name;
            private final String value;
            private final boolean inline;
            public Field(String name, String value, boolean inline) { this.name = name; this.value = value; this.inline = inline; }
        }
    }

    // ==========================================
    // OTHER STRUCTURES
    // ==========================================

    @Getter
    public static class AllowedMentions {
        private List<String> parse = new ArrayList<>();
        private List<String> roles = new ArrayList<>();
        private List<String> users = new ArrayList<>();
        private boolean repliedUser;

        public AllowedMentions allowRoles() { this.parse.add("roles"); return this; }
        public AllowedMentions allowUsers() { this.parse.add("users"); return this; }
        public AllowedMentions allowEveryone() { this.parse.add("everyone"); return this; }
        public AllowedMentions addRole(String roleId) { this.roles.add(roleId); return this; }
        public AllowedMentions addUser(String userId) { this.users.add(userId); return this; }
        public AllowedMentions setRepliedUser(boolean repliedUser) { this.repliedUser = repliedUser; return this; }
    }

    @Getter
    public static class Poll {
        private final String question;
        private final List<Answer> answers = new ArrayList<>();
        private Integer durationHours;
        private boolean allowMultiselect;

        public Poll(String question) { this.question = question; }

        public Poll addAnswer(String text) { this.answers.add(new Answer(text, null)); return this; }
        public Poll addAnswer(String text, String emoji) { this.answers.add(new Answer(text, emoji)); return this; }
        public Poll setDurationHours(Integer hours) { this.durationHours = hours; return this; }
        public Poll setAllowMultiselect(boolean allow) { this.allowMultiselect = allow; return this; }

        @Getter
        public static class Answer {
            private final String text;
            private final String emoji;
            public Answer(String text, String emoji) { this.text = text; this.emoji = emoji; }
        }
    }

    // ==========================================
    // JSON HELPER
    // ==========================================

    public static class JSONObject {
        private final LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        public static final Object JSON_NULL = new Object() {
            @Override
            public String toString() {
                return "null";
            }
        };

        public void put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        public void putNullable(String key, Object value) {
            map.put(key, value == null ? JSON_NULL : value);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("{");
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                sb.append(quote(entry.getKey())).append(":");
                sb.append(valueToString(entry.getValue()));
                if (it.hasNext()) sb.append(",");
            }

            return sb.append("}").toString();
        }

        private String valueToString(Object val) {
            if (val == JSON_NULL) {
                return "null";
            } else if (val instanceof String) {
                return quote(escape((String) val));
            } else if (val instanceof Number || val instanceof Boolean) {
                return val.toString();
            } else if (val instanceof JSONObject) {
                return val.toString();
            } else if (val.getClass().isArray()) {
                StringBuilder sb = new StringBuilder("[");
                int len = Array.getLength(val);
                for (int i = 0; i < len; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(valueToString(Array.get(val, i)));
                }
                return sb.append("]").toString();
            }
            return "null";
        }

        private String quote(String s) {
            return "\"" + s + "\"";
        }

        private String escape(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}
