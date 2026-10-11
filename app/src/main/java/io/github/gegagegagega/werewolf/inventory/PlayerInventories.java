package io.github.gegagegagega.werewolf.inventory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Lectern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.bukkit.persistence.PersistentDataType;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.Judgment;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Locks down participants' inventories during a game. Each player keeps one
 * writable book (their will) in {@link #BOOK_SLOT}; during judgment the
 * innocent/guilty buttons sit in {@link #INNOCENT_SLOT} and {@link #GUILTY_SLOT}.
 */
public final class PlayerInventories implements Listener {

    public static final int BOOK_SLOT = 0;
    public static final int JUDGMENT_HOLD_SLOT = 4;
    public static final int INNOCENT_SLOT = 7;
    public static final int GUILTY_SLOT = 8;

    private final WerewolfPlugin plugin;
    private final NamespacedKey controlledBookKey;

    public PlayerInventories(WerewolfPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.controlledBookKey = new NamespacedKey(plugin, "controlled_book");
    }

    /** Participants of a running game have their inventory locked. */
    private boolean isControlled(Player player) {
        Game game = plugin.game();
        return game.isActive() && game.hasPlayer(player.getName());
    }

    /**
     * Clears everything except the player's will. If they already have one it
     * is kept, preserving what they wrote.
     */
    public void prepareInventory(Player player) {
        if (!isControlled(player)) return;
        PlayerInventory inventory = player.getInventory();

        ItemStack book = findControlledBook(inventory);
        if (book == null) {
            book = createControlledBook();
        } else {
            book.setAmount(1);
        }

        inventory.clear();
        player.setItemOnCursor(new ItemStack(Material.AIR));

        inventory.setItem(BOOK_SLOT, book);
        inventory.setHeldItemSlot(BOOK_SLOT);
    }

    /** Shows or removes the judgment buttons, highlighting the current choice. */
    public void applyJudgmentButtons(Player player, boolean show, Judgment selected) {
        PlayerInventory inventory = player.getInventory();
        if (!show) {
            clearSlot(inventory, INNOCENT_SLOT);
            clearSlot(inventory, GUILTY_SLOT);
            return;
        }
        setIfDifferent(inventory, INNOCENT_SLOT,
                judgmentButton(Material.LIME_CONCRETE, "INNOCENT", NamedTextColor.GREEN, selected == Judgment.INNOCENT));
        setIfDifferent(inventory, GUILTY_SLOT,
                judgmentButton(Material.RED_CONCRETE, "GUILTY", NamedTextColor.RED, selected == Judgment.GUILTY));
    }

    /** Which verdict the button in this hotbar slot stands for, or null. */
    public static Judgment judgmentForSlot(int slot) {
        return switch (slot) {
            case INNOCENT_SLOT -> Judgment.INNOCENT;
            case GUILTY_SLOT -> Judgment.GUILTY;
            default -> null;
        };
    }

    private static ItemStack judgmentButton(Material material, String label, NamedTextColor color, boolean selected) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(label, color));
        if (selected) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static void setIfDifferent(PlayerInventory inventory, int slot, ItemStack item) {
        ItemStack current = inventory.getItem(slot);
        if (current == null || !current.isSimilar(item)) {
            inventory.setItem(slot, item);
        }
    }

    private static void clearSlot(PlayerInventory inventory, int slot) {
        ItemStack current = inventory.getItem(slot);
        if (current != null && !current.getType().isAir()) {
            inventory.setItem(slot, null);
        }
    }

    /** Returns a copy of the current pages in the player's book. */
    public List<String> getPages(Player player) {
        ItemStack book = player.getInventory().getItem(BOOK_SLOT);
        if (!isControlledBook(book) || book.getType() != Material.WRITABLE_BOOK) {
            return List.of();
        }
        if (!(book.getItemMeta() instanceof WritableBookMeta bookMeta)) {
            return List.of();
        }
        return List.copyOf(bookMeta.getPages());
    }

    /** Returns all pages as one String. */
    public String getText(Player player) {
        return String.join("\n\n", getPages(player));
    }

    /** Puts a copy of the player's will on their lectern, unless one is already there. */
    public boolean copyBookToLectern(Player player, Location lecternLocation) {
        if (lecternLocation == null || !(lecternLocation.getBlock().getState() instanceof Lectern lectern)) {
            return false;
        }
        ItemStack original = player.getInventory().getItem(BOOK_SLOT);
        if (!isControlledBook(original) || original.getType() != Material.WRITABLE_BOOK) {
            return false;
        }
        if (lectern.getInventory().getItem(0) != null) {
            return false;
        }
        ItemStack copy = original.clone();
        copy.setAmount(1);
        lectern.getInventory().setItem(0, copy);
        return true;
    }

    public boolean wipeLecternBook(Location lecternLocation) {
        if (!(lecternLocation.getBlock().getState() instanceof Lectern lectern)) {
            return false;
        }
        lectern.getInventory().clear();
        return true;
    }

    private ItemStack createControlledBook() {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        WritableBookMeta meta = (WritableBookMeta) book.getItemMeta();
        meta.displayName(Component.text("Your Will", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(controlledBookKey, PersistentDataType.BYTE, (byte) 1);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack findControlledBook(PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (isControlledBook(item)) {
                return item;
            }
        }
        return null;
    }

    private boolean isControlledBook(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        if (item.getType() != Material.WRITABLE_BOOK && item.getType() != Material.WRITTEN_BOOK) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(controlledBookKey, PersistentDataType.BYTE);
    }

    // ---------------------------------------------------------
    // Prevent inventory movement
    // ---------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Blocks normal, shift, number-key, double and creative clicks.
        if (event.getWhoClicked() instanceof Player player && isControlled(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && isControlled(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (isControlled(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        // Prevents moving the book into the offhand with F.
        if (isControlled(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && isControlled(player)) {
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------
    // Prevent placing the book into blocks or entities
    // ---------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!isControlled(event.getPlayer())) return;
        if (!isControlledBook(event.getItem())) return;
        // Right-clicking air is allowed, which opens the book.
        if (event.getClickedBlock() == null) return;
        // Stop lecterns, chiseled bookshelves and pots from taking the book; it still opens.
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.ALLOW);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!isControlled(event.getPlayer())) return;
        ItemStack heldItem = switch (event.getHand()) {
            case HAND -> event.getPlayer().getInventory().getItemInMainHand();
            case OFF_HAND -> event.getPlayer().getInventory().getItemInOffHand();
            default -> null;
        };
        if (isControlledBook(heldItem)) {
            // Prevents putting the book into an item frame.
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------
    // Book editing
    // ---------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        if (!isControlledBook(player.getInventory().getItem(BOOK_SLOT))) {
            return;
        }
        WWPlayer participant = plugin.game().getPlayer(player.getName());
        if (isControlled(player) && participant != null && !participant.isAlive()) {
            // The dead can't edit their will.
            event.setCancelled(true);
        }

        // The new meta is what the player just submitted; keep our marker on it.
        BookMeta newMeta = event.getNewBookMeta();
        newMeta.getPersistentDataContainer().set(controlledBookKey, PersistentDataType.BYTE, (byte) 1);
        // Signing would turn it into a written book that can't be edited.
        event.setSigning(false);
        event.setNewBookMeta(newMeta);
    }

    // ---------------------------------------------------------
    // Keep the book through death
    // ---------------------------------------------------------

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!isControlled(event.getPlayer())) return;
        Iterator<ItemStack> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemStack item = drops.next();
            drops.remove();
            event.getItemsToKeep().add(item);
        }
    }
}
