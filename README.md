# ULTIMA ELEVATORS — Forge 1.20.1

Dünyanın en gelişmiş Minecraft asansör modu: Schindler tarzı kat kapıları, KONE Polaris
varış bazlı dispatch, gerçek MR/MRL makine ayrımı, zorunlu karşı ağırlık ve kuyu yayları (tamponlar),
marka/çerçeve özelleştirme, kat göstergeleri ve hol fenerleri.

## Derleme (GitHub Actions)

Repo'yu GitHub'a push'la → `.github/workflows/build.yml` otomatik çalışır:
- JDK 17 (Temurin) + Gradle 8.1.1 kurar
- `gradle build` çalıştırır
- JAR'ı **Actions Artifacts**'a yükler
- `v*` etiketi push'larsan otomatik **Release** oluşturur

Lokal derleme (isteğe bağlı):
```bash
gradle wrapper --gradle-version 8.1.1   # bir kez
./gradlew build
```
Çıktı: `build/libs/ultimaelevators-1.0.0.jar`

## Kurulum Rehberi (oyun içi)

Şaft sütunu aşağıdan yukarı şu sırayla kurulur (kontrolörün baktığı kolon):

1. **Karşı Ağırlık Rayı** (`counterweight_rail`) — en altta 1 adet, ZORUNLU
2. **Tampon / Kuyu Yayı** (`oil_buffer` veya `spring_buffer`) — 1-2 adet, ZORUNLU
   - MR Gearless (6 m/s) için **oil_buffer** zorunludur
3. **Şaft blokları** — `shaft_frame` / `shaft_glass` / `shaft_rail` karışık
4. **Kat kapıları** (`elevator_door`) — her katta şafta yaslanmış (üst blok otomatik gelir)
5. **Makine** — en üstte:
   - `traction_machine` (GEARED/gearless) = **MR** (üstünde 1 blok boşluk = makine dairesi zorunlu)
   - `mrl_drive` = **MRL** (hız limiti 1.6 m/s)
6. **Kontrolör** (`elevator_controller`) — şaftın yanına, şafta bakacak şekilde
7. Sağ tık → GUI: Marka / Çerçeve / Hız / Bekleme / Grup + **ŞAFTI YENİDEN TARA**

### KONE Polaris Dispatch
- Herhangi bir kioska **Lift Linker** ile sağ tıkla → grubu kopyala
- Aynı gruptaki tüm kontrolörlere linker ile uygula → aynı bank
- Kiosktan varış katını seç → en uygun araba atanır, kiosk ekranında "Car B" görünür

### Kullanım
- Kapılar açılınca kabinin 0.5 yüksek döşemesine yürüyerek bin
- Kabindeyken **R** → Kumanda Paneli (kat butonları + kapı aç/kapat)
- Kat göstergesi + yön oku her katta çalışır; ışık perdesi kapı arasında biri varken kapatmaz

### Markalar
Schindler / KONE / Otis / Mitsubishi / TKE / Ultima — çerçeve stilleri:
Brushed Stainless / Black Steel / Champagne Gold / Panoramic Glass

## Mimari

- `ElevatorCarEntity` — SCAN dispatch, yumuşak hızlanma/fren, ışık perdesi, yolcu taşıma
- `ControllerBlockEntity` — şaft doğrulama, MR/MRL tespiti, çağrı kuyruğu, kabin yönetimi
- `DispatchManager` — grup bazlı varış ataması (kiosk → en iyi araba)
- Network: 6 SimpleChannel mesajı, menüsüz GUI'ler (S2C open / C2S action)

Mod ID: `ultimaelevators` — License: MIT
