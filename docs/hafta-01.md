# Hafta 1 – Git, Maven ve JUnit 5

**Hedef:** Hafta sonunda projeyi GitHub'a yüklemiş, `FeeCalculator` için en az 12 test yazmış ve
koddaki 3 hatayı bulup raporlamış olacaksın.

**Süre:** Yaklaşık 12 saat (kurulum 1 saat, konu anlatımı 3 saat, uygulama 6–7 saat, mülakat soruları 2 saat).

---

## 0. Kurulum (Mac)

### Java 21 (IntelliJ içinden, en kolay yol)
1. IntelliJ IDEA'yı aç → **Open** → `Documents/fintech-qa-lab` klasörünü seç → **Open as Project**.
2. Sağ altta "Project JDK is not defined" uyarısı çıkarsa: **File → Project Structure → Project → SDK →
   Download JDK** → Version: **21**, Vendor: **Eclipse Temurin** → Download.
3. Project Structure'da **Language level: 21** olduğundan emin ol → OK.
4. IntelliJ projeyi Maven projesi olarak tanıyıp kütüphaneleri (JUnit, REST Assured) indirecek.
   İlk seferde birkaç dakika sürebilir. Sağdaki **Maven** panelinde hata görünmemeli.

### Git
1. **Terminal** uygulamasını aç ve şunu yaz: `xcode-select --install`.
   Açılan pencerede **Install**'a bas. Bu işlem Git'i de kurar.
2. Kontrol: `git --version` (ör. `git version 2.x` yazmalı).
3. Kimliğini tanıt (commit'lerde görünür):
   ```bash
   git config --global user.name "Batuhan Yalcin"
   git config --global user.email "batubatu48@gmail.com"
   git config --global init.defaultBranch main
   ```

### İlk çalıştırma
1. `payment-api/src/main/java/.../PaymentApiApplication.java` dosyasını aç → `main` yanındaki yeşil ▶ → **Run**.
2. Tarayıcıda `http://localhost:8080/health` adresini aç → `{"status":"UP"}` görmelisin.
3. `payment-api/src/test/java/.../FeeCalculatorTest.java` → sınıf adına sağ tık → **Run** → 2 yeşil test.

---

## 1. Git: neden ve nasıl

Git, kodun versiyon geçmişini tutar. Ekipte herkes kendi **branch**'inde çalışır,
işi bitince **pull request (PR)** açar, başkası review eder ve `main`'e birleştirilir.
Mülakatlarda "Git kullanıyor musun?" değil, "ekip içinde akışınız nasıldı?" diye sorulur.

| Komut | Ne yapar |
| --- | --- |
| `git init` | Klasörü Git deposu yapar |
| `git status` | Değişen dosyaları gösterir (en sık kullanacağın komut) |
| `git add <dosya>` / `git add .` | Değişiklikleri commit'e hazırlar (staging) |
| `git commit -m "mesaj"` | Hazırlanan değişiklikleri kaydeder |
| `git log --oneline` | Geçmişi kısa gösterir |
| `git switch -c feature/fee-tests` | Yeni branch açar ve ona geçer |
| `git switch main` | main'e döner |
| `git push -u origin <branch>` | Branch'i GitHub'a gönderir |
| `git pull` | GitHub'daki son hali çeker |

**İyi commit mesajı:** Kısa, emir kipinde, ne yaptığını söyler.
`Add boundary tests for EUR minimum fee` iyi; `test` ya da `düzeltmeler` kötü.

**Bu haftaki akış:**
```bash
cd ~/Documents/fintech-qa-lab
git init
git add .
git commit -m "Initial project setup"
# GitHub'da boş bir repo aç: fintech-qa-lab (README ekleme), sonra:
git remote add origin https://github.com/<kullanici-adin>/fintech-qa-lab.git
git push -u origin main

git switch -c feature/fee-calculator-tests
# ... testleri yaz, küçük adımlarla commit et ...
git push -u origin feature/fee-calculator-tests
# GitHub'da Pull Request aç ve main'e merge et
```
GitHub ilk push'ta şifre yerine **Personal Access Token** ister:
GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → `repo` yetkisi.

---

## 2. Maven: proje yapısı

Maven, Java projesinin **bağımlılıklarını** (kütüphaneler) ve **build adımlarını** yönetir.

- `pom.xml`: Projenin kimlik kartı. Hangi kütüphaneler, hangi Java versiyonu.
- Bu proje **multi-module**: kökteki `pom.xml` iki modülü yönetir (`payment-api`, `qa-tests`) ve
  versiyonları tek yerde tutar (`dependencyManagement`).
- `src/main/java` uygulama kodu, `src/test/java` test kodu. Testler canlı uygulamaya paketlenmez.
- `<scope>test</scope>`: Bu kütüphane sadece testlerde kullanılır.
- Yaşam döngüsü: `mvn compile` → `mvn test` → `mvn package`. Bir adım öncekileri de çalıştırır.

IntelliJ'de sağdaki **Maven** paneli → `fintech-qa-lab` → **Lifecycle → test** ile tüm testleri çalıştırabilirsin.

---

## 3. JUnit 5

Her test **AAA** düzenini izler: **Arrange** (hazırla), **Act** (çalıştır), **Assert** (doğrula).

```java
@Test
@DisplayName("EUR: 0.5% of 1,000.00 is 5.00")
void eurFeeIsHalfAPercent() {
    BigDecimal fee = calculator.calculateFee(new BigDecimal("1000.00"), "EUR", false); // Act
    assertEquals(new BigDecimal("5.00"), fee);                                         // Assert
}
```

| Araç | Ne işe yarar |
| --- | --- |
| `@Test` | Metodu test olarak işaretler |
| `@DisplayName` | Raporda okunur bir başlık |
| `@BeforeEach` | Her testten önce çalışır (ortak hazırlık) |
| `assertEquals(beklenen, gerçek)` | Eşitlik kontrolü. Sıra önemli: önce beklenen |
| `assertThrows(Exception.class, () -> ...)` | Hata fırlatılmasını bekler |
| `assertAll(...)` | Birden fazla kontrolü tek seferde raporlar |
| `@ParameterizedTest` + `@CsvSource` | Aynı testi farklı verilerle çalıştırır |

Parametrik test örneği (bunu kendi değerlerinle doldur):
```java
@ParameterizedTest(name = "{0} {1} -> fee {2}")
@CsvSource({
        "1000.00, EUR, 5.00",
        "2000.00, EUR, 10.00"
})
void feeIsCalculatedPerCurrency(String amount, String currency, String expectedFee) {
    BigDecimal fee = calculator.calculateFee(new BigDecimal(amount), currency, false);
    assertEquals(new BigDecimal(expectedFee), fee);
}
```
Gerekli import'lar: `org.junit.jupiter.params.ParameterizedTest` ve `org.junit.jupiter.params.provider.CsvSource`.

**Dikkat (fintech için önemli):** `BigDecimal` karşılaştırmasında `assertEquals` ölçeğe (scale) de bakar:
`5.0` ile `5.00` eşit sayılmaz. Para hesabında bu bilinçli bir tercihtir.

---

## 4. Test tasarımı: denklik sınıfları ve sınır değer analizi

Bu iki teknik ISTQB CTFL sınavının da temel konusu.

- **Denklik sınıfları (Equivalence Partitioning):** Aynı şekilde davranması beklenen girdileri gruplara ayır,
  her gruptan bir temsilci test et.
- **Sınır değer analizi (Boundary Value Analysis):** Hatalar en çok sınırlarda çıkar.
  Her sınırın tam üstünü, tam altını ve kendisini test et.

`FeeCalculator` için başlangıç tablosu (eksikleri sen tamamla):

| Kural | Geçerli sınıf | Geçersiz sınıf | Sınır değerler |
| --- | --- | --- | --- |
| Tutar | 0.01 – 50,000.00 | ≤ 0, > 50,000.00, null | 0.00, 0.01, 49,999.99, 50,000.00, 50,000.01 |
| Para birimi | EUR, USD, GBP | JPY, "", null, "eur" | – |
| EUR min ücret 0.50 | ? | – | Ücretin tam 0.50 olduğu tutar nedir? |
| EUR max ücret 20.00 | ? | – | Ücretin tam 20.00 olduğu tutar nedir? |
| Yuvarlama HALF_UP | – | – | Ücretin 3. hanesi 5 olan bir tutar |

---

## 5. Ödev (işaretle)

- [ ] Java 21, Git kurulumu ve projeyi IntelliJ'de çalıştırma
- [ ] GitHub hesabı ve `fintech-qa-lab` reposu, ilk push
- [ ] `FeeCalculatorTest` içine en az 10 yeni test (toplam 12+)
- [ ] En az bir `@ParameterizedTest`
- [ ] Koddaki **3 hatayı** bul. Her biri için `docs/bug-reports.md` dosyasına bir rapor yaz:
      Başlık, Adımlar, Beklenen sonuç, Gerçek sonuç, Önem derecesi (Severity)
- [ ] Hataları gösteren testler **kırmızı** kalsın. Kodu düzeltme; QA olarak görevin hatayı kanıtlamak.
- [ ] Çalışmayı bir branch'te yap, küçük commit'lerle ilerle, Pull Request açıp merge et

İpucu: API'de ayrıca **2 hata** daha var. Onları 2. haftada REST Assured ile bulacağız.

---

## 6. Haftanın mülakat soruları (Cuma birlikte çalışacağız)

Önce kendi İngilizce cevabını yaz, sonra birlikte güçlendirelim.

1. What is the difference between equivalence partitioning and boundary value analysis? Give an example from a payments system.
2. Why should money be stored as `BigDecimal` and not `double`?
3. A test fails in CI. How do you decide whether the bug is in the product or in the test?
4. Describe the Git workflow you used in your last team.
5. What is the test pyramid, and where do unit tests, API tests and UI tests sit in it?
6. What does `@ParameterizedTest` give you compared with copy-pasted tests?
