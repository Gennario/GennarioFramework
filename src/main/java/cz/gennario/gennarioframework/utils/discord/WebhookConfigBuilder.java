package cz.gennario.gennarioframework.utils.discord;


import cz.gennario.gennarioframework.utils.replacement.Replacement;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.awt.*;
import java.io.IOException;

public class WebhookConfigBuilder {

    private Section section;
    private Replacement replacement;
    private DiscordWebhook discordWebhook;

    public WebhookConfigBuilder(Section section, Replacement replacement) {
        this.section = section;
        this.replacement = replacement;
    }

    public void build() {
        if (!section.getBoolean("enabled")) return;

        discordWebhook = new DiscordWebhook(section.getString("url"));
        if (section.contains("username")) discordWebhook.setUsername(replacement.replace(null, section.getString("username")));
        if (section.contains("avatar")) discordWebhook.setAvatarUrl(replacement.replace(null, section.getString("avatar")));
        if (section.contains("tts")) discordWebhook.setTts(section.getBoolean("tts"));
        if (section.contains("content")) discordWebhook.setContent(replacement.replace(null, section.getString("content")));

        if(section.contains("embeds")) {
            Section embeds = section.getSection("embeds");
            for (String routesAsString : embeds.getRoutesAsStrings(false)) {
                Section embedSection = embeds.getSection(routesAsString);

                DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
                if (embedSection.contains("title")) embedObject.setTitle(replacement.replace(null, embedSection.getString("title")));
                if (embedSection.contains("description")) embedObject.setDescription(replacement.replace(null, embedSection.getString("description")));
                if (embedSection.contains("url")) embedObject.setUrl(embedSection.getString("url"));
                if (embedSection.contains("color")) embedObject.setColor(Color.decode(embedSection.getString("color")));
                if (embedSection.contains("footer")) embedObject.setFooter(
                        replacement.replace(null, embedSection.getString("footer.text")),
                        embedSection.getString("footer.image")
                );
                if (embedSection.contains("image")) embedObject.setImage(embedSection.getString("image"));
                if (embedSection.contains("thumbnail")) embedObject.setThumbnail(embedSection.getString("thumbnail"));
                if (embedSection.contains("author")) embedObject.setAuthor(
                        replacement.replace(null, embedSection.getString("author.name")),
                        embedSection.getString("author.url"),
                        embedSection.getString("author.icon")
                );
                if (embedSection.contains("fields")) {
                    Section fields = embedSection.getSection("fields");
                    for (String field : fields.getRoutesAsStrings(false)) {
                        Section fieldSection = fields.getSection(field);
                        embedObject.addField(
                                replacement.replace(null, fieldSection.getString("name")),
                                replacement.replace(null, fieldSection.getString("value")),
                                fieldSection.getBoolean("inline")
                        );
                    }
                }

                discordWebhook.addEmbed(embedObject);
            }
        }
    }

    public void send() {
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
