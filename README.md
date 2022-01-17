# Compose Sketchable Library

## Description

**Compose Sketchable** Jetpack Compose Multiplatform Library for Drawing And Sketching

### Usage

```kotlin
var sketchableSettings by remember { mutableStateOf(false) }
val sketchableState = remember { SketchableState() }
val sketchableColors = remember {
    listOf(
        Color.White, Color.Black,
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Magenta,
        Color.Red,
        Color.Gray
    )
}

Column {
    SketchTools(
        modifier = Modifier.fillMaxWidth().zIndex(2f),
        sketchableState = sketchableState,
        colorsList = sketchableColors,
        onChangeZoom = {
            if (it) {
                sketchableState.mode = PointerMode.Zoom
            } else {
                sketchableState.mode = PointerMode.Draw
            }
        },
        onSettingsClick = {
            sketchableSettings = true
        },
        onSaveSketch = {
            sketchableState.export(
                SketchableExportOptions(
                    type = SketchableExportType.ImagePNG,
                    filePath = File.createTempFile("myfile", ".png").absolutePath.apply { println(this) }
                )
            )
        },
        onShowColorPickerDialogForBrush = {
            //doing nothing for now
        }
    )

    val density = LocalDensity.current

    Canvas(
        modifier = Modifier.canvasModifier(state = sketchableState, density = density)
            .zoomableSketch(state = sketchableState).drawingPointer(sketchableState).zIndex(1f)
    ) {
        drawSketch(sketchableState)
    }
    if (sketchableSettings) {
        SketchableSettings(
            sketchableState,
            onShowColorDialogForBackground = {
                // todo
            }
        )
    }
}
```