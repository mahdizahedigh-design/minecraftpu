package net.persianchat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

public final class PersianChatPlugin extends JavaPlugin implements Listener {

    private List<String> rawLanguages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        rawLanguages = getConfig().getStringList("raw-languages")
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new StaticTextListener(
                getConfig().getBoolean("signs", true),
                getConfig().getBoolean("books", true),
                getConfig().getInt("book-line-chars", 14)), this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        ChatRenderer previous = event.renderer();
        event.renderer((source, sourceDisplayName, message, viewer) ->
                previous.render(source, sourceDisplayName, convert(message, viewer), viewer));
    }

    private Component convert(Component message, Audience viewer) {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        if (!ArabicShaper.needsProcessing(plain)) return message;
        // Java players whose game language is Persian/Arabic shape the text themselves.
        if (viewer instanceof Player p
                && rawLanguages.contains(p.locale().getLanguage().toLowerCase(Locale.ROOT))) {
            return message;
        }
        return Component.text(ArabicShaper.process(plain)).style(message.style());
    }
}
