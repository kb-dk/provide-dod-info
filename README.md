# provide-dod-info

# Overview
This project will search through a folder with DOD pdf-files, extract the barcode from the file names,
make a query in Alma to extract metadata, determine if published date is older than 140 years, extract specific
information from Alma and save it to excel-file, extract text data from related pdfs and save it to txt-file.
All files wil be stored in directories with 50 years intervals.
The output is zipped to the file out.zip.



# Requirements
pdftotext must be installed,

Access to the DOD pdf files (corpus_orig_dir in provide-dod-info.yml)

Java 11, Maven 3

A configuration file provide-dod-info.yml must be provided: 
```
provide-dod-info:
  out_file_name: $ The name of the excel-file with data extracted from Alma (must have .xlsx extension) 
  corpus_orig_dir: $ The directory where the original pdf-files are placed
  cut_year: $ Only files with Publish date older than this will be handled (140 years: e.g in 2021 it must be 1881)
  out_dir: $ The directory where the zipped ouput-files will be placed
  alma_sru_search: https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&

```
# Build
To create the release package, retrieve the project from GitHub and build with:
```
mvn clean package -DskipTests
```
# Execution
Extract the file provide-dod-info-*version*-distribution.tar.gz (*version* must be replaced by the correct version number e.g. 1.0.0)
to wanted directory e.g.:
```
tar -xf provide-dod-info-1.0.0-distribution.tar.gz -C .
```
and then run from the same directory it was unpacked to and add path to provide-dod-info.yml file as parameter e.g.:
```
provide-dod-info-1.0.0/bin/provide-dod-info.sh ./provide-dod-info.yml
```


