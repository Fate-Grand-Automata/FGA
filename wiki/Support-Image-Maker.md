[Support Selection](Support-Selection) recognizes Servants, CEs and friends by small images in your storage folder. The Support Image Maker creates those images from the support screen.

## Default images

FGA comes with images of common supports, such as Waver, Castoria, Skadi, Kaleidoscope and Black Grail. It extracts them:

- when you choose the storage folder,
- automatically after every app update,
- manually with **Extract Default Support Images** in [More Options](More-Options#storage) → Storage.

Extracting overwrites the default images, but never touches images you created yourself. There are no default friend images.

## Folder structure

The images are in the `support` folder inside the storage folder you chose:

- Servant images in `servant`
- CE images in `ce`
- Friend name images in `friend`

To match all ascensions and costumes of a Servant with one entry, put all their images in a subfolder. You then select the subfolder in the battle config.

```
FGA/
└── support/
    ├── servant/
    │   ├── Ozymandias.png
    │   ├── Merlin/
    │   │   ├── merlin1.png
    │   │   ├── merlin2.png
    │   │   └── merlin3.png
    │   └── Waver/
    │       ├── waver1.png
    │       └── waver2.png
    ├── ce/
    │   ├── Kaleidoscope.png
    │   └── Black Grail.png
    └── friend/
        └── my-friend.png
```

## Creating images

You can use the Support selection screen, or the Friend List, where you don't have to refresh until the Servant shows up.

1. Open the Support selection screen or the Friend List in FGO. Make sure the whole Servant and CE of the supports you want are visible.
2. Press PLAY.
3. Press **Support Image Maker** in the bottom-left corner of the dialog.

   Support Selection screen:

   ![Support Selection](https://i.imgur.com/ztFcyDq.png)

   Friend List screen:

   ![Friend List](https://i.imgur.com/amGZlCc.png)

4. FGA takes images of the first 2 supports that are fully on screen: Servant, CE and friend name. For Grand Servants, it takes both CEs in the free and bonus-up slots.

   Servant & CE

   ![Servant & CE](https://i.imgur.com/VRuOlwM.png)

   CE & Friend
   ![CE & Friend](https://i.imgur.com/uRPAviO.png)

5. Check the images you want to keep and give each a name, without `.png`.

   Use `Folder/name` to save into a subfolder, for example `Nero/asc1` saves `asc1.png` in `support/servant/Nero`.

   ![Saving](https://i.imgur.com/gBXrveP.png)

6. Press **Done**. The images are saved to the right folders.

   ![Done](https://i.imgur.com/yYpb2lH.png)

7. You can now select them in your battle configs.
