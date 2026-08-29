# Game servers and assets

`app/src/main/assets/{En,Jp,Cn,Tw,Kr}/` hold per-server template images, named by `Images`
enum entries (`scripts/.../Images.kt`). `ImageLoader` looks up the current server's folder
and **falls back to `En`** when the file is absent, so only add a server-specific copy when
the art actually differs. `IFgoAutomataApi.findImage` additionally tries the `En` image on
JP to support TranslateFGO. `assets/Support/` holds user-provided servant/CE images;
`assets/tessdata/` the OCR data. Template images must be 720p — see the coordinates guide.

`docs/reference-images/{En,Jp,Cn,Tw,Kr}/` holds device screenshots, usually contributed by
users, that the per-server templates were cut from. Keep the screenshot a template came from
so a later fix can be re-cut and checked against the same source. They never ship in the APK.

**Store them at 720p**, scaled so the *game area* is 720px tall — on a screen with letterbox
bars that leaves the image itself taller. That is the resolution matching runs at, so a crop
taken from a reference image is exactly the pixels the script compares, with no resampling in
between. Downscale a contributed screenshot before committing it.

Server choice and app language are independent: a user can run the JP server with the app's
UI in English, or the EN server in Korean. Don't assume they're coupled when reading prefs or
adding server-specific behavior.
