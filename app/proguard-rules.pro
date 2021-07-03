# Preserve line numbers for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

-keep class org.opencv.core.CvException { *; }

-keep class com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum { *; }
-keep class com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum { *; }
-keep class com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget { *; }
