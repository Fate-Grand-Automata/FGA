# The two coordinate systems (most common source of bugs)

Two resolutions are in play at once, and mixing them up is the most frequent bug in this
repo. `CONTRIBUTING.md` has a step-by-step guide for measuring new coordinates in an image
editor; the essentials:

- **Image matching happens at 720p grayscale.** Screenshots are captured at native
  resolution, converted to grayscale, scaled to 720p, cropped to the game area
  (letterbox/notch bars removed), then cropped to the search region. **Template images
  added to `app/src/main/assets/` must be 720p** and are grayscaled unless matched inside
  `useColor { }`.
- **Small templates on scrolling content lose match score at sub-pixel offsets.** On a
  freely scrolling list (e.g. the support screen), content can land half a 720p pixel off
  the grid, which drops a template of ~20 px or less by 0.15–0.2 — below the default
  `minSimilarity` of 0.8. Give such templates an explicit `similarity` at the call site,
  chosen between the worst true-match score and the best false-match score inside the
  search region. Measure both by replaying the app's pipeline (INTER_AREA downscale to
  720p, grayscale, `TM_CCOEFF_NORMED`) at fractional shifts from 0.0 to 0.9 px, on
  original-resolution screenshots: a resized copy adds a resampling step that skews scores.
- **Coordinates are expressed at 1440p** (`scriptDimension` 1440p, `compareDimension`
  720p; `Scale` converts between script/screen/image space). Origin is the top-left of the
  *game area*, not the physical screen.
- Y ranges 0..1440; the right edge X depends on aspect ratio, so absolute X is almost
  always wrong. Use the `IScriptAreaTransforms` helpers — `xFromCenter()`, `xFromRight()`,
  `yFromBottom()` — on both `Location` and `Region`. `isWide` (wider than 18:9) selects
  alternate coordinates for many UI elements; follow the existing `if (isWide) … else …`
  style in `locations/`.
- Coordinates belong in `scripts/.../locations/*.kt` as named `val`s on the `Locations`
  graph, not inline in modules.
