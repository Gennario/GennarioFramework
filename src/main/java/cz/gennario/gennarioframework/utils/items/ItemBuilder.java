package cz.gennario.gennarioframework.utils.items;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import cz.gennario.gennarioframework.utils.Pair;
import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ItemBuilder {

    private final ItemStack itemStack;
    private ItemMeta itemMeta;
    private ReplacementPackage replacement;


    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.replacement = new ReplacementPackage();
        this.itemMeta = itemStack.getType() == Material.PLAYER_HEAD ? (SkullMeta) itemStack.getItemMeta() : itemStack.getItemMeta();
    }

    public ItemBuilder(Material m, int amount) {
        this.itemStack = new ItemStack(m, amount);
        this.replacement = new ReplacementPackage();
        this.itemMeta = itemStack.getType() == Material.PLAYER_HEAD ? (SkullMeta) itemStack.getItemMeta() : itemStack.getItemMeta();
    }

    public ItemBuilder(Material m, int amount, byte durability) {
        this.itemStack = new ItemStack(m, amount, durability);
        this.replacement = new ReplacementPackage();
        this.itemMeta = itemStack.getType() == Material.PLAYER_HEAD ? (SkullMeta) itemStack.getItemMeta() : itemStack.getItemMeta();
    }

    /* Replacement */

    public void setReplacement(ReplacementPackage replacement) {
        this.replacement = replacement;
    }


    /* Skull settings */
    public ItemBuilder setSkullOwner(String owner) {
        try {
            final SkullMeta im = (SkullMeta) this.itemStack.getItemMeta();
            im.setOwner(owner);
            this.itemStack.setItemMeta(im);
        } catch (ClassCastException ignored) {
        }
        return this;
    }

    public ItemBuilder setSkullBase64(String base64) {
        itemStack.setItemMeta(XSkull.createItem().profile(Profileable.detect(base64)).apply().getItemMeta());
        return this;
    }

    public ItemBuilder setSkull(String value) {
        itemStack.setItemMeta(XSkull.of(itemMeta).profile(Profileable.username(value)).apply());
        return this;
    }


    /* Custom model id */
    public int getCustomModelData() {
        return itemMeta.getCustomModelData();
    }

    public ItemBuilder setCustomModelData(int id) {
        itemMeta.setCustomModelData(id);
        updateItemMeta();
        return this;
    }

    /* Unbreakable */
    public boolean isUnbreakable() {
        return itemMeta.isUnbreakable();
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        updateItemMeta();
        return this;
    }

    /* Amount settings */
    public int getAmount() {
        return itemStack.getAmount();
    }

    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /* Durability settings */
    public int getDurability() {
        return itemStack.getDurability();
    }

    public ItemBuilder setDurability(short dur) {
        itemStack.setDurability(dur);
        return this;
    }


    public ItemBuilder setInfinityDurability() {
        itemStack.setDurability(Short.MAX_VALUE);
        return this;
    }

    /* DisplayName settings */
    public String getName() {
        return itemMeta.getDisplayName();
    }

    public ItemBuilder setDisplayName(String name) {
        itemMeta.setDisplayName(name);
        updateItemMeta();
        return this;
    }

    public ItemBuilder setName(String name) {
        itemMeta.setDisplayName(replacement.replace(null, name));
        updateItemMeta();
        return this;
    }

    public ItemBuilder setName(Player player, String name) {
        if (player == null) return setName(name);

        itemMeta.setDisplayName(replacement.replace(player, name));
        updateItemMeta();
        return this;
    }

    /* ItemFlags settings */
    public Set<ItemFlag> getItemFlags() {
        return itemMeta.getItemFlags();
    }

    public ItemBuilder addItemFlag(ItemFlag... itemFlag) {
        itemMeta.addItemFlags(itemFlag);
        updateItemMeta();
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag itemFlag) {
        itemMeta.addItemFlags(itemFlag);
        updateItemMeta();
        return this;
    }

    public ItemBuilder addItemFlag(List<ItemFlag> itemFlag) {
        itemFlag.forEach(this::addItemFlag);
        return this;
    }

    /* Enchants settings */
    public Map<Enchantment, Integer> getEnchants() {
        return itemMeta.getEnchants();
    }

    public List<Pair<Enchantment, Integer>> getEnchantments() {
        List<Pair<Enchantment, Integer>> list = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : getEnchants().entrySet()) {
            list.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        return list;
    }

    public ItemBuilder addEnchant(Enchantment ench, int level) {
        itemMeta.addEnchant(ench, level, true);
        updateItemMeta();
        return this;
    }

    public ItemBuilder addEnchant(List<Pair<Enchantment, Integer>> enchantments) {
        enchantments.forEach((pair) -> addEnchant(pair.getFirst(), pair.getSecond()));
        return this;
    }

    /* Lore settings */
    private List<String> getLore() {
        return itemMeta.getLore();
    }


    public ItemBuilder setLoreList(List<String> lore) {
        itemMeta.setLore(lore);
        updateItemMeta();
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        itemMeta.setLore((replacement.replace(null, Arrays.asList(lore))));
        updateItemMeta();
        return this;
    }

    public ItemBuilder setLore(Player player, String... lore) {
        if (player == null) return setLore(lore);
        itemMeta.setLore(replacement.replace(player, Arrays.asList(lore)));
        updateItemMeta();
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        itemMeta.setLore(replacement.replace(null, lore));
        updateItemMeta();
        return this;
    }

    public ItemBuilder setLore(Player player, List<String> lore) {
        if (player == null) return setLore(lore);
        itemMeta.setLore(replacement.replace(player, lore));
        updateItemMeta();
        return this;
    }

    public ItemBuilder removeLoreLine(String line) {
        List<String> lore = new ArrayList<>(getLore());
        if (!lore.contains(line)) return this;
        lore.remove(line);
        itemMeta.setLore(lore);
        updateItemMeta();
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        List<String> lore = new ArrayList<>(getLore());
        if (index < 0 || index > lore.size()) return this;
        lore.remove(index);
        itemMeta.setLore(lore);
        updateItemMeta();
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        List<String> lore = new ArrayList<>();
        if (itemMeta.hasLore()) lore = new ArrayList<>(getLore());
        lore.add(line);
        itemMeta.setLore(lore);
        updateItemMeta();
        return this;
    }

    public ItemBuilder addLoreLine(String line, int pos) {
        List<String> lore = new ArrayList<>(getLore());
        lore.set(pos, line);
        itemMeta.setLore(lore);
        updateItemMeta();
        return this;
    }

    /* Dye color settings */
    @SuppressWarnings("deprecation")
    public ItemBuilder setDyeColor(DyeColor color) {
        this.itemStack.setDurability(color.getDyeData());
        return this;
    }

    public ItemBuilder setLeatherColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
            itemStack.setItemMeta(leatherMeta);
            itemMeta = itemStack.getItemMeta();
        }
        return this;
    }

    public ItemBuilder build() {
        return build(null);
    }

    public ItemBuilder build(Player player) {
        setAmount(itemStack.getAmount());
        ItemMeta meta = itemMeta;
        if (meta == null) return clone();

        setName(player, meta.getDisplayName());

        List<String> lore = meta.getLore();
        if (lore != null) {
            setLore(player, lore);
        }

        return clone();
    }

    public ItemStack toItemStack() {
        return itemStack;
    }

    public ItemBuilder clone() {
        return new ItemBuilder(itemStack);
    }

    private void updateItemMeta() {
        itemStack.setItemMeta(itemMeta);
    }
}
