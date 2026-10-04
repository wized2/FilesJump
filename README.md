# FilesJump

Minimal Android home-screen shortcut that opens the **AOSP DocumentsUI** (native Files app) and closes itself immediately.

## Behavior

1. Tap the **Files** icon
2. Launches `com.android.documentsui` via `android.provider.action.BROWSE` → `FilesActivity`
3. Finishes and is excluded from Recents (`excludeFromRecents` + `finishAndRemoveTask`)

## Limits

- Does **not** unlock `Android/data` / `Android/obb` (third-party scoped storage rules still apply)
- If DocumentsUI is missing or disabled, shows a short toast and exits

## Build

```bash
gradle :app:assembleRelease
```

Requires JDK 17. CI builds a signed release when secrets are configured (same pattern as Fern / D-Harness).

## License

MIT
