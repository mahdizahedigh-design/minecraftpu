package net.persianchat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Signs and books store one text for every viewer, so the text is shaped once, when saved.
 * This assumes players keep their game language on English (not Persian/Arabic).
 */
public final class StaticTextListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final boolean signs;
    private final boolean books;
    private final int bookLineChars;

    public StaticTextListener(boolean signs, boolean books, int bookLineChars) {
        this.signs = signs;
        this.books = books;
        this.bookLineChars = bookLineChars;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSign(SignChangeEvent event) {
        if (!signs) return;
        int count = event.lines().size();
        for (int i = 0; i < count; i++) {
            String plain = PLAIN.serialize(event.line(i) == null ? Component.empty() : event.line(i));
            String fixed = PersianText.processLine(plain);
            if (!fixed.equals(plain)) event.line(i, Component.text(fixed));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBook(PlayerEditBookEvent event) {
        if (!books) return;
        BookMeta meta = event.getNewBookMeta();
        boolean changed = false;

        List<Component> pages = new ArrayList<>();
        for (Component page : meta.pages()) {
            String plain = PLAIN.serialize(page);
            String fixed = PersianText.processPage(plain, bookLineChars);
            if (fixed.equals(plain)) {
                pages.add(page);
            } else {
                pages.add(Component.text(fixed));
                changed = true;
            }
        }
        if (changed) meta.pages(pages);

        if (event.isSigning() && meta.title() != null) {
            String title = PLAIN.serialize(meta.title());
            String fixed = PersianText.processLine(title);
            if (!fixed.equals(title)) {
                meta.title(Component.text(fixed));
                changed = true;
            }
        }
        if (changed) event.setNewBookMeta(meta);
    }
}
