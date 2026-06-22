package com.example.data

import com.example.R

data class RoomType(
    val name: String,
    val price: String
)

data class ResortReview(
    val author: String,
    val rating: Float,
    val comment: String
)

data class LatLng(val latitude: Double, val longitude: Double)

data class Accommodation(
    val id: String,
    val name: String,
    val nameEn: String,
    val rating: Float,
    val priceRange: String,
    val distanceKmToBridge: Float,
    val description: String = "",
    val category: String,
    val features: List<String>,
    val phone: String,
    val quote: String,
    val location: LatLng,
    val roomTypes: List<RoomType>,
    val reviews: List<ResortReview>,
    val imgResId: Int = R.drawable.img_resort_placeholder
)

object AccommodationRepository {
    // Mon Bridge is at LatLng(15.1407, 98.4498)
    val MON_BRIDGE_LOCATION = LatLng(15.1407, 98.4498)

    val list = listOf(
        Accommodation(
            id = "samprasop",
            name = "สามประสบ รีสอร์ท",
            nameEn = "Samprasop Resort",
            rating = 4.6f,
            priceRange = "1,500 - 4,500 บ.",
            distanceKmToBridge = 0.1f,
            category = "วิวแม่น้ำ",
            features = listOf("วิวสะพานมอญแบบพาโนรามา", "สระว่ายน้ำกลางแจ้ง", "ห้องอาหารริมน้ำ", "ที่จอดรถส่วนตัวกว้างขวาง", "ฟรี Wi-Fi"),
            phone = "034-595050",
            quote = "จุดชมวิวหลักล้านที่เห็นสะพานมอญและจุดสามประสบ (แม่น้ำ 3 สายสบกัน) ได้สวยที่สุดในสังขละบุรี",
            location = LatLng(15.1384, 98.4485),
            roomTypes = listOf(
                RoomType("Standard River View (สแตนดาร์ดวิวแม่น้ำ)", "2,200 บ./คืน"),
                RoomType("Deluxe Panorama (ดีลักซ์พาโนรามา)", "3,500 บ./คืน"),
                RoomType("Family Suite (จองสวีทครอบครัว)", "4,500 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณวิภาดา", 5f, "วิวสวยสมราคาจริงๆ ตื่นมาเห็นหมอกเหนือน้ำและสะพานมอญจากระเบียงห้องพักเลย"),
                ResortReview("คุณกรวิชญ์", 4.5f, "อาหารเช้าอร่อยมาก ทำเลดีมาก เดินไปสะพานมอญนิดเดียวครับ")
            )
        ),
        Accommodation(
            id = "love_bridge",
            name = "บ้านสะพานรัก",
            nameEn = "Love Bridge House",
            rating = 4.5f,
            priceRange = "800 - 1,800 บ.",
            distanceKmToBridge = 0.2f,
            category = "ติดสะพานมอญ",
            features = listOf("ติดตักบาตรฝั่งมอญ", "ใกล้ท่าเรือเช่า", "อนุญาตสัตว์เลี้ยง (มีค่าบริการเพิ่มเติม)", "อบอุ่นเป็นกันเอง", "ฟรี Wi-Fi"),
            phone = "089-828-5683",
            quote = "บ้านพักไม้กึ่งปูนสไตล์อบอุ่น ตั้งอยู่ชุมชนชาวมอญ เดินมาสะพานมอญเช้ามืดง่ายมาก สะดวกต่อการตักบาตรมอญ",
            location = LatLng(15.1415, 98.4502),
            roomTypes = listOf(
                RoomType("Standard Double Room (ฝั่งหัวสะพาน)", "1,200 บ./คืน"),
                RoomType("Wooden Cozy Room (ห้องไม้สไตล์โฮมมี่)", "1,500 บ./คืน"),
                RoomType("Budget Fan Room (ห้องพัดลมราคาประหยัด)", "800 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณนัท", 5f, "เจ้าของน่ารักมาก แนะนำการเช่าชุดมอญตักบาตรเป็นกันเอง คุ้มค่า คุ้มราคามากค่ะ"),
                ResortReview("คุณสมพงษ์", 4f, "ที่พักสะอาด บรรยากาศเงียบสงบล้อมรอบด้วยวัฒนธรรมของชุมชนมอญ")
            )
        ),
        Accommodation(
            id = "p_guesthouse",
            name = "พี เกสท์เฮ้าส์",
            nameEn = "P Guesthouse",
            rating = 4.4f,
            priceRange = "600 - 1,400 บ.",
            distanceKmToBridge = 0.8f,
            category = "ราคาประหยัด",
            features = listOf("วิวแม่น้ำซองกาเลียกว้าง", "กิจกรรมพายเรือแคนู", "ลานกิจกรรมรอบกองไฟ", "มีมอเตอร์ไซค์ให้เช่า", "ที่จอดรถ"),
            phone = "034-595061",
            quote = "ที่พักและลานกางเต็นท์สไตล์ธรรมชาติ ริมแม่น้ำซองกาเลีย เหมาะสำหรับนักเดินทางสายลุยและแบคแพคเกอร์",
            location = LatLng(15.1378, 98.4412),
            roomTypes = listOf(
                RoomType("River View Fan Cottage (กระท่อมพัดลมวิวแม่น้ำ)", "600 บ./คืน"),
                RoomType("Deluxe AC Room (ห้องแอร์ดีลักซ์วิวสวน)", "1,000 บ./คืน"),
                RoomType("Premium Lake View (ห้องแอร์พรีเมียมวิวลำน้ำ)", "1,400 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณแบงค์", 4.5f, "เงียบสงบมาก เช่ามอไซค์จากพี่ยอดฮิต ขี่เที่ยววัดใต้น้ำลุยๆ ดีมากๆ"),
                ResortReview("คุณอรนิชา", 4.3f, "ที่นี่ลมพัดเย็นสบายตลอดปี ร้านอาหารรสชาติต้นตำรับประทับใจพายแคนูเล่น")
            )
        ),
        Accommodation(
            id = "sankhla_resort",
            name = "สังขละ รีสอร์ท",
            nameEn = "Sankhla Resort",
            rating = 4.3f,
            priceRange = "1,200 - 2,500 บ.",
            distanceKmToBridge = 1.2f,
            category = "วิวแม่น้ำ",
            features = listOf("สถาปัตยกรรมไม้สักไทย-รามัญ", "ระเบียงนั่งชมหมอก", "สวนร่มรื่น", "เหมาะสำหรับครอบครัว", "เครื่องปรับอากาศ"),
            phone = "081-308-1614",
            quote = "บ้านไม้สักเรือนไทยที่ผสานสไตล์วิถีพุทธมอญ ตั้งอยู่ริมตลิ่งแม่น้ำซองกาเลีย แวดล้อมด้วยต้นไม้และสายหมอกยามเช้า",
            location = LatLng(15.1432, 98.4398),
            roomTypes = listOf(
                RoomType("Thai Wooden Cottage (บ้านไม้เดี่ยววิวสวน)", "1,500 บ./คืน"),
                RoomType("Misty Riverfront Sweet (บ้านไม้สักทองวิวริมฝั่งลำน้ำ)", "2,500 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณชานนท์", 4f, "เงียบ สงบ ลมเย็นตลอดคืน ระเบียงเหมาะสำหรับนั่งอ่านหนังสือเงียบๆ"),
                ResortReview("คุณรุจิรา", 4.6f, "สไตล์อนุรักษ์ดีมาก สะอาด ที่จอดรถสะดวกสบาย")
            )
        ),
        Accommodation(
            id = "oh_dee",
            name = "โอดี โฮสเทล",
            nameEn = "Oh Dee Hostel",
            rating = 4.7f,
            priceRange = "350 - 900 บ.",
            distanceKmToBridge = 0.5f,
            category = "ราคาประหยัด",
            features = listOf("ดาดฟ้าชมวิวพระอาทิตย์ตก", "จักรยานให้เช่าฟรี", "มุมกาแฟและขนมฟังชิลล์", "ใกล้ตลาดถนนคนเดิน", "ฟรี Wi-Fi"),
            phone = "096-857-9654",
            quote = "โฮสเทลยอดนิยมอันดับต้นๆ ตั้งอยู่ใจกลางเมืองสังขละบุรี เดินไปตลาดเช้า ตลาดเย็น และสะพานมอญได้อย่างสะดวกสบาย",
            location = LatLng(15.1444, 98.4491),
            roomTypes = listOf(
                RoomType("Single Bed in Mixed Dorm (เตียงนอนเดี่ยวในห้องรวม)", "350 บ./เตียง"),
                RoomType("Private Standard Double Room (ห้องเดี่ยวเตียงคู่พัดลม)", "700 บ./คืน"),
                RoomType("Private AC King Bed (ห้องแอร์เตียงเดี่ยวคิงไซส์)", "900 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณแพร", 5f, "เป็นโฮสเทลที่สะอาดมาก เจ้าของอธิบายดี มีคุกกี้ชารวมดื่มฟรี ดาดฟ้ายามค่ำคืนดาวเต็มฟ้า"),
                ResortReview("คุณจอนห์น", 4.4f, "Very clean, friendly hosts, amazing location just minutes walk to market and bridge!")
            )
        ),
        Accommodation(
            id = "baan_suan_rak",
            name = "บ้านสวนรักเสน่ห์",
            nameEn = "Baan Suan Rak Saneh",
            rating = 4.2f,
            priceRange = "700 - 1,500 บ.",
            distanceKmToBridge = 1.5f,
            category = "สัตว์เลี้ยงเข้าได้",
            features = listOf("สุนัข/แมวเข้าพักฟรี (แจ้งล่วงหน้า)", "ลานหญ้ากว้างขวางสำหรับสัตว์เลี้ยง", "กิจกรรมปิ้งย่างบาร์บีคิว", "ที่จอดรถหน้าบ้านพัก", "ฟรี Wi-Fi"),
            phone = "086-163-4464",
            quote = "บ้านสวนเงียบสงบ ล้อมรอบด้วยไอหมอกขุนเขาสองฟากทาง เหมาะสำหรับผู้เข้าพักที่มีน้องหมาน้องแมวร่วมเดินทางด้วย",
            location = LatLng(15.1501, 98.4520),
            roomTypes = listOf(
                RoomType("Cozy Green Bungalow (บังกะโลบ้านสวนแอร์)", "1,000 บ./คืน"),
                RoomType("Family Pet suite (บ้านหลังใหญ่สำหรับครอบครัวมีสัตว์เลี้ยง)", "1,500 บ./คืน")
            ),
            reviews = listOf(
                ResortReview("คุณณิชา", 4.5f, "พาสุนัขไปพักด้วย แฮปปี้มาก มีสนามหญ้ากว้างให้หมาวิ่งเล่น เจ้าของใจดีและรักษาสะอาดดีค่ะ"),
                ResortReview("คุณอนันต์", 3.9f, "ค่อนข้างเงียบและส่วนตัวมาก ห่างสะพานมอญนิดหน่อยแต่คุ้มถ้าเอารถมาเอง")
            )
        )
    )
}
