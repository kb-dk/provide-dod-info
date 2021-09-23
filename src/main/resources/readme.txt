Datasættet baserer sig på metadata og OCR generede tekst.
Metadata er ikke ORC genereret og består af følgende felter:
    • Barcode: Barcode henviser den strengkode værket har i bibliotekets katalog.
    • Alma: Alma henviser til om værket kan findes i bibliotekssystemet.
    • Year: Year er værkets publikationsår
    • Place: Place er værkets udgivelsessted. Stavemåderne er historiske, dvs. at København findes som Kiøbenhavn, Kjøbenhavn, Kbh. Copenhagen, Havniæ osv.
    • Author: Værkets forfatter
    • Publisher: Publisher er ofte et forlag
    • Classification: Klassifikation stammer fra Nationalbibliotekts ældre samling, og et kendskab til denne klassifikation giver bedre mulighed for at filtre datasættet efter kategorier.
    • Title: Title er værkets titel. Stavemåderne er historiske.

Følgende filstruktur findes i out.zip filen :

outDir____1400-1449____ barcode1.marc.xml
         |           |__barcode1.txt
         |           |__barcode2.marc.xml
         |           |__barcode2.txt
         |
         |_1450-1499____barcode3.marc.xml
         |           |__barcode3.txt
         |           |__….
         |
         |__1500-1549….
         |
         .
         .
         .
         |__1850-1899____barcodeX.marc.xml
         |            |__barcodeX.txt
         |
         AlmaExtractResult.xlsx
         readme.txt


Værker er placeret i underfoldere af 50 års perioder og er placeret efter publikationsår (Year).

barcodeY.marc.xlm indeholder MARC21 metadata for det værk i Alma, som stregkoden hører til.

barcodeY.txt er værkets OCR genererede tekst.

AlmaExtractResult.xlsx indeholder metadata som beskrevet ovenfor.
