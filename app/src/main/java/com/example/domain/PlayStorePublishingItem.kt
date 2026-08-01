package com.example.domain

data class PlayStoreChecklistItem(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val detailedInstruction: String,
    val codeSnippet: String? = null,
    val isMandatory: Boolean = true,
    var isCompleted: Boolean = false
)

object PlayStoreChecklistProvider {

    fun getPublishingChecklist(): List<PlayStoreChecklistItem> {
        return listOf(
            // 1. Manifest Settings & Permissions
            PlayStoreChecklistItem(
                id = "play_manifest_appid",
                category = "הגדרות מניפסט וזהות (Manifest)",
                title = "אימות Application ID ו-Package Name",
                summary = "וידוא מזהה ייחודי עבור האפליקציה ב-app/build.gradle.kts",
                detailedInstruction = "ודא שה-applicationId בבלוק defaultConfig מוגדר למזהה ייחודי (למשל: com.company.appname). שים לב שאי אפשר לשנות את ה-Application ID לאחר הפרסום בחנות.",
                codeSnippet = """defaultConfig {
    applicationId = "com.company.myapp"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0.0"
}""",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_manifest_versioning",
                category = "הגדרות מניפסט וזהות (Manifest)",
                title = "עדכון הגדרות גרסה (versionCode ו-versionName)",
                summary = "קביעת מספר גרסה רציף עבור כל העלאה ב-Google Play Console",
                detailedInstruction = "ה-versionCode חייב להיות מספר שלם שעולה בכל העלאה של AAB/APK חדש. ה-versionName הוא הטקסט המוצג למשתמשים (לדוגמה 1.0.0).",
                codeSnippet = """versionCode = 1
versionName = "1.0.0"""",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_manifest_target_sdk",
                category = "הגדרות מניפסט וזהות (Manifest)",
                title = "עמידה בדרישות Target SDK 34+",
                summary = "הגדרת targetSdk עדכנית בהתאם למדיניות Google Play",
                detailedInstruction = "Google Play מחייבת אפליקציות חדשות ועדכונים לפגוש את גרסת ה-Android העדכנית ביותר (Target SDK 34 ומעלה).",
                codeSnippet = "targetSdk = 34",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_manifest_exported",
                category = "הגדרות מניפסט וזהות (Manifest)",
                title = "אימות רכיבי AndroidManifest והרשאות (android:exported)",
                summary = "הצהרה מפורשת על android:exported לכל Activity, Service ו-Receiver",
                detailedInstruction = "החל מ-Android 12, כל רכיב במניפסט הכולל intent-filter מחויב בהצהרה מפורשת על android:exported=\"true\" או \"false\" למניעת פרצות אבטחה.",
                codeSnippet = """<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>""",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_manifest_adaptive_icon",
                category = "הגדרות מניפסט וזהות (Manifest)",
                title = "הגדרת סמלים אדפטיביים (Adaptive Icons)",
                summary = "וידוא קיומם של android:icon ו-android:roundIcon במניפסט",
                detailedInstruction = "וודא שמשאבי האייקון שלך מוגדרים תחת res/mipmap ותומכים ברקע ושכבה קדמית (foreground/background) לתצוגה תקינה בכל מכשירי Android.",
                codeSnippet = """<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round" ...>""",
                isMandatory = true
            ),

            // 2. Signing Configurations & Keystore
            PlayStoreChecklistItem(
                id = "play_signing_keystore",
                category = "הגדרות חתימת אפליקציה (App Signing)",
                title = "יצירת Keystore דיגיטלי להפצה (Release Keystore)",
                summary = "הפקת מפתח חתימה סודי באמצעות keytool או Android Studio",
                detailedInstruction = "צור קובץ Keystore מאובטח ושמור אותו במקום מוגן. אובדן המפתח ימנע ממך לעדכן את האפליקציה בעתיד במידה ואינך משתמש ב-Play App Signing.",
                codeSnippet = "keytool -genkeypair -v -keystore release.jks -alias myKeyAlias -keyalg RSA -keysize 2048 -validity 10000",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_signing_gradle_config",
                category = "הגדרות חתימת אפליקציה (App Signing)",
                title = "הגדרת signingConfigs ב-app/build.gradle.kts",
                summary = "קונפיגורציית החתימה האוטומטית עבור Release build",
                detailedInstruction = "הגדר את בלוק ה-signingConfigs בגרדל וקשר אותו ל-buildTypes.release. מומלץ לקרוא את הסיסמאות ממשתני סביבה ולא לכתוב אותן כקוד קשיח.",
                codeSnippet = """android {
    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_password"
            keyAlias = "myKeyAlias"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}""",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_signing_play_app_signing",
                category = "הגדרות חתימת אפליקציה (App Signing)",
                title = "הפעלת Google Play App Signing בקונסולה",
                summary = "הרשמת האפליקציה לשירות החתימה המאובטח של Google",
                detailedInstruction = "Google Play App Signing מנהלת ומגינה על מפתח החתימה הראשי של האפליקציה בשרתי Google, בעוד שאתה משתמש במפתח העלאה (Upload Key) בלבד.",
                isMandatory = true
            ),

            // 3. Release Build & Optimization
            PlayStoreChecklistItem(
                id = "play_build_r8_minify",
                category = "בנייה ואופטימיזציה (Release Build)",
                title = "הפעלת אופטימיזציה וכיווץ קוד (R8 / ProGuard)",
                summary = "הגדרת isMinifyEnabled ו-isShrinkResources ב-buildType.release",
                detailedInstruction = "מזעור קוד ומשאבים מקטין באופן משמעותי את גודל קובץ ה-AAB ומקשה על הנדסה לאחור (Decompilation). ודא שכללי Proguard תקפים עבור ספריות שונות במידת הצורך.",
                codeSnippet = """buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}""",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_build_aab_export",
                category = "בנייה ואופטימיזציה (Release Build)",
                title = "הפקת קובץ Android App Bundle (.aab)",
                summary = "הרצת פקודת הבנייה bundleRelease ליצירת קובץ העלאה לחנות",
                detailedInstruction = "קובצי AAB הם הפורמט הנדרש ע\"י Google Play. Google מפיקה מהם קובצי APKמותאמים אישית לכל דגם מכשיר ורזולוציה.",
                codeSnippet = "./gradlew bundleRelease",
                isMandatory = true
            ),

            // 4. Store Listing & Compliance
            PlayStoreChecklistItem(
                id = "play_store_graphics",
                category = "דף מוצר ומדיניות (Store Listing)",
                title = "הכנת נכסי גרפיקה וצילומי מסך",
                summary = "סמל אפליקציה (512x512), Feature Graphic (1024x500) וצילומי מסך",
                detailedInstruction = "נדרשים: סמל ברזולוציה 512x512 בפורמט PNG, באנר מרכזי (Feature Graphic) בגודל 1024x500, ולפחות 2 צילומי מסך ברזולוציה גבוהה עבור מכשירי נייד וטאבלטים.",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_store_privacy_policy",
                category = "דף מוצר ומדיניות (Store Listing)",
                title = "פרסום קישור למדיניות פרטיות (Privacy Policy URL)",
                summary = "הוספת URL נגיש המפרט את אופן איסוף הנתונים והשימוש בהם",
                detailedInstruction = "כל אפליקציה ב-Google Play מחויבת בקישור פומבי ונגיש למדיניות פרטיות המפרטת אילו הרשאות ונתונים נאספים.",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_store_data_safety",
                category = "דף מוצר ומדיניות (Store Listing)",
                title = "מילוי שאלון בטיחות נתונים (Data Safety Form)",
                summary = "הצהרה ב-Google Play Console על סוגי הנתונים שנאספים ומשותפים",
                detailedInstruction = "ענה על שאלון בטיחות הנתונים בקונסולה לגבי מיקום, נתוני אנליטיקה, מזהי מכשיר ואבטחת מידע ברשת.",
                isMandatory = true
            ),

            // 5. Release Tracks & Rollout
            PlayStoreChecklistItem(
                id = "play_release_internal_testing",
                category = "מסלולי הפצה (Release Rollout)",
                title = "העלאה למסלול בדיקות פנימיות (Internal Testing Track)",
                summary = "בדיקת ה-AAB ע\"י בודקים מורשים לפני הפצה לציבור הרחב",
                detailedInstruction = "צור גרסה במסלול Internal Testing והוסף רשימת מיילים של בודקים. זה מחזק את האמינות ומאפשר לאתר תקלות בזמן אמת.",
                isMandatory = true
            ),
            PlayStoreChecklistItem(
                id = "play_release_prelaunch_report",
                category = "מסלולי הפצה (Release Rollout)",
                title = "סקירת דוח קודם להפצה (Pre-launch Report)",
                summary = "ניתוח תוצאות הבדיקה האוטומטית של Google במכשירים פיזיים בענן",
                detailedInstruction = "סקור את דוח ה-Pre-launch ב-Console לזיהוי קראשים אוטומטיים, בעיות נגישות ובעיות ביצועים בעשרות דגמי מכשירים.",
                isMandatory = false
            ),
            PlayStoreChecklistItem(
                id = "play_release_staged_rollout",
                category = "מסלולי הפצה (Release Rollout)",
                title = "ביצוע שחרור הדרגתי לציבור (Staged Rollout)",
                summary = "הפצת הגרסה באופן מדורג (למשל 20% -> 50% -> 100%)",
                detailedInstruction = "שחרור הדרגתי מאפשר לנטר נתוני קראשים ומשוב משתמשים ראשוני לפני הגעת העדכון לכלל משתמשי האפליקציה.",
                isMandatory = false
            )
        )
    }
}
