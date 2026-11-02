# Shadowfall

JavaFX game project built with Maven and Java 17.

## Run

```bash
mvn javafx:run
```

## Project layout

### Java packages

- `app` - application entry point
- `application` - game use cases and services
- `demo` - standalone previews and demonstrations
- `domain` - game entities and rules
- `pattern` - builder, decorator, factory, observer, and strategy patterns
- `persistence` - database and save mapping
- `ui/character`, `ui/inventory`, and `ui/menu` - screens by feature
- `ui/components` - reusable controls

Tests under `src/test/java/com/gamelab/` follow the production packages; integration
tests live in `com.gamelab.integration`.

### Resources

- `assets/ui/` - backgrounds, branding, button frames, and labels
- `assets/effects/` - particle atlases and effect data
- `assets/items/` - item sprites grouped by type
- `assets/references/` - unused art and design references
- `db/` - database schema

The Maven JavaFX plugin launches `com.gamelab.app.Main`. Runtime assets are loaded from
`src/main/resources`, so resource paths in Java begin at the resource root (for example,
`/assets/ui/buttons/button_frame.png`).
