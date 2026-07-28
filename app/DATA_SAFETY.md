# טבלת Data Safety ממופה עבור Google Play Console

להלן המיפוי המלא המיועד למילוי שאלון **Data Safety** ב-Google Play Console עבור אפליקציית **Smart Device Migration & Cloning**:

---

## 1. סקירת איסוף ושיתוף נתונים (Data Collection & Sharing Overview)

| שאלה בשאלון Play Console | תשובה | פירוט |
| :--- | :--- | :--- |
| **האם האפליקציה אוספת או משתפת סוגי מידע של המשתמש?** | **Yes** (כן) | המידע מעובד לצורך העברת נתונים וגיבוי |
| **האם כל הנתונים הנאספים מוצפנים בתנועה (In transit)?** | **Yes** (כן) | כל הקשורות מתבצעות ב-HTTPS / TLS מוצפן |
| **האם המשתמש יכול לבקש מחיקת נתונים?** | **Yes** (כן) | זמין דרך מסך ההגדרות באפליקציה ובמייל תמיכה |

---

## 2. פירוט סוגי הנתונים (Data Types Mapping)

### א. אנשי קשר (Personal Info - Contacts)
- **Data Type:** Contacts
- **Collected:** Yes
- **Shared:** No (רק מועבר למכשיר החדש של המשתמש/ענן פרטי)
- **Ephemeral (זמני בלבד):** No (נשמר בבסיס הנתונים המקומי/ענן עד סיום המעבר)
- **Required or Optional:** Optional (לפי אישור הרשאה מהמשתמש)
- **Purposes:** App functionality (העברת אנשי קשר למכשיר חדש)

### ב. תמונות וסרטונים (Photos and Videos)
- **Data Type:** Photos, Videos
- **Collected:** Yes
- **Shared:** No
- **Ephemeral:** No
- **Required or Optional:** Optional
- **Purposes:** App functionality (גיבוי והעברת מדיה)

### ג. אפליקציות מותקנות (App info and performance - Installed Apps)
- **Data Type:** Installed apps
- **Collected:** Yes
- **Shared:** No
- **Ephemeral:** Yes (עיבוד בזמן אמת לצורך שיבוט אפליקציות)
- **Required or Optional:** Required for Cloning feature
- **Purposes:** App functionality (שחזור ושיבוט אפליקציות במכשיר היעד)

### ד. מזהי מכשיר (Device or other IDs)
- **Data Type:** Device or other IDs
- **Collected:** Yes
- **Shared:** No
- **Required or Optional:** Required
- **Purposes:** App functionality, Security & Device Admin pairing

---

## 3. פרקטיקות אבטחה (Security Practices)

- **Data Encryption in Transit:** כל הנתונים מוצפנים בפרוטוקול HTTPS/TLS בעת העברה לענן או בין מכשירים.
- **Data Encryption at Rest:** קבצי גיבוי מוצפנים בבלוקים של AES-256.
- **Data Deletion Mechanism:** המשתמש יכול למחוק את כל הנתונים בכל עת ישירות מתוך האפליקציה (Settings -> Clear Data).
