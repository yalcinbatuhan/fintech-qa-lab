# Ücret Kuralları – Hızlı Özet

Kaynak: `payment-api/src/main/java/com/fintechqalab/payment/FeeCalculator.java` (11–17. satırlar)

## Kurallar

| # | Kural | Türkçesi |
| --- | --- | --- |
| 1 | Tutar | 0.00'dan büyük, en fazla 50,000.00 (dahil). Dışındaysa hata |
| 2 | EUR | Tutarın %0.5'i. En az 0.50, en fazla 20.00 |
| 3 | USD ve GBP | Tutarın %1'i. En az 1.00, en fazla 50.00 |
| 4 | Diğer para birimleri | Hata (JPY, TRY, boş...) |
| 5 | Instant | Taban/tavan uygulandıktan SONRA +1.00 |
| 6 | Yuvarlama | 2 ondalık, HALF_UP (0.755 → 0.76) |

## Hesap sırası

1. Tutar geçerli mi? (Kural 1)
2. Oranla çarp (EUR: × 0.005, USD/GBP: × 0.01)
3. Tabanın altındaysa tabana çıkar
4. Tavanın üstündeyse tavana indir
5. Instant ise +1.00 ekle
6. 2 ondalığa yuvarla (HALF_UP)

## Hazır örnekler

| Tutar | Para birimi | Instant | Hesap | Beklenen ücret |
| --- | --- | --- | --- | --- |
| 10.00 | EUR | Hayır | 0.05 → taban | 0.50 |
| 1,000.00 | EUR | Hayır | 5.00 | 5.00 |
| 10,000.00 | EUR | Hayır | 50.00 → tavan | 20.00 |
| 50.00 | USD | Hayır | 0.50 → taban | 1.00 |
| 1,000.00 | USD | Hayır | 10.00 | 10.00 |
| 10,000.00 | USD | Hayır | 100.00 → tavan | 50.00 |

## Sınır noktaları

| Para birimi | Taban biter | Tavan başlar |
| --- | --- | --- |
| EUR | 100.00 (0.50 / 0.005) | 4,000.00 (20.00 / 0.005) |
| USD / GBP | 100.00 (1.00 / 0.01) | 5,000.00 (50.00 / 0.01) |
