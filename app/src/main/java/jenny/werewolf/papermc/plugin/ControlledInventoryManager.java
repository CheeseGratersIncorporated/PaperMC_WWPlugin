package jenny.werewolf.papermc.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Lectern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.enchantments.Enchantment;     
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ControlledInventoryManager implements Listener {

    private final ExamplePlugin plugin;
    private final NamespacedKey controlledBookKey;
    private final int bookSlot;

    /*
     * Called whenever a player clicks "Done" after editing the book.
     *
     * First argument: player
     * Second argument: current book pages
     */
    private final BiConsumer<Player, List<String>> bookUpdateCallback;

    public ControlledInventoryManager(
            ExamplePlugin plugin,
            int bookSlot,
            BiConsumer<Player, List<String>> bookUpdateCallback
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.bookUpdateCallback =
                Objects.requireNonNull(bookUpdateCallback);

        if (bookSlot < 0 || bookSlot > 8) {
            throw new IllegalArgumentException(
                    "bookSlot must be between 0 and 8"
            );
        }

        this.bookSlot = bookSlot;
        this.controlledBookKey =
                new NamespacedKey(plugin, "controlled_book");

        Bukkit.getPluginManager().registerEvents(this, plugin);

        /*
         * Handle players who are already online if the manager
         * is created during a plugin reload.
         */
        for (Player player : Bukkit.getOnlinePlayers()) {
            prepareInventory(player);
        }
    }

    public ControlledInventoryManager(
            ExamplePlugin plugin,
            int bookSlot
    ) {
        this(plugin, bookSlot, (player, pages) -> {
        });
    }

    /**
     * Clears everything except the player's existing controlled book.
     *
     * If the controlled book already exists, the same ItemStack is reused,
     * preserving all pages the player wrote.
     */
    public void prepareInventory(Player player) {
        if(!plugin.gameInstance.getWWPlayerNames().contains(player.getName())||!plugin.gameInstance.isActive())return;
        PlayerInventory inventory = player.getInventory();

        ItemStack book = findControlledBook(inventory);

        if (book == null) {
            book = createControlledBook();
        } else {
            book.setAmount(1);
        }

        inventory.clear();
        player.setItemOnCursor(new ItemStack(Material.AIR));

        inventory.setItem(bookSlot, book);
        inventory.setHeldItemSlot(bookSlot);
    }

    public void toggleJudgment(Player player){
        if(!plugin.gameInstance.getWWPlayerNames().contains(player.getName())||!plugin.gameInstance.isActive())return;
        if(!plugin.gameInstance.getWWPlayer(player.getName()).isAlive())return;
        if(plugin.gameInstance.getWWPlayer(player.getName())==plugin.gameInstance.getSentenced())return;
        
        PlayerInventory inventory = player.getInventory();

        if(!plugin.gameInstance.canJudge()){
            inventory.setItem(7, new ItemStack(Material.AIR));
            inventory.setItem(8, new ItemStack(Material.AIR));
            return;
        }
        ItemStack innocent = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = innocent.getItemMeta();

        meta.displayName(Component.text("INNOCENT", NamedTextColor.GREEN));

        innocent.setItemMeta(meta);


        ItemStack guilty = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta2 = guilty.getItemMeta();

        meta2.displayName(Component.text("GUILTY", NamedTextColor.RED));

        guilty.setItemMeta(meta2);

        inventory.setItem(7, innocent);
        inventory.setItem(8, guilty);
        
        inventory.setHeldItemSlot(4);
    }
    public void toggleJudgmentEnchant(Player player, int slot){
        if(!plugin.gameInstance.getWWPlayerNames().contains(player.getName())||!plugin.gameInstance.isActive())return;
        if(!plugin.gameInstance.getWWPlayer(player.getName()).isAlive())return;

        
        PlayerInventory inventory = player.getInventory();

        if(!plugin.gameInstance.canJudge()){
            inventory.setItem(7, new ItemStack(Material.AIR));
            inventory.setItem(8, new ItemStack(Material.AIR));
            return;
        }
        ItemStack innocent = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = innocent.getItemMeta();

        meta.displayName(Component.text("INNOCENT", NamedTextColor.GREEN));
        if(slot==7){
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
            innocent.setItemMeta(meta);


        ItemStack guilty = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta2 = guilty.getItemMeta();

        meta2.displayName(Component.text("GUILTY", NamedTextColor.RED));
        if(slot==8){
            meta2.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        guilty.setItemMeta(meta2);

        inventory.setItem(7, innocent);
        inventory.setItem(8, guilty);
    }
    /**
     * Returns a copy of the current pages in the player's book.
     */
    public List<String> getPages(Player player) {
        ItemStack book =
                player.getInventory().getItem(bookSlot);

        if (!isControlledBook(book)) {
            return List.of();
        }

        if (book.getType() != Material.WRITABLE_BOOK) {
            return List.of();
        }

        ItemMeta itemMeta = book.getItemMeta();

        if (!(itemMeta instanceof WritableBookMeta bookMeta)) {
            return List.of();
        }

        return List.copyOf(bookMeta.getPages());
    }

    /**
     * Returns all pages as one String.
     */
    public String getText(Player player) {
        return String.join("\n\n", getPages(player));
    }

    public boolean copyBookToLectern(Player player, Location lecternLocation) {
    if (!(lecternLocation.getBlock().getState() instanceof Lectern lectern)) {
        return false;
    }

    ItemStack original =
            player.getInventory().getItem(bookSlot);

    if (!isControlledBook(original)
            || original.getType() != Material.WRITABLE_BOOK) {
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
        ItemStack book =
                new ItemStack(Material.WRITABLE_BOOK);

        WritableBookMeta meta =
                (WritableBookMeta) book.getItemMeta();

        meta.displayName(
                Component.text(
                                "Your Will",
                                NamedTextColor.GOLD
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );

        meta.getPersistentDataContainer().set(
                controlledBookKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        book.setItemMeta(meta);
        return book;
    }

    private ItemStack findControlledBook(
            PlayerInventory inventory
    ) {
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

        if (item.getType() != Material.WRITABLE_BOOK
                && item.getType() != Material.WRITTEN_BOOK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                controlledBookKey,
                PersistentDataType.BYTE
        );
    }


    
    /*
     * ---------------------------------------------------------
     * Player setup
     * ---------------------------------------------------------
     */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        /*
         * Searches for the old controlled book before clearing,
         * so reconnecting does not erase its pages.
         */
        prepareInventory(event.getPlayer());
    }

    /*
     * ---------------------------------------------------------
     * Prevent inventory movement
     * ---------------------------------------------------------
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(
            InventoryClickEvent event
    ) {
        if (event.getWhoClicked() instanceof Player) {
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getWhoClicked().getName())){
/*
             * This prevents:
             *
             * - Normal clicks
             * - Shift clicks
             * - Number-key swaps
             * - Double clicks
             * - Moving items into containers
             * - Creative inventory manipulation
             */
            event.setCancelled(true);
            }
            
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(
            InventoryDragEvent event
    ) {
        if (event.getWhoClicked() instanceof Player) {
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getWhoClicked().getName()))

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        /*
         * Since controlled players should have no other items,
         * blocking every drop is reasonable.
         */
        
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getPlayer().getName()))
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHands(
            PlayerSwapHandItemsEvent event
    ) {
        /*
         * Prevents moving the book into the offhand with F.
         */
        
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getPlayer().getName()))
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            /*
             * Keeps the rest of the inventory empty.
             */
            
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getEntity().getName()))
            event.setCancelled(true);
        }
    }

    /*
     * ---------------------------------------------------------
     * Prevent placing the book into blocks or entities
     * ---------------------------------------------------------
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getPlayer().getName())){

                    
                if (!isControlledBook(event.getItem())) {
                    return;
                }

                if (event.getClickedBlock() == null) {
                    /*
                    * Right-clicking air is allowed, which opens the book.
                    */
                    return;
                }

                /*
                * Prevent the clicked block from taking the book, such as:
                *
                * - Lecterns
                * - Chiseled bookshelves
                * - Decorated pots
                *
                * The item itself is still allowed to activate, so the book
                * should open instead.
                */
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.ALLOW);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(
            PlayerInteractEntityEvent event
    ) {
        
            if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getPlayer().getName())){

                    
                ItemStack heldItem = switch (event.getHand()) {
                    case HAND ->
                            event.getPlayer()
                                    .getInventory()
                                    .getItemInMainHand();

                    case OFF_HAND ->
                            event.getPlayer()
                                    .getInventory()
                                    .getItemInOffHand();

                    default -> null;
                };

                if (isControlledBook(heldItem)) {
                    /*
                    * Prevents putting the book into an item frame or
                    * interacting with another entity using the book.
                    */
                    event.setCancelled(true);
                }
            }
    }

    /*
     * ---------------------------------------------------------
     * Read book contents
     * ---------------------------------------------------------
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();

        ItemStack currentBook =
                player.getInventory().getItem(bookSlot);

        if (!isControlledBook(currentBook)) {
            return;
        }
        if(plugin.gameInstance.getWWPlayerNames().contains(player.getName())
            &&plugin.gameInstance.isActive()
            &&!plugin.gameInstance.getWWPlayer(player.getName()).isAlive()){
                event.setCancelled(true);
            }

        /*
         * This contains the text the player just submitted.
         *
         * Reading the ItemStack from the inventory here may give
         * you the old contents because the event has not finished yet.
         */
        BookMeta newMeta = event.getNewBookMeta();

        /*
         * Preserve our plugin marker.
         */
        newMeta.getPersistentDataContainer().set(
                controlledBookKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        /*
         * Do not let players sign it. Signing normally converts a
         * writable book into a written book, making it non-editable.
         */
        event.setSigning(false);
        event.setNewBookMeta(newMeta);

        List<String> pages =
                List.copyOf(newMeta.getPages());

        bookUpdateCallback.accept(player, pages);
    }

    /*
     * ---------------------------------------------------------
     * Preserve the book through death
     * ---------------------------------------------------------
     */

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if(plugin.gameInstance.isActive()&&plugin.gameInstance.getWWPlayerNames().contains(event.getPlayer().getName())){

            Iterator<ItemStack> drops =
                    event.getDrops().iterator();

            while (drops.hasNext()) {
                ItemStack item = drops.next();

                // if (!isControlledBook(item)) {
                //     continue;
                // }

                drops.remove();
                event.getItemsToKeep().add(item);
            }
        }
    }

    // @EventHandler
    // public void onRespawn(PlayerRespawnEvent event) {
    //     /*
    //      * Wait until Minecraft has finished rebuilding the
    //      * player's inventory after respawning.
    //      */
    //     Bukkit.getScheduler().runTask(
    //             plugin,
    //             () -> prepareInventory(event.getPlayer())
    //     );
    // }
}