-keepattributes LineNumberTable,SourceFile
-keep class org.opencv.core.CvException { *; }

-keep class io.github.fate_grand_automatapts.enums.* { *; }
-keep class io.github.fate_grand_automata.scripts.models.* { *; }
-dontwarn io.github.fate_grand_automata.prefs.**
-dontwarn io.github.fate_grand_automata.prefs.core.**