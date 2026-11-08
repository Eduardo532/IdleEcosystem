# =============================================================================
# R8 para release y preview (alpha/beta)
# =============================================================================

# Sin ofuscar: R8 solo elimina el código y los recursos que no se usan y optimiza
# el resto. Los APK se publican en GitHub sin archivo de mapeo, y así los errores
# que reporten los jugadores traen nombres de clases y métodos legibles.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable

# --- Códigos de partida (Gson) -----------------------------------------------
# Gson lee y escribe los campos por reflexión: el NOMBRE de cada campo es el
# formato del código de partida. Si R8 los elimina o los cambia, los códigos
# exportados dejan de ser compatibles con otras versiones del juego.
# Gson también crea estos objetos sin llamar al constructor, así que hay que
# impedir que R8 asuma que nunca se instancian.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

-keep class com.EGomez.idleecosystem.domain.logic.FullSaveData { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.BuildingSaveModel { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.PrestigeUpgradeSaveModel { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.RawSaveData { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.RawBuilding { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.RawPrestigeUpgrade { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.domain.logic.RawStatistics { <init>(...); <fields>; }
-keep class com.EGomez.idleecosystem.data.database.GameStatisticsEntity { <init>(...); <fields>; }

# Gson 2.10 no trae sus propias reglas
-dontwarn sun.misc.**
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
