# GUI Editor Framework

A complete in-game GUI editor system for Minecraft plugins, allowing players to edit plugin configurations directly through inventory menus.

## Features

- **Hierarchical Menus** - Navigate through nested configuration sections
- **Multiple Editor Types** - Toggle, Number, Text, Enum, ItemStack, Lore, Message, List
- **Chat Input** - Seamless text input through chat
- **Pagination** - Handle large lists with ease
- **MiniMessage Support** - Full formatting support throughout
- **Centralized Design** - Consistent visual styling
- **Builder API** - Easy-to-use fluent API for developers

## Quick Start

### 1. Initialize the Editor System

```java
import cz.gennario.gennarioframework.guieditor.GUIEditorAPI;

// In your plugin's onEnable:
GUIEditorAPI.init();
```

### 2. Create a Simple Editor

```java
import cz.gennario.gennarioframework.guieditor.GUIEditorAPI;
import cz.gennario.gennarioframework.guieditor.config.ConfigAccessor;

// Get your config
YamlDocument config = yourConfigDocument;
ConfigAccessor accessor = GUIEditorAPI.config(config);

// Create and open editor
GUIEditorAPI.builder(accessor, "My Plugin Settings")
    .toggle("enabled", "Plugin Enabled")
    .number("max-players", "Max Players")
    .text("prefix", "Chat Prefix")
    .message("welcome-message", "Welcome Message")
    .onBack(() -> player.closeInventory())
    .open(player);
```

## Editor Types

### Toggle Editor
For boolean values. Click to toggle between enabled/disabled.

```java
.toggle("settings.enabled", "Enabled")
```

### Number Editor
For integer or decimal values. Provides +/- buttons and manual input.

```java
.number("settings.max-players", "Max Players")
.intNumber("settings.limit", "Limit", 0, 100)  // With bounds
```

### Text Editor
For single-line text values. Opens chat input.

```java
.text("settings.name", "Display Name")
.text("settings.prefix", "Prefix", 32, false)  // Max length, required
```

### Enum Editor
For cycling through enum values.

```java
.enumValue("settings.mode", "Mode", GameMode.class)
```

### ItemStack Editor
For editing item configurations.

```java
.itemStack("rewards.item", "Reward Item")
```

### Lore Editor
For lists of strings with add/edit/delete/reorder support.

```java
.lore("item.lore", "Item Lore")
```

### Message Editor
For plugin messages with MiniMessage support.

```java
.message("messages.join", "Join Message")
.multilineMessage("messages.welcome", "Welcome Message")  // Multiple lines
```

### List Editor
For lists of complex objects (e.g., crates, rewards).

```java
.list("crates", "Crates", "Crate", Material.CHEST, 
    (player, context) -> openCrateEditor(player, context.getPath()))
```

## Hierarchical Menus

For complex nested configurations:

```java
EditorMenu mainMenu = GUIEditorAPI.menu(accessor, "Plugin Editor");

mainMenu.addSubmenu("Settings", Material.COMPARATOR, 
    List.of("Configure plugin settings"),
    settingsMenu -> {
        settingsMenu.addAction("General", Material.PAPER, 
            List.of("General settings"),
            player -> openGeneralSettings(player));
    });

mainMenu.addSubmenu("Crates", Material.CHEST,
    List.of("Manage crates"),
    cratesMenu -> {
        // Add crate management
    });

mainMenu.open(player);
```

## Custom Editors

Implement the `Editor` interface or extend `AbstractEditor`:

```java
public class ColorEditor extends AbstractEditor {

    public ColorEditor() {
        super("color", "Color Editor");
    }

    @Override
    public void open(Player player, EditorContext context) {
        // Open color picker GUI
    }

    @Override
    public ItemStack createDisplayItem(EditorContext context, String label) {
        String color = (String) getValue(context.getConfigAccessor(), context.getPath());
        // Create display item
    }

    @Override
    public Object getValue(ConfigAccessor accessor, String path) {
        return accessor.getString(path, "#FFFFFF");
    }

    @Override
    public void setValue(ConfigAccessor accessor, String path, Object value) {
        accessor.set(path, value);
    }
}

// Register the custom editor
GUIEditorAPI.registry().registerType("color", ColorEditor::new);
```

## Player Input

Request text input from players:

```java
GUIEditorAPI.requestInput(player, "Enter Name", currentValue, 
    input -> {
        // Handle input
        accessor.set("name", input);
        accessor.saveSilent();
    },
    () -> {
        // Handle cancel
    }
);
```

## Design Customization

Modify `EditorDesign` class to customize:

- Color schemes and gradients
- Button styles and icons
- Message formats
- GUI titles

```java
// In EditorDesign.java
public static final String PRIMARY_GRADIENT = "<gradient:#00c6ff:#0072ff>";
public static final String SUCCESS_COLOR = "<green>";
public static final String ERROR_COLOR = "<red>";
```

## Package Structure

```
guieditor/
├── GUIEditorAPI.java          # Main API class
├── builder/
│   └── EditorBuilder.java     # Fluent builder API
├── config/
│   ├── ConfigAccessor.java    # Config manipulation
│   └── ItemStackSerializer.java
├── design/
│   └── EditorDesign.java      # Visual design system
├── input/
│   ├── InputMode.java
│   ├── InputRequest.java
│   ├── MultilineInputHandler.java
│   └── PlayerInputManager.java
├── menu/
│   ├── EditorContext.java     # Context for editors
│   └── EditorMenu.java        # Hierarchical menus
├── pagination/
│   └── PaginatedListGUI.java
├── registry/
│   └── EditorRegistry.java    # Editor type registry
├── types/
│   ├── Editor.java            # Base interface
│   ├── AbstractEditor.java    # Base class
│   ├── ToggleEditor.java
│   ├── NumberEditor.java
│   ├── TextEditor.java
│   ├── EnumEditor.java
│   ├── ItemStackEditor.java
│   ├── LoreEditor.java
│   ├── MessageEditor.java
│   └── ListEditor.java
├── util/
│   └── EditorUtils.java
└── example/
    └── CrateEditorExample.java
```

## Example: Crate Editor

See `CrateEditorExample.java` for a complete implementation of a crate management system using this framework.

```java
CrateEditorExample crateEditor = new CrateEditorExample(cratesConfig);
crateEditor.openMainMenu(player);
```

## Dependencies

- Paper/Spigot API
- BoostedYAML (config library)
- GennarioGUI (inventory framework)
- Adventure API (MiniMessage)
