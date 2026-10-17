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

## Support servant images are crops of Atlas Academy face art

`assets/Support/servant/<Name>/f_<faceId>.png` are not screenshots. Each one is the servant's
128x128 face art from [Atlas Academy](https://apps.atlasacademy.io/db) (the same art for every
region) scaled to 157x157, with the 125x44 window the support list shows cut out of it — 3px from
the left, 47px from the top. `SupportServantImage` (`app/.../atlas/SupportServantImage.kt`) turns
the same art into the same image when a servant is added from the DB, and what it produces matches
the shipped images at 0.98+ correlation — far above the 80% matching threshold — so a re-cut or
DB-generated image can stand in for one of them. Check the constants there before measuring new
ones by hand.
