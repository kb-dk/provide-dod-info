# provide-dod-info

# Overview
This project will search through a folder with DOD pdf-files, extract the barcode from the file names,
make a query in Alma to extract metadata, determine if published date is older than 140 years, extract specific
information fron Alma, extract text data from relevant pdfs,
save data to csv/excel file, and more to come...


# Requirements
Java 11, Maven 3

A configuration file provide-dod-info.yml must be provided: To be updated...
```
provide-dod-info:
  ebook_output_dir: $ The directory where the e-book mods-files are placed
  audio_output_dir: $ The directory where the audio book mods-files are placed
  output_dir: $ The directory where the excell ouput-file will be placed
  alma_sru_search: https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&

```
# Execution
Extract the file provide-dod-info-*version*-distribution.tar.gz ('version' must be replaced by the correct version number):
```
tar -xf target/provide-dod-info-version-distribution.tar.gz -C target/
```
and then run:
```
target/provide-dod-info-version/bin/provide-dod-info.sh path-to/provide-dod-info.yml
```


