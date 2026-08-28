# Game servers and assets

`app/src/main/assets/{En,Jp,Cn,Tw,Kr}/` hold per-server template images, named by `Images`
enum entries (`scripts/.../Images.kt`). `ImageLoader` looks up the current server's folder
and **falls back to `En`** when the file is absent, so only add a server-specific copy when
the art actually differs. `IFgoAutomataApi.findImage` additionally tries the `En` image on
JP to support TranslateFGO. `assets/Support/` holds user-provided servant/CE images;
`assets/tessdata/` the OCR data. Template images must be 720p — see the coordinates guide.

Server choice and app language are independent: a user can run the JP server with the app's
UI in English, or the EN server in Korean. Don't assume they're coupled when reading prefs or
adding server-specific behavior.
