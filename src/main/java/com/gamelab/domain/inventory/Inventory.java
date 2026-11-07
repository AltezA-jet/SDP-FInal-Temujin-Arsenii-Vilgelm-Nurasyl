package com.gamelab.domain.inventory;

import com.gamelab.domain.Item;
import com.gamelab.domain.item.Weapon;
import java.util.*;

/** Fixed-array inventory with per-owner, centrally configured constraints. */
public final class Inventory {
    private final int capacity;
    private final Item[] slots;
    private final String ownerClass;
    private final InventoryRules rules;
    private final boolean enforceOwner;
    private Weapon equippedWeapon;

    /** Backward-compatible constructor for legacy callers. */
    public Inventory(int capacity) { this(capacity, "ALL", null, false); }
    public Inventory(String ownerClass, InventoryRules rules) { this(Objects.requireNonNull(rules).maxItems(), ownerClass, rules, true); }
    public Inventory(int capacity, String ownerClass, InventoryRules rules) {
        this(capacity, ownerClass, rules, true);
    }
    private Inventory(int capacity, String ownerClass, InventoryRules rules, boolean enforceOwner) {
        if (capacity <= 0) throw new IllegalArgumentException("Inventory capacity must be positive");
        this.ownerClass = Objects.requireNonNull(ownerClass, "Owner class cannot be null");
        this.rules = rules == null ? InventoryRules.defaults(ownerClass, capacity) : rules;
        this.enforceOwner = enforceOwner;
        this.capacity = Math.min(capacity, this.rules.maxItems());
        this.slots = new Item[this.capacity];
    }
    public void addItem(Item item) {
        Objects.requireNonNull(item, "Item cannot be null");
        String reason = rejectionReason(item);
        if (reason != null) throw new IllegalStateException(reason);
        for (int i = 0; i < slots.length; i++) if (slots[i] == null) { slots[i] = item; return; }
    }
    public Optional<String> tryAddItem(Item item) {
        if (item == null) return Optional.of("Item cannot be null");
        String reason = rejectionReason(item);
        if (reason != null) return Optional.of(reason);
        addItem(item); return Optional.empty();
    }
    public String rejectionReason(Item item) {
        Objects.requireNonNull(item, "Item cannot be null");
        if (Arrays.stream(slots).anyMatch(slot -> slot == item)) return "This item is already in the inventory";
        if (enforceOwner && !rules.permits(item, ownerClass)) return "This item is not allowed for " + ownerClass;
        if (item.getWeight() < 0 || !Double.isFinite(item.getWeight())) return "Item has invalid weight";
        double weight = Arrays.stream(slots).filter(Objects::nonNull).mapToDouble(Item::getWeight).sum();
        if (weight + item.getWeight() > rules.maxWeight()) return "Item exceeds the weight limit";
        if (isFull()) return "Inventory is full";
        return null;
    }
    public boolean removeItem(Item item) {
        if (item == null) return false;
        for (int i = 0; i < slots.length; i++) if (slots[i] == item || Objects.equals(slots[i], item)) {
            slots[i] = null; if (equippedWeapon == item) equippedWeapon = null; return true;
        }
        return false;
    }
    public void equipWeapon(Weapon weapon, String characterClass) {
        Objects.requireNonNull(weapon, "Weapon cannot be null");
        ensureItemExists(weapon);
        if (!weapon.canBeUsedBy(characterClass)) throw new IllegalArgumentException("This character cannot use " + weapon.getName());
        equippedWeapon = weapon;
    }
    private void ensureItemExists(Item item) { if (!getItems().contains(item)) throw new IllegalStateException("Item is not in the inventory"); }
    public Optional<Weapon> getEquippedWeapon() { return Optional.ofNullable(equippedWeapon); }
    public int getWeaponDamageBonus() { return equippedWeapon == null ? 0 : equippedWeapon.getDamageBonus(); }
    public List<Item> getItems() { return Arrays.stream(slots).filter(Objects::nonNull).toList(); }
    public Item[] getSlots() { return slots.clone(); }
    public String getOwnerClass() { return ownerClass; }
    public InventoryRules getRules() { return rules; }
    public int getCapacity() { return capacity; }
    public int getSize() { return (int) Arrays.stream(slots).filter(Objects::nonNull).count(); }
    public int getRemainingSlots() { return capacity - getSize(); }
    public boolean isFull() { return getSize() >= capacity; }
    public boolean isEmpty() { return getSize() == 0; }
}
