package com.example.data.repository

import com.example.data.local.CustomHolidayDao
import com.example.data.local.CustomHolidayEntity
import com.example.data.model.HolidayCategory
import com.example.data.model.HolidayItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.TimeZone

class HolidayRepository(private val customHolidayDao: CustomHolidayDao) {

    fun getCustomHolidaysFlow(): Flow<List<HolidayItem>> {
        return customHolidayDao.getAllCustomHolidays().map { entities ->
            entities.map { entity ->
                HolidayItem(
                    id = "custom_${entity.id}",
                    title = entity.title,
                    subtitle = entity.subtitle.ifEmpty { "Kişisel Özel Tatil" },
                    category = HolidayCategory.CUSTOM,
                    targetEpochMillis = entity.targetEpochMillis,
                    durationDays = entity.durationDays,
                    description = entity.description.ifEmpty { "Sizin tarafınızdan oluşturulan özel tatil planı." },
                    bridgeTip = null,
                    location = entity.location.ifEmpty { null },
                    iconKey = entity.iconKey,
                    accentColorHex = entity.colorHex,
                    isCustom = true
                )
            }
        }
    }

    suspend fun addCustomHoliday(
        title: String,
        subtitle: String,
        targetEpochMillis: Long,
        durationDays: Double,
        description: String,
        location: String,
        iconKey: String,
        colorHex: Long
    ): Long {
        val entity = CustomHolidayEntity(
            title = title,
            subtitle = subtitle,
            targetEpochMillis = targetEpochMillis,
            durationDays = durationDays,
            description = description,
            location = location,
            iconKey = iconKey,
            colorHex = colorHex
        )
        return customHolidayDao.insertCustomHoliday(entity)
    }

    suspend fun deleteCustomHoliday(idString: String) {
        val rawId = idString.removePrefix("custom_").toLongOrNull() ?: return
        customHolidayDao.deleteCustomHoliday(rawId)
    }

    /**
     * Builds static and dynamically calculated holidays for target years.
     * Takes current time into account to present the upcoming occurrence if past.
     */
    fun getOfficialAndCalendarHolidays(baseEpochMillis: Long): List<HolidayItem> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul")).apply {
            timeInMillis = baseEpochMillis
        }
        val currentYear = calendar.get(Calendar.YEAR)
        val yearsToGenerate = listOf(currentYear, currentYear + 1)

        val list = mutableListOf<HolidayItem>()

        for (year in yearsToGenerate) {
            // --- 1. RESMİ TATİLLER ---
            // 1 Ocak Yılbaşı
            val yilbasiMillis = buildDateMillis(year, Calendar.JANUARY, 1, 0, 0)
            list.add(
                HolidayItem(
                    id = "yilbasi_$year",
                    title = "1 Ocak Yılbaşı",
                    subtitle = "Yeni Yılın İlk Günü",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = yilbasiMillis,
                    durationDays = 1.0,
                    description = "Yeni yılın ilk günü tüm kamu ve özel sektör çalışanları için resmi tatildir. Yeni hedefler, umutlar ve dinlenme zamanı.",
                    bridgeTip = calculateBridgeTip(yilbasiMillis, 1.0),
                    iconKey = "celebration",
                    accentColorHex = 0xFF0284C7
                )
            )

            // 23 Nisan Ulusal Egemenlik ve Çocuk Bayramı
            val nisan23Millis = buildDateMillis(year, Calendar.APRIL, 23, 0, 0)
            list.add(
                HolidayItem(
                    id = "23nisan_$year",
                    title = "23 Nisan Çocuk Bayramı",
                    subtitle = "Ulusal Egemenlik ve Çocuk Bayramı",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = nisan23Millis,
                    durationDays = 1.0,
                    description = "Türkiye Büyük Millet Meclisi'nin açılışı ve Mustafa Kemal Atatürk'ün tüm dünya çocuklarına armağan ettiği milli bayramımızdır.",
                    bridgeTip = calculateBridgeTip(nisan23Millis, 1.0),
                    iconKey = "flag",
                    accentColorHex = 0xFFDC2626
                )
            )

            // 1 Mayıs Emek ve Dayanışma Günü
            val mayis1Millis = buildDateMillis(year, Calendar.MAY, 1, 0, 0)
            list.add(
                HolidayItem(
                    id = "1mayis_$year",
                    title = "1 Mayıs Emek Günü",
                    subtitle = "Emek ve Dayanışma Günü",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = mayis1Millis,
                    durationDays = 1.0,
                    description = "İşçi ve emekçilerin birlik, dayanışma ve haksızlıklarla mücadele günü olarak kutlanan uluslararası resmi tatil günüdür.",
                    bridgeTip = calculateBridgeTip(mayis1Millis, 1.0),
                    iconKey = "work",
                    accentColorHex = 0xFFEA580C
                )
            )

            // 19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı
            val mayis19Millis = buildDateMillis(year, Calendar.MAY, 19, 0, 0)
            list.add(
                HolidayItem(
                    id = "19mayis_$year",
                    title = "19 Mayıs Gençlik ve Spor",
                    subtitle = "Atatürk'ü Anma, Gençlik ve Spor Bayramı",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = mayis19Millis,
                    durationDays = 1.0,
                    description = "Kurtuluş Savaşı'nın başlangıcı kabul edilen Mustafa Kemal Atatürk'ün Samsun'a ayak basışının yıldönümü ve gençlere armağan edilen bayram.",
                    bridgeTip = calculateBridgeTip(mayis19Millis, 1.0),
                    iconKey = "sports",
                    accentColorHex = 0xFF2563EB
                )
            )

            // 15 Temmuz Demokrasi ve Milli Birlik Günü
            val temmuz15Millis = buildDateMillis(year, Calendar.JULY, 15, 0, 0)
            list.add(
                HolidayItem(
                    id = "15temmuz_$year",
                    title = "15 Temmuz Milli Birlik Günü",
                    subtitle = "Demokrasi ve Milli Birlik Günü",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = temmuz15Millis,
                    durationDays = 1.0,
                    description = "Demokrasi ve milli birlik bilincini yaşatmak ve aziz şehitlerimizi anmak adına resmi tatil ilan edilen gün.",
                    bridgeTip = calculateBridgeTip(temmuz15Millis, 1.0),
                    iconKey = "star",
                    accentColorHex = 0xFFDC2626
                )
            )

            // 30 Ağustos Zafer Bayramı
            val agustos30Millis = buildDateMillis(year, Calendar.AUGUST, 30, 0, 0)
            list.add(
                HolidayItem(
                    id = "30agustos_$year",
                    title = "30 Ağustos Zafer Bayramı",
                    subtitle = "Büyük Taarruz ve Zafer",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = agustos30Millis,
                    durationDays = 1.0,
                    description = "1922 yılında Dumlupınar'da Gazi Mustafa Kemal'in başkumandanlığında zaferle sonuçlanan Büyük Taarruz'u anmak için kutlanan ulusal bayram.",
                    bridgeTip = calculateBridgeTip(agustos30Millis, 1.0),
                    iconKey = "flag",
                    accentColorHex = 0xFFDC2626
                )
            )

            // 29 Ekim Cumhuriyet Bayramı (28 Ekim 13:00'te başlar)
            val ekim29Millis = buildDateMillis(year, Calendar.OCTOBER, 28, 13, 0)
            list.add(
                HolidayItem(
                    id = "29ekim_$year",
                    title = "29 Ekim Cumhuriyet Bayramı",
                    subtitle = "Cumhuriyetimizin İlanı (1.5 Gün)",
                    category = HolidayCategory.NATIONAL,
                    targetEpochMillis = ekim29Millis,
                    durationDays = 1.5,
                    description = "Türkiye Cumhuriyeti'nin resmen ilan edildiği en büyük milli bayramımız. 28 Ekim günü öğleden sonra yarım gün ile başlayıp toplam 1.5 gündür.",
                    bridgeTip = calculateBridgeTip(ekim29Millis, 1.5),
                    iconKey = "celebration",
                    accentColorHex = 0xFFDC2626
                )
            )

            // --- 2. DİNİ BAYRAMLAR (Hicri Takvim Projeksiyonları) ---
            addReligiousHolidaysForYear(list, year)

            // --- 3. OKUL & SÖMESTR TATİLLERİ ---
            // Sömestr (Yarıyıl Tatili) - Yaklaşık Ocak 3. haftası
            val somestrMillis = buildDateMillis(year, Calendar.JANUARY, 19, 0, 0)
            list.add(
                HolidayItem(
                    id = "somestr_$year",
                    title = "Sömestr Yarıyıl Tatili",
                    subtitle = "Kış Okul Tatili (~16 Gün)",
                    category = HolidayCategory.SCHOOL,
                    targetEpochMillis = somestrMillis,
                    durationDays = 16.0,
                    description = "İlk ve orta dereceli okullarda birinci dönemin sona erdiği, öğrenci ve öğretmenlerin kış molası verdiği 2 haftalık yarıyıl tatili.",
                    bridgeTip = "Toplam 16 günlük kesintisiz kış tatili dönemi!",
                    iconKey = "school",
                    accentColorHex = 0xFF0284C7
                )
            )

            // Nisan Ara Tatili
            val nisanAraMillis = buildDateMillis(year, Calendar.APRIL, 6, 0, 0)
            list.add(
                HolidayItem(
                    id = "nisan_ara_$year",
                    title = "Nisan İkinci Ara Tatil",
                    subtitle = "İlkbahar Okul Molası (~9 Gün)",
                    category = HolidayCategory.SCHOOL,
                    targetEpochMillis = nisanAraMillis,
                    durationDays = 9.0,
                    description = "Milli Eğitim Bakanlığı takvimine göre ikinci dönem ara tatili. Hafta sonlarıyla birlikte 9 günlük dinlenme molası.",
                    bridgeTip = "Hafta sonlarıyla beraber 9 günlük dinlenme imkanı!",
                    iconKey = "school",
                    accentColorHex = 0xFF10B981
                )
            )

            // Yaz Tatili (Okulların Kapanması)
            val yazTatiliMillis = buildDateMillis(year, Calendar.JUNE, 19, 0, 0)
            list.add(
                HolidayItem(
                    id = "yaz_tatili_$year",
                    title = "Okul Yaz Tatili",
                    subtitle = "Büyük Yaz Tatili Başlangıcı",
                    category = HolidayCategory.SCHOOL,
                    targetEpochMillis = yazTatiliMillis,
                    durationDays = 85.0,
                    description = "Eğitim-öğretim yılının sona ermesi ve karnelerin alınmasıyla başlayan, Eylül ayına kadar süren yaklaşık 3 aylık büyük yaz tatili.",
                    bridgeTip = "Deniz, güneş ve dinlenme dolu uzun yaz molası!",
                    iconKey = "beach",
                    accentColorHex = 0xFFF59E0B
                )
            )

            // Kasım Ara Tatili
            val kasimAraMillis = buildDateMillis(year, Calendar.NOVEMBER, 9, 0, 0)
            list.add(
                HolidayItem(
                    id = "kasim_ara_$year",
                    title = "Kasım Birinci Ara Tatil",
                    subtitle = "Sonbahar Okul Molası (~9 Gün)",
                    category = HolidayCategory.SCHOOL,
                    targetEpochMillis = kasimAraMillis,
                    durationDays = 9.0,
                    description = "Birinci dönemin ortasında uygulanan, öğrencilerin ve velilerin nefes almasını sağlayan 9 günlük sonbahar ara tatili.",
                    bridgeTip = "Hafta sonları birleşimiyle 9 günlük tatil.",
                    iconKey = "school",
                    accentColorHex = 0xFF8B5CF6
                )
            )

            // --- 4. ÖZEL GÜNLER & KUTLAMALAR ---
            // Sevgililer Günü
            val sevgililerMillis = buildDateMillis(year, Calendar.FEBRUARY, 14, 0, 0)
            list.add(
                HolidayItem(
                    id = "sevgililer_$year",
                    title = "Sevgililer Günü",
                    subtitle = "14 Şubat Romantik Kutlama",
                    category = HolidayCategory.SPECIAL,
                    targetEpochMillis = sevgililerMillis,
                    durationDays = 1.0,
                    description = "Tüm dünyada sevginin ve romantizmin kutlandığı özel gün.",
                    bridgeTip = null,
                    iconKey = "heart",
                    accentColorHex = 0xFFEC4899
                )
            )

            // Dünya Kadınlar Günü
            val kadinlarGunuMillis = buildDateMillis(year, Calendar.MARCH, 8, 0, 0)
            list.add(
                HolidayItem(
                    id = "kadinlar_$year",
                    title = "Dünya Kadınlar Günü",
                    subtitle = "8 Mart Emekçi Kadınlar",
                    category = HolidayCategory.SPECIAL,
                    targetEpochMillis = kadinlarGunuMillis,
                    durationDays = 1.0,
                    description = "Kadınların sosyal, ekonomik ve siyasi başarılarının kutlandığı uluslararası farkındalık günü.",
                    bridgeTip = null,
                    iconKey = "flower",
                    accentColorHex = 0xFFA855F7
                )
            )

            // Yılbaşı Gecesi
            val yilbasiGecesiMillis = buildDateMillis(year, Calendar.DECEMBER, 31, 23, 59)
            list.add(
                HolidayItem(
                    id = "yilbasi_gecesi_$year",
                    title = "Yılbaşı Gecesi Geri Sayım",
                    subtitle = "Yeni Yıla Merhaba",
                    category = HolidayCategory.SPECIAL,
                    targetEpochMillis = yilbasiGecesiMillis,
                    durationDays = 1.0,
                    description = "Eski yıla veda edip yeni bir yıla heyecan ve umutla girilen sihirli gece.",
                    bridgeTip = "Ertesi gün 1 Ocak resmi tatil!",
                    iconKey = "party",
                    accentColorHex = 0xFFF97316
                )
            )
        }

        return list
    }

    private fun addReligiousHolidaysForYear(list: MutableList<HolidayItem>, year: Int) {
        when (year) {
            2025 -> {
                // Ramazan 2025: Arefe 29 Mart 13:00, Bayram 30 Mart - 1 Nisan (3.5 gün)
                val ramazan2025 = buildDateMillis(2025, Calendar.MARCH, 29, 13, 0)
                list.add(
                    HolidayItem(
                        id = "ramazan_2025",
                        title = "Ramazan Bayramı",
                        subtitle = "Şeker Bayramı (Arefe + 3 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = ramazan2025,
                        durationDays = 3.5,
                        description = "Ramazan orucunun tamamlanmasının ardından kutlanan mübarek bayram. Aile ziyaretleri, tatlılar ve sevinç dolu günler.",
                        bridgeTip = "Hafta sonu ile birleşen 3.5 günlük bayram tatili.",
                        iconKey = "mosque",
                        accentColorHex = 0xFF16A34A
                    )
                )
                // Kurban 2025: Arefe 5 Haziran 13:00, Bayram 6 Haziran - 9 Haziran (4.5 gün)
                val kurban2025 = buildDateMillis(2025, Calendar.JUNE, 5, 13, 0)
                list.add(
                    HolidayItem(
                        id = "kurban_2025",
                        title = "Kurban Bayramı",
                        subtitle = "Kurban Bayramı (Arefe + 4 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = kurban2025,
                        durationDays = 4.5,
                        description = "İslam aleminin en büyük bayramlarından biri. Paylaşma, yardımlaşma ve kurban ibadetinin yerine getirildiği 4.5 günlük bayram.",
                        bridgeTip = "Hafta sonu ile birleştiğinde 9 günlük bayram tatili fırsatı yaratabilir!",
                        iconKey = "mosque",
                        accentColorHex = 0xFF059669
                    )
                )
            }
            2026 -> {
                // Ramazan 2026: Arefe 19 Mart 13:00, Bayram 20-22 Mart
                val ramazan2026 = buildDateMillis(2026, Calendar.MARCH, 19, 13, 0)
                list.add(
                    HolidayItem(
                        id = "ramazan_2026",
                        title = "Ramazan Bayramı",
                        subtitle = "Şeker Bayramı (Arefe + 3 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = ramazan2026,
                        durationDays = 3.5,
                        description = "Ramazan ayının nihayete ermesiyle gelen coşkulu 3.5 günlük bayram sevinci.",
                        bridgeTip = "Cuma, Cumartesi, Pazar günlerine denk gelen hazır 3 günlük tatil.",
                        iconKey = "mosque",
                        accentColorHex = 0xFF16A34A
                    )
                )
                // Kurban 2026: Arefe 26 Mayıs 13:00, Bayram 27-30 Mayıs
                val kurban2026 = buildDateMillis(2026, Calendar.MAY, 26, 13, 0)
                list.add(
                    HolidayItem(
                        id = "kurban_2026",
                        title = "Kurban Bayramı",
                        subtitle = "Kurban Bayramı (Arefe + 4 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = kurban2026,
                        durationDays = 4.5,
                        description = "Paylaşmanın ve bereketin simgesi Kurban Bayramı.",
                        bridgeTip = "Çarşamba-Cumartesi arası! Pazartesi-Salı izin alınırsa 9 günlük dev tatil!",
                        iconKey = "mosque",
                        accentColorHex = 0xFF059669
                    )
                )
            }
            2027 -> {
                // Ramazan 2027: Arefe 9 Mart 13:00, Bayram 10-12 Mart
                val ramazan2027 = buildDateMillis(2027, Calendar.MARCH, 9, 13, 0)
                list.add(
                    HolidayItem(
                        id = "ramazan_2027",
                        title = "Ramazan Bayramı",
                        subtitle = "Şeker Bayramı (Arefe + 3 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = ramazan2027,
                        durationDays = 3.5,
                        description = "Birlik ve beraberliğin pekiştiği Ramazan Bayramı.",
                        bridgeTip = "Hafta sonu köprüsüyle 5 güne uzatılabilir.",
                        iconKey = "mosque",
                        accentColorHex = 0xFF16A34A
                    )
                )
                // Kurban 2027: Arefe 16 Mayıs 13:00, Bayram 17-20 Mayıs
                val kurban2027 = buildDateMillis(2027, Calendar.MAY, 16, 13, 0)
                list.add(
                    HolidayItem(
                        id = "kurban_2027",
                        title = "Kurban Bayramı",
                        subtitle = "Kurban Bayramı (Arefe + 4 Gün)",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = kurban2027,
                        durationDays = 4.5,
                        description = "Kurban ibadeti ve bayramlaşma günleri.",
                        bridgeTip = "Pazartesi-Perşembe arası! Cuma 1 gün izin alarak 9 günlük tatil yapabilirsiniz!",
                        iconKey = "mosque",
                        accentColorHex = 0xFF059669
                    )
                )
            }
            else -> {
                // Approximate fallback for other years (shifts ~11 days earlier each solar year)
                val yearDiff = year - 2026
                val approxRamazanMillis = buildDateMillis(2026, Calendar.MARCH, 19, 13, 0) - (yearDiff * 11L * 24 * 3600 * 1000)
                val approxKurbanMillis = buildDateMillis(2026, Calendar.MAY, 26, 13, 0) - (yearDiff * 11L * 24 * 3600 * 1000)

                list.add(
                    HolidayItem(
                        id = "ramazan_$year",
                        title = "Ramazan Bayramı",
                        subtitle = "$year Yılı Ramazan Bayramı",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = approxRamazanMillis,
                        durationDays = 3.5,
                        description = "Ramazan Bayramı sevinci ve aile buluşmaları.",
                        bridgeTip = "Köprü tatili fırsatlarını değerlendirin.",
                        iconKey = "mosque",
                        accentColorHex = 0xFF16A34A
                    )
                )
                list.add(
                    HolidayItem(
                        id = "kurban_$year",
                        title = "Kurban Bayramı",
                        subtitle = "$year Yılı Kurban Bayramı",
                        category = HolidayCategory.RELIGIOUS,
                        targetEpochMillis = approxKurbanMillis,
                        durationDays = 4.5,
                        description = "Kurban Bayramı ibadeti ve tatili.",
                        bridgeTip = "Köprü tatili fırsatlarını değerlendirin.",
                        iconKey = "mosque",
                        accentColorHex = 0xFF059669
                    )
                )
            }
        }
    }

    private fun buildDateMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun calculateBridgeTip(epochMillis: Long, durationDays: Double): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul")).apply {
            timeInMillis = epochMillis
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.TUESDAY -> "💡 Salı gününe denk geliyor! Pazartesi 1 gün izin alarak 4 gün tatil yapabilirsiniz."
            Calendar.THURSDAY -> "💡 Perşembe gününe denk geliyor! Cuma 1 gün izin alarak 4 günlük uzun bir hafta sonu tatili yapabilirsiniz."
            Calendar.WEDNESDAY -> "💡 Çarşamba gününe denk geliyor! Pazartesi-Salı veya Perşembe-Cuma 2 gün izinle 5 gün tatil yapabilirsiniz."
            Calendar.FRIDAY -> "🎉 Cuma gününe denk geliyor! Hafta sonuyla birleşen 3 günlük hazır tatil fırsatı."
            Calendar.MONDAY -> "🎉 Pazartesi gününe denk geliyor! Hafta sonuyla birleşen 3 günlük kesintisiz tatil."
            Calendar.SATURDAY, Calendar.SUNDAY -> "🏖️ Hafta sonuna denk geliyor."
            else -> "Tatilin tadını çıkarın!"
        }
    }
}
