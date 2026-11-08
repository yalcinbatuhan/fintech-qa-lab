# Playwright UI Testleri ve CI – Adım Adım Rehber

Bu rehber, projeye eklenen Playwright UI testlerini ve GitHub Actions CI'ı nasıl çalıştıracağını,
kontrol edeceğini ve kendi testlerini nasıl yazacağını anlatır.

## 1. Neler eklendi?

| Dosya | Ne işe yarıyor |
| --- | --- |
| `payment-api/src/main/resources/web/index.html` | Test edilecek ödeme ekranı (giriş, ödeme formu, ödeme listesi) |
| `PaymentApiApplication.java` | Ekranı `http://localhost:8080/` adresinde sunan küçük bir ekleme (`serveUi`) |
| `pom.xml`, `qa-tests/pom.xml` | Playwright bağımlılığı ve "known-bug" ayarı |
| `qa-tests/.../ui/BaseUiTest.java` | Ortak kurulum: API'yi ve tarayıcıyı başlatır, her testten sonra kapatır |
| `qa-tests/.../ui/LoginUiTest.java` | Örnek testler (önce bunu oku) |
| `qa-tests/.../ui/PaymentUiTest.java` | **Senin dolduracağın 4 test** |
| `.github/workflows/ci.yml` | Her push'ta testleri GitHub'da otomatik çalıştırır |
| `FeeCalculatorTest.java` | 3 bug testi `@Tag("known-bug")` ile işaretlendi; BUG-2 satırı tablodan ayrı teste taşındı |

## 2. Neden "known-bug" etiketi?

BUG-1, BUG-2 ve BUG-3 testleri bilerek başarısız oluyor, çünkü bug'lar açık. Etiket olmadan CI hep
kırmızı görünürdü ve gerçek yeni bir hatayı fark etmek zorlaşırdı. Artık:

- Ana job (**Unit, API and UI tests**) bu 3 testi atlar ve yeşil kalır.
- Ayrı job (**Known bugs**) sadece bu 3 testi çalıştırır. Kırmızı olması beklenir ve pipeline'ı durdurmaz.

Mülakatta anlatılacak cümle: "Bilinen hataları gösteren testleri etiketleyip ayrı, bloklamayan bir CI
job'unda çalıştırdım. Böylece ana pipeline gerçek regresyonları yakalamaya odaklı kaldı."

## 3. İlk çalıştırma (bilgisayarında)

1. IntelliJ'de projeyi aç. Sağ üstteki **Maven** panelinde yenile (↻) butonuna bas. Playwright indirilir.
2. `LoginUiTest.java` dosyasını aç, sınıf adının yanındaki yeşil ▶ butonuna bas.
3. İlk çalıştırmada Playwright Chromium tarayıcısını indirir (1–2 dakika sürebilir). 2 test yeşil olmalı.
4. Tarayıcıyı görerek izlemek istersen terminalde: `mvn test -pl qa-tests -am -Dheadless=false`

## 4. Kendi testlerini yaz (PaymentUiTest)

Her test için:

1. `@Disabled("TODO: write this test")` satırını sil.
2. `// TODO` satırlarının yerine kodu yaz. Gereken her komut dosyanın en üstündeki açıklamada ve
   `LoginUiTest` içinde var.
3. Testin yanındaki ▶ butonuna bas. Kırmızıysa hata mesajını oku: beklenen (expected) ile gerçekleşen
   (actual) değeri karşılaştır.

Önerilen sıra: `eurPaymentShowsCorrectFee` → `invalidIbanShowsError` → `createdPaymentAppearsInList`
→ `instantUsdPaymentAddsSurcharge` (beklenen ücreti önce `docs/ucret-kurallari-ozet.md` ile kendin hesapla).

## 5. Git ile kaydet ve GitHub'a gönder

Değişiklikleri ayrı bir dalda göndermek, önceki PR'larındaki düzene uyar:

```
git checkout -b feature/playwright-ui-tests
git add .
git commit -m "Add payment web UI, Playwright UI test setup and GitHub Actions CI"
git push -u origin feature/playwright-ui-tests
```

Sonra GitHub'da **Pull request** aç. CI otomatik çalışır; PR sayfasında iki job görürsün:
- **Unit, API and UI tests** → yeşil olmalı
- **Known bugs** → kırmızı olması normal

Ana job yeşilse PR'ı merge et. Kendi testlerini yazdıkça aynı şekilde yeni commit'ler ekle.
