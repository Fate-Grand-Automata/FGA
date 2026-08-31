-keepattributes LineNumberTable,SourceFile
-keep class org.opencv.core.CvException { *; }

# Enum constants go into SharedPreferences by name and come back through enumValueOf,
# so their names have to survive obfuscation.
-keep class io.github.fate_grand_automata.scripts.enums.* { *; }
